package com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.consts.CookiesExpiredException
import com.lemon.mcdevmanagermp.data.repository.ResourceRepositoryImpl
import com.lemon.mcdevmanagermp.data.vo.netease.resource.NewResDetailData
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import com.lemon.mcdevmanagermp.utils.Logger
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class ModAnalysisViewModel : BaseViewModel<ModAnalysisState, ModAnalysisAction, ModAnalysisEffect>(
    ModAnalysisState()
) {
    private val resourceRepository = ResourceRepositoryImpl.INSTANCE

    companion object {
        private const val TAG = "ModAnalysisVM"
        private const val ANALYSIS_DAYS = 7
    }

    override fun dispatch(action: ModAnalysisAction) {
        when (action) {
            is ModAnalysisAction.InitLoad -> initLoad(action.iid, action.platform)
            is ModAnalysisAction.LoadData -> loadAnalysisData(action.iid, action.platform)
            ModAnalysisAction.RefreshData -> {
                val s = state.value
                if (s.selectedIid.isNotEmpty()) {
                    loadAnalysisData(s.selectedIid, s.selectedPlatform)
                }
            }

            is ModAnalysisAction.SelectMetric -> setState { copy(metricType = action.type) }
            ModAnalysisAction.ToggleChartType -> setState {
                copy(chartType = if (chartType == ChartType.LINE) ChartType.COLUMN else ChartType.LINE)
            }

            is ModAnalysisAction.SelectResource -> {
                setState { copy(selectedIid = action.iid, isResourceSelectorExpanded = false) }
                loadAnalysisData(action.iid, state.value.selectedPlatform)
            }

            ModAnalysisAction.ToggleResourceSelector -> setState {
                copy(isResourceSelectorExpanded = !isResourceSelectorExpanded)
            }
        }
    }

    /**
     * 初始化：加载资源列表，若预设了 iid 则直接加载分析数据
     */
    private fun initLoad(iid: String, platform: String) {
        setState { copy(selectedPlatform = platform, isResListLoading = true) }
        viewModelScope.launch {
            when (val result = resourceRepository.getAllResources(platform)) {
                is NetworkState.Success -> {
                    result.data?.let { data ->
                        setState { copy(resList = data.item, isResListLoading = false) }
                    }
                    // 若预设了 iid，直接加载分析数据
                    if (iid.isNotEmpty()) {
                        setState { copy(selectedIid = iid) }
                        loadAnalysisData(iid, platform)
                    }
                }

                is NetworkState.Error -> {
                    Logger.e("$TAG: 获取资源列表失败: ${result.msg}")
                    setState { copy(isResListLoading = false) }
                    if (result.e is CookiesExpiredException) {
                        sendEffect(ModAnalysisEffect.NeedReLogin)
                    } else {
                        sendEffect(ModAnalysisEffect.ShowToast("获取资源列表失败: ${result.msg}"))
                    }
                }
            }
        }
    }

    /**
     * 加载模组分析数据
     */
    private fun loadAnalysisData(iid: String, platform: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true) }

            // 计算日期范围：最近 7 天
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val endDate = today.minus(1, DateTimeUnit.DAY) // 昨天（数据延迟一天）
            val startDate = endDate.minus(ANALYSIS_DAYS - 1, DateTimeUnit.DAY)

            val startDateStr = formatDateParam(startDate)
            val endDateStr = formatDateParam(endDate)

            val apiPlatform = if (platform == "pe") "pe" else "comp"

            when (val result = resourceRepository.getNewDayDetail(
                platform = apiPlatform,
                category = apiPlatform,
                startDate = startDateStr,
                endDate = endDateStr,
                itemListStr = iid
            )) {
                is NetworkState.Success -> {
                    result.data?.let { data ->
                        if (data.data.isNotEmpty()) {
                            val first = data.data.first()
                            val metrics = computeSummaryMetrics(data.data)
                            setState {
                                copy(
                                    isLoading = false,
                                    analysisData = data.data,
                                    modName = first.resName,
                                    modScore = first.starAdjusted,
                                    selectedIid = iid,
                                    summaryMetrics = metrics,
                                )
                            }
                        } else {
                            setState { copy(isLoading = false, analysisData = emptyList()) }
                            sendEffect(ModAnalysisEffect.ShowToast("暂无分析数据"))
                        }
                    } ?: run {
                        setState { copy(isLoading = false) }
                        sendEffect(ModAnalysisEffect.ShowToast("数据为空"))
                    }
                }

                is NetworkState.Error -> {
                    Logger.e("$TAG: 获取分析数据失败: $result")
                    setState { copy(isLoading = false) }
                    if (result.e is CookiesExpiredException) {
                        sendEffect(ModAnalysisEffect.NeedReLogin)
                    } else {
                        sendEffect(ModAnalysisEffect.ShowToast("获取分析数据失败: ${result.msg}"))
                    }
                }
            }
        }
    }

    /**
     * 从原始数据计算四指标汇总（参考旧项目逻辑：取日均值 + 百分位均值）
     */
    private fun computeSummaryMetrics(data: List<NewResDetailData>): SummaryMetrics {
        if (data.isEmpty()) return SummaryMetrics()
        return SummaryMetrics(
            newPurchaseCount = data.sumOf { it.cntBuy } / data.size,
            newPurchasePercent = data.sumOf { it.passBuyCntRatio } / data.size.toDouble(),
            dau = data.sumOf { it.dau } / data.size,
            dauPercent = data.sumOf { it.passCntRolePlayRatio } / data.size.toDouble(),
            newFollowCount = data.sumOf { it.focusCnt } / data.size,
            newFollowPercent = data.sumOf { it.passFocusCntRatio } / data.size.toDouble(),
            avgPlayTime = data.sumOf { it.avgPlaytime } / data.size.toDouble(),
            avgPlayTimePercent = data.sumOf { it.passAvgRoleTimeRatio } / data.size.toDouble(),
        )
    }

    /**
     * 格式化日期参数：yyyy-MM-dd → yyyyMMdd
     */
    private fun formatDateParam(date: LocalDate): String {
        return date.toString().replace("-", "")
    }
}
