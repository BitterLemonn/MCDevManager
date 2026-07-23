package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Effect 处理作用域，作为 [collectUiEffect] 处理 lambda 的接收者。
 *
 * 封装常用 UI 副作用工具，免去每个页面重复获取
 * [SnackbarHostState] / [CoroutineScope] 并手写 `scope.launch { showSnackbar }` 的样板。
 */
class EffectScope(
    private val snackbarHostState: SnackbarHostState,
    private val toastScope: CoroutineScope
) {
    // 当前正在显示的 Snackbar 协程；连续 showToast 时取消它，使新 toast 立即替换旧的，而非排队等待。
    private var showJob: Job? = null

    /**
     * 显示一条全局 Snackbar（host 由顶层 AppNavigation 提供）。
     *
     * showSnackbar 挂起在 [toastScope]（全局、生命周期与导航树绑定）上，因此即使用户在
     * Toast 显示期间导航离开当前页面，正在显示的 Snackbar 也不会因页面 composition 销毁
     * 而被取消。连续调用会取消上一条的显示协程，确保新 toast 立即替换旧的。
     */
    fun showToast(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        showJob?.cancel()
        showJob = toastScope.launch {
            snackbarHostState.showSnackbar(message = message, duration = duration)
        }
    }
}

/**
 * 收集 [Flow] 中的 effect，并自动处理生命周期（[minActiveState] 以上才消费）。
 *
 * 与 `utils/extension/MVICore.kt` 中的 [collectEffect][com.lemon.mcdevmanagermp.utils.extension.collectEffect]
 * 区别：本函数的处理 lambda 以 [EffectScope] 作为接收者，可直接调用 [EffectScope.showToast]，
 * 省去每页手写 `LocalSnackbarHostState.current` + `rememberCoroutineScope()` 的样板。
 *
 * 用法:
 * ```
 * viewModel.effect.collectUiEffect { effect ->
 *     when (effect) {
 *         is XxxEffect.ShowToast -> showToast(effect.message)
 *         XxxEffect.Navigate -> onNavigate()
 *     }
 * }
 * ```
 */
@Composable
fun <T : IUiEffect> Flow<T>.collectUiEffect(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    handler: suspend EffectScope.(T) -> Unit
) {
    val snackbarHostState = LocalSnackbarHostState.current
    val toastScope = LocalToastScope.current
    val scope = remember(snackbarHostState, toastScope) {
        EffectScope(snackbarHostState, toastScope)
    }
    LaunchedEffect(this, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(minActiveState) {
            collect { scope.handler(it) }
        }
    }
}
