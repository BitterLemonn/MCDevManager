package com.lemon.mcdevmanagermp.domain.activity

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.common.NoNeedData
import com.lemon.mcdevmanagermp.data.dto.netease.activity.CancelJoinDiscountDTO
import com.lemon.mcdevmanagermp.data.dto.netease.activity.JoinActivityDTO
import com.lemon.mcdevmanagermp.data.dto.netease.activity.JoinDiscountDTO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ActivityCandidatesVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ActivityItemsVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ActivityReviewVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.DiscountActivityVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.DiscountCandidatesVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.DiscountItemsVO

interface ActivityRepository {
    suspend fun getReviewActivity(start: Int, span: Int = 10): NetworkState<ActivityReviewVO>
    suspend fun getActivityCandidates(
        activityId: String,
        modulesId: String
    ): NetworkState<ActivityCandidatesVO>

    suspend fun getActivityItems(
        activityId: String,
        modulesId: String
    ): NetworkState<ActivityItemsVO>

    suspend fun joinActivity(
        activityId: String,
        modulesId: String,
        content: JoinActivityDTO
    ): NetworkState<NoNeedData>

    // ===== 折扣特卖 =====

    suspend fun getDiscountActivity(): NetworkState<DiscountActivityVO>

    suspend fun getDiscountCandidates(
        activityId: String,
        moduleId: String
    ): NetworkState<DiscountCandidatesVO>

    suspend fun getDiscountJoinedItems(moduleId: String): NetworkState<DiscountItemsVO>

    suspend fun joinDiscount(content: JoinDiscountDTO): NetworkState<NoNeedData>

    suspend fun cancelDiscountJoin(content: CancelJoinDiscountDTO): NetworkState<NoNeedData>
}
