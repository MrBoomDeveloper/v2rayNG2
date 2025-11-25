package com.mrboomdev.v2rayng2.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.v2ray.ang.dto.ServersCache
import com.v2ray.ang.dto.SubscriptionItem
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.handler.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    context: Context
): ViewModel() {
    private val _servers = MutableStateFlow<List<ServersCache>?>(null)
    val servers = _servers.asStateFlow()
    
    private val _groups = MutableStateFlow(MmkvManager.decodeSubscriptions())
    val groups: StateFlow<List<Pair<String, SubscriptionItem>>> = _groups.asStateFlow()
    
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
    }
}