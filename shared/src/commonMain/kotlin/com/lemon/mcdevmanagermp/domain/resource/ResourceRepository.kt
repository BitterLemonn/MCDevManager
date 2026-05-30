package com.lemon.mcdevmanagermp.domain.resource

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.vo.netease.resource.NewResDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceVO

interface ResourceRepository {
    suspend fun getAllResources(platform: String = "pe"): NetworkState<ResourceVO>
    suspend fun getNewDayDetail(
        platform: String,
        category: String,
        startDate: String,
        endDate: String,
        itemListStr: String
    ): NetworkState<NewResDetailVO>
}
