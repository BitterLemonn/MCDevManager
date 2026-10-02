package com.lemon.mcdevmanagermp.domain.resource

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData

/**
 * 获取指定平台资源列表：解包 [com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceListVO.item]
 * @param onlineOnly true 时过滤掉从未上架过的作品（收益/分析类查询无收益数据，无需展示）
 * @param excludePrerequisites true 时过滤掉前置模组（pri_type=9，平台强制免费、不产生收益）。
 *   收益/分析类统计需置 true；作品上架管理需保留前置模组以便编辑，保持默认 false。
 */
class GetResourceListUseCase(
    private val resourceRepository: ResourceRepository
) {
    suspend operator fun invoke(
        platform: String,
        onlineOnly: Boolean = false,
        excludePrerequisites: Boolean = false
    ): NetworkState<List<ResourceData>> {
        return when (val result = resourceRepository.getResources(platform)) {
            is NetworkState.Success -> NetworkState.Success(
                filterResourceList(result.data?.item ?: emptyList(), onlineOnly, excludePrerequisites)
            )

            is NetworkState.Error -> NetworkState.Error(result.msg, result.e)
        }
    }
}

/** 资源列表过滤：按需剔除未上架作品与前置模组。 */
internal fun filterResourceList(
    items: List<ResourceData>,
    onlineOnly: Boolean,
    excludePrerequisites: Boolean
): List<ResourceData> = items.filter { item ->
    (!onlineOnly || item.hasEverBeenOnline()) && (!excludePrerequisites || !item.isPrerequisite())
}
