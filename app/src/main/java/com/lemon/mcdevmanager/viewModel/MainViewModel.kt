package com.lemon.mcdevmanager.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanager.data.common.CookiesStore
import com.lemon.mcdevmanager.data.common.NETEASE_USER_COOKIE
import com.lemon.mcdevmanager.data.database.database.GlobalDataBase
import com.lemon.mcdevmanager.data.database.entities.OverviewEntity
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
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

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
                getUserInfoLogic()
                getOverviewLogic()
            }.onStart {
                _viewStates.setState { copy(isLoadingOverview = true) }
                CookiesStore.clearCookies()
            }.catch {
                _viewEvents.setEvent(MainViewEvent.ShowToast(it.message ?: "未知错误"))
            }.onCompletion {
                CookiesStore.clearCookies()
                _viewStates.setState { copy(isLoadingOverview = false) }
            }.flowOn(Dispatchers.IO).collect()
        }
    }

    private suspend fun getUserInfoLogic() {
        when (val userInfo = repository.getUserInfo()) {
            is NetworkState.Success -> {
                getLevelInfoLogic()
                userInfo.data?.let {
                    _viewStates.setState {
                        copy(
                            username = it.nickname,
                            avatarUrl = it.headImg
                        )
                    }
                } ?: throw Exception("获取用户信息失败")
            }

            is NetworkState.Error -> {
                _viewEvents.setEvent(MainViewEvent.ShowToast(userInfo.msg))
            }
        }
    }

    private suspend fun getLevelInfoLogic() {
        when (val levelInfo = repository.getLevelInfo()) {
            is NetworkState.Success -> {
                levelInfo.data?.let {
                    val levelText = when (it.currentClass) {
                        1 -> "元气新星"
                        2 -> "巧手工匠"
                        3 -> "杰出精英"
                        4 -> "创造大师"
                        5 -> "传奇宗师"
                        else -> "元气新星"
                    }
                    _viewStates.setState {
                        copy(
                            mainLevel = it.currentClass,
                            subLevel = it.currentLevel,
                            levelText = "$levelText LV. ${it.currentLevel}"
                        )
                    }
                } ?: throw Exception("获取等级信息失败")
            }

            is NetworkState.Error -> {
                _viewEvents.setEvent(MainViewEvent.ShowToast(levelInfo.msg))
            }
        }
    }

    private suspend fun getOverviewLogic() {
        val overviewEntity = withContext(Dispatchers.IO) {
            GlobalDataBase.database.infoDao().getLatestOverviewByNickname(AppContext.nowNickname)
        }

        if (overviewEntity != null) {
            val instant = Instant.ofEpochMilli(overviewEntity.timestamp)
            val chinaZoneId = ZoneId.of("Asia/Shanghai")
            val chinaTime = ZonedDateTime.ofInstant(instant, chinaZoneId)

            var isLoad = false
            if (chinaTime.hour >= 11 && overviewEntity.yesterdayDownload != 0) isLoad = true
            else if (chinaTime.hour >= 12) isLoad = true
            if (isDifferentDay(overviewEntity.timestamp)) isLoad = false

            if (isLoad) {
                _viewStates.setState {
                    copy(
                        curMonthProfit = overviewEntity.thisMonthDiamond,
                        curMonthDl = overviewEntity.thisMonthDownload,
                        lastMonthProfit = overviewEntity.lastMonthDiamond,
                        lastMonthDl = overviewEntity.lastMonthDownload,
                        yesterdayProfit = overviewEntity.yesterdayDiamond,
                        halfAvgProfit = overviewEntity.days14AverageDiamond,
                        yesterdayDl = overviewEntity.yesterdayDownload,
                        halfAvgDl = overviewEntity.days14AverageDownload
                    )
                }
            } else getOverviewByServer()
        } else getOverviewByServer()
        computeMoney()
    }

    private suspend fun getOverviewByServer() {
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
                    // 保存概览信息到数据库
                    withContext(Dispatchers.IO) {
                        val overviewEntity = OverviewEntity(
                            nickname = AppContext.nowNickname,
                            days14AverageDownload = it.days14AverageDownload,
                            days14AverageDiamond = it.days14AverageDiamond,
                            days14TotalDownload = it.days14TotalDownload,
                            days14TotalDiamond = it.days14TotalDiamond,
                            lastMonthDiamond = it.lastMonthDiamond,
                            lastMonthDownload = it.lastMonthDownload,
                            thisMonthDiamond = it.thisMonthDiamond,
                            thisMonthDownload = it.thisMonthDownload,
                            yesterdayDiamond = it.yesterdayDiamond,
                            yesterdayDownload = it.yesterdayDownload
                        )
                        GlobalDataBase.database.infoDao().insertOverview(overviewEntity)
                    }
                    // 如果昨日数据为0, 且当前时间在11点之前, 则提示用户数据可能未刷新
                    if (it.yesterdayDiamond == 0 && it.yesterdayDownload == 0) {
                        val chinaZoneId = ZoneId.of("Asia/Shanghai")
                        val currentTimeInChina = ZonedDateTime.now(chinaZoneId)
                        val currentHourInChina = currentTimeInChina.hour

                        if (currentHourInChina < 11) {
                            _viewEvents.setEvent(MainViewEvent.MaybeDataNoRefresh)
                        }
                    }
                } ?: throw Exception("获取概览信息失败")
            }

            is NetworkState.Error -> {
                _viewEvents.setEvent(MainViewEvent.ShowToast(overview.msg))
            }
        }
    }

    private fun computeMoney() {
        val thisMonthProfit = viewStates.value.curMonthProfit / 100.0

        var realMoney = 0.0
        realMoney += if (thisMonthProfit > 1500) 1500 * 0.65 else thisMonthProfit * 0.65
        if (thisMonthProfit > 1500) realMoney +=
            (if (thisMonthProfit < 50000) (thisMonthProfit - 1500) * 0.7 else 50000 * 0.7) * 0.65
        if (thisMonthProfit > 50000) realMoney += (thisMonthProfit - 50000) * 0.75 * 0.65

        val taxMoney =
            if (realMoney < 800) 0.0 else if (realMoney < 4000) (realMoney - 800) * 0.2 else (realMoney * 0.8) * 0.2

        val df = DecimalFormat("0.00")
        val taxMoneyStr = df.format(taxMoney)
        val realMoneyStr = df.format(realMoney - taxMoney)
        _viewStates.setState { copy(realMoney = realMoneyStr, taxMoney = taxMoneyStr) }
    }

    private fun isDifferentDay(timestamp: Long): Boolean {
        val chinaZoneId = ZoneId.of("Asia/Shanghai")
        val timeToCheck = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), chinaZoneId)
        val currentTimeInChina = ZonedDateTime.now(chinaZoneId)

        return !timeToCheck.truncatedTo(ChronoUnit.DAYS)
            .isEqual(currentTimeInChina.truncatedTo(ChronoUnit.DAYS))
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
    val isLoadingOverview: Boolean = false,

    val username: String = "开发者",
    val avatarUrl: String = "https://gss0.baidu.com/-fo3dSag_xI4khGko9WTAnF6hhy/zhidao/pic/item/bd315c6034a85edf3b752e104b540923dd54750c.jpg",

    val mainLevel: Int = 0,
    val subLevel: Int = 0,
    val levelText: String = "",

    val realMoney: String = "0.00",
    val taxMoney: String = "0.00"
)

sealed class MainViewEvent {
    data class RouteToPath(val path: String) : MainViewEvent()
    data class ShowToast(val msg: String) : MainViewEvent()
    data object MaybeDataNoRefresh : MainViewEvent()
}

sealed class MainViewAction {
    data class LoadData(val nickname: String) : MainViewAction()
}