package com.lemon.mcdevmanagermp.domain.activity

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.common.NoNeedData
import com.lemon.mcdevmanagermp.data.dto.netease.activity.JoinActivityDTO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ActivityItemsVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.CandidatesVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ReviewActivityVO

interface ActivityRepository {
    suspend fun getReviewActivity(start: Int, span: Int = 10): NetworkState<ReviewActivityVO>
    suspend fun getActivityCandidates(
        activityId: String,
        modulesId: String
    ): NetworkState<CandidatesVO>

    suspend fun getActivityItems(
        activityId: String,
        modulesId: String
    ): NetworkState<ActivityItemsVO>

    suspend fun joinActivity(
        activityId: String,
        modulesId: String,
        content: JoinActivityDTO
    ): NetworkState<NoNeedData>
}
