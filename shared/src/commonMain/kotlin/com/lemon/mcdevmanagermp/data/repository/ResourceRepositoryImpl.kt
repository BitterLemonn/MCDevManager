package com.lemon.mcdevmanagermp.data.repository

import com.lemon.mcdevmanagermp.data.api.ResourceApi
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.common.NoNeedData
import com.lemon.mcdevmanagermp.data.dto.netease.work.ApplyReviewDTO
import com.lemon.mcdevmanagermp.data.dto.netease.work.ApplySelfTestDTO
import com.lemon.mcdevmanagermp.data.dto.netease.work.AppointOnlineDTO
import com.lemon.mcdevmanagermp.data.dto.netease.work.ChangePriceDTO
import com.lemon.mcdevmanagermp.data.dto.netease.work.OnlineItemDTO
import com.lemon.mcdevmanagermp.data.dto.netease.work.WorkCreateDTO
import com.lemon.mcdevmanagermp.data.dto.netease.work.WorkUpdateDTO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ItemTagVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.MCConstsVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.RequirementVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceListVO
import com.lemon.mcdevmanagermp.data.vo.netease.work.ReviewApplyResultVO
import com.lemon.mcdevmanagermp.data.vo.netease.work.ReviewFeedbackVO
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
    ): NetworkState<ResourceListVO> =
        UnifiedExceptionHandler.handleRequest {
            resourceApi.getAllResource(
                platform = platform,
                itemName = itemName,
                mcStatus = mcStatus
            )
        }

    override suspend fun getCompRequirements(itemName: String): NetworkState<RequirementVO> =
        UnifiedExceptionHandler.handleRequest { resourceApi.getRequirements(itemName) }

    override suspend fun getResourceDetail(itemId: String): NetworkState<ResourceDetailVO> =
        UnifiedExceptionHandler.handleRequest { resourceApi.getResourceDetail(itemId) }

    override suspend fun deleteItem(itemId: String): NetworkState<NoNeedData> =
        UnifiedExceptionHandler.handleRequest { resourceApi.deleteItem(itemId) }

    override suspend fun getItemTag(): NetworkState<ItemTagVO> =
        UnifiedExceptionHandler.handleRequest { resourceApi.getItemTag() }

    override suspend fun getMCConsts(): NetworkState<MCConstsVO> =
        UnifiedExceptionHandler.handleRequest { resourceApi.getMCConsts() }

    override suspend fun updateItem(itemId: String, item: WorkUpdateDTO): NetworkState<NoNeedData> =
        UnifiedExceptionHandler.handleRequest { resourceApi.updateItem(itemId, item) }

    override suspend fun createItem(body: WorkCreateDTO): NetworkState<NoNeedData> =
        UnifiedExceptionHandler.handleRequest { resourceApi.createItem(body) }

    override suspend fun applyReview(
        itemId: String,
        content: ApplyReviewDTO
    ): NetworkState<ReviewApplyResultVO> =
        UnifiedExceptionHandler.handleRequest { resourceApi.applyReview(itemId, content) }

    override suspend fun cancelReview(itemId: String): NetworkState<NoNeedData> =
        UnifiedExceptionHandler.handleRequest { resourceApi.cancelReview(itemId) }

    override suspend fun getReviewFeedback(itemId: String): NetworkState<ReviewFeedbackVO> =
        UnifiedExceptionHandler.handleRequest { resourceApi.getReviewFeedback(itemId) }

    override suspend fun applySelfTest(
        itemId: String,
        content: ApplySelfTestDTO
    ): NetworkState<NoNeedData> =
        UnifiedExceptionHandler.handleRequest { resourceApi.applySelfTest(itemId, content) }

    override suspend fun cancelSelfTest(itemId: String): NetworkState<NoNeedData> =
        UnifiedExceptionHandler.handleRequest { resourceApi.cancelSelfTest(itemId) }

    override suspend fun changePrice(
        itemId: String,
        content: ChangePriceDTO
    ): NetworkState<NoNeedData> =
        UnifiedExceptionHandler.handleRequest { resourceApi.changePrice(itemId, content) }

    override suspend fun onlineItem(
        itemId: String,
        content: OnlineItemDTO
    ): NetworkState<NoNeedData> =
        UnifiedExceptionHandler.handleRequest { resourceApi.onlineItem(itemId, content) }

    override suspend fun appointOnlineItem(
        itemId: String,
        content: AppointOnlineDTO
    ): NetworkState<NoNeedData> =
        UnifiedExceptionHandler.handleRequest { resourceApi.appointOnlineItem(itemId, content) }
}
