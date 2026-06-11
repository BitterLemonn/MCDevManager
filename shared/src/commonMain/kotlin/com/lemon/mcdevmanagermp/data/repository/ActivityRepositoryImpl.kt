package com.lemon.mcdevmanagermp.data.repository

import com.lemon.mcdevmanagermp.data.api.ActivityApi
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.common.NoNeedData
import com.lemon.mcdevmanagermp.data.dto.netease.activity.JoinActivityDTO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ActivityItemsVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.CandidatesVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ReviewActivityVO
import com.lemon.mcdevmanagermp.domain.activity.ActivityRepository
import com.lemon.mcdevmanagermp.utils.UnifiedExceptionHandler

class ActivityRepositoryImpl : ActivityRepository {
    companion object {
        val INSTANCE by lazy { ActivityRepositoryImpl() }
        private val activityApi = ActivityApi.INSTANCE
    }

    override suspend fun getReviewActivity(start: Int, span: Int): NetworkState<ReviewActivityVO> {
        return UnifiedExceptionHandler.handleRequest {
            activityApi.getReviewActivity(start = start, span = span)
        }
    }

    override suspend fun getActivityCandidates(
        activityId: String,
        modulesId: String
    ): NetworkState<CandidatesVO> {
        return UnifiedExceptionHandler.handleRequest {
            activityApi.getActivityCandidates(activityId = activityId, modulesId = modulesId)
        }
    }

    override suspend fun getActivityItems(
        activityId: String,
        modulesId: String
    ): NetworkState<ActivityItemsVO> {
        return UnifiedExceptionHandler.handleRequest {
            activityApi.getActivityItems(activityId = activityId, modulesId = modulesId)
        }
    }

    override suspend fun joinActivity(
        activityId: String,
        modulesId: String,
        content: JoinActivityDTO
    ): NetworkState<NoNeedData> {
        return UnifiedExceptionHandler.handleRequest {
            activityApi.joinActivity(
                activityId = activityId,
                modulesId = modulesId,
                content = content
            )
        }
    }
}
