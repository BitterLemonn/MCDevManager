package com.lemon.mcdevmanagermp.domain.resource

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.common.NoNeedData
import com.lemon.mcdevmanagermp.data.dto.netease.work.ApplyReviewDTO
import com.lemon.mcdevmanagermp.data.dto.netease.work.AppointOnlineDTO
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

interface ResourceRepository {
    suspend fun getResources(
        platform: String = "pe",
        itemName: String? = null,
        mcStatus: Int? = null
    ): NetworkState<ResourceListVO>

    suspend fun getCompRequirements(itemName: String): NetworkState<RequirementVO>

    suspend fun getResourceDetail(itemId: String): NetworkState<ResourceDetailVO>

    suspend fun getItemTag(): NetworkState<ItemTagVO>

    suspend fun getMCConsts(): NetworkState<MCConstsVO>

    // ===== 开平写操作（第1-7节）=====

    suspend fun updateItem(itemId: String, item: WorkUpdateDTO): NetworkState<NoNeedData>

    /** 新建作品（pe/upload） */
    suspend fun createItem(body: WorkCreateDTO): NetworkState<NoNeedData>

    suspend fun applyReview(
        itemId: String,
        content: ApplyReviewDTO
    ): NetworkState<ReviewApplyResultVO>

    suspend fun cancelReview(itemId: String): NetworkState<NoNeedData>

    suspend fun getReviewFeedback(itemId: String): NetworkState<ReviewFeedbackVO>

    suspend fun onlineItem(itemId: String, content: OnlineItemDTO): NetworkState<NoNeedData>

    suspend fun appointOnlineItem(
        itemId: String,
        content: AppointOnlineDTO
    ): NetworkState<NoNeedData>
}
