package com.lemon.mcdevmanagermp.domain.resource

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResDetailData
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/**
 * 日详情 UseCase：封装资源列表获取、日详情数据获取与分组逻辑
 */
class DayDetailUseCase(
    private val resourceRepository: ResourceRepository
) {
    companion object {
        private const val DEFAULT_DAYS = 14
    }

    /**
     * 计算默认日期范围：昨天往前 DEFAULT_DAYS 天
     * @return (startDate, endDate) 格式为 "yyyyMMdd"
     */
    fun getDefaultDateRange(today: LocalDate): Pair<String, String> {
        val endDate = today.minus(1, DateTimeUnit.DAY)
        val startDate = endDate.minus(DEFAULT_DAYS - 1, DateTimeUnit.DAY)
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
     * 获取多资源日详情数据，并按 iid 分组
     * @param platform 平台标识（UI 层的 "pe" / 其他）
     * @param startDate 开始日期 "yyyyMMdd"
     * @param endDate 结束日期 "yyyyMMdd"
     * @param iids 资源 IID 列表
     * @return 按 iid 分组的日详情数据
     */
    suspend fun getGroupedDayDetail(
        platform: String,
        startDate: String,
        endDate: String,
        iids: List<String>
    ): NetworkState<Map<String, List<ResDetailData>>> {
        val itemListStr = iids.joinToString(",")
        val apiPlatform = if (platform == "pe") "pe" else "comp"

        return when (val result = resourceRepository.getDayDetail(
            platform = apiPlatform,
            category = apiPlatform,
            startDate = startDate,
            endDate = endDate,
            itemListStr = itemListStr
        )) {
            is NetworkState.Success -> {
                val grouped = result.data?.data?.groupBy { it.iid } ?: emptyMap()
                NetworkState.Success(grouped)
            }

            is NetworkState.Error -> NetworkState.Error(result.msg, result.e)
        }
    }

    private fun formatDateParam(date: LocalDate): String {
        return date.toString().replace("-", "")
    }
}
