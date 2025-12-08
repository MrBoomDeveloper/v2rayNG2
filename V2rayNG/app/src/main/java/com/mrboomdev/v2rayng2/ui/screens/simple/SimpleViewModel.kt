package com.mrboomdev.v2rayng2.ui.screens.simple

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.v2ray.ang.dto.ProfileItem
import com.v2ray.ang.dto.SubscriptionItem
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.handler.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SimpleViewModel(
    context: Context
): ViewModel() {
    private val context = context.applicationContext

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    private val _selectedServer = MutableStateFlow<Pair<String, ProfileItem>?>(null)
    val selectedServer = _selectedServer.asStateFlow()

    private val _selectedGroup = MutableStateFlow<Pair<String, SubscriptionItem>?>(null)
    val selectedGroup = _selectedGroup.asStateFlow()
    
    init {
        viewModelScope.launch(Dispatchers.Default) {
            SettingsManager.initAssets(context.applicationContext, context.assets)
            
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
    
    fun toggle() {
        viewModelScope.launch { 
            _isRunning.emit(!_isRunning.value)
        }
    }
}