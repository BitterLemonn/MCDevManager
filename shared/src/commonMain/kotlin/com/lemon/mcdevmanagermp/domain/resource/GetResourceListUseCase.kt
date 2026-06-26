package com.lemon.mcdevmanagermp.domain.resource

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData

/**
 * 获取指定平台资源列表：解包 [com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceListVO.item]
 */
class GetResourceListUseCase(
    private val resourceRepository: ResourceRepository
) {
    suspend operator fun invoke(platform: String): NetworkState<List<ResourceData>> {
        return when (val result = resourceRepository.getResources(platform)) {
            is NetworkState.Success -> NetworkState.Success(result.data?.item ?: emptyList())
            is NetworkState.Error -> NetworkState.Error(result.msg, result.e)
        }
    }
}
