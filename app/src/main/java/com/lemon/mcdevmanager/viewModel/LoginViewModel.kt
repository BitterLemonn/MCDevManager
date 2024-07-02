package com.lemon.mcdevmanager.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Object
import com.lemon.mcdevmanager.data.common.CookiesStore
import com.lemon.mcdevmanager.data.common.JSONConverter
import com.lemon.mcdevmanager.data.common.MAIN_PAGE
import com.lemon.mcdevmanager.data.common.NETEASE_USER_COOKIE
import com.lemon.mcdevmanager.data.database.database.GlobalDataBase
import com.lemon.mcdevmanager.data.database.entities.UserEntity
import com.lemon.mcdevmanager.data.global.AppContext
import com.lemon.mcdevmanager.data.netease.login.PVInfo
import com.lemon.mcdevmanager.data.netease.login.PVResultStrBean
import com.lemon.mcdevmanager.data.repository.LoginRepository
import com.lemon.mcdevmanager.utils.NetworkState
import com.lemon.mcdevmanager.utils.dataJsonToString
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

class LoginViewModel : ViewModel() {
    private val repository = LoginRepository.getInstance()

    private val _viewState = MutableStateFlow(LoginViewState())
    val viewState = _viewState.asStateFlow()
    private val _viewEvent = SharedFlowEvents<LoginViewEvent>()
    val viewEvent = _viewEvent.asSharedFlow()

    private lateinit var pvResultBean: PVResultStrBean
    private lateinit var tk: String

    private var retryCount = 0

    fun dispatch(action: LoginViewAction) {
        when (action) {
            is LoginViewAction.UpdatePowerScript -> _viewState.setState { copy(powerScript = action.script) }
            is LoginViewAction.UpdateUsername -> _viewState.setState { copy(username = action.username) }
            is LoginViewAction.UpdatePassword -> _viewState.setState { copy(password = action.password) }
            is LoginViewAction.UpdateCookies -> _viewState.setState { copy(cookies = action.cookies) }
            is LoginViewAction.Login -> login()
            is LoginViewAction.SetUser -> setUser(action.nickname)
        }
    }

    private fun login() {

        viewModelScope.launch {
            if (_viewState.value.cookies.isNotEmpty()) {
                CookiesStore.addCookie(NETEASE_USER_COOKIE, _viewState.value.cookies)
                _viewEvent.setEvent(LoginViewEvent.LoginSuccess("登录成功"))
            } else {
                flow<Unit> {
                    initLogic()
                }.catch {
                    _viewEvent.setEvent(LoginViewEvent.LoginFailed(it.message ?: "登录失败"))
                }.onStart {
                    _viewState.setState { copy(isStartLogin = true) }
                }.onCompletion {
                    _viewState.setState { copy(isStartLogin = false) }
                }.flowOn(Dispatchers.IO).collect()
            }
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
            _viewState.value.username, topUrl = "https://mcdev.webapp.163.com/#/login"
        )
        when (power) {
            is NetworkState.Success -> {
                power.data?.let {
                    val pvInfo = JSONConverter.decodeFromString<PVInfo>(it)

                    val e = """
                    var e = {
                        sid: "${pvInfo.sid}",
                        hashFunc: "${pvInfo.hashFunc}",
                        needCheck: ${pvInfo.needCheck},
                        args: ${dataJsonToString(pvInfo.args)},
                        maxTime: ${pvInfo.maxTime},
                        minTime: ${pvInfo.minTime}
                    };
                    var e = vdfFun(e);
                    """.trimIndent()
                    V8.createV8Runtime().use { runtime ->
                        runtime.executeVoidScript(_viewState.value.powerScript)
                        runtime.executeVoidScript(e)
                        (runtime.executeScript("e") as V8Object).use { result ->
                            pvResultBean = PVResultStrBean(
                                maxTime = result.getInteger("maxTime"),
                                args = result.getString("args"),
                                puzzle = result.getString("puzzle"),
                                runTimes = result.getInteger("runTimes"),
                                sid = result.getString("sid"),
                                spendTime = result.getInteger("spendTime")
                            )
                        }
                    }
                    getTicket()
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
            }.flowOn(Dispatchers.IO).collect()
        }
    }

    private suspend fun getTicketLogic() {
        Logger.d("开始获取ticket")
        when (val ticket = repository.getTicket(
            _viewState.value.username, "https://mcdev.webapp.163.com/#/login"
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
            _viewState.value.username, _viewState.value.password, tk, pvResultBean
        )) {
            is NetworkState.Success -> {
                _viewEvent.setEvent(LoginViewEvent.LoginSuccess("登录成功"))

            }

            is NetworkState.Error -> {
                val errorState = Array(4) { "80${it + 1}" }
                if (errorState.contains(login.msg) && retryCount < 3) {
                    getPowerLogic()
                    retryCount++
                } else {
                    retryCount = 0
                    when (login.msg) {
                        "413" -> throw Exception("邮箱或密码错误")
                        else -> throw Exception("登录失败, 请重试")
                    }
                }
            }
        }
    }

    private fun setUser(nickname: String) {
        viewModelScope.launch(Dispatchers.IO) {
            flow<Unit> {
                // room持久化
                val cookie = CookiesStore.getCookie(NETEASE_USER_COOKIE)
                    ?: throw Exception("获取用户信息失败, 请重新登录")
                val username = _viewState.value.username
                val password = _viewState.value.password
                val userInfo = UserEntity(
                    username = username,
                    password = password,
                    nickname = nickname,
                    cookie = cookie
                )
                // 持久化
                GlobalDataBase.database.userDao().updateUser(userInfo)
                AppContext.cookiesStore[nickname] = cookie
                AppContext.nowNickname = nickname
                AppContext.accountList.add(nickname)
            }.onCompletion {
                _viewEvent.setEvent(LoginViewEvent.RouteToPath(MAIN_PAGE, true))
            }.catch {
                _viewEvent.setEvent(LoginViewEvent.LoginFailed(it.message ?: "未知错误"))
            }.collect()
        }
    }
}

data class LoginViewState(
    val isStartLogin: Boolean = false,
    val username: String = "",
    val password: String = "",
    val cookies: String = "",
    val powerScript: String = ""
)

sealed class LoginViewEvent {
    data class LoginSuccess(val username: String) : LoginViewEvent()
    data class LoginFailed(val message: String) : LoginViewEvent()
    data class RouteToPath(val path: String, val needPop: Boolean = false) : LoginViewEvent()
}

sealed class LoginViewAction {
    data class UpdatePowerScript(val script: String) : LoginViewAction()
    data class UpdateUsername(val username: String) : LoginViewAction()
    data class UpdatePassword(val password: String) : LoginViewAction()
    data class UpdateCookies(val cookies: String) : LoginViewAction()
    data object Login : LoginViewAction()
    data class SetUser(val nickname: String) : LoginViewAction()
}