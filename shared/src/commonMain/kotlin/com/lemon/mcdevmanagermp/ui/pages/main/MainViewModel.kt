package com.lemon.mcdevmanagermp.ui.pages.main

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.page.RankCategoryData
import com.lemon.mcdevmanagermp.data.page.RankCategoryTypeEnum
import com.lemon.mcdevmanagermp.data.repository.RankListRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.ResourceRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.UserRepositoryImpl
import com.lemon.mcdevmanagermp.domain.main.MainUseCase
import com.lemon.mcdevmanagermp.utils.ProfitData
import com.lemon.mcdevmanagermp.domain.rankList.RankListUseCase
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class MainViewModel : BaseViewModel<MainState, MainAction, MainEffect>(MainState()) {

    companion object {
        var cachedProfitData: ProfitData? = null
        var cachedMonthLabel: String? = null
    }

    private val mainUseCase = MainUseCase(
        userRepository = UserRepositoryImpl.INSTANCE,
        resourceRepository = ResourceRepositoryImpl.INSTANCE
    )
    private val rankListUseCase = RankListUseCase(
        rankListRepository = RankListRepositoryImpl.INSTANCE
    )

    init {
        loadDashboard()
        loadRankList()
        loadProfit()
    }

    override fun dispatch(action: MainAction) {
        when (action) {
            is MainAction.SelectTab -> setState { copy(selectedTab = action.tab) }
            MainAction.LoadData -> {
                loadDashboard()
                loadProfit()
            }
            MainAction.RefreshData -> {
                loadDashboard()
                loadProfit()
            }
            MainAction.ToggleDrawer -> setState { copy(showDrawer = !showDrawer) }
            is MainAction.GetRankData -> loadRankCategory(action.category, action.subCategory)
            MainAction.ToggleProfitExpand -> setState { copy(profitExpanded = !profitExpanded) }
        }
    }

    private fun loadDashboard() {
        setState { copy(isRefreshing = true) }
        viewModelScope.launch {
            try {
                val result = mainUseCase.loadDashboard()
                setState {
                    copy(
                        userInfo = result.userInfo,
                        overview = result.overview,
                        levelInfo = result.levelInfo,
                        isRefreshing = false
                    )
                }
                if (mainUseCase.isSessionExpired(result.userInfo, result.overview, result.levelInfo)) {
                    sendEffect(MainEffect.SessionExpired)
                } else {
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
        setState { copy(isProfitLoading = true, profitExpanded = false) }
        viewModelScope.launch {
            try {
                val timeZone = TimeZone.of("Asia/Shanghai")
                val now = Clock.System.now().toLocalDateTime(timeZone)
                val result = mainUseCase.computeProfit(now.year, now.monthNumber)
                cachedProfitData = result.thisMonth
                cachedMonthLabel = "${now.year}年${now.monthNumber}月"
                setState {
                    copy(
                        profitData = result.thisMonth,
                        lastProfitData = result.lastMonth,
                        isProfitLoading = false,
                        showLastMonthProfit = now.day <= 10
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
            setState {
                copy(
                    rankListData = categories.map {
                        results[it] ?: RankCategoryData(
                            it.typeName,
                            com.lemon.mcdevmanagermp.data.page.commonRankCategoryContent
                        )
                    }
                )
            }
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
