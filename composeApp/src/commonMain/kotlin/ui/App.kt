package ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import ui.component.MainPage
import ui.component.PopUpDialog
import ui.component.SettingPage
import ui.component.WithCustomGesturesDetectTimeout
import ui.vm.AppVM

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
@Preview
fun App(vm: AppVM = viewModel()) {
    val state by vm.uiState.collectAsState()
    val terms by vm.terms.collectAsState()
    MaterialTheme {
        WithCustomGesturesDetectTimeout(doubleTapTimeoutMillis = 200, longPressTimeoutMillis = 300) {
            val pagerState = rememberPagerState(0, pageCount = { 2 })
            val uiScope = rememberCoroutineScope()
            Scaffold(topBar = {
                TopAppBar(title = {
                    Text(text = "学在吉大")
                }, actions = {
                    OutlinedButton({
                        val to = when (pagerState.currentPage) {
                            0 -> {
                                1
                            }

                            1 -> {
                                0
                            }

                            else -> error("this should not happen")
                        }
                        uiScope.launch { pagerState.animateScrollToPage(to) }
                    }) {
                        Icon(Icons.Filled.Settings, null)
                    }
                    var display by remember { mutableStateOf(false) }
                    OutlinedButton(
                        {
                            display = !display
                            if (vm.courses.isEmpty())
                                vm.refreshTerms {
                                    vm.refreshVideos()
                                }
                        }, contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(state.courseNameFilter.ifBlank { "全部课程" })
                            DropdownMenu(expanded = display, onDismissRequest = { display = false }) {
                                DropdownMenuItem(text = {
                                    Text(text = "无")
                                }, onClick = {
                                    display = false
                                    vm.setCourseNameFilter("")
                                })
                                for (item in vm.courses) DropdownMenuItem(text = {
                                    Text(text = item)
                                }, onClick = {
                                    display = false
                                    vm.setCourseNameFilter(item)
                                })
                            }
                        }
                    }
                    var display2 by remember { mutableStateOf(false) }
                    OutlinedButton(
                        {
                            display2 = !display2
                            if (vm.terms.value.isEmpty())
                                vm.refreshTerms {
                                    vm.refreshVideos()
                                }
                        }, contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(state.currentTerm?.let { it.year + it.name } ?: "not specific")
                            DropdownMenu(expanded = display2, onDismissRequest = { display2 = false }) {
                                for (item in terms) DropdownMenuItem(text = {
                                    Text(text = item.year + item.name)
                                }, onClick = {
                                    display2 = false
                                    vm.setTerm(item)
                                })
                            }
                        }
                    }

                })
            }, snackbarHost = {
                SnackbarHost(
                    hostState = vm.snackbarHostState
                ) {
                    Snackbar(Modifier.padding(10.dp), action = {
                        TextButton(onClick = {
                            vm.snackbarHostState.currentSnackbarData?.dismiss()
                        }) {
                            Text(text = vm.snackbarHostState.currentSnackbarData?.visuals?.actionLabel ?: "")
                        }
                    }) {
                        Text(text = vm.snackbarHostState.currentSnackbarData?.visuals?.message ?: "")
                    }
                }
            }) { padding ->
                HorizontalPager(pagerState) { page ->
                    Box(Modifier.padding(padding).fillMaxSize()) {
                        when (page) {
                            0 -> {
                                MainPage(vm)
                            }

                            1 -> {
                                SettingPage(state, vm)
                            }
                        }
                        PopUpDialog(state, vm)
                    }
                }
            }
            LaunchedEffect(Unit) {
                vm.casLogin {
                    vm.refreshTerms {
                        vm.refreshVideos()
                    }
                }
            }
        }
    }
}

