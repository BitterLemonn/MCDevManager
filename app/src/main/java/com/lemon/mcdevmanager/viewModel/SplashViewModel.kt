package com.lemon.mcdevmanager.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanager.data.common.LOGIN_PAGE
import com.lemon.mcdevmanager.data.common.MAIN_PAGE
import com.lemon.mcdevmanager.data.database.database.GlobalDataBase
import com.lemon.mcdevmanager.data.global.AppContext
import com.orhanobut.logger.Logger
import com.zj.mvi.core.SharedFlowEvents
import com.zj.mvi.core.setEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {
    private val _viewEvents = SharedFlowEvents<SplashViewEvent>()
    val viewEvents = _viewEvents.asSharedFlow()

    fun dispatch(action: SplashViewAction) {
        when (action) {
            is SplashViewAction.GetDatabase -> getDatabase()
        }
    }

    private fun getDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            flow<Unit> {
                val userInfoList = GlobalDataBase.database.userDao().getAllUsers()
                Logger.d("userInfoList: $userInfoList")
                userInfoList?.let {
                    if (userInfoList.isNotEmpty()) {
                        for (user in userInfoList) {
                            if (userInfoList.indexOf(user) == 0) AppContext.nowNickname =
                                user.nickname
                            AppContext.cookiesStore[user.nickname] = user.cookie
                        }
                        _viewEvents.setEvent(SplashViewEvent.RouteToPath(MAIN_PAGE))
                    } else {
                        _viewEvents.setEvent(SplashViewEvent.RouteToPath(LOGIN_PAGE))
                    }
                } ?: run {
                    _viewEvents.setEvent(SplashViewEvent.RouteToPath(LOGIN_PAGE))
                }
            }.collect()
        }
    }
}

sealed class SplashViewAction {
    data object GetDatabase : SplashViewAction()
}

sealed class SplashViewEvent {
    data class RouteToPath(val path: String) : SplashViewEvent()
}