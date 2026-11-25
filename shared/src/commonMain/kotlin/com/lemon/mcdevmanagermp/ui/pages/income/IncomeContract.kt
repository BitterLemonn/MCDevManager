package com.lemon.mcdevmanagermp.ui.pages.income

import com.lemon.mcdevmanagermp.data.vo.netease.income.ApplyIncomeDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.income.IncomeVO
import com.lemon.mcdevmanagermp.utils.extension.IUiAction
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import com.lemon.mcdevmanagermp.utils.extension.IUiState

/**
 * 收益结算单状态筛选维度。
 */
enum class IncomeStatusFilter(val label: String) {
    ALL("全部"),
    UNSETTLED("未结算"),
    PROCESSING("结算中"),
    SETTLED("已打款"),
    ATTENTION("异常")
}

internal fun IncomeVO.matchesFilter(filter: IncomeStatusFilter): Boolean = when (filter) {
    IncomeStatusFilter.ALL -> true
    IncomeStatusFilter.UNSETTLED -> status == "未结算"
    IncomeStatusFilter.PROCESSING -> status == "结算中"
    IncomeStatusFilter.SETTLED -> status == "已打款"
    IncomeStatusFilter.ATTENTION -> isAttentionStatus()
}

internal fun IncomeVO.isAttentionStatus(): Boolean =
    status == "结算失败" || status == "打款失败" || status == "结算信息待更正"

internal fun IncomeVO.isSettleable(): Boolean =
    status == "未结算" && (availableIncome.toDoubleOrNull() ?: 0.0) > 0.0

data class IncomeState(
    val isLoading: Boolean = false,
    val isLoadingApplyDetail: Boolean = false,
    val isApplyingIncome: Boolean = false,
    val peList: List<IncomeVO> = emptyList(),
    val pcList: List<IncomeVO> = emptyList(),
    val selectedPlatform: String = "pe",
    val statusFilter: IncomeStatusFilter = IncomeStatusFilter.ALL,
    val selectedIncomeId: String? = null,
    val applyDetailList: List<ApplyIncomeDetailVO> = emptyList(),
    val unExtractedIncome: String = "0.00",
    /** 历史生态费率（数据月份 yyyy-MM → 费率），来自本地保存的反推记录 */
    val ecoFeeRates: Map<String, Double> = emptyMap(),
) : IUiState {

    val currentList: List<IncomeVO>
        get() = if (selectedPlatform == "pe") peList else pcList

    val filteredList: List<IncomeVO>
        get() = currentList.filter { it.matchesFilter(statusFilter) }

    val hasAttentionItems: Boolean
        get() = currentList.any { it.isAttentionStatus() }

    val settleableItem: IncomeVO?
        get() = currentList.firstOrNull { it.isSettleable() }

    val hasSettleableIncome: Boolean
        get() = settleableItem != null

    val settleableIncomeId: String
        get() = settleableItem?.id ?: ""

    val effectiveSelectedIncome: IncomeVO?
        get() = filteredList.firstOrNull { it.id == selectedIncomeId } ?: filteredList.firstOrNull()
}

sealed interface IncomeAction : IUiAction {
    data object LoadData : IncomeAction
    data class SelectPlatform(val platform: String) : IncomeAction
    data class SelectStatusFilter(val filter: IncomeStatusFilter) : IncomeAction
    data class SelectIncome(val incomeId: String) : IncomeAction
    data class GetApplyDetail(val incomeId: String) : IncomeAction
    data class ApplyIncome(val incomeIds: List<String>) : IncomeAction
    data object DismissApplyDetail : IncomeAction
}

sealed interface IncomeEffect : IUiEffect {
    data class ShowToast(val message: String) : IncomeEffect
}
