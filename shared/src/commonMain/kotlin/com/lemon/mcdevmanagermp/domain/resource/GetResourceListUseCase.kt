package com.lemon.mcdevmanagermp.domain.resource

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData

/**
 * 获取指定平台资源列表：解包 [com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceListVO.item]
 * @param onlineOnly true 时过滤掉从未上架过的作品（收益/分析类查询无收益数据，无需展示）
 */
class GetResourceListUseCase(
    private val resourceRepository: ResourceRepository
) {
    suspend operator fun invoke(
        platform: String,
        onlineOnly: Boolean = false
    ): NetworkState<List<ResourceData>> {
        return when (val result = resourceRepository.getResources(platform)) {
            is NetworkState.Success -> NetworkState.Success(
                (result.data?.item ?: emptyList()).filter { !onlineOnly || it.hasEverBeenOnline() }
            )

            is NetworkState.Error -> NetworkState.Error(result.msg, result.e)
        }
    }
}
