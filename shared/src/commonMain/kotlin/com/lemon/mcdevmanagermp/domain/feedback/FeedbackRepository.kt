package com.lemon.mcdevmanagermp.domain.feedback

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.common.NoNeedData
import com.lemon.mcdevmanagermp.data.dto.netease.feedback.DeveloperFeedbackDTO
import com.lemon.mcdevmanagermp.data.dto.netease.feedback.ReplyDTO
import com.lemon.mcdevmanagermp.data.vo.netease.feedback.DeveloperFeedbackVO
import com.lemon.mcdevmanagermp.data.vo.netease.feedback.FeedbackVO

interface FeedbackRepository {
    suspend fun loadFeedback(
        from: Int,
        size: Int,
        status: String? = null,
        key: String? = null
    ): NetworkState<FeedbackVO>

    suspend fun sendReply(feedbackId: String, content: ReplyDTO): NetworkState<NoNeedData>

    suspend fun submitFeedback(feedback: DeveloperFeedbackDTO): NetworkState<DeveloperFeedbackVO>
}
