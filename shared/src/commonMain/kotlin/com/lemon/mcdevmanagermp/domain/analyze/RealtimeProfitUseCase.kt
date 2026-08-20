package com.lemon.mcdevmanagermp.domain.analyze

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.dto.netease.income.LobbyIncomeResourceListVO
import com.lemon.mcdevmanagermp.data.dto.netease.income.OneResRealtimeIncomeVO
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/**
 * 实时收益 UseCase：封装资源列表获取与逐资源实时收益查询逻辑
 */
class RealtimeProfitUseCase(
    private val analyzeRepository: AnalyzeRepository
) {
    /**
     * 获取单个资源的实时收益
     */
    suspend fun getRealtimeIncome(
        platform: String,
        iid: String,
        beginTime: String,
        endTime: String
    ): NetworkState<OneResRealtimeIncomeVO> {
        val apiPlatform = if (platform == "pe") "pe" else "comp"
        return analyzeRepository.getOneResRealtimeIncome(
            platform = apiPlatform,
            iid = iid,
            beginTime = beginTime,
            endTime = endTime
        )
    }

    suspend fun getLobbyIncomeResources(): NetworkState<LobbyIncomeResourceListVO> =
        analyzeRepository.getLobbyIncomeResources()

    suspend fun getLobbyRealtimeIncome(
        iid: String,
        beginTime: String,
        endTime: String
    ): NetworkState<OneResRealtimeIncomeVO> = analyzeRepository.getLobbyRealtimeIncome(
        iid = iid,
        beginTime = beginTime,
        endTime = endTime
    )

    /**
     * 计算实时收益查询的时间范围：前一天 16:00 ~ 当天 15:59
     * @param checkDay 格式 "yyyy-MM-dd"
     * @return (beginTime, endTime) 格式为 ISO 时间字符串
     */
    fun computeTimeRange(checkDay: String): Pair<String, String> {
        val prevDay = computePrevDay(checkDay)
        val beginTime = "${prevDay}T16:00:00.000Z"
        val endTime = "${checkDay}T15:59:59.999Z"
        return beginTime to endTime
    }

    /**
     * 计算前一天日期（yyyy-MM-dd）
     */
    private fun computePrevDay(dateStr: String): String {
        val date = LocalDate.parse(dateStr)
        return date.minus(1, DateTimeUnit.DAY).toString()
    }
}

internal fun mergeRealtimeIncome(
    current: OneResRealtimeIncomeVO?,
    incoming: OneResRealtimeIncomeVO
): OneResRealtimeIncomeVO = OneResRealtimeIncomeVO(
    count = (current?.count ?: 0) + incoming.count,
    totalDiamonds = (current?.totalDiamonds ?: 0) + incoming.totalDiamonds,
    totalPoints = (current?.totalPoints ?: 0) + incoming.totalPoints,
    orders = current?.orders.orEmpty() + incoming.orders
)
