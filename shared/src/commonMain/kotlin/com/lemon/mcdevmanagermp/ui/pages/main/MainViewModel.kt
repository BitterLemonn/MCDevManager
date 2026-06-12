package com.lemon.mcdevmanagermp.ui.pages.main

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.page.RankCategoryData
import com.lemon.mcdevmanagermp.data.page.RankCategoryTypeEnum
import com.lemon.mcdevmanagermp.data.repository.AccountRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.RankListRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.ResourceRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.UserRepositoryImpl
import com.lemon.mcdevmanagermp.data.vo.netease.user.LevelInfoVO
import com.lemon.mcdevmanagermp.data.vo.netease.user.OverviewVO
import com.lemon.mcdevmanagermp.data.vo.netease.user.UserInfoVO
import com.lemon.mcdevmanagermp.domain.main.MainUseCase
import com.lemon.mcdevmanagermp.domain.rankList.RankListUseCase
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import com.lemon.mcdevmanagermp.utils.ProfitData
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class MainViewModel : BaseViewModel<MainState, MainAction, MainEffect>(MainState()) {

    companion object {
        private val cacheTimeZone = TimeZone.of("Asia/Shanghai")

        private var cachedDashboardDate: String? = null
        private var cachedRankListDate: String? = null
        private var cachedProfitDate: String? = null

        var cachedUserInfo: NetworkState<UserInfoVO>? = null
            private set
        var cachedOverview: NetworkState<OverviewVO>? = null
            private set
        var cachedLevelInfo: NetworkState<LevelInfoVO>? = null
            private set

        var cachedProfitData: ProfitData? = null
            private set
        var cachedMonthLabel: String? = null
            private set
        var cachedLastMonthProfitData: ProfitData? = null
            private set
        var cachedLastMonthLabel: String? = null
            private set

        var cachedRankListData: List<RankCategoryData> = emptyList()
            private set

        var cachedShowLastMonthProfit: Boolean = false
            private set

        private fun todayString(): String {
            val now = Clock.System.now().toLocalDateTime(cacheTimeZone)
            return "${now.year}-${now.month.number}-${now.day}"
        }

        fun invalidateCache() {
            cachedDashboardDate = null
            cachedRankListDate = null
            cachedProfitDate = null
        }

        fun invalidateAllCache() {
            invalidateCache()
            cachedUserInfo = null
            cachedOverview = null
            cachedLevelInfo = null
            cachedProfitData = null
            cachedLastMonthProfitData = null
            cachedRankListData = emptyList()
        }
    }

    private val mainUseCase = MainUseCase(
        userRepository = UserRepositoryImpl.INSTANCE,
        resourceRepository = ResourceRepositoryImpl.INSTANCE
    )
    private val rankListUseCase = RankListUseCase(
        rankListRepository = RankListRepositoryImpl.INSTANCE
    )
    private val accountRepository = AccountRepositoryImpl.INSTANCE

    init {
        val today = todayString()

        // 从缓存中恢复仪表盘数据
        if (today == cachedDashboardDate && cachedUserInfo != null) {
            setState {
                copy(
                    userInfo = cachedUserInfo,
                    overview = cachedOverview,
                    levelInfo = cachedLevelInfo,
                    isRefreshing = false
                )
            }
        } else {
            loadDashboard()
        }

        // 从缓存中恢复排行榜数据
        if (today == cachedRankListDate && cachedRankListData.isNotEmpty()) {
            setState { copy(rankListData = cachedRankListData) }
        } else {
            loadRankList()
        }

        // 从缓存中恢复利润数据
        if (today == cachedProfitDate && cachedProfitData != null) {
            setState {
                copy(
                    profitData = cachedProfitData,
                    lastProfitData = cachedLastMonthProfitData,
                    isProfitLoading = false,
                    showLastMonthProfit = cachedShowLastMonthProfit
                )
            }
        } else {
            loadProfit()
        }
    }

    override fun dispatch(action: MainAction) {
        when (action) {
            is MainAction.SelectTab -> setState { copy(selectedTab = action.tab) }
            MainAction.LoadData -> {
                invalidateAllCache()
                loadDashboard()
                loadProfit()
            }

            MainAction.RefreshData -> {
                invalidateCache()
                loadDashboard()
                loadProfit()
            }

            MainAction.ToggleDrawer -> setState { copy(showDrawer = !showDrawer) }
            is MainAction.GetRankData -> loadRankCategory(action.category, action.subCategory)
            MainAction.ToggleProfitExpand -> setState { copy(profitExpanded = !profitExpanded) }
            MainAction.ToggleLastProfitExpand -> setState { copy(lastProfitExpanded = !lastProfitExpanded) }
        }
    }

    private fun loadDashboard() {
        setState { copy(isRefreshing = true) }
        viewModelScope.launch {
            try {
                val result = mainUseCase.loadDashboard()
                cachedUserInfo = result.userInfo
                cachedOverview = result.overview
                cachedLevelInfo = result.levelInfo
                cachedDashboardDate = todayString()
                setState {
                    copy(
                        userInfo = result.userInfo,
                        overview = result.overview,
                        levelInfo = result.levelInfo,
                        isRefreshing = false
                    )
                }
                if (mainUseCase.isSessionExpired(
                        result.userInfo,
                        result.overview,
                        result.levelInfo
                    )
                ) {
                    sendEffect(MainEffect.SessionExpired)
                } else {
                    // 兼容旧版本：将当前账号的 email 字段更新为 nickname
                    val userInfo = (result.userInfo as? NetworkState.Success)?.data
                    if (userInfo != null) {
                        val currentAccount = accountRepository.getLastUsedAccount()
                        if (currentAccount != null && currentAccount.nickname != userInfo.nickname) {
                            accountRepository.updateNicknameById(
                                currentAccount.id,
                                userInfo.nickname
                            )
                        }
                    }
                    val overview = (result.overview as? NetworkState.Success)?.data
                    if (overview != null && overview.yesterdayDiamond == 0 && overview.yesterdayDownload == 0) {
                        sendEffect(MainEffect.ShowToast("昨日数据可能未更新"))
                    }
                }
            } catch (_: Exception) {
                setState { copy(isRefreshing = false) }
                sendEffect(MainEffect.ShowToast("数据加载失败"))
            }
        }
    }

    private fun loadProfit() {
        setState { copy(isProfitLoading = true, profitExpanded = false, lastProfitExpanded = false) }
        viewModelScope.launch {
            try {
                val timeZone = TimeZone.of("Asia/Shanghai")
                val now = Clock.System.now().toLocalDateTime(timeZone)
                val result = mainUseCase.computeProfit(now.year, now.month.number)
                cachedProfitData = result.thisMonth
                cachedMonthLabel = "${now.year}年${now.month.number}月"
                cachedLastMonthProfitData = result.lastMonth
                val lastMonthNumber = if (now.month.number == 1) 12 else now.month.number - 1
                val lastMonthYear = if (now.month.number == 1) now.year - 1 else now.year
                cachedLastMonthLabel = "${lastMonthYear}年${lastMonthNumber}月"
                cachedShowLastMonthProfit = now.day <= 10
                cachedProfitDate = todayString()
                setState {
                    copy(
                        profitData = result.thisMonth,
                        lastProfitData = result.lastMonth,
                        isProfitLoading = false,
                        showLastMonthProfit = cachedShowLastMonthProfit
                    )
                }
            } catch (_: Exception) {
                setState { copy(isProfitLoading = false) }
            }
        }
    }

    private fun loadRankList() {
        viewModelScope.launch {
            val categories = RankCategoryTypeEnum.entries
            val results = coroutineScope {
                categories.map { category ->
                    async { category to rankListUseCase.getRankData(category) }
                }.associate { it.await() }
            }
            val rankData = categories.map {
                results[it] ?: RankCategoryData(
                    it.typeName,
                    com.lemon.mcdevmanagermp.data.page.commonRankCategoryContent
                )
            }
            cachedRankListData = rankData
            cachedRankListDate = todayString()
            setState { copy(rankListData = rankData) }
        }
    }

    private fun loadRankCategory(
        category: RankCategoryTypeEnum,
        subCategory: com.lemon.mcdevmanagermp.data.page.RankSubCategoryTypeEnum?
    ) {
        viewModelScope.launch {
            try {
                val newData = rankListUseCase.getRankData(category, subCategory)
                setState {
                    copy(rankListData = rankListData.map {
                        if (it.categoryTitle == category.typeName) newData else it
                    })
                }
            } catch (_: Exception) {
                sendEffect(MainEffect.ShowToast("排行榜加载失败"))
            }
        }
    }
}
