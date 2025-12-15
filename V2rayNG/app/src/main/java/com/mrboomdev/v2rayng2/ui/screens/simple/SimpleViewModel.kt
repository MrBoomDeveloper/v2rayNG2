package com.mrboomdev.v2rayng2.ui.screens.simple

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.v2ray.ang.dto.ProfileItem
import com.v2ray.ang.dto.SubscriptionItem
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
        if(_state.value.isLoading) return
        
        viewModelScope.launch {
            val isOn = _state.value == SimpleState.On
            if(isOn) stop() else start()
        }
    }
    
    private suspend fun stop() {
        _state.emit(SimpleState.Off)
    }
    
    private suspend fun start() {
        _state.emit(SimpleState.UpdatingSubscription)
        delay(2000)
        _state.emit(SimpleState.UrlTesting(0, 5))
        delay(2000)
        _state.emit(SimpleState.On)
    }
}