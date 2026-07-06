package com.lemon.mcdevmanagermp.data.repository

import com.lemon.mcdevmanagermp.data.api.ActivityApi
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
import com.lemon.mcdevmanagermp.domain.activity.ActivityRepository
import com.lemon.mcdevmanagermp.utils.UnifiedExceptionHandler

class ActivityRepositoryImpl : ActivityRepository {
    companion object {
        val INSTANCE by lazy { ActivityRepositoryImpl() }
        private val activityApi = ActivityApi.INSTANCE
    }

    override suspend fun getReviewActivity(start: Int, span: Int): NetworkState<ActivityReviewVO> {
        return UnifiedExceptionHandler.handleRequest {
            activityApi.getReviewActivity(start = start, span = span)
        }
    }

    override suspend fun getActivityCandidates(
        activityId: String,
        modulesId: String
    ): NetworkState<ActivityCandidatesVO> {
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

    override suspend fun getDiscountActivity(): NetworkState<DiscountActivityVO> {
        return UnifiedExceptionHandler.handleRequest {
            activityApi.getDiscountActivity()
        }
    }

    override suspend fun getDiscountCandidates(
        activityId: String,
        moduleId: String
    ): NetworkState<DiscountCandidatesVO> {
        return UnifiedExceptionHandler.handleRequest {
            activityApi.getDiscountCandidates(activityId = activityId, moduleId = moduleId)
        }
    }

    override suspend fun getDiscountJoinedItems(moduleId: String): NetworkState<DiscountItemsVO> {
        return UnifiedExceptionHandler.handleRequest {
            activityApi.getDiscountJoinedItems(moduleId = moduleId)
        }
    }

    override suspend fun joinDiscount(content: JoinDiscountDTO): NetworkState<NoNeedData> {
        return UnifiedExceptionHandler.handleRequest {
            activityApi.joinDiscount(content = content)
        }
    }

    override suspend fun cancelDiscountJoin(content: CancelJoinDiscountDTO): NetworkState<NoNeedData> {
        return UnifiedExceptionHandler.handleRequest {
            activityApi.cancelDiscountJoin(content = content)
        }
    }
}
