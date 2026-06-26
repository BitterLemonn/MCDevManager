package com.lemon.mcdevmanagermp.domain.resource

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ItemTagVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.MCConstsVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.RequirementVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceListVO

interface ResourceRepository {
    suspend fun getResources(
        platform: String = "pe",
        itemName: String? = null,
        mcStatus: Int? = null
    ): NetworkState<ResourceListVO>

    suspend fun getCompRequirements(itemName: String): NetworkState<RequirementVO>

    suspend fun getResourceDetail(itemId: String): NetworkState<ResourceDetailVO>

    suspend fun getItemTag(): NetworkState<ItemTagVO>

    suspend fun getMCConsts(): NetworkState<MCConstsVO>
}