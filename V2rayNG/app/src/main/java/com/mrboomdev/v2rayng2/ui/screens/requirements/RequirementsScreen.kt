package com.mrboomdev.v2rayng2.ui.screens.requirements

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mrboomdev.navigation.core.safePop
import com.mrboomdev.v2rayng2.R
import com.mrboomdev.v2rayng2.ui.FontFamilies
import com.mrboomdev.v2rayng2.ui.Navigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequirementsScreen() {
    val navigation = Navigation.current()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navigation.safePop()
                        }
                    ) {
                        Icon(
                            modifier = Modifier.padding(6.dp),
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = "Back"
                        )
                    }
                },
                
                title = {
                    Text(
                        fontFamily = FontFamilies.googleSansFlex,
                        text = "My requirements"
                    )
                }
            )
        }
    ) { contentPadding ->
        
    }
}