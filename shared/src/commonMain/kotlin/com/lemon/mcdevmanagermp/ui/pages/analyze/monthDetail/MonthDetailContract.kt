package com.lemon.mcdevmanagermp.ui.pages.analyze.monthDetail

import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResMonthAnalyzeData
import com.lemon.mcdevmanagermp.utils.extension.IUiAction
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import com.lemon.mcdevmanagermp.utils.extension.IUiState

/**
 * 快捷时间范围
 */
object QuickTimeRange {
    const val THIS_MONTH = 0
    const val LAST_3_MONTHS = 1
    const val LAST_6_MONTHS = 2
    const val LAST_12_MONTHS = 3
}

/**
 * 月度趋势图指标类型
 */
object MonthMetricType {
    const val TOTAL_DIAMOND = 0
    const val TOTAL_POINTS = 1
    const val AVG_DAU = 2
    const val AVG_DAY_BUY = 3
    const val DOWNLOAD = 4
    const val MAU = 5
}

data class MonthDetailState(
    val isLoading: Boolean = false,
    val platform: String = "pe",
    val quickTimeRange: Int = QuickTimeRange.LAST_3_MONTHS,
    val startDate: String = "",
    val endDate: String = "",
    val monthData: List<ResMonthAnalyzeData> = emptyList(),
    val selectedMetric: Int = MonthMetricType.TOTAL_DIAMOND,
) : IUiState

sealed interface MonthDetailAction : IUiAction {
    data object InitLoad : MonthDetailAction
    data class LoadData(val platform: String, val start: String, val end: String) : MonthDetailAction
    data object RefreshData : MonthDetailAction
    data class SetPlatform(val platform: String) : MonthDetailAction
    data class SetQuickTimeRange(val range: Int) : MonthDetailAction
    data class SelectMetric(val type: Int) : MonthDetailAction
}

sealed interface MonthDetailEffect : IUiEffect {
    data class ShowToast(val message: String) : MonthDetailEffect
    data object NeedReLogin : MonthDetailEffect
}
