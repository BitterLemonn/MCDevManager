package com.lemon.mcdevmanagermp.ui.pages.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.components.AppScaffold
import com.lemon.mcdevmanagermp.ui.navigation.Route
import com.lemon.mcdevmanagermp.ui.pages.main.layout.CompactLayout
import com.lemon.mcdevmanagermp.ui.pages.main.layout.ExpandedLayout
import com.lemon.mcdevmanagermp.ui.pages.main.layout.MediumLayout
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import kotlinx.coroutines.launch

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

    AppScaffold(
        viewEffect = viewModel.effect,
        onEffect = { effect ->
            when (effect) {
                is MainEffect.ShowToast -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                }

                is MainEffect.NavigateTo -> onNavigateToSubPage(effect.route)
                MainEffect.SessionExpired -> onNavigateToLogin()
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
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
                    onAccountSwitched = { viewModel.dispatch(MainAction.RefreshData) }
                )

                WindowWidthSizeClass.Medium -> MediumLayout(
                    state = state,
                    onAction = viewModel::dispatch,
                    onNavigateToSubPage = onNavigateToSubPage,
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToAddAccount = onNavigateToAddAccount,
                    onAccountSwitched = { viewModel.dispatch(MainAction.RefreshData) }
                )

                else -> ExpandedLayout(
                    state = state,
                    onAction = viewModel::dispatch,
                    onNavigateToSubPage = onNavigateToSubPage,
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToAddAccount = onNavigateToAddAccount,
                    onAccountSwitched = { viewModel.dispatch(MainAction.RefreshData) }
                )
            }
        }
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
