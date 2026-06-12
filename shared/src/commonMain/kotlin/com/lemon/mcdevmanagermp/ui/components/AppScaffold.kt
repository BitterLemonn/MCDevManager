package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import com.lemon.mcdevmanagermp.utils.extension.collectEffect
import kotlinx.coroutines.flow.SharedFlow

/**
 * 全局 SnackbarHostState，由顶层 AppNavigation 提供。
 * 所有页面通过此 Local 显示 Toast，避免页面切换时 Toast 丢失。
 */
val LocalSnackbarHostState = compositionLocalOf<SnackbarHostState> {
    error("LocalSnackbarHostState not provided")
}

/**
 * 全局窗口宽度尺寸类，由顶层 AppNavigation 通过 BoxWithConstraints 计算。
 * 页面直接读取，无需各自重复 600.dp / 840.dp 断点判断。
 */
val LocalWindowWidthSizeClass = compositionLocalOf { WindowWidthSizeClass.Compact }

@Composable
fun <E : IUiEffect> AppScaffold(
    viewEffect: SharedFlow<E>? = null,
    onEffect: ((E) -> Unit)? = null,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    if (viewEffect != null && onEffect != null) {
        viewEffect.collectEffect { event ->
            onEffect(event)
        }
    }

    Scaffold(
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        content(innerPadding)
    }
}
