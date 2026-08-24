package com.lemon.mcdevmanagermp.domain.analyze

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.dto.netease.income.LobbyIncomeResourceListVO
import com.lemon.mcdevmanagermp.data.dto.netease.income.OneResRealtimeIncomeVO
import com.lemon.mcdevmanagermp.data.vo.netease.analyze.ResDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.analyze.ResMonthDetailVO

/**
 * 数据追踪页「上次查询配置」（按账号 + 平台隔离）。
 */
data class DayDetailConfig(
    val accountKey: String,
    val platform: String,
    val dateSpanDays: Int,
    val selectedIIDs: List<String>
)

interface AnalyzeRepository {
    suspend fun getDayDetail(
        platform: String,
        category: String,
        startDate: String,
        endDate: String,
        itemListStr: String,
        isLobby: Boolean = false
    ): NetworkState<ResDetailVO>

    suspend fun getMonthDetail(
        platform: String,
        category: String,
        startDate: String,
        endDate: String,
        dayDateId: String,
        isLobby: Boolean = false
    ): NetworkState<ResMonthDetailVO>

    suspend fun getOneResRealtimeIncome(
        platform: String,
        iid: String,
        beginTime: String,
        endTime: String
    ): NetworkState<OneResRealtimeIncomeVO>

    suspend fun getLobbyIncomeResources(): NetworkState<LobbyIncomeResourceListVO>

    suspend fun getLobbyRealtimeIncome(
        iid: String,
        beginTime: String,
        endTime: String
    ): NetworkState<OneResRealtimeIncomeVO>

    suspend fun getDayDetailConfig(accountKey: String, platform: String): DayDetailConfig?

    suspend fun saveDayDetailConfig(config: DayDetailConfig)
}
