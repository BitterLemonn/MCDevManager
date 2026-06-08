package com.lemon.mcdevmanagermp.ui.pages.analyze.realtimeProfit

import com.lemon.mcdevmanagermp.data.dto.netease.income.OneResRealtimeIncomeVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import com.lemon.mcdevmanagermp.utils.extension.IUiAction
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import com.lemon.mcdevmanagermp.utils.extension.IUiState

data class RealtimeProfitState(
    val isLoading: Boolean = false,
    val resList: List<ResourceData> = emptyList(),
    val profitMap: Map<String, OneResRealtimeIncomeVO> = emptyMap(),
    val checkDay: String = "",
    val platform: String = "pe",
    val lastRequestTime: Long = 0,
    val totalDiamond: Int = 0,
    val totalPoints: Int = 0,
    val isDateSelectorVisible: Boolean = false,
) : IUiState

sealed interface RealtimeProfitAction : IUiAction {
    data class LoadData(val day: String) : RealtimeProfitAction
    data object RefreshData : RealtimeProfitAction
    data class UpdateCheckDay(val day: String) : RealtimeProfitAction
    data object ToggleDateSelector : RealtimeProfitAction
    data class SelectPlatform(val platform: String) : RealtimeProfitAction
}

sealed interface RealtimeProfitEffect : IUiEffect {
    data class ShowToast(val message: String) : RealtimeProfitEffect
    data object NeedReLogin : RealtimeProfitEffect
}
