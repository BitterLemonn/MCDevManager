package com.lemon.mcdevmanagermp.ui.base

import androidx.lifecycle.ViewModel
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
}