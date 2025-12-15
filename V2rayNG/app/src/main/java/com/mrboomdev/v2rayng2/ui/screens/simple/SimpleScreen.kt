package com.mrboomdev.v2rayng2.ui.screens.simple

import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrboomdev.navigation.core.plusAssign
import com.mrboomdev.v2rayng2.R
import com.mrboomdev.v2rayng2.ui.FontFamilies
import com.mrboomdev.v2rayng2.ui.Navigation
import com.mrboomdev.v2rayng2.ui.Routes
import com.v2ray.ang.ui.SubSettingActivity

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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
    val state by viewModel.state.collectAsState()
    
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
                        enabled = !state.isLoading,
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
                    if(state.isLoading) {
                        return@ExtendedFloatingActionButton
                    }
                    
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
            Box(
                contentAlignment = Alignment.Center
            ) {
                val buttonInteractionSource = remember { MutableInteractionSource() }
                val isButtonPressed by buttonInteractionSource.collectIsPressedAsState()

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(animateFloatAsState(if (!state.isLoading) 0f else 1f).value)
                        .scale(animateFloatAsState(if (!state.isLoading) .8f else 1f).value)
                        .animateContentSize(),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LoadingIndicator(Modifier.size(192.dp))

                        Text(
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamilies.googleSansFlex,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,

                            text = when (val state = state) {
                                SimpleState.Off, SimpleState.On -> ""
                                SimpleState.UpdatingSubscription -> "Updating your group. It may take some time..."
                                is SimpleState.DeepTesting -> "Deep testing configurations (${state.current}/${state.total})"
                                is SimpleState.PingTesting -> "TCPing testing configurations (${state.current}/${state.total})"
                                is SimpleState.UrlTesting -> "Url testing configurations (${state.current}/${state.total})"
                            }
                        )
                    }
                }

                Button(
                    modifier = Modifier
                        .padding(horizontal = 64.dp)
                        .padding(top = 16.dp, bottom = 64.dp)
                        .widthIn(max = 256.dp)
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .scale(
                            animateFloatAsState(
                                if (isButtonPressed) .95f else 1f,
                                spring(stiffness = Spring.StiffnessLow)
                            ).value
                        )
                        .alpha(animateFloatAsState(if (state.isLoading) 0f else 1f).value)
                        .scale(animateFloatAsState(if (state.isLoading) .5f else 1f).value),

                    interactionSource = buttonInteractionSource,
                    enabled = !state.isLoading,
                    shape = CircleShape,

                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state == SimpleState.On) {
                            MaterialTheme.colorScheme.primary
                        } else MaterialTheme.colorScheme.primaryContainer,

                        contentColor = if (state == SimpleState.On) {
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
            }

            Column(
                modifier = Modifier
                    .alpha(animateFloatAsState(
                        targetValue = if(state.isLoading) .25f else 1f, 
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ).value)
                    .padding(top = animateDpAsState(
                        targetValue = if(state.isLoading) 64.dp else 0.dp, 
                        animationSpec = spring(stiffness = Spring.StiffnessVeryLow)
                    ).value)
            ) {
                Text(
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 8.dp),
    
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamilies.googleSansFlex,
                    text = "Current configuration"
                )
    
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 16.dp, horizontal = 20.dp),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !state.isLoading,
                    onClick = {
                        // TODO: Open configuration editor
                    }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        currentGroup?.also { group ->
                            Text(
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = FontFamilies.googleSansFlex,
                                text = "From group: ${group.second.remarks}",
    
                                color = if (state.isLoading) {
                                    Color.Unspecified
                                } else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
    
                        Text(
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = FontFamilies.googleSansFlex,
                            text = currentConfiguration?.second?.remarks ?: "None",
    
                            color = if (state.isLoading) {
                                Color.Unspecified
                            } else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
    
                Text(
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 20.dp, bottom = 8.dp),
    
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamilies.googleSansFlex,
                    text = "Selected group"
                )
    
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 16.dp, horizontal = 20.dp),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !state.isLoading,
                    onClick = {
    //                    navigation += Routes.Requirements
                    }
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        fontFamily = FontFamilies.googleSansFlex,
                        text = ""
                    )
                }
    
                Text(
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 20.dp, bottom = 8.dp),
    
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamilies.googleSansFlex,
                    text = "My requirements"
                )
    
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 16.dp, horizontal = 20.dp),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !state.isLoading,
                    onClick = {
                        navigation += Routes.Requirements
                    }
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        fontFamily = FontFamilies.googleSansFlex,
                        text = "Select apps that has to work"
                    )
    
                    Icon(
                        modifier = Modifier
                            .size(16.dp)
                            .scale(scaleX = -1f, scaleY = 1f),
    
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = null
                    )
                }
            }
        }
    }
}