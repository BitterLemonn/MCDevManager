package com.lemon.mcdevmanagermp.utils.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

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
 * 创建一个用于一次性事件 (Effects) 的 [Channel]。
 *
 * 使用 [Channel.UNLIMITED] 无界缓冲：发送的 effect 会无条件暂存，直到订阅者消费，
 * **绝不会因为订阅者尚未就绪或连续快速发送而丢失**。
 *
 * 之前使用 `MutableSharedFlow(replay = 0)` 时，当收集协程尚未注册为订阅者
 * （页面刚进入、嵌套重组、连续快速 emit）期间发出的 effect 会被静默丢弃，
 * 导致子页面 Toast 不显示。改用 Channel 彻底解决该问题。
 */
fun <T : IUiEffect> createEffectChannel(): Channel<T> {
    return Channel(Channel.UNLIMITED)
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
 * 在 Composable 中收集副作用 (Effect) 的扩展函数。
 * 自动处理生命周期，当生命周期至少为 STARTED 时收集，离开页面自动取消。
 *
 * 接收 [Flow]（[MVIContainer] 内部为 Channel + receiveAsFlow）。由于 Channel 会暂存
 * 未消费的 effect，即使在生命周期低于 STARTED 期间发出的事件，重新进入 STARTED 后
 * 仍能被收到，不会丢失。
 */
@Composable
fun <T : IUiEffect> Flow<T>.collectEffect(
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
 *         mvi.tryEmitEffect(MyEffect.ShowToast("Hello"))
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

    private val _effect = createEffectChannel<EFFECT>()
    val effect: Flow<EFFECT> = _effect.receiveAsFlow()

    fun setState(reducer: STATE.() -> STATE) {
        _state.update(reducer)
    }

    suspend fun emitEffect(effect: EFFECT) {
        _effect.send(effect)
    }

    fun tryEmitEffect(effect: EFFECT) {
        _effect.trySend(effect)
    }
}
