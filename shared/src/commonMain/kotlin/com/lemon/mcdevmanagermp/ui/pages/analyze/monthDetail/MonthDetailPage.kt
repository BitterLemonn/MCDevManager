package com.lemon.mcdevmanagermp.ui.pages.analyze.monthDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.lemon.mcdevmanagermp.ui.pages.analyze.monthDetail.layout.MonthDetailCompactLayout
import com.lemon.mcdevmanagermp.ui.pages.analyze.monthDetail.layout.MonthDetailExpandedLayout
import com.lemon.mcdevmanagermp.ui.pages.analyze.monthDetail.layout.MonthDetailMediumLayout
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import kotlinx.coroutines.launch

/**
 * 数据汇总页面入口
 * 根据屏幕宽度选择对应布局
 */
@Composable
fun MonthDetailPage(
    onBack: () -> Unit,
) {
    val viewModel = remember { MonthDetailViewModel() }
    val state by viewModel.state.collectAsState()
    val colors = LocalAppColors.current
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Effect 收集
    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MonthDetailEffect.ShowToast -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                }

                MonthDetailEffect.NeedReLogin -> {
                    scope.launch { snackbarHostState.showSnackbar("登录已过期，请重新登录") }
                }
            }
        }
    }

    // 初始化加载
    LaunchedEffect(Unit) {
        viewModel.dispatch(MonthDetailAction.InitLoad)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        val widthSizeClass = when {
            maxWidth < 600.dp -> WindowWidthSizeClass.Compact
            maxWidth < 840.dp -> WindowWidthSizeClass.Medium
            else -> WindowWidthSizeClass.Expanded
        }

        when (widthSizeClass) {
            WindowWidthSizeClass.Compact -> MonthDetailCompactLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack,
                navBarBottom = navBarBottom,
            )

            WindowWidthSizeClass.Medium -> MonthDetailMediumLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack,
                navBarBottom = navBarBottom,
            )

            else -> MonthDetailExpandedLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack,
                navBarBottom = navBarBottom,
            )
        }

        // Loading
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(36.dp),
                color = colors.primary,
                strokeWidth = 3.dp
            )
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = navBarBottom + 8.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                containerColor = colors.surface,
                contentColor = colors.textColor
            )
        }
    }
}
