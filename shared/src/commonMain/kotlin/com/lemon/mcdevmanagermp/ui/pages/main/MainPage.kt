package com.lemon.mcdevmanagermp.ui.pages.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.platform.BackHandler
import com.lemon.mcdevmanagermp.platform.openUrl
import com.lemon.mcdevmanagermp.ui.components.AppScaffold
import com.lemon.mcdevmanagermp.ui.navigation.Route
import com.lemon.mcdevmanagermp.ui.pages.main.layout.CompactLayout
import com.lemon.mcdevmanagermp.ui.pages.main.layout.ExpandedLayout
import com.lemon.mcdevmanagermp.ui.pages.main.layout.MediumLayout
import com.lemon.mcdevmanagermp.ui.pages.update.UpdateAction
import com.lemon.mcdevmanagermp.ui.pages.update.UpdateDialog
import com.lemon.mcdevmanagermp.ui.pages.update.UpdateEffect
import com.lemon.mcdevmanagermp.ui.pages.update.UpdateViewModel
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.Notification
import com.mohamedrejeb.calf.permissions.Permission
import com.mohamedrejeb.calf.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainPage(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToAddAccount: () -> Unit = {},
    onNavigateToSubPage: (Route) -> Unit = {}
) {
    val viewModel = remember { MainViewModel() }
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 更新检查
    val updateViewModel = remember { UpdateViewModel() }
    val updateState by updateViewModel.state.collectAsState()
    val notificationPermissionState = rememberPermissionState(Permission.Notification)

    LaunchedEffect(Unit) {
        updateViewModel.dispatch(UpdateAction.CheckUpdate)
    }

    LaunchedEffect(Unit) {
        updateViewModel.effect.collect { effect ->
            when (effect) {
                is UpdateEffect.ShowToast -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                }

                is UpdateEffect.OpenUrl -> openUrl(effect.url)
            }
        }
    }

    // 非HOME_TAB 在返回时会转到 HOME_TAB
    BackHandler(enabled = state.selectedTab != MainTab.Home) {
        viewModel.dispatch(MainAction.SelectTab(MainTab.Home))
    }

    AppScaffold(
        viewEffect = viewModel.effect,
        onEffect = { effect ->
            when (effect) {
                is MainEffect.ShowToast -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                }

                is MainEffect.NavigateTo -> onNavigateToSubPage(effect.route)
                MainEffect.SessionExpired -> {
                    scope.launch { snackbarHostState.showSnackbar("登录已过期，请重新登录") }
                    onNavigateToLogin()
                }
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 80.dp) // 避免 Snackbar 遮挡底部 NavigationBar
            ) { data ->
                val colors = LocalAppColors.current
                Snackbar(
                    snackbarData = data,
                    shape = RoundedCornerShape(8.dp),
                    containerColor = colors.surface,
                    contentColor = colors.onSurface
                )
            }
        }
    ) {
        val onCheckUpdate: () -> Unit = { updateViewModel.dispatch(UpdateAction.CheckUpdate) }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val widthSizeClass = when {
                maxWidth < 600.dp -> WindowWidthSizeClass.Compact
                maxWidth < 840.dp -> WindowWidthSizeClass.Medium
                else -> WindowWidthSizeClass.Expanded
            }
            when (widthSizeClass) {
                WindowWidthSizeClass.Compact -> CompactLayout(
                    state = state,
                    onAction = viewModel::dispatch,
                    onNavigateToSubPage = onNavigateToSubPage,
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToAddAccount = onNavigateToAddAccount,
                    onAccountSwitched = { viewModel.dispatch(MainAction.RefreshData) },
                    onCheckUpdate = onCheckUpdate
                )

                WindowWidthSizeClass.Medium -> MediumLayout(
                    state = state,
                    onAction = viewModel::dispatch,
                    onNavigateToSubPage = onNavigateToSubPage,
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToAddAccount = onNavigateToAddAccount,
                    onAccountSwitched = { viewModel.dispatch(MainAction.RefreshData) },
                    onCheckUpdate = onCheckUpdate
                )

                else -> ExpandedLayout(
                    state = state,
                    onAction = viewModel::dispatch,
                    onNavigateToSubPage = onNavigateToSubPage,
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToAddAccount = onNavigateToAddAccount,
                    onAccountSwitched = { viewModel.dispatch(MainAction.RefreshData) },
                    onCheckUpdate = onCheckUpdate
                )
            }
        }
    }

    // 更新 Dialog
    if (updateState.showDialog) {
        UpdateDialog(
            state = updateState,
            onAction = updateViewModel::dispatch,
            notificationPermissionState = notificationPermissionState
        )
    }
}

@Composable
internal fun PlaceholderTabContent(name: String) {
    val colors = LocalAppColors.current
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onSurface.copy(alpha = 0.5f)
        )
    }
}
