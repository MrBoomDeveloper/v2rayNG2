package com.mrboomdev.v2rayng2.ui.screens.home

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrboomdev.v2rayng2.R
import com.mrboomdev.v2rayng2.ui.FontFamilies
import com.mrboomdev.v2rayng2.ui.components.InfoBox
import com.mrboomdev.v2rayng2.utils.exclude
import com.v2ray.ang.extension.toast
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.ui.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private enum class HomeScreenState {
    EMPTY, NOTHING_FOUND, NORMAL, LOADING
}

@OptIn(ExperimentalTextApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = run {
        val context = LocalContext.current
        viewModel { HomeViewModel(context) }
    }
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val activity = LocalActivity.current!!
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val groups by viewModel.groups.collectAsState()
    val selectedServer by viewModel.selectedServer.collectAsState()
    val testingState by viewModel.testingState.collectAsState()
    val pagerState = rememberPagerState { groups.size }
    
    var isSearching by rememberSaveable { mutableStateOf(false) }
    val searchQuery = rememberTextFieldState()

    val vpnPermissionRequestLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if(it.resultCode == RESULT_OK) {
            viewModel.startVpn()
        }
    }
    
    BackHandler(enabled = drawerState.isOpen || isSearching) {
        when {
            drawerState.isOpen -> {
                coroutineScope.launch {
                    drawerState.close()
                }
            }
            
            isSearching -> {
                isSearching = false
                searchQuery.clearText()
            }
        }
    }
    
    testingState?.also { testingState -> 
        AlertDialog(
            onDismissRequest = {},
            
            title = {
                Text(when(testingState) {
                    TestingState.Finalizing -> "Final steps"
                    is TestingState.Testing -> "Testing"
                    TestingState.UpdatingSubscription -> "Updating subscription"
                })
            },
            
            text = {
                Text(when(val state = testingState) {
                    TestingState.Finalizing -> "Not so much stuff has left. Wait just a little bit..."
                    is TestingState.Testing -> "${state.done}/${state.total}\nWe are checking which servers are working..."
                    TestingState.UpdatingSubscription -> "Please wait a little bit. If subscription is large it can take a while..."
                })
            },
            
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelTest()
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    ModalNavigationDrawer(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        
        drawerState = drawerState,
        
        drawerContent = { 
            HomeSideSheet(
                activity = activity,
                onDismiss = {
                    coroutineScope.launch {
                        drawerState.close()
                    }
                }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),

            topBar = {
                if(isSearching) {
                    val focusRequester = remember { FocusRequester() }

                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                    
                    SearchBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        
                        expanded = false,
                        onExpandedChange = {},
                        
                        inputField = {
                            SearchBarDefaults.InputField(
                                leadingIcon = {
                                    Icon(
                                        modifier = Modifier.size(24.dp),
                                        painter = painterResource(R.drawable.ic_search_outlined),
                                        contentDescription = null
                                    ) 
                                },
                                    
                                modifier = Modifier.fillMaxWidth(),
                                query = searchQuery.text.toString(),
                                onQueryChange = { searchQuery.edit { replace(0, length, it) } },
                                expanded = false,
                                onExpandedChange = {},
                                placeholder = { Text("Search") },
                                
                                onSearch = {
                                    keyboardController?.hide()
                                },
                                    
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            isSearching = false
                                            searchQuery.clearText()
                                        }
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(24.dp),
                                            painter = painterResource(R.drawable.ic_close),
                                            contentDescription = "Close"
                                        )
                                    }
                                }
                            )
                        }
                    ) {}
                    
                    return@Scaffold
                }
                
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch { 
                                    drawerState.open() 
                                } 
                            }
                        ) {
                            Icon(
                                modifier = Modifier.padding(6.dp),
                                painter = painterResource(R.drawable.ic_menu),
                                contentDescription = "Menu"
                            )
                        }
                    },

                    title = {
                        Text(
                            fontFamily = FontFamilies.googleSansFlex,
                            text = "V2rayNG2"
                        )
                    },
                    
                    actions = {
                        IconButton(
                            onClick = {
                                isSearching = true
                            }
                        ) {
                            Icon(
                                modifier = Modifier.padding(6.dp),
                                painter = painterResource(R.drawable.ic_search_outlined),
                                contentDescription = stringResource(R.string.menu_item_search)
                            )
                        }

                        IconButton(
                            onClick = {
                                viewModel.requestUpdate(
                                    groupId = groups[pagerState.currentPage].first,
                                    onComplete = { activity.toast("Done updating") }
                                )
                            }
                        ) {
                            Icon(
                                modifier = Modifier.padding(6.dp),
                                painter = painterResource(R.drawable.ic_sync),
                                contentDescription = "Update"
                            )
                        }

                        Box {
                            var showDropdown by remember { mutableStateOf(false) }
                            
                            IconButton(
                                onClick = {
                                    showDropdown = true
                                }
                            ) {
                                Icon(
                                    modifier = Modifier.padding(2.dp),
                                    painter = painterResource(R.drawable.ic_add),
                                    contentDescription = stringResource(R.string.menu_item_add_config)
                                )
                            }

                            DropdownMenu(
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .widthIn(min = 175.dp),
                                
                                expanded = showDropdown,
                                onDismissRequest = { showDropdown = false },
                                shape = RoundedCornerShape(32.dp)
                            ) {
                                Text(
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamilies.googleSansFlex,
                                    color = MaterialTheme.colorScheme.secondary,
                                    text = "Import"
                                )
                                
                                DropdownMenuItem(
                                    contentPadding = PaddingValues(horizontal = 24.dp),
                                    
                                    leadingIcon = { 
                                        Icon(
                                            modifier = Modifier.size(24.dp),
                                            painter = painterResource(R.drawable.ic_qr_code_scanner), 
                                            contentDescription = "QR code"
                                        )
                                    },
                                    
                                    text = {
                                        Text(
                                            fontFamily = FontFamilies.googleSansFlex,
                                            text = "QR code"
                                        ) 
                                    },
                                    
                                    onClick = {
                                        showDropdown = false
//                                        activity.startActivity(Intent(activity, ScScannerActivity::class.java))
                                        activity.toast("Not implemented yet!")
                                    }
                                )

                                DropdownMenuItem(
                                    contentPadding = PaddingValues(horizontal = 24.dp),

                                    leadingIcon = {
                                        Icon(
                                            modifier = Modifier.size(24.dp),
                                            painter = painterResource(R.drawable.ic_paste_outlined),
                                            contentDescription = "Paste from clipboard"
                                        )
                                    },

                                    text = {
                                        Text(
                                            fontFamily = FontFamilies.googleSansFlex,
                                            text = "Paste from clipboard"
                                        )
                                    },

                                    onClick = {
                                        showDropdown = false
                                        activity.toast("Not implemented yet!")
                                    }
                                )

                                DropdownMenuItem(
                                    contentPadding = PaddingValues(horizontal = 24.dp),

                                    leadingIcon = {
                                        Icon(
                                            modifier = Modifier.size(24.dp),
                                            painter = painterResource(R.drawable.ic_upload_file_outlined),
                                            contentDescription = "Select file"
                                        )
                                    },

                                    text = {
                                        Text(
                                            fontFamily = FontFamilies.googleSansFlex,
                                            text = "Select file"
                                        )
                                    },

                                    onClick = {
                                        showDropdown = false
                                        activity.toast("Not implemented yet!")
                                    }
                                )

                                Text(
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamilies.googleSansFlex,
                                    color = MaterialTheme.colorScheme.secondary,
                                    text = "Type manually"
                                )

                                listOf(
                                    "a" to "VMess",
                                    "b" to "VLESS",
                                    "d" to "Shadowsocks",
                                    "1" to "SOCKS",
                                    "2" to "HTTP",
                                    "c" to "Trojan",
                                    "e" to "WireGuard",
                                    "h" to "Hysteria2"
                                ).forEach { (key, title) ->
                                    DropdownMenuItem(
                                        contentPadding = PaddingValues(horizontal = 24.dp),

                                        text = {
                                            Text(
                                                fontFamily = FontFamilies.googleSansFlex,
                                                text = title
                                            )
                                        },

                                        onClick = {
                                            showDropdown = false
                                            activity.toast("Not implemented yet!")
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            },
            
            floatingActionButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    SmallFloatingActionButton(
                        onClick = {

                        }
                    ) {
                        Icon(
                            modifier = Modifier.size(16.dp),
                            painter = painterResource(R.drawable.ic_magic_filled),
                            contentDescription = "Automatically pick best"
                        )
                    }
                    
                    val isRunning by viewModel.isRunning.collectAsState()
                    
                    FloatingActionButton(
                        containerColor = if(isRunning) {
                            MaterialTheme.colorScheme.primary
                        } else MaterialTheme.colorScheme.primaryContainer,

                        contentColor = if(isRunning) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else MaterialTheme.colorScheme.primary,
                        
                        onClick = {
                            if(viewModel.selectedServer.value == null) {
                                activity.toast("Select server at first!")
                                return@FloatingActionButton
                            }
                            
                            viewModel.toggleVpn(
                                onFailure = { intent ->
                                    vpnPermissionRequestLauncher.launch(intent)
                                }
                            )
                        }
                    ) {
                        Icon(
                            modifier = Modifier.size(32.dp),
                            contentDescription = null,

                            painter = painterResource(if(isRunning) {
                                R.drawable.ic_stop_filled
                            } else R.drawable.ic_play_filled),
                        )
                    }
                }
            }
        ) { contentPadding ->
            Column(Modifier.fillMaxSize()) {
                if(groups.isNotEmpty()) {
                    PrimaryScrollableTabRow(
                        modifier = Modifier.padding(contentPadding.exclude(bottom = true)),
                        minTabWidth = 16.dp,
                        edgePadding = 0.dp,
                        selectedTabIndex = pagerState.currentPage,
                        divider = {}
                    ) {
                        groups.forEachIndexed { index, group ->
                            Tab(
                                selected = pagerState.currentPage == index,

                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.scrollToPage(index)
                                    }
                                }
                            ) {
                                Text(
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamilies.googleSansFlex,
                                    text = group.second.remarks
                                )
                            }
                        }
                    }

                    HorizontalDivider()
                }
                
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = false
                ) { page ->
                    val servers by remember(searchQuery.text) {
                        viewModel.servers.map { servers ->
                            servers?.filter { server ->
                                groups[page].first == server.profile.subscriptionId
                                        && server.profile.remarks.contains(searchQuery.text)
                            } ?: emptyList()
                        }
                    }.collectAsState(emptyList())
                    
                    val areThereAnyServers by viewModel.servers.map { servers ->
                        servers?.any { server ->
                            groups[page].first == server.profile.subscriptionId
                        }
                    }.collectAsState(null)

                    Crossfade(
                        modifier = Modifier.fillMaxSize(),
                        targetState = when {
                            areThereAnyServers == false -> HomeScreenState.EMPTY
                            areThereAnyServers == null -> HomeScreenState.LOADING
                            servers.isEmpty() -> HomeScreenState.NOTHING_FOUND
                            else -> HomeScreenState.NORMAL
                        }
                    ) { state ->
                        when(state) {
                            HomeScreenState.EMPTY -> {
                                InfoBox(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .wrapContentSize(Alignment.Center),
                                    icon = painterResource(R.drawable.ic_sentiment_neutral_outlined),
                                    title = "No servers",
                                    message = ""
                                )
                            }
                            
                            HomeScreenState.LOADING -> {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .wrapContentSize(Alignment.Center)
                                )
                            }
                            
                            HomeScreenState.NOTHING_FOUND -> {
                                InfoBox(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .wrapContentSize(Alignment.Center),
                                    icon = painterResource(R.drawable.ic_search_outlined),
                                    title = "No servers found",
                                    message = ""
                                )
                            }
                            
                            HomeScreenState.NORMAL -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = contentPadding.exclude(top = true)
                                ) {
                                    items(
                                        items = servers,
                                        key = { it.guid }
                                    ) {
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .animateItem(),
                                            
                                            color = if(it.guid == selectedServer?.first) {
                                                MaterialTheme.colorScheme.surfaceContainerHigh
                                            } else MaterialTheme.colorScheme.surface,

                                            onClick = {
                                                viewModel.selectServer(it.guid)
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .padding(vertical = 8.dp, horizontal = 16.dp)
                                                        .weight(1f),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        fontFamily = FontFamilies.googleSansFlex,
                                                        text = it.profile.remarks
                                                    )

                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                                    ) {
                                                        Text(
                                                            color = MaterialTheme.colorScheme.secondary,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontFamily = FontFamilies.googleSansFlex,
                                                            text = it.profile.configType.name
                                                        )

                                                        MmkvManager.decodeServerAffiliationInfo(it.guid)?.also { delay ->
                                                            Text(
                                                                modifier = Modifier.alpha(.5f),
                                                                style = MaterialTheme.typography.bodySmall,
                                                                fontFamily = FontFamilies.googleSansFlex,

                                                                color = if(delay.testDelayMillis > 0L) {
                                                                    Color.Green
                                                                } else MaterialTheme.colorScheme.error,

                                                                text = delay.getTestDelayString()
                                                            )
                                                        }
                                                    }
                                                }

                                                Box {
                                                    var showDropdown by remember { mutableStateOf(false) }

                                                    IconButton(
                                                        onClick = {
                                                            showDropdown = true
                                                        }
                                                    ) {
                                                        Icon(
                                                            modifier = Modifier.size(24.dp),
                                                            painter = painterResource(id = R.drawable.ic_more_vertical),
                                                            contentDescription = "More"
                                                        )
                                                    }

                                                    DropdownMenu(
                                                        modifier = Modifier.widthIn(min = 175.dp),
                                                        expanded = showDropdown,
                                                        onDismissRequest = { showDropdown = false },
                                                        shape = RoundedCornerShape(32.dp)
                                                    ) {
                                                        DropdownMenuItem(
                                                            contentPadding = PaddingValues(horizontal = 16.dp),

                                                            leadingIcon = {
                                                                Icon(
                                                                    modifier = Modifier.size(24.dp),
                                                                    painter = painterResource(R.drawable.ic_edit_outlined),
                                                                    contentDescription = "Edit"
                                                                )
                                                            },

                                                            text = {
                                                                Text(
                                                                    fontFamily = FontFamilies.googleSansFlex,
                                                                    text = "Edit"
                                                                )
                                                            },

                                                            onClick = {
                                                                showDropdown = false
                                                                activity.toast("Not implemented yet!")
                                                            }
                                                        )

                                                        DropdownMenuItem(
                                                            contentPadding = PaddingValues(horizontal = 16.dp),

                                                            leadingIcon = {
                                                                Icon(
                                                                    modifier = Modifier.size(24.dp),
                                                                    painter = painterResource(R.drawable.ic_share_outlined),
                                                                    contentDescription = "Share"
                                                                )
                                                            },

                                                            text = {
                                                                Text(
                                                                    fontFamily = FontFamilies.googleSansFlex,
                                                                    text = "Share"
                                                                )
                                                            },

                                                            onClick = {
                                                                showDropdown = false
                                                                activity.toast("Not implemented yet!")
                                                            }
                                                        )

                                                        DropdownMenuItem(
                                                            contentPadding = PaddingValues(horizontal = 16.dp),

                                                            leadingIcon = {
                                                                Icon(
                                                                    modifier = Modifier.size(24.dp),
                                                                    painter = painterResource(R.drawable.ic_delete_outlined),
                                                                    contentDescription = "Delete"
                                                                )
                                                            },

                                                            text = {
                                                                Text(
                                                                    fontFamily = FontFamilies.googleSansFlex,
                                                                    text = "Delete"
                                                                )
                                                            },

                                                            onClick = {
                                                                showDropdown = false
                                                                activity.toast("Not implemented yet!")
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun HomeSideSheet(
    activity: Activity,
    onDismiss: () -> Unit
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                modifier = Modifier.padding(
                    start = 16.dp,
                    top = 16.dp,
                    bottom = 12.dp
                ),

                fontFamily = FontFamily(
                    Font(
                        R.font.google_sans_flex,
                        variationSettings = FontVariation.Settings(
                            FontVariation.weight(1)
                        )
                    )
                ),

                style = MaterialTheme.typography.displayMedium,
                text = "v2rayNG2"
            )

            NavigationDrawerItem(
                selected = false,

                label = {
                    Text(
                        fontFamily = FontFamilies.googleSansFlex,
                        text = "Groups"
                    )
                },

                icon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_list_outlined),
                        contentDescription = "Groups"
                    )
                },

                onClick = {
                    activity.startActivity(Intent(activity, SubSettingActivity::class.java))
                    onDismiss()
                }
            )

            NavigationDrawerItem(
                selected = false,

                label = {
                    Text(
                        fontFamily = FontFamilies.googleSansFlex,
                        text = stringResource(R.string.per_app_proxy_settings)
                    )
                },

                icon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_apps),
                        contentDescription = stringResource(R.string.per_app_proxy_settings)
                    )
                },

                onClick = {
                    activity.startActivity(Intent(activity, PerAppProxyActivity::class.java))
                    onDismiss()
                }
            )

            NavigationDrawerItem(
                selected = false,

                label = {
                    Text(
                        fontFamily = FontFamilies.googleSansFlex,
                        text = stringResource(R.string.routing_settings_title)
                    )
                },

                icon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_route),
                        contentDescription = stringResource(R.string.routing_settings_title)
                    )
                },

                onClick = {
                    activity.startActivity(Intent(activity, RoutingSettingActivity::class.java))
                    onDismiss()
                }
            )

            NavigationDrawerItem(
                selected = false,

                label = {
                    Text(
                        fontFamily = FontFamilies.googleSansFlex,
                        text = stringResource(R.string.title_user_asset_setting)
                    )
                },

                icon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_folder_outlined),
                        contentDescription = stringResource(R.string.title_user_asset_setting)
                    )
                },

                onClick = {
                    activity.startActivity(Intent(activity, UserAssetActivity::class.java))
                    onDismiss()
                }
            )

            NavigationDrawerItem(
                selected = false,

                label = {
                    Text(
                        fontFamily = FontFamilies.googleSansFlex,
                        text = stringResource(R.string.title_settings)
                    )
                },

                icon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_settings_outlined),
                        contentDescription = stringResource(R.string.title_settings)
                    )
                },

                onClick = {
                    activity.startActivity(Intent(activity, SettingsActivity::class.java))
                    onDismiss()
                }
            )

            HorizontalDivider(
                modifier = Modifier
                    .padding(vertical = 8.dp)
            )

            NavigationDrawerItem(
                selected = false,

                label = {
                    Text(
                        fontFamily = FontFamilies.googleSansFlex,
                        text = stringResource(R.string.update_check_for_update)
                    )
                },

                icon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_update),
                        contentDescription = stringResource(R.string.update_check_for_update)
                    )
                },

                onClick = {
                    activity.startActivity(Intent(activity, CheckUpdateActivity::class.java))
                    onDismiss()
                }
            )

            NavigationDrawerItem(
                selected = false,

                label = {
                    Text(
                        fontFamily = FontFamilies.googleSansFlex,
                        text = stringResource(R.string.title_about)
                    )
                },

                icon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_info_outlined),
                        contentDescription = stringResource(R.string.title_about)
                    )
                },

                onClick = {
                    activity.startActivity(Intent(activity, AboutActivity::class.java))
                    onDismiss()
                }
            )
        }
    }
}