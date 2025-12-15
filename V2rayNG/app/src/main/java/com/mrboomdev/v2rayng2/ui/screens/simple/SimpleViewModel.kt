package com.mrboomdev.v2rayng2.ui.screens.simple

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.v2ray.ang.dto.ProfileItem
import com.v2ray.ang.dto.SubscriptionItem
import com.v2ray.ang.handler.AngConfigManager
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.handler.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SimpleState {
    data object On: SimpleState {
        override val isLoading = false
    }
    
    data object Off: SimpleState {
        override val isLoading = false
    }
    
    data object UpdatingSubscription: SimpleState {
        override val isLoading = true
    }

    data class PingTesting(
        val current: Int,
        val total: Int
    ): SimpleState {
        override val isLoading = true
    }
    
    data class UrlTesting(
        val current: Int, 
        val total: Int
    ): SimpleState {
        override val isLoading = true
    }
    
    data class DeepTesting(
        val currentService: String, 
        val current: Int, 
        val total: Int
    ): SimpleState {
        override val isLoading = true
    }
    
    val isLoading: Boolean
}

class SimpleViewModel(
    context: Context
): ViewModel() {
    @SuppressLint("StaticFieldLeak")
    private val context = context.applicationContext
    
    private val _state = MutableStateFlow<SimpleState>(SimpleState.Off)
    val state = _state.asStateFlow()

    private val _selectedServer = MutableStateFlow<Pair<String, ProfileItem>?>(null)
    val selectedServer = _selectedServer.asStateFlow()

    private val _selectedGroup = MutableStateFlow<Pair<String, SubscriptionItem>?>(null)
    val selectedGroup = _selectedGroup.asStateFlow()

    private val _groups = MutableStateFlow<List<Pair<String, SubscriptionItem>>>(emptyList())
    val groups = _groups.asStateFlow()
    
    init {
        init()
    }
    
    fun init() {
        viewModelScope.launch(Dispatchers.Default) {
            SettingsManager.initAssets(context.applicationContext, context.assets)
            _groups.emit(MmkvManager.decodeSubscriptions())
            
            _selectedServer.emit(MmkvManager.getSelectServer()?.let { serverGuid ->
                MmkvManager.decodeServerConfig(serverGuid)?.let { server ->
                    serverGuid to server
                }?.also { server ->
                    MmkvManager.decodeSubscription(server.second.subscriptionId)?.also { sub ->
                        _selectedGroup.emit(server.second.subscriptionId to sub)
                    }
                }
            })
        }
    }
    
    fun selectGroups(groups: Set<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            _groups.value.forEach { group ->
                MmkvManager.encodeSubscription(group.first, group.second.apply {
                    enabled = group.first in groups
                })
            }
            
            _groups.emit(_groups.value.toList())
        }
    }
    
    fun toggle() {
        if(_state.value.isLoading) return
        
        viewModelScope.launch(Dispatchers.IO) {
            val isOn = _state.value == SimpleState.On
            if(isOn) stop() else start()
        }
    }
    
    private suspend fun start() {
        groups.value.filter { it.second.enabled }.forEach { group ->
            _state.emit(SimpleState.UpdatingSubscription)
            
            val servers = try {
                AngConfigManager.updateConfigViaSub(group)
                removeDuplicateServer(MmkvManager.decodeServerList().mapNotNull { guid ->
                    MmkvManager.decodeServerConfig(guid)?.let { guid to it }
                })
            } catch(e: Exception) {
                Log.e("SimpleViewModel", "Failed to update subscription!", e)

                MmkvManager.decodeServerList().mapNotNull { guid ->
                    MmkvManager.decodeServerConfig(guid)?.let { guid to it }
                }
            }

            delay(2000)
            _state.emit(SimpleState.UrlTesting(0, 5))
            delay(2000)
            _state.emit(SimpleState.On)
        }
    }

    private suspend fun stop() {
        _state.emit(SimpleState.Off)
    }

    fun removeDuplicateServer(
        serversCache: List<Pair<String, ProfileItem>>
    ): List<Pair<String, ProfileItem>> {
        val serversCacheCopy = serversCache.toMutableList()
        
        val deleteServer = mutableListOf<String>()
        serversCacheCopy.forEachIndexed { index, it ->
            val outbound = it.second
            serversCacheCopy.forEachIndexed { index2, it2 ->
                if(index2 > index) {
                    if(outbound == it2.second && !deleteServer.contains(it2.first)) {
                        deleteServer.add(it2.first)
                    }
                }
            }
        }

        for(it in deleteServer) {
            MmkvManager.removeServer(it)
        }
        
        return serversCacheCopy
    }
}