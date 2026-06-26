package com.lemon.mcdevmanagermp.data.repository

import com.lemon.mcdevmanagermp.data.api.ResourceApi
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ItemTagVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.MCConstsVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.RequirementVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceListVO
import com.lemon.mcdevmanagermp.domain.resource.ResourceRepository
import com.lemon.mcdevmanagermp.utils.UnifiedExceptionHandler

class ResourceRepositoryImpl : ResourceRepository {

    companion object {
        val INSTANCE by lazy { ResourceRepositoryImpl() }
        private val resourceApi = ResourceApi.INSTANCE
    }

    override suspend fun getResources(
        platform: String,
        itemName: String?,
        mcStatus: Int?
    ): NetworkState<ResourceListVO> {
        return UnifiedExceptionHandler.handleRequest {
            resourceApi.getAllResource(
                platform = platform,
                itemName = itemName,
                mcStatus = mcStatus
            )
        }
    }

    override suspend fun getCompRequirements(itemName: String): NetworkState<RequirementVO> {
        return UnifiedExceptionHandler.handleRequest {
            resourceApi.getRequirements(itemName)
        }
    }

    override suspend fun getResourceDetail(itemId: String): NetworkState<ResourceDetailVO> {
        return UnifiedExceptionHandler.handleRequest {
            resourceApi.getResourceDetail(itemId)
        }
    }

    override suspend fun getItemTag(): NetworkState<ItemTagVO> {
        return UnifiedExceptionHandler.handleRequest {
            resourceApi.getItemTag()
        }
    }

    override suspend fun getMCConsts(): NetworkState<MCConstsVO> {
        return UnifiedExceptionHandler.handleRequest {
            resourceApi.getMCConsts()
        }
    }
}