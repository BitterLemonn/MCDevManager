package com.lemon.mcdevmanagermp.ui.pages.main

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.page.RankCategoryData
import com.lemon.mcdevmanagermp.data.page.RankCategoryTypeEnum
import com.lemon.mcdevmanagermp.data.page.RankSubCategoryTypeEnum
import com.lemon.mcdevmanagermp.data.vo.netease.user.LevelInfoVO
import com.lemon.mcdevmanagermp.data.vo.netease.user.OverviewVO
import com.lemon.mcdevmanagermp.data.vo.netease.user.UserInfoVO
import com.lemon.mcdevmanagermp.ui.navigation.Route
import com.lemon.mcdevmanagermp.utils.ProfitData
import com.lemon.mcdevmanagermp.utils.extension.IUiAction
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import com.lemon.mcdevmanagermp.utils.extension.IUiState

data class MainState(
    val selectedTab: MainTab = MainTab.Home,
    val userInfo: NetworkState<UserInfoVO>? = null,
    val overview: NetworkState<OverviewVO>? = null,
    val levelInfo: NetworkState<LevelInfoVO>? = null,
    val isRefreshing: Boolean = false,
    val showDrawer: Boolean = false,
    val rankListData: List<RankCategoryData> = emptyList(),
    val profitData: ProfitData? = null,
    val lastProfitData: ProfitData? = null,
    val isProfitLoading: Boolean = true,
    val profitExpanded: Boolean = false,
    val showLastMonthProfit: Boolean = false
) : IUiState

sealed interface MainAction : IUiAction {
    data class SelectTab(val tab: MainTab) : MainAction
    data object LoadData : MainAction
    data object RefreshData : MainAction
    data object ToggleDrawer : MainAction
    data class GetRankData(
        val category: RankCategoryTypeEnum,
        val subCategory: RankSubCategoryTypeEnum? = null
    ) : MainAction
    data object ToggleProfitExpand : MainAction
}

sealed interface MainEffect : IUiEffect {
    data class ShowToast(val message: String) : MainEffect
    data class NavigateTo(val route: Route) : MainEffect
    data object SessionExpired : MainEffect
}
