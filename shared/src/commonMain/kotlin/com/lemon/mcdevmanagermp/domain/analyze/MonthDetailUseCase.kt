package com.lemon.mcdevmanagermp.domain.analyze

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.vo.netease.analyze.ResMonthAnalyzeData
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.number

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
 * 月详情 UseCase：封装月度汇总数据获取与排序逻辑
 */
class MonthDetailUseCase(
    private val analyzeRepository: AnalyzeRepository
) {
    /**
     * 获取月度汇总数据，按 monthId 降序排列
     * @param platform 平台标识（UI 层的 "pe" / 其他）
     * @param startDate 开始日期 "yyyyMMdd"
     * @param endDate 结束日期 "yyyyMMdd"
     * @return 排序后的月度数据列表
     */
    suspend fun getMonthDetail(
        platform: String,
        startDate: String,
        endDate: String
    ): NetworkState<List<ResMonthAnalyzeData>> {
        val isLobby = platform == "lobby"
        val apiPlatform = if (platform == "comp") "comp" else "pe"

        return when (val result = analyzeRepository.getMonthDetail(
            platform = apiPlatform,
            category = apiPlatform,
            startDate = startDate,
            endDate = endDate,
            dayDateId = endDate,
            isLobby = isLobby
        )) {
            is NetworkState.Success -> {
                val sorted = result.data?.data?.sortedByDescending { it.monthId } ?: emptyList()
                NetworkState.Success(sorted)
            }

            is NetworkState.Error -> NetworkState.Error(result.msg, result.e)
        }
    }

    /**
     * 根据快捷时间范围计算起止日期
     * @return (startDate, endDate) 格式为 "yyyyMMdd"
     */
    fun getQuickTimeRange(range: Int, today: LocalDate): Pair<String, String> {
        val (start, end) = when (range) {
            QuickTimeRange.THIS_MONTH -> {
                val start = LocalDate(today.year, today.month.number, 1)
                start to today.minus(1, DateTimeUnit.DAY)
            }

            QuickTimeRange.LAST_3_MONTHS -> {
                val end = today.minus(1, DateTimeUnit.DAY)
                val start = LocalDate(today.year, today.month.number, 1)
                    .minus(2, DateTimeUnit.MONTH)
                start to end
            }

            QuickTimeRange.LAST_6_MONTHS -> {
                val end = today.minus(1, DateTimeUnit.DAY)
                val start = LocalDate(today.year, today.month.number, 1)
                    .minus(5, DateTimeUnit.MONTH)
                start to end
            }

            QuickTimeRange.LAST_12_MONTHS -> {
                val end = today.minus(1, DateTimeUnit.DAY)
                val start = LocalDate(today.year, today.month.number, 1)
                    .minus(11, DateTimeUnit.MONTH)
                start to end
            }

            else -> {
                val end = today.minus(1, DateTimeUnit.DAY)
                val start = end.minus(90, DateTimeUnit.DAY)
                start to end
            }
        }

        return formatDateParam(start) to formatDateParam(end)
    }

    private fun formatDateParam(date: LocalDate): String {
        return date.toString().replace("-", "")
    }
}
