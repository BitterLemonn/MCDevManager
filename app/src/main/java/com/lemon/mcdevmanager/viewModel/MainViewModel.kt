package com.lemon.mcdevmanager.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanager.data.common.CookiesStore
import com.lemon.mcdevmanager.data.common.LOGIN_PAGE
import com.lemon.mcdevmanager.data.common.NETEASE_USER_COOKIE
import com.lemon.mcdevmanager.data.database.database.GlobalDataBase
import com.lemon.mcdevmanager.data.database.entities.OverviewEntity
import com.lemon.mcdevmanager.data.global.AppContext
import com.lemon.mcdevmanager.data.netease.resource.ResourceBean
import com.lemon.mcdevmanager.data.repository.DetailRepository
import com.lemon.mcdevmanager.data.repository.MainRepository
import com.lemon.mcdevmanager.data.repository.RealtimeProfitRepository
import com.lemon.mcdevmanager.utils.CookiesExpiredException
import com.lemon.mcdevmanager.utils.NetworkState
import com.lemon.mcdevmanager.utils.UnifiedExceptionHandler
import com.lemon.mcdevmanager.utils.UnifiedExceptionHandler.CancelException
import com.lemon.mcdevmanager.utils.logout
import com.orhanobut.logger.Logger
import com.zj.mvi.core.SharedFlowEvents
import com.zj.mvi.core.setEvent
import com.zj.mvi.core.setState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
import java.time.Month
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.min

class MainViewModel : ViewModel() {
    private val mainRepository = MainRepository.getInstance()
    private val realtimeProfitRepository = RealtimeProfitRepository.getInstance()
    private val overviewRepository = DetailRepository.getInstance()

    private val _viewStates = MutableStateFlow(MainViewState())
    val viewStates = _viewStates.asStateFlow()
    private val _viewEvents = SharedFlowEvents<MainViewEvent>()
    val viewEvents = _viewEvents.asSharedFlow()

    fun dispatch(action: MainViewAction) {
        when (action) {
            is MainViewAction.LoadData -> loadData(action.nickname)
            is MainViewAction.DeleteAccount -> deleteAccount(action.accountName)
            is MainViewAction.ChangeAccount -> changeAccount(action.accountName)
        }
    }

    private fun loadData(nickname: String) {
        Logger.d("loadData: $nickname")
        viewModelScope.launch(Dispatchers.IO) {
            flow<Unit> {
                val cookie = AppContext.cookiesStore[nickname]
                    ?: throw Exception("未找到用户信息, 请重新登录")
                CookiesStore.addCookie(NETEASE_USER_COOKIE, cookie)
                getUserInfoLogic()
                getOverviewLogic()
            }.onStart {
                _viewStates.setState {
                    copy(
                        isLoadingOverview = true,
                        isLoadingProfit = true
                    )
                }
                CookiesStore.clearCookies()
            }.catch {
                if (it !is CancelException) {
                    _viewEvents.setEvent(MainViewEvent.ShowToast(it.message ?: "未知错误"))
                    _viewStates.setState { copy(isLoadingOverview = false) }
                } else {
                    Logger.d(it.message ?: "未知错误")
                }
            }.onCompletion {
                CookiesStore.clearCookies()
                _viewStates.setState { copy(isLoadingOverview = false) }
            }.flowOn(Dispatchers.IO).collect()
        }
    }

    private suspend fun getUserInfoLogic() {
        when (val result = mainRepository.getUserInfo()) {
            is NetworkState.Success -> {
                getLevelInfoLogic()
                result.data?.let { userInfo ->
                    AppContext.curUserInfo = userInfo
                    _viewStates.setState {
                        copy(
                            username = userInfo.nickname,
                            avatarUrl = userInfo.headImg ?: avatarUrl
                        )
                    }
                } ?: run {
                    _viewEvents.setEvent(MainViewEvent.RouteToPath(LOGIN_PAGE, true))
                    _viewEvents.setEvent(MainViewEvent.ShowToast("无法找到用户信息, 请重新登录"))
                    logout(AppContext.nowNickname)
                }
            }

            is NetworkState.Error -> {
                when (result.e) {
                    is CookiesExpiredException -> {
                        _viewEvents.setEvent(MainViewEvent.RouteToPath(LOGIN_PAGE, true))
                        _viewEvents.setEvent(MainViewEvent.ShowToast("登录过期, 请重新登录"))
                        logout(AppContext.nowNickname)
                    }

                    is CancelException -> {
                        throw CancelException(result.msg)
                    }

                    else -> {
                        _viewEvents.setEvent(MainViewEvent.ShowToast(result.msg))
                    }
                }
            }
        }
    }

