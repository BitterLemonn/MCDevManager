package com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.components.LocalWindowWidthSizeClass
import com.lemon.mcdevmanagermp.ui.components.collectUiEffect
import com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail.layout.DayDetailCompactLayout
import com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail.layout.DayDetailExpandedLayout
import com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail.layout.DayDetailMediumLayout
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

/**
 * 数据追踪页面入口
 * 根据屏幕宽度选择对应布局
 */
@Composable
fun DayDetailPage(
    onBack: () -> Unit,
) {
    val viewModel = remember { DayDetailViewModel() }
    val state by viewModel.state.collectAsState()
    val colors = LocalAppColors.current
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    viewModel.effect.collectUiEffect { effect ->
        when (effect) {
            is DayDetailEffect.ShowToast -> showToast(effect.message)

            DayDetailEffect.NeedReLogin -> showToast("登录已过期，请重新登录")
        }
    }

    // 初始化加载
    LaunchedEffect(Unit) {
        viewModel.dispatch(DayDetailAction.InitLoad)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        val widthSizeClass = LocalWindowWidthSizeClass.current

        when (widthSizeClass) {
            WindowWidthSizeClass.Compact -> DayDetailCompactLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack,
                navBarBottom = navBarBottom,
            )

            WindowWidthSizeClass.Medium -> DayDetailMediumLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack,
                navBarBottom = navBarBottom,
            )

            else -> DayDetailExpandedLayout(
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

    }
}
