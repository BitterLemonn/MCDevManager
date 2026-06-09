package com.lemon.mcdevmanagermp.domain.resource

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.vo.netease.resource.NewResDetailData
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis.SummaryMetrics
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/**
 * 模组分析 UseCase：封装资源列表获取、模组分析数据获取与指标汇总逻辑
 */
class ModAnalysisUseCase(
    private val resourceRepository: ResourceRepository
) {
    companion object {
        private const val ANALYSIS_DAYS = 7
    }

    /**
     * 计算分析日期范围：昨天往前 ANALYSIS_DAYS 天
     * @return (startDate, endDate) 格式为 "yyyyMMdd"
     */
    fun getAnalysisDateRange(today: LocalDate): Pair<String, String> {
        val endDate = today.minus(1, DateTimeUnit.DAY)
        val startDate = endDate.minus(ANALYSIS_DAYS - 1, DateTimeUnit.DAY)
        return formatDateParam(startDate) to formatDateParam(endDate)
    }

    /**
     * 获取指定平台的资源列表
     */
    suspend fun getResourceList(platform: String): NetworkState<List<ResourceData>> {
        return when (val result = resourceRepository.getAllResources(platform)) {
            is NetworkState.Success -> {
                val list = result.data?.item ?: emptyList()
                NetworkState.Success(list)
            }

            is NetworkState.Error -> NetworkState.Error(result.msg, result.e)
        }
    }

    /**
     * 获取模组分析数据
     * @param platform 平台标识（UI 层的 "pe" / 其他）
     * @param iid 资源 IID
     * @param startDate 开始日期 "yyyyMMdd"
     * @param endDate 结束日期 "yyyyMMdd"
     * @return 分析数据列表与汇总指标
     */
    suspend fun getAnalysisData(
        platform: String,
        iid: String,
        startDate: String,
        endDate: String
    ): NetworkState<ModAnalysisResult> {
        val apiPlatform = if (platform == "pe") "pe" else "comp"

        return when (val result = resourceRepository.getNewDayDetail(
            platform = apiPlatform,
            category = apiPlatform,
            startDate = startDate,
            endDate = endDate,
            itemListStr = iid
        )) {
            is NetworkState.Success -> {
                val data = result.data?.data ?: emptyList()
                val metrics = computeSummaryMetrics(data)
                NetworkState.Success(ModAnalysisResult(data, metrics))
            }

            is NetworkState.Error -> NetworkState.Error(result.msg, result.e)
        }
    }

    /**
     * 从原始数据计算四指标汇总（取日均值 + 百分位均值）
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

    private fun formatDateParam(date: LocalDate): String {
        return date.toString().replace("-", "")
    }
}

/**
 * 模组分析结果
 */
data class ModAnalysisResult(
    val analysisData: List<NewResDetailData>,
    val summaryMetrics: SummaryMetrics
)
