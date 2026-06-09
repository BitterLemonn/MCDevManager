package com.lemon.mcdevmanagermp.domain.resource

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.dto.netease.income.OneResRealtimeIncomeVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/**
 * 实时收益 UseCase：封装资源列表获取与逐资源实时收益查询逻辑
 */
class RealtimeProfitUseCase(
    private val resourceRepository: ResourceRepository
) {
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
     * 获取单个资源的实时收益
     */
    suspend fun getRealtimeIncome(
        platform: String,
        iid: String,
        beginTime: String,
        endTime: String
    ): NetworkState<OneResRealtimeIncomeVO> {
        val apiPlatform = if (platform == "pe") "pe" else "comp"
        return resourceRepository.getOneResRealtimeIncome(
            platform = apiPlatform,
            iid = iid,
            beginTime = beginTime,
            endTime = endTime
        )
    }

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
