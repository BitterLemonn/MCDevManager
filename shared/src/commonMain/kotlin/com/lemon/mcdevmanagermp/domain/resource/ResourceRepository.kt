package com.lemon.mcdevmanagermp.domain.resource

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.dto.netease.income.OneResRealtimeIncomeVO
import com.lemon.mcdevmanagermp.data.vo.netease.analyze.NewResDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.analyze.ResDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.analyze.ResMonthDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceListVO

interface ResourceRepository {
    suspend fun getAllResources(platform: String = "pe"): NetworkState<ResourceListVO>

    /** 获取指定作品的详情（基本信息 / PC 同步 / PE 等完整字段） */
    suspend fun getResourceDetail(itemId: String): NetworkState<ResourceDetailVO>
    suspend fun getDayDetail(
        platform: String,
        category: String,
        startDate: String,
        endDate: String,
        itemListStr: String
    ): NetworkState<ResDetailVO>
    suspend fun getNewDayDetail(
        platform: String,
        category: String,
        startDate: String,
        endDate: String,
        itemListStr: String
    ): NetworkState<NewResDetailVO>
    suspend fun getMonthDetail(
        platform: String,
        category: String,
        startDate: String,
        endDate: String,
        dayDateId: String
    ): NetworkState<ResMonthDetailVO>
    suspend fun getOneResRealtimeIncome(
        platform: String,
        iid: String,
        beginTime: String,
        endTime: String
    ): NetworkState<OneResRealtimeIncomeVO>
}
