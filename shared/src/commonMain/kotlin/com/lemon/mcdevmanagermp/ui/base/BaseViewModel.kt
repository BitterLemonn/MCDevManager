package com.lemon.mcdevmanagermp.ui.base

import androidx.lifecycle.ViewModel
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.consts.CookiesExpiredException
import com.lemon.mcdevmanagermp.utils.Logger
import com.lemon.mcdevmanagermp.utils.extension.IUiAction
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import com.lemon.mcdevmanagermp.utils.extension.IUiState
import com.lemon.mcdevmanagermp.utils.extension.MVIContainer
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

abstract class BaseViewModel<STATE : IUiState, ACTION : IUiAction, EFFECT : IUiEffect>(
    initialState: STATE
) : ViewModel() {

    private val mvi = MVIContainer<STATE, EFFECT>(initialState)

    val state: StateFlow<STATE> = mvi.state
    val effect: SharedFlow<EFFECT> = mvi.effect

    abstract fun dispatch(action: ACTION)

    protected fun setState(reducer: STATE.() -> STATE) {
        mvi.setState(reducer)
    }

    protected fun sendEffect(effect: EFFECT) {
        mvi.tryEmitEffect(effect)
    }

    /**
     * 统一处理网络请求错误
     * @param result 网络请求错误结果
     * @param onNeedReLogin Cookies 过期时发送的 Effect
     * @param onShowToast 其他错误时发送的 Effect，接收错误消息
     */
    protected fun handleError(
        result: NetworkState.Error<*>,
        onNeedReLogin: () -> EFFECT,
        onShowToast: (String) -> EFFECT
    ) {
        if (result.e is CookiesExpiredException) {
            sendEffect(onNeedReLogin())
        } else {
            Logger.e("请求失败: ${result.msg}\n${result.e}")
            sendEffect(onShowToast("请求失败: ${result.msg}"))
        }
    }
}