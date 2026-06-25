package com.lemon.mcdevmanagermp.domain.work

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import com.lemon.mcdevmanagermp.domain.resource.GetResourceListUseCase

/**
 * 作品上架管理 UseCase：封装作品列表获取逻辑。
 *
 * 注意：提交审核 / 上架 / 更新等写操作的网易接口尚未掌握，
 * 当前 UI 层操作为占位实现。接口补齐后在下方新增 submitForReview / publish / update 等方法即可。
 */
class WorkManageUseCase(
    private val getResourceListUseCase: GetResourceListUseCase
) {
    /**
     * 获取指定平台的作品列表（全量返回，作品数量通常较少无需分页）
     */
    suspend fun getWorkList(platform: String = "pe"): NetworkState<List<ResourceData>> {
        return getResourceListUseCase(platform)
    }
}
