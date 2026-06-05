package com.lemon.mcdevmanagermp.ui.pages.income

import com.lemon.mcdevmanagermp.data.vo.netease.income.ApplyIncomeDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.income.IncomeVO
import com.lemon.mcdevmanagermp.utils.extension.IUiAction
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import com.lemon.mcdevmanagermp.utils.extension.IUiState

data class IncomeState(
    val isLoading: Boolean = false,
    val peList: List<IncomeVO> = emptyList(),
    val pcList: List<IncomeVO> = emptyList(),
    val selectedPlatform: String = "pe",
    val applyDetailList: List<ApplyIncomeDetailVO> = emptyList(),
    val unExtractedIncome: String = "0.00",
) : IUiState {

    val currentList: List<IncomeVO>
        get() = if (selectedPlatform == "pe") peList else pcList

    val hasSettleableIncome: Boolean
        get() = currentList.any { it.status == "未结算" && it.availableIncome != "0.00" }

    val settleableIncomeId: String
        get() = currentList.firstOrNull { it.status == "未结算" && it.availableIncome != "0.00" }?.id ?: ""
}

sealed interface IncomeAction : IUiAction {
    data object LoadData : IncomeAction
    data class SelectPlatform(val platform: String) : IncomeAction
    data class GetApplyDetail(val incomeId: String) : IncomeAction
    data class ApplyIncome(val incomeIds: List<String>) : IncomeAction
    data object DismissApplyDetail : IncomeAction
}

sealed interface IncomeEffect : IUiEffect {
    data class ShowToast(val message: String) : IncomeEffect
}
