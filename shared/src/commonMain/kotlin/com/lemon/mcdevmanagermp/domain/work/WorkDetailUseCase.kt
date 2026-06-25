package com.lemon.mcdevmanagermp.domain.work

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ItemTagVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.MCConstsVO
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
}
