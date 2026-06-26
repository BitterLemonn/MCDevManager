package com.lemon.mcdevmanagermp.domain.work

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ItemTagVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.MCConstsVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.RequirementItemData
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailVO
import com.lemon.mcdevmanagermp.domain.resource.ResourceRepository

/**
 * 作品详情 UseCase：封装获取作品详情逻辑。
 */
class WorkDetailUseCase(
    private val resourceRepository: ResourceRepository
) {
    suspend fun getResourceDetail(itemId: String): NetworkState<ResourceDetailVO> {
        return resourceRepository.getResourceDetail(itemId)
    }

    suspend fun getItemTag(): NetworkState<ItemTagVO> {
        return resourceRepository.getItemTag()
    }

    suspend fun getMCConsts(): NetworkState<MCConstsVO> {
        return resourceRepository.getMCConsts()
    }

    suspend fun getResourceList(
        platform: String,
        itemName: String? = null,
        mcStatus: Int? = null
    ): NetworkState<List<ResourceData>> {
        return when (val result = resourceRepository.getResources(platform, itemName, mcStatus)) {
            is NetworkState.Success -> NetworkState.Success(result.data?.item ?: emptyList())
            is NetworkState.Error -> NetworkState.Error(result.msg, result.e)
        }
    }

    /**
     * PC 前置模组搜索（comp 专用 requirements 接口，按名称搜索可作前置的模组）。
     */
    suspend fun getCompRequirements(itemName: String): NetworkState<List<RequirementItemData>> {
        return when (val result = resourceRepository.getCompRequirements(itemName)) {
            is NetworkState.Success -> NetworkState.Success(result.data?.item ?: emptyList())
            is NetworkState.Error -> NetworkState.Error(result.msg, result.e)
        }
    }
}
