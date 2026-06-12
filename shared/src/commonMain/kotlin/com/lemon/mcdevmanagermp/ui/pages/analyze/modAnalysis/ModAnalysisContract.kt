package com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis

import com.lemon.mcdevmanagermp.data.vo.netease.resource.NewResAnalyzeData
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import com.lemon.mcdevmanagermp.utils.extension.IUiAction
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import com.lemon.mcdevmanagermp.utils.extension.IUiState

/**
 * 图表类型枚举
 */
enum class ChartType {
    LINE,
    COLUMN
}

/**
 * 指标类型：0=新增购买, 1=日活, 2=新增粉丝, 3=人均游玩时间
 */
object MetricType {
    const val NEW_PURCHASE = 0
    const val DAU = 1
    const val NEW_FOLLOW = 2
    const val AVG_PLAY_TIME = 3
}

/**
 * 四指标汇总数据
 */
data class SummaryMetrics(
    val newPurchaseCount: Int = 0,
    val newPurchasePercent: Double = 0.0,
    val dau: Int = 0,
    val dauPercent: Double = 0.0,
    val newFollowCount: Int = 0,
    val newFollowPercent: Double = 0.0,
    val avgPlayTime: Double = 0.0,
    val avgPlayTimePercent: Double = 0.0,
)

data class ModAnalysisState(
    val isLoading: Boolean = false,
    val resList: List<ResourceData> = emptyList(),
    val selectedIid: String = "",
    val selectedPlatform: String = "pe",
    val analysisData: List<NewResAnalyzeData> = emptyList(),
    val modName: String = "",
    val modScore: Double = 0.0,
    val metricType: Int = MetricType.NEW_PURCHASE,
    val chartType: ChartType = ChartType.LINE,
    val summaryMetrics: SummaryMetrics = SummaryMetrics(),
    val isResourceSelectorExpanded: Boolean = false,
    val isResListLoading: Boolean = false,
) : IUiState

sealed interface ModAnalysisAction : IUiAction {
    data class InitLoad(val iid: String, val platform: String) : ModAnalysisAction
    data class LoadData(val iid: String, val platform: String) : ModAnalysisAction
    data object RefreshData : ModAnalysisAction
    data class SelectMetric(val type: Int) : ModAnalysisAction
    data object ToggleChartType : ModAnalysisAction
    data class SelectResource(val iid: String) : ModAnalysisAction
    data object ToggleResourceSelector : ModAnalysisAction
}

sealed interface ModAnalysisEffect : IUiEffect {
    data class ShowToast(val message: String) : ModAnalysisEffect
    data object NeedReLogin : ModAnalysisEffect
}
