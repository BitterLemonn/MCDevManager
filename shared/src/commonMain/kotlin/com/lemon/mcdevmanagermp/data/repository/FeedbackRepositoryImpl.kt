package com.lemon.mcdevmanagermp.data.repository

import com.lemon.mcdevmanagermp.data.api.FeedbackApi
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.common.NoNeedData
import com.lemon.mcdevmanagermp.data.dto.netease.feedback.DeveloperFeedbackDTO
import com.lemon.mcdevmanagermp.data.dto.netease.feedback.ReplyDTO
import com.lemon.mcdevmanagermp.data.vo.netease.feedback.DeveloperFeedbackVO
import com.lemon.mcdevmanagermp.data.vo.netease.feedback.FeedbackVO
import com.lemon.mcdevmanagermp.domain.feedback.FeedbackRepository
import com.lemon.mcdevmanagermp.utils.UnifiedExceptionHandler

class FeedbackRepositoryImpl : FeedbackRepository {
    companion object {
        val INSTANCE by lazy { FeedbackRepositoryImpl() }
        private val feedbackApi = FeedbackApi.INSTANCE
    }

    override suspend fun loadFeedback(
        from: Int,
        size: Int,
        status: String?,
        key: String?
    ): NetworkState<FeedbackVO> {
        return UnifiedExceptionHandler.handleRequest {
            feedbackApi.loadFeedback(
                from = from,
                size = size,
                status = status,
                key = key
            )
        }
    }

    override suspend fun sendReply(feedbackId: String, content: ReplyDTO): NetworkState<NoNeedData> {
        return UnifiedExceptionHandler.handleRequest {
            feedbackApi.sendReply(feedbackId, content)
        }
    }

    override suspend fun submitFeedback(feedback: DeveloperFeedbackDTO): NetworkState<DeveloperFeedbackVO> {
        return UnifiedExceptionHandler.handleRequest {
            feedbackApi.seedFeedback(feedback)
        }
    }
}
