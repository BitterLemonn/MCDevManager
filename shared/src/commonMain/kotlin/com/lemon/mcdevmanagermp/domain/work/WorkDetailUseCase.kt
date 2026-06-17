package com.lemon.mcdevmanagermp.domain.work

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailVO
import com.lemon.mcdevmanagermp.domain.resource.ResourceRepository

/**
 * 作品详情 UseCase：封装获取作品详情逻辑。
 *
 * 写操作（更新作品 / 上传资源包 / 上传图片等）接口尚未掌握，本期仅提供读取。
 */
class WorkDetailUseCase(
    private val resourceRepository: ResourceRepository
) {
    suspend fun getResourceDetail(itemId: String): NetworkState<ResourceDetailVO> {
        return resourceRepository.getResourceDetail(itemId)
    }
}
