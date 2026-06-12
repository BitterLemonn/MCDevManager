package com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.components.LocalSnackbarHostState
import com.lemon.mcdevmanagermp.ui.components.LocalWindowWidthSizeClass
import com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis.layout.ModAnalysisCompactLayout
import com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis.layout.ModAnalysisExpandedLayout
import com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis.layout.ModAnalysisMediumLayout
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import kotlinx.coroutines.launch

/**
 * 模组分析页面入口
 * 根据屏幕宽度选择对应布局
 */
@Composable
fun ModAnalysisPage(
    initialIid: String = "",
    initialPlatform: String = "pe",
    onBack: () -> Unit,
) {
    val viewModel = remember { ModAnalysisViewModel() }
    val state by viewModel.state.collectAsState()
    val colors = LocalAppColors.current
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()

    // Effect 收集
    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ModAnalysisEffect.ShowToast -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                }

                ModAnalysisEffect.NeedReLogin -> {
                    scope.launch { snackbarHostState.showSnackbar("登录已过期，请重新登录") }
                }
            }
        }
    }

    // 初始化加载
    LaunchedEffect(Unit) {
        viewModel.dispatch(ModAnalysisAction.InitLoad(initialIid, initialPlatform))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        val widthSizeClass = LocalWindowWidthSizeClass.current

        when (widthSizeClass) {
            WindowWidthSizeClass.Compact -> ModAnalysisCompactLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack,
                navBarBottom = navBarBottom,
            )

            WindowWidthSizeClass.Medium -> ModAnalysisMediumLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack,
                navBarBottom = navBarBottom,
            )

            else -> ModAnalysisExpandedLayout(
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
