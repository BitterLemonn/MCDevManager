package com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail

import androidx.compose.ui.graphics.Color
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResAnalyzeData
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis.ChartType
import com.lemon.mcdevmanagermp.utils.extension.IUiAction
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import com.lemon.mcdevmanagermp.utils.extension.IUiState

/**
 * 指标类型：0=新增购买, 1=下载量, 2=钻石收益, 3=绿宝石收益, 4=日活, 5=退款率
 */
object DayDetailMetricType {
    const val NEW_PURCHASE = 0
    const val DOWNLOAD = 1
    const val DIAMOND = 2
    const val POINTS = 3
    const val DAU = 4
    const val REFUND_RATE = 5
}

/**
 * 多资源图表颜色
 */
val CHART_COLORS = listOf(
    Color(0x42 / 255f, 0x85 / 255f, 0xF4 / 255f),  // 蓝 #4285F4
    Color(0xEA / 255f, 0x43 / 255f, 0x35 / 255f),  // 红 #EA4335
    Color(0x34 / 255f, 0xA8 / 255f, 0x53 / 255f),  // 绿 #34A853
    Color(0xFB / 255f, 0xBC / 255f, 0x05 / 255f),  // 黄 #FBBC05
    Color(0x9C / 255f, 0x27 / 255f, 0xB0 / 255f),  // 紫 #9C27B0
)

data class DayDetailState(
    val isLoading: Boolean = false,
    val resList: List<ResourceData> = emptyList(),
    val selectedIIDs: List<String> = emptyList(),
    val platform: String = "pe",
    val startDate: String = "",
    val endDate: String = "",
    val metricType: Int = DayDetailMetricType.NEW_PURCHASE,
    val chartType: ChartType = ChartType.LINE,
    val detailData: Map<String, List<ResAnalyzeData>> = emptyMap(),
    val isResSelectorVisible: Boolean = false,
    val isResListLoading: Boolean = false,
) : IUiState

sealed interface DayDetailAction : IUiAction {
    data object InitLoad : DayDetailAction
    data class LoadData(val iids: List<String>) : DayDetailAction
    data object RefreshData : DayDetailAction
    data class ToggleResource(val iid: String) : DayDetailAction
    data class SetPlatform(val platform: String) : DayDetailAction
    data class SetDateRange(val start: String, val end: String) : DayDetailAction
    data class SelectMetric(val type: Int) : DayDetailAction
    data object ToggleChartType : DayDetailAction
    data object ToggleResSelector : DayDetailAction
}

sealed interface DayDetailEffect : IUiEffect {
    data class ShowToast(val message: String) : DayDetailEffect
    data object NeedReLogin : DayDetailEffect
}
