package com.lemon.mcdevmanagermp.ui.pages.work.workmanage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.lemon.mcdevmanagermp.ui.components.LocalSnackbarHostState
import com.lemon.mcdevmanagermp.ui.components.LocalWindowWidthSizeClass
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.WorkManageCompactLayout
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.WorkManageExpandedLayout
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.WorkManageMediumLayout
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import kotlinx.coroutines.launch

@Composable
fun WorkManagePage(
    onBack: () -> Unit,
    onNeedReLogin: () -> Unit = {}
) {
    val viewModel = remember { WorkManageViewModel() }
    val state by viewModel.state.collectAsState()
    val colors = LocalAppColors.current

    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is WorkManageEffect.ShowToast -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                }

                WorkManageEffect.NeedReLogin -> onNeedReLogin()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatch(WorkManageAction.LoadData)
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        val widthSizeClass = LocalWindowWidthSizeClass.current
        when (widthSizeClass) {
            WindowWidthSizeClass.Expanded -> WorkManageExpandedLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack
            )

            WindowWidthSizeClass.Medium -> WorkManageMediumLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack
            )

            else -> WorkManageCompactLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack
            )
        }
    }
}
