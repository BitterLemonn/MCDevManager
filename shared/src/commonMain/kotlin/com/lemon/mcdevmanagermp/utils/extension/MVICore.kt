package com.lemon.mcdevmanagermp.utils.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI 状态标记接口
 */
interface IUiState

/**
 * MVI 动作标记接口 (用户操作)
 */
interface IUiAction

/**
 * MVI 副作用标记接口 (一次性事件，如导航、Toast)
 */
interface IUiEffect

/**
 * 创建一个配置为一次性事件 (Effects) 的 [MutableSharedFlow]。
 *
 * 配置:
 * - replay = 0: 新的订阅者不会收到旧的事件。
 * - extraBufferCapacity = 1: 允许缓冲一个事件。
 * - onBufferOverflow = BufferOverflow.DROP_OLDEST: 如果缓冲区已满，丢弃最旧的事件。
 */
fun <T : IUiEffect> createEffectFlow(): MutableSharedFlow<T> {
    return MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
}

/**
 * [MutableStateFlow] 的扩展函数，用于使用 reducer lambda 更新状态。
 * 这为状态更新提供了更清晰的语法：
 * `_state.setState { copy(isLoading = true) }`
 */
fun <T : IUiState> MutableStateFlow<T>.setState(reducer: T.() -> T) {
    this.update(reducer)
}

/**
 * [ViewModel] 的扩展函数，用于向 [MutableSharedFlow] 发送副作用 (Effect)。
 * 在 [viewModelScope] 中启动协程。
 */
fun <T : IUiEffect> ViewModel.sendEffect(
    effectFlow: MutableSharedFlow<T>,
    effect: T
) {
    viewModelScope.launch {
        effectFlow.emit(effect)
    }
}

/**
 * [ViewModel] 的扩展函数，用于使用构建器向 [MutableSharedFlow] 发送副作用 (Effect)。
 */
fun <T : IUiEffect> ViewModel.sendEffect(
    effectFlow: MutableSharedFlow<T>,
    builder: () -> T
) {
    viewModelScope.launch {
        effectFlow.emit(builder())
    }
}

/**
 * 在 Composable 中收集副作用 (Effect) 的扩展函数。
 * 自动处理生命周期，当生命周期至少为 STARTED 时收集，离开页面自动取消。
 */
@Composable
fun <T : IUiEffect> SharedFlow<T>.collectEffect(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    collector: suspend (T) -> Unit
) {
    LaunchedEffect(this, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(minActiveState) {
            this@collectEffect.collect {
                collector(it)
            }
        }
    }
}

/**
 * 如果你更喜欢基于类的 State 和 Effect 方法，可以使用这个容器助手。
 * 用法:
 * ```
 * class MyViewModel : ViewModel() {
 *     val mvi = MVIContainer<MyState, MyEffect>(MyState())
 *     val state = mvi.state
 *     val effect = mvi.effect
 *
 *     fun doSomething() {
 *         mvi.setState { copy(loading = true) }
 *         mvi.sendEffect(MyEffect.ShowToast("Hello"))
 *     }
 * }
 * ```
 */
class MVIContainer<STATE : IUiState, EFFECT : IUiEffect>(
    initialState: STATE,
    private val scope: CoroutineScope? = null
) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<STATE> = _state.asStateFlow()

    private val _effect = createEffectFlow<EFFECT>()
    val effect: SharedFlow<EFFECT> = _effect.asSharedFlow()

    fun setState(reducer: STATE.() -> STATE) {
        _state.update(reducer)
    }

    suspend fun emitEffect(effect: EFFECT) {
        _effect.emit(effect)
    }
    
    fun tryEmitEffect(effect: EFFECT) {
        _effect.tryEmit(effect)
    }
}
