package com.mrboomdev.v2rayng2.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composer
import androidx.compose.runtime.ExperimentalComposeRuntimeApi
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.mrboomdev.v2rayng2.BuildConfig
import com.mrboomdev.v2rayng2.ui.screens.home.HomeScreen

class MainActivity2: AppCompatActivity() {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalTextApi::class, ExperimentalComposeRuntimeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        
        if(BuildConfig.DEBUG) {
            Composer.setDiagnosticStackTraceEnabled(true)
        }
        
        setContent {
            MaterialTheme(
                colorScheme = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if(isSystemInDarkTheme()) {
                        dynamicDarkColorScheme(this)
                    } else dynamicLightColorScheme(this)
                } else {
                    if(isSystemInDarkTheme()) {
                        darkColorScheme()
                    } else lightColorScheme()
                }
            ) {
                HomeScreen()
            }
        }
    }
}