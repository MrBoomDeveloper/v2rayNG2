package com.mrboomdev.v2rayng2.ui.screens.home

import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.v2ray.ang.AppConfig
import com.v2ray.ang.AppConfig.VPN
import com.v2ray.ang.dto.ProfileItem
import com.v2ray.ang.dto.ServersCache
import com.v2ray.ang.dto.SubscriptionItem
import com.v2ray.ang.handler.AngConfigManager
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.handler.SettingsManager
import com.v2ray.ang.handler.V2RayServiceManager
import com.v2ray.ang.service.V2RayTestService.Companion.startRealPing
import com.v2ray.ang.util.Utils
import go.Seq
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import libv2ray.Libv2ray

sealed interface TestingState {
    data object UpdatingSubscription: TestingState
    data class Testing(val done: Int, val total: Int): TestingState
    data object Finalizing: TestingState
}

class HomeViewModel(
    context: Context
): ViewModel() {
    private val context = context.applicationContext
    
    private val _servers = MutableStateFlow<List<ServersCache>?>(null)
    val servers = _servers.asStateFlow()
    
    private val _groups = MutableStateFlow(MmkvManager.decodeSubscriptions())
    val groups: StateFlow<List<Pair<String, SubscriptionItem>>> = _groups.asStateFlow()
    
    private val _selectedServer = MutableStateFlow<Pair<String, ProfileItem>?>(null)
    val selectedServer = _selectedServer.asStateFlow()
    
    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()
    
    private var testingJob: Job? = null
    private val _testingState = MutableStateFlow<TestingState?>(null)
    val testingState = _testingState.asStateFlow()
    
    init {
        viewModelScope.launch(Dispatchers.Default) {
            SettingsManager.initAssets(context.applicationContext, context.assets)
            updateServers()
        }
    }
    
    private suspend fun updateServers() {
        _servers.emit(MmkvManager.decodeServerList().map { serverGuid ->
            ServersCache(serverGuid, MmkvManager.decodeServerConfig(serverGuid).also { decodedProfile ->
                if(decodedProfile == null) return@map null
            }!!)
        }.filterNotNull())
        
        _selectedServer.emit(MmkvManager.getSelectServer()?.let { serverGuid ->
            MmkvManager.decodeServerConfig(serverGuid)?.let { 
                serverGuid to it
            }
        })
    }
    
    fun selectServer(serverGuid: String) {
        if(serverGuid == _selectedServer.value?.first) return
        MmkvManager.setSelectServer(serverGuid)
        
        viewModelScope.launch {
            _selectedServer.emit(MmkvManager.decodeServerConfig(serverGuid)?.let {
                serverGuid to it
            })
            
            if(_isRunning.value) {
                V2RayServiceManager.stopVService(context)
                
                try {
                    delay(500)
                    V2RayServiceManager.startVService(context)
                } catch (e: Exception) {
                    Log.e(AppConfig.TAG, "Failed to restart V2Ray service", e)
                }
            }
        }
    }
    
    fun toggleVpn(onFailure: (Intent) -> Unit) {
        viewModelScope.launch {
            if(_isRunning.value) {
                V2RayServiceManager.stopVService(context)
                _isRunning.emit(false)
            } else if((MmkvManager.decodeSettingsString(AppConfig.PREF_MODE) ?: VPN) == VPN) {
                val intent = VpnService.prepare(context)
                
                if(intent == null) {
                    V2RayServiceManager.startVService(context)
                    _isRunning.emit(true)
                } else {
                    onFailure(intent)
                }
            } else {
                V2RayServiceManager.startVService(context)
                _isRunning.emit(true)
            }
        }
    }
    
    fun startVpn() {
        viewModelScope.launch {
            V2RayServiceManager.startVService(context)
            _isRunning.emit(true)
        }
    }
    
    fun requestUpdate(
        groupId: String,
        onComplete: () -> Unit
    ) {
        testingJob?.cancel()
        
        testingJob = viewModelScope.launch(Dispatchers.IO) {
            val group = _groups.value.first { it.first == groupId }
            
            if(group.second.url.isNotBlank()) {
                _testingState.emit(TestingState.UpdatingSubscription)
                AngConfigManager.updateConfigViaSub(group)
                updateServers()
            }

            _testingState.emit(TestingState.Finalizing)
            removeDuplicates(groupId)

            _testingState.emit(TestingState.Testing(0, 0))
            testAllRealPingSync(groupId).buffer(15).collect { (done, total) ->
                _testingState.emit(TestingState.Testing(done, total))
            }
            
            MmkvManager.decodeServerList().sortedBy { guid ->
                MmkvManager.decodeServerAffiliationInfo(guid)?.testDelayMillis?.takeIf { delay ->
                    delay > 0L 
                } ?: Long.MAX_VALUE
            }.also { MmkvManager.encodeServerList(it) }
            
            updateServers()
            _testingState.emit(null)
            onComplete()
        }
    }

    private fun testAllRealPingSync(
        groupId: String
    ): Flow<Pair<Int, Int>> {
        val servers = _servers.value?.filter {
            it.profile.subscriptionId == groupId
        } ?: return flowOf()
        
        MmkvManager.clearAllTestDelayResults(servers.map { it.guid }.toList())

        return channelFlow {
            var count = 0
            send(0 to servers.size)

            Seq.setContext(context)
            Libv2ray.initCoreEnv(Utils.userAssetPath(context), Utils.getDeviceIdForXUDPBaseKey())

            for(server in servers) {
                launch(Dispatchers.Default) {
                    val result = startRealPing(context, server.guid)
                    MmkvManager.encodeServerTestDelayMillis(server.guid, result)

                    count++
                    send(count to servers.size)
                }
            }
        }
    }
    
    private fun removeDuplicates(groupId: String) {
        val serversCacheCopy = mutableListOf<Pair<String, ProfileItem>>()
        
        _servers.value?.forEach {
            val config = MmkvManager.decodeServerConfig(it.guid) ?: return@forEach
            if(config.subscriptionId != groupId) return@forEach
            serversCacheCopy.add(Pair(it.guid, config))
        }

        val deleteServer = mutableListOf<String>()
        serversCacheCopy.forEachIndexed { index, it ->
            val outbound = it.second
            serversCacheCopy.forEachIndexed { index2, it2 ->
                if(index2 > index) {
                    val outbound2 = it2.second

                    if(outbound == outbound2 && !deleteServer.contains(it2.first)) {
                        deleteServer.add(it2.first)
                    }
                }
            }
        }

        for(it in deleteServer) {
            MmkvManager.removeServer(it)
        }
    }
    
    fun requestMagicPick(
        
    ) {
        // TODO: Implement
    }
    
    fun cancelTest() {
        viewModelScope.launch {
            testingJob?.cancel()
            updateServers()
            _testingState.emit(null)
        }
    }
}