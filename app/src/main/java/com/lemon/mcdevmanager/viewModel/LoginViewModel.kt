package com.lemon.mcdevmanager.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanager.data.common.JSONConverter
import com.lemon.mcdevmanager.data.netease.login.PVArgs
import com.lemon.mcdevmanager.data.netease.login.PVInfo
import com.lemon.mcdevmanager.data.netease.login.PVResultArgs
import com.lemon.mcdevmanager.data.netease.login.PVResultBean
import com.lemon.mcdevmanager.data.netease.login.PVResultStrBean
import com.lemon.mcdevmanager.data.repository.LoginRepository
import com.lemon.mcdevmanager.utils.NetworkState
import com.lemon.mcdevmanager.utils.vdfCompute
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

    private lateinit var pvResultBean: PVResultStrBean
    private lateinit var tk: String

    fun dispatch(action: LoginViewAction) {
        when (action) {
            is LoginViewAction.UpdateUsername -> _viewState.setState { copy(username = action.username) }
            is LoginViewAction.UpdatePassword -> _viewState.setState { copy(password = action.password) }
            is LoginViewAction.Login -> login()
            is LoginViewAction.ComputePower -> {
                this.pvResultBean = action.pvResultStrBean
                getTicket()
            }
        }
    }

    private fun login() {
        viewModelScope.launch {
            flow {
                initLogic()
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

    private suspend fun initLogic() {
        Logger.d("开始初始化")
        val init = repository.init("https://mcdev.webapp.163.com/#/login")
        when (init) {
            is NetworkState.Success -> {
                getPowerLogic()
            }

            is NetworkState.Error -> {
                throw Exception("获取登录ticket失败")
            }
        }
    }

    private suspend fun getPowerLogic() {
        Logger.d("开始获取权限")
        val power = repository.getPower(
            _viewState.value.username,
            topUrl = "https://mcdev.webapp.163.com/#/login"
        )
        when (power) {
            is NetworkState.Success -> {
                power.data?.let {
                    val pvInfo = JSONConverter.decodeFromString<PVInfo>(it)
                    _viewEvent.setEvent(LoginViewEvent.ComputePower(pvInfo))
                }
            }

            is NetworkState.Error -> {
                throw Exception("获取权限失败")
            }
        }
    }

    private fun getTicket() {
        viewModelScope.launch {
            flow {
                getTicketLogic()
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

    private suspend fun getTicketLogic() {
        Logger.d("开始获取ticket")
        when (val ticket = repository.getTicket(
            _viewState.value.username,
            "https://mcdev.webapp.163.com/#/login"
        )) {
            is NetworkState.Success -> {
                this.tk = ticket.data ?: ""
                safeLoginLogic()
            }

            is NetworkState.Error -> {
                throw Exception("获取ticket失败")
            }
        }
    }

    private suspend fun safeLoginLogic() {
        Logger.d("开始安全登录")
        when (val login = repository.loginWithTicket(
            _viewState.value.username,
            _viewState.value.password,
            tk,
            pvResultBean
        )) {
            is NetworkState.Success -> {
                _viewEvent.setEvent(LoginViewEvent.LoginSuccess("登录成功"))

            }

            is NetworkState.Error -> {
//                if (login.msg == "804") {
//                    getPowerLogic(username, password)
//                } else {
//                    throw Exception("登录失败")
//                }
                throw Exception("登录失败")
            }
        }
    }
}

data class LoginViewState(
    val isLoading: Boolean = false,
    val username: String = "873811906@qq.com",
    val password: String = "27880426Win2"
)

sealed class LoginViewEvent {
    data class LoginSuccess(val username: String) : LoginViewEvent()
    data class LoginFailed(val message: String) : LoginViewEvent()
    data object StartLogin : LoginViewEvent()
    data class ComputePower(val pvInfo: PVInfo) : LoginViewEvent()
}

sealed class LoginViewAction {
    data class UpdateUsername(val username: String) : LoginViewAction()
    data class UpdatePassword(val password: String) : LoginViewAction()
    data object Login : LoginViewAction()

    data class ComputePower(val pvResultStrBean: PVResultStrBean) : LoginViewAction()
}