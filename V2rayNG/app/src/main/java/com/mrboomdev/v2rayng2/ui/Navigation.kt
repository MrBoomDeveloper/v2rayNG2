package com.mrboomdev.v2rayng2.ui

import androidx.compose.runtime.Composable
import com.mrboomdev.navigation.core.TypeSafeNavigation
import com.mrboomdev.v2rayng2.ui.screens.home.HomeScreen
import com.mrboomdev.v2rayng2.ui.screens.requirements.RequirementsScreen
import com.mrboomdev.v2rayng2.ui.screens.simple.SimpleScreen
import kotlinx.serialization.Serializable

val Navigation = TypeSafeNavigation<Routes>()

@Serializable
sealed interface Routes {
    @Serializable
    data object SimpleDashboard: Routes {
        @Composable
        override fun Content() = SimpleScreen()
    }
    
    @Serializable
    data object AdvancedDashboard: Routes {
        @Composable
        override fun Content() = HomeScreen()
    }
    
    @Serializable
    data object Requirements: Routes {
        @Composable
        override fun Content() = RequirementsScreen()
    }
    
    @Composable
    fun Content()
}