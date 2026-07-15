package com.lemon.mcdevmanagermp.domain.work

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.common.NoNeedData
import com.lemon.mcdevmanagermp.data.dto.netease.work.ApplyReviewDTO
import com.lemon.mcdevmanagermp.data.dto.netease.work.AppointOnlineDTO
import com.lemon.mcdevmanagermp.data.dto.netease.work.OnlineItemDTO
import com.lemon.mcdevmanagermp.data.dto.netease.work.toWorkUpdateDTO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.work.ReviewApplyResultVO
import com.lemon.mcdevmanagermp.data.vo.netease.work.ReviewFeedbackVO
import com.lemon.mcdevmanagermp.domain.resource.GetResourceListUseCase
import com.lemon.mcdevmanagermp.domain.resource.ResourceRepository

/**
 * 作品上架管理 UseCase：封装作品列表获取与开平写操作（保存提审 / 提交审核 / 撤销 / 反馈 / 上架 / 定时上架）。
 *
 * 写操作对应开平接口第1-7节。
 */
class WorkManageUseCase(
    private val getResourceListUseCase: GetResourceListUseCase,
    private val resourceRepository: ResourceRepository
) {
    /**
     * 获取指定平台的作品列表（全量返回，作品数量通常较少无需分页）
     */
    suspend fun getWorkList(platform: String = "pe"): NetworkState<List<ResourceData>> {
        return getResourceListUseCase(platform)
    }

    /**
     * 第1/2节 保存作品信息。isCheckApply=true 保存并发起审核，false 仅保存。
     * ponytail: 请求体复用 ResourceDetailVO（详情即编辑表单数据源），后端忽略只读字段；
     *           若后端对 update 字段严格校验，再独立建 WorkUpdateDTO。
     */
    suspend fun updateWork(
        item: ResourceDetailVO,
        isCheckApply: Boolean
    ): NetworkState<NoNeedData> {
        if (item.itemId.isEmpty()) return NetworkState.Error("作品 ID 为空")
        return resourceRepository.updateItem(item.itemId, item.toWorkUpdateDTO(isCheckApply))
    }

    /**
     * 第3节 提交审核。isCheckApply=false 直接提交，true 先校验。
     */
    suspend fun submitForReview(
        itemId: String,
        applyReviewText: String = "",
        conflictNotify: Int = 1,
        conflictNotifyType: List<Int> = listOf(1),
        isCheckApply: Boolean = false
    ): NetworkState<ReviewApplyResultVO> {
        return resourceRepository.applyReview(
            itemId,
            ApplyReviewDTO(applyReviewText, conflictNotify, conflictNotifyType, isCheckApply)
        )
    }

    /**
     * 第4节 撤销审核
     */
    suspend fun cancelReview(itemId: String): NetworkState<NoNeedData> {
        return resourceRepository.cancelReview(itemId)
    }

    /**
     * 第5节 查看审核反馈
     */
    suspend fun getReviewFeedback(itemId: String): NetworkState<ReviewFeedbackVO> {
        return resourceRepository.getReviewFeedback(itemId)
    }

    /**
     * 第6节 上架。opPlatform 默认 "all" 全平台。
     */
    suspend fun publish(
        itemId: String,
        opPlatform: String = "all"
    ): NetworkState<NoNeedData> {
        return resourceRepository.onlineItem(itemId, OnlineItemDTO(opPlatform))
    }

    /**
     * 第7节 定时上架。appointOnlineTime 格式 "YYYY-MM-DD HH:mm:ss"；传 null 取消定时上架。
     */
    suspend fun appointOnline(
        itemId: String,
        appointOnlineTime: String?,
        opPlatform: String = "all"
    ): NetworkState<NoNeedData> {
        return resourceRepository.appointOnlineItem(
            itemId,
            AppointOnlineDTO(appointOnlineTime, opPlatform)
        )
    }
}
