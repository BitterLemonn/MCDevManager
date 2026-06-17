package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.CoroutineScope

/**
 * 全局 SnackbarHostState，由顶层 AppNavigation 提供。
 * 所有页面通过此 Local 显示 Toast，避免页面切换时 Toast 丢失。
 */
val LocalSnackbarHostState = compositionLocalOf<SnackbarHostState> {
    error("LocalSnackbarHostState not provided")
}

/**
 * 全局 Toast 协程作用域，由顶层 AppNavigation 通过 [androidx.compose.runtime.rememberCoroutineScope] 提供。
 *
 * 生命周期与整个导航树绑定（比任何子页面都长）。页面通过 [EffectScope.showToast] 显示
 * Snackbar 时，showSnackbar 挂起在此 scope 上，从而避免页面销毁（导航离开）导致
 * 正在显示的 Snackbar 协程被取消、Toast 提前消失。
 */
val LocalToastScope = compositionLocalOf<CoroutineScope> {
    error("LocalToastScope not provided")
}

/**
 * 全局窗口宽度尺寸类，由顶层 AppNavigation 通过 BoxWithConstraints 计算。
 * 页面直接读取，无需各自重复 600.dp / 840.dp 断点判断。
 */
val LocalWindowWidthSizeClass = compositionLocalOf { WindowWidthSizeClass.Compact }

/**
 * 统一的 Scaffold 封装，固定 contentWindowInsets 为 0。
 *
 * Effect 收集统一使用 [collectUiEffect]（提供 [EffectScope.showToast] 等 UI 工具），
 * 不再在此处通过参数转发，保持单一职责。
 */
@Composable
fun AppScaffold(
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        content(innerPadding)
    }
}
