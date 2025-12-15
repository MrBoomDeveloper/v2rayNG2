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
import androidx.compose.runtime.remember
import androidx.compose.runtime.tooling.ComposeStackTraceMode
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.material.color.DynamicColors
import com.mrboomdev.navigation.core.sealedNavigationGraph
import com.mrboomdev.navigation.jetpack.JetpackNavigationHost
import com.mrboomdev.navigation.jetpack.rememberJetpackNavigation
import com.mrboomdev.v2rayng2.BuildConfig

class MainActivity2: AppCompatActivity() {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalTextApi::class, ExperimentalComposeRuntimeApi::class,
        ExperimentalMaterial3ExpressiveApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        DynamicColors.applyToActivityIfAvailable(this)
        enableEdgeToEdge()
        
        if(BuildConfig.DEBUG) {
            Composer.setDiagnosticStackTraceMode(ComposeStackTraceMode.SourceInformation)
        }
        
        setContent {
            MaterialExpressiveTheme(
                colorScheme = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if(isSystemInDarkTheme()) {
                        dynamicDarkColorScheme(this)
                    } else dynamicLightColorScheme(this)
                } else {
                    if(isSystemInDarkTheme()) {
                        darkColorScheme()
                    } else expressiveLightColorScheme()
                }
            ) {
                JetpackNavigationHost<Routes>(
                    navigation = rememberJetpackNavigation(Routes.SimpleDashboard),
                    graph = remember {
                        sealedNavigationGraph { 
                            it.Content()
                        }
                    }
                )
            }
        }
    }
}