    private suspend fun getLevelInfoLogic() {
        when (val levelInfo = mainRepository.getLevelInfo()) {
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
                            levelText = "$levelText LV. ${it.currentLevel}",
                            maxLevelExp = it.expCeiling,
                            currentExp = it.totalExp,
                            canLevelUp = it.upgradeClassAchieve,
                            contributionMonth = it.contributionMonth,
                            netGameClass = it.contributionNetGameClass,
                            netGameRank = it.contributionNetGameRank,
                            netGameScore = it.contributionNetGameScore,
                            contributionClass = it.contributionClass,
                            contributionRank = it.contributionRank,
                            contributionScore = it.contributionScore
                        )
                    }
                } ?: throw Exception("获取等级信息失败")
            }

            is NetworkState.Error -> {
                when (levelInfo.e) {
                    is CookiesExpiredException -> {
                        _viewEvents.setEvent(MainViewEvent.RouteToPath(LOGIN_PAGE, true))
                        _viewEvents.setEvent(MainViewEvent.ShowToast("登录过期, 请重新登录"))
                        logout(AppContext.nowNickname)
                    }

                    is CancelException -> {
                        throw CancelException(levelInfo.msg)
                    }

                    else -> {
                        _viewEvents.setEvent(MainViewEvent.ShowToast(levelInfo.msg))
                    }
                }
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
            if (overviewEntity.yesterdayDownload != 0) isLoad = true
            else if (chinaTime.hour > 11 || (chinaTime.hour == 11 && chinaTime.minute >= 30)) isLoad =
                true
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
                        halfAvgDl = overviewEntity.days14AverageDownload,
                        realMoney = overviewEntity.thisMonthProfit,
                        taxMoney = overviewEntity.thisMonthTax,
                        lastRealMoney = overviewEntity.lastMonthProfit,
                        lastTaxMoney = overviewEntity.lastMonthTax,
                        isLoadingProfit = false
                    )
                }
            } else getOverviewByServer()
        } else getOverviewByServer()

        if (ZonedDateTime.now().dayOfMonth <= 10)
            _viewEvents.setEvent(MainViewEvent.ShowLastMonthProfit)
    }

    private suspend fun getOverviewByServer() {
        // 只有需要请求概览数据时才请求资源列表
        getResListLogic()
        when (val overview = mainRepository.getOverview()) {
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
                when (overview.e) {
                    is CookiesExpiredException -> {
                        _viewEvents.setEvent(MainViewEvent.RouteToPath(LOGIN_PAGE, true))
                        _viewEvents.setEvent(MainViewEvent.ShowToast("登录过期, 请重新登录"))
                        logout(AppContext.nowNickname)
                    }

                    is CancelException -> {
                        throw CancelException(overview.msg)
                    }

                    else -> {
                        _viewEvents.setEvent(MainViewEvent.ShowToast(overview.msg))
                    }
                }
            }
        }
    }

    private suspend fun getResListLogic() {
        when (val result = overviewRepository.getAllResource("pe")) {
            is NetworkState.Success -> {
                result.data?.let { _viewStates.setState { copy(resList = it.item.filter { it.onlineTime != "UNKNOWN" }) } }
                computeMoney()
            }

            is NetworkState.Error -> {
                Logger.e("获取资源列表失败: ${result.msg}")
                if (result.e is CookiesExpiredException) {
                    _viewEvents.setEvent(MainViewEvent.RouteToPath(LOGIN_PAGE, true))
                    throw result.e
                } else {
                    throw Exception("获取资源列表失败: ${result.msg}")
                }
            }
        }
    }

    private fun deleteAccount(accountName: String) {
        val isLogout = accountName == AppContext.nowNickname
        viewModelScope.launch {
            logout(accountName)
            if (isLogout)
                _viewEvents.setEvent(MainViewEvent.RouteToPath(LOGIN_PAGE, true))
        }
    }

    private fun changeAccount(accountName: String) {
        viewModelScope.launch {
            mainRepository.stopAllCalls()
            delay(100)
            AppContext.nowNickname = accountName
            loadData(AppContext.nowNickname)
        }
    }

    private fun computeMoney() {
        viewModelScope.launch {
            val thisMonthProfit =
                getOneMonthProfit(ZonedDateTime.now().year, ZonedDateTime.now().monthValue) / 100.0
            val lastMonth = ZonedDateTime.now().minusMonths(1)
            val lastMonthProfit = getOneMonthProfit(lastMonth.year, lastMonth.monthValue) / 100.0

            val realMoney = getRealMoney(thisMonthProfit)
            val taxMoney = getTaxMoney(realMoney)
            val lastRealMoney = getRealMoney(lastMonthProfit)
            val lastTaxMoney = getTaxMoney(lastRealMoney)
            val df = DecimalFormat("0.00")
            val taxMoneyStr = df.format(taxMoney)
            val realMoneyStr = df.format(realMoney - taxMoney)
            val lastTaxMoneyStr = df.format(lastTaxMoney)
            val lastRealMoneyStr = df.format(lastRealMoney - lastTaxMoney)
            _viewStates.setState {
                copy(
                    isLoadingProfit = false,
                    realMoney = realMoneyStr,
                    taxMoney = taxMoneyStr,
                    lastRealMoney = lastRealMoneyStr,
                    lastTaxMoney = lastTaxMoneyStr
                )
            }

            withContext(Dispatchers.IO) {
                val dataBase = GlobalDataBase.database.infoDao()
                val latestOverview = dataBase.getLatestOverviewByNickname(AppContext.nowNickname)
                if (latestOverview != null) {
                    val updatedOverview = latestOverview.copy(
                        thisMonthProfit = realMoneyStr,
                        thisMonthTax = taxMoneyStr,
                        lastMonthProfit = lastRealMoneyStr,
                        lastMonthTax = lastTaxMoneyStr,
                        timestamp = System.currentTimeMillis()
                    )
                    dataBase.insertOverview(updatedOverview)
                }
            }
        }
    }

    private suspend fun getOneMonthProfit(year: Int, month: Int): Double {
        var monthProfit = 0.0
        flow {
            for (itemId in viewStates.value.resList) {
                when (val result =
                    realtimeProfitRepository.getOneMonthDetail("pe", itemId.itemId, year, month)) {
                    is NetworkState.Success -> {
                        result.data?.let { profit ->
                            emit(profit.totalDiamonds)
                        } ?: throw Exception("获取收益失败")
                    }

                    is NetworkState.Error -> {
                        Logger.e("获取收益失败: ${result.msg}")
                        if (result.e is CookiesExpiredException) {
                            _viewEvents.setEvent(MainViewEvent.RouteToPath(LOGIN_PAGE, true))
                            throw result.e
                        } else {
                            throw Exception("获取收益失败: ${result.msg}")
                        }
                    }
                }
            }
        }.catch {
            if (it !is CancelException) {
                _viewEvents.setEvent(MainViewEvent.ShowToast(it.message ?: "未知错误"))
                _viewStates.setState { copy(isLoadingProfit = false) }
            } else {
                Logger.d(it.message ?: "未知错误")
            }
        }.flowOn(Dispatchers.IO).collect {
            monthProfit += it
        }
        return monthProfit
    }

    private fun isDifferentDay(timestamp: Long): Boolean {
        val chinaZoneId = ZoneId.of("Asia/Shanghai")
        val timeToCheck = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), chinaZoneId)
        val currentTimeInChina = ZonedDateTime.now(chinaZoneId)

        return !timeToCheck.truncatedTo(ChronoUnit.DAYS)
            .isEqual(currentTimeInChina.truncatedTo(ChronoUnit.DAYS))
    }

    private fun getRealMoney(profit: Double): Double {
        // 渠道分成
        val channelMoney = profit * 0.35
        val lastMoney = profit - channelMoney

        // 网易阶段分成
        var level1 = if (profit > 1500) min(profit - 1500, 50000.0) * 0.3 else 0.0
        var level2 = if (profit > 50000) (profit - 50000) * 0.4 else 0.0
        val neteaseMoney = level1 + level2
        val neteasePercent = if (profit > 0) neteaseMoney / profit else 0.0

        // 技术服务费
        level1 = if (profit > 100000) min(profit - 100000, 1000000.0) * 0.1 else 0.0
        level2 = if (profit > 1000000) min(profit - 1000000, 3000000.0) * 0.15 else 0.0
        val level3 = if (profit > 3000000) min(profit - 3000000, 5000000.0) * 0.2 else 0.0
        val level4 = if (profit > 5000000) (profit - 5000000) * 0.25 else 0.0
        val serviceMoney = level1 + level2 + level3 + level4
        val servicePercent = if (profit > 0) serviceMoney / profit else 0.0

        return lastMoney - (lastMoney * neteasePercent) - (lastMoney * servicePercent)
    }

    private fun getTaxMoney(realMoney: Double): Double {
        return if (realMoney < 800) 0.0 else if (realMoney < 4000) (realMoney - 800) * 0.2 else (realMoney * 0.8) * 0.2
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
    val maxLevelExp: Double = 1.0,
    val currentExp: Double = 1.0,
    val canLevelUp: Boolean = false,

    val contributionMonth: String = "",
    val netGameClass: Int = 0,
    val netGameRank: Int = 0,
    val netGameScore: String = "0",
    val contributionClass: Int = 0,
    val contributionRank: Int = 0,
    val contributionScore: String = "0",

    val resList: List<ResourceBean> = emptyList(),

    val realMoney: String = "0.00",
    val taxMoney: String = "0.00",

    val lastRealMoney: String = "0.00",
    val lastTaxMoney: String = "0.00",

    val isLoadingProfit: Boolean = false
)

sealed class MainViewEvent {
    data class RouteToPath(val path: String, val needPop: Boolean = false) : MainViewEvent()
    data class ShowToast(val msg: String) : MainViewEvent()
    data object MaybeDataNoRefresh : MainViewEvent()
    data object ShowLastMonthProfit : MainViewEvent()
}

sealed class MainViewAction {
    data class LoadData(val nickname: String) : MainViewAction()
    data class DeleteAccount(val accountName: String) : MainViewAction()
    data class ChangeAccount(val accountName: String) : MainViewAction()
}