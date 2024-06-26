package com.lemon.mcdevmanager.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanager.data.common.CookiesStore
import com.lemon.mcdevmanager.data.common.NETEASE_USER_COOKIE
import com.lemon.mcdevmanager.data.database.database.GlobalDataBase
import com.lemon.mcdevmanager.data.global.AppContext
import com.lemon.mcdevmanager.data.repository.MainRepository
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

class MainViewModel : ViewModel() {
    private val repository = MainRepository.getInstance()

    private val _viewStates = MutableStateFlow(MainViewState())
    val viewStates = _viewStates.asStateFlow()
    private val _viewEvents = SharedFlowEvents<MainViewEvent>()
    val viewEvents = _viewEvents.asSharedFlow()

    fun dispatch(action: MainViewAction) {
        when (action) {
            is MainViewAction.LoadData -> loadData(action.nickname)
        }
    }

    private fun loadData(nickname: String) {
        viewModelScope.launch(Dispatchers.IO) {
            flow<Unit> {
                val cookie = AppContext.cookiesStore[nickname]
                    ?: throw Exception("未找到用户信息, 请重新登录")
                CookiesStore.addCookie(NETEASE_USER_COOKIE, cookie)
                getOverviewLogic()
            }.onStart {
                Logger.d("开始获取概览信息")
                CookiesStore.clearCookies()
            }.catch {
                _viewEvents.setEvent(MainViewEvent.ShowToast(it.message ?: "未知错误"))
            }.onCompletion {
                CookiesStore.clearCookies()
            }.flowOn(Dispatchers.IO).collect()
        }
        viewModelScope.launch(Dispatchers.IO) {
            flow<Unit> {
                val cookie = AppContext.cookiesStore[nickname]
                    ?: throw Exception("未找到用户信息, 请重新登录")
                CookiesStore.addCookie(NETEASE_USER_COOKIE, cookie)
                getUserInfoLogic()
            }.onStart {
                Logger.d("开始获取用户信息")
                CookiesStore.clearCookies()
            }.catch {
                _viewEvents.setEvent(MainViewEvent.ShowToast(it.message ?: "未知错误"))
            }.onCompletion {
                CookiesStore.clearCookies()
            }.flowOn(Dispatchers.IO).collect()
        }
    }

    private suspend fun getUserInfoLogic() {
        when (val userInfo = repository.getUserInfo()) {
            is NetworkState.Success -> {
                userInfo.data?.let {
                    _viewStates.setState {
                        copy(username = it.nickname, avatarUrl = it.headImg)
                    }
                } ?: throw Exception("获取用户信息失败")
            }

            is NetworkState.Error -> {
                _viewEvents.setEvent(MainViewEvent.ShowToast(userInfo.msg))
            }
        }
    }

    private suspend fun getOverviewLogic() {
        when (val overview = repository.getOverview()) {
            is NetworkState.Success -> {
                overview.data?.let {
                    _viewStates.setState {
                        copy(
                            curMonthProfit = it.thisMonthDiamond,
                            curMonthDl = it.thisMonthDownload,
                            lastMonthProfit = it.lastMonthDiamond,
                            lastMonthDl = it.lastMonthDownload,
                            yesterdayProfit = it.yesterdayDiamond,
                            halfAvgProfit = it.days14AverageDiamond,
                            yesterdayDl = it.yesterdayDownload,
                            halfAvgDl = it.days14AverageDownload
                        )
                    }
                } ?: throw Exception("获取概览信息失败")
            }

            is NetworkState.Error -> {
                _viewEvents.setEvent(MainViewEvent.ShowToast(overview.msg))
            }
        }
    }
}

data class MainViewState(
    val curMonthProfit: Int = 0,
    val curMonthDl: Int = 0,
    val lastMonthProfit: Int = 0,
    val lastMonthDl: Int = 0,
    val yesterdayProfit: Int = 0,
    val halfAvgProfit: Int = 0,
    val yesterdayDl: Int = 0,
    val halfAvgDl: Int = 0,
    val isLoading: Boolean = false,

    val username: String = "开发者",
    val avatarUrl: String = "https://gss0.baidu.com/-fo3dSag_xI4khGko9WTAnF6hhy/zhidao/pic/item/bd315c6034a85edf3b752e104b540923dd54750c.jpg"
)

sealed class MainViewEvent {
    data class RouteToPath(val path: String) : MainViewEvent()
    data class ShowToast(val msg: String) : MainViewEvent()
}

sealed class MainViewAction {
    data class LoadData(val nickname: String) : MainViewAction()
}