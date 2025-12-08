package com.mrboomdev.v2rayng2.ui.screens.simple

import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrboomdev.navigation.core.plusAssign
import com.mrboomdev.v2rayng2.R
import com.mrboomdev.v2rayng2.ui.FontFamilies
import com.mrboomdev.v2rayng2.ui.Navigation
import com.mrboomdev.v2rayng2.ui.Routes
import com.v2ray.ang.ui.SubSettingActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleScreen(
    viewModel: SimpleViewModel = run {
        val context = LocalContext.current
        viewModel { SimpleViewModel(context) }
    }
) {
    val activity = LocalActivity.current!!
    val navigation = Navigation.current()

    val currentGroup by viewModel.selectedGroup.collectAsState()
    val currentConfiguration by viewModel.selectedServer.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        fontFamily = FontFamilies.googleSansFlex,
                        text = "V2rayNG2"
                    )
                },
                
                actions = {
                    IconButton(
                        onClick = {
                            activity.startActivity(Intent(activity, SubSettingActivity::class.java))
                        }
                    ) {
                        Icon(
                            modifier = Modifier.padding(6.dp),
                            painter = painterResource(R.drawable.ic_list_outlined),
                            contentDescription = "Groups"
                        )
                    }
                }
            )
        },
        
        floatingActionButton = {
            ExtendedFloatingActionButton(
                icon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_sentiment_neutral_outlined),
                        contentDescription = null
                    )
                },
                
                text = {
                    Text(
                        fontFamily = FontFamilies.googleSansFlex,
                        text = "Advanced"
                    )
                },
                
                onClick = {
                    navigation.clear()
                    navigation += Routes.AdvancedDashboard
                }
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                modifier = Modifier
                    .padding(64.dp)
                    .widthIn(max = 256.dp)
                    .fillMaxWidth()
                    .aspectRatio(1f),
                
                shape = CircleShape,
                
                colors = ButtonDefaults.buttonColors(
                    containerColor = if(isRunning) {
                        MaterialTheme.colorScheme.primary
                    } else MaterialTheme.colorScheme.primaryContainer,

                    contentColor = if(isRunning) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else MaterialTheme.colorScheme.primary,
                ),
                
                onClick = {
                    viewModel.toggle()
                }
            ) { 
                Icon(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    
                    painter = painterResource(R.drawable.ic_power),
                    contentDescription = null
                )
            }

            Text(
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp),

                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamilies.googleSansFlex,
                text = "Current configuration"
            )

            Surface(
                color = Color.Transparent,
                border = BorderStroke(1.dp, SolidColor(MaterialTheme.colorScheme.secondary))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    currentGroup?.also { group ->
                        Text(
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamilies.googleSansFlex,
                            text = "From group: ${group.second.remarks}"
                        )
                    }
                    
                    Text(
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = FontFamilies.googleSansFlex,
                        text = currentConfiguration?.second?.remarks ?: "None"
                    )
                }
            }
            
            Text(
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 16.dp, bottom = 8.dp),
                
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamilies.googleSansFlex,
                text = "My requirements"
            )

            Surface(
                color = Color.Transparent,
                border = BorderStroke(1.dp, SolidColor(MaterialTheme.colorScheme.secondary)),
                
                onClick = {
                    navigation += Routes.Requirements
                }
            ) {
                Row(
                   modifier = Modifier
                       .fillMaxWidth()
                       .padding(16.dp)
                ) {
                    Text(
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = FontFamilies.googleSansFlex,
                        text = "Select apps that has to work"
                    )
                }
            }
        }
    }
}