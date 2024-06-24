package com.lemon.mcdevmanager.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanager.data.common.JSONConverter
import com.lemon.mcdevmanager.data.netease.login.PVArgs
import com.lemon.mcdevmanager.data.repository.LoginRepository
import com.lemon.mcdevmanager.utils.NetworkState
import com.orhanobut.logger.Logger
import com.zj.mvi.core.SharedFlowEvents
import com.zj.mvi.core.setEvent
import com.zj.mvi.core.setState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString

class LoginViewModel : ViewModel() {
    private val repository = LoginRepository.getInstance()

    private val _viewState = MutableStateFlow(LoginViewState())
    val viewState = _viewState.asStateFlow()
    private val _viewEvent = SharedFlowEvents<LoginViewEvent>()
    val viewEvent = _viewEvent.asSharedFlow()

    var pvInfo = ""

    fun dispatch(action: LoginViewAction) {
        when (action) {
            is LoginViewAction.Login -> login(action.username, action.password)
        }
    }

    private fun login(username: String, password: String) {
        viewModelScope.launch {
            flow {
                initLogic(username, password)
                emit("")
            }.catch {
                _viewEvent.setEvent(LoginViewEvent.LoginFailed(it.message ?: "登录失败"))
            }.onStart {
                _viewState.setState { copy(isLoading = true) }
            }.onCompletion {
                _viewState.setState { copy(isLoading = false) }
            }.flowOn(Dispatchers.IO).collect()
        }
    }

    private suspend fun initLogic(username: String, password: String) {
        Logger.d("开始初始化")
        val init = repository.init("https://mcdev.webapp.163.com/#/login")
        when (init) {
            is NetworkState.Success -> {
                getPowerLogic(username, password)
            }

            is NetworkState.Error -> {
                throw Exception("获取登录ticket失败")
            }
        }
    }

    private suspend fun getPowerLogic(username: String, password: String) {
        Logger.d("开始获取权限")
        val power = repository.getPower(username, topUrl = "https://mcdev.webapp.163.com/#/login")
        when (power) {
            is NetworkState.Success -> {
                power.data?.let {
                    Logger.d(power.data)
                }
                getTicketLogic(username, password)
            }

            is NetworkState.Error -> {
                throw Exception("获取权限失败")
            }
        }
    }

    private suspend fun getTicketLogic(username: String, password: String){
        Logger.d("开始获取ticket")
        val ticket = repository.getTicket(username)
        when (ticket) {
            is NetworkState.Success -> {

            }

            is NetworkState.Error -> {
                throw Exception("获取登录ticket失败")
            }
        }
    }
}

data class LoginViewState(
    val isLoading: Boolean = false,
    val username: String = "",
    val password: String = ""
)

sealed class LoginViewEvent {
    data class LoginSuccess(val username: String) : LoginViewEvent()
    data class LoginFailed(val message: String) : LoginViewEvent()
    object StartLogin : LoginViewEvent()
}

sealed class LoginViewAction {
    data class Login(val username: String, val password: String) : LoginViewAction()
}