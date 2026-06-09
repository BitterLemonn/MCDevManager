package com.lemon.mcdevmanagermp.domain.feedback

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.common.NoNeedData
import com.lemon.mcdevmanagermp.data.dto.netease.feedback.DeveloperFeedbackDTO
import com.lemon.mcdevmanagermp.data.dto.netease.feedback.ReplyDTO
import com.lemon.mcdevmanagermp.data.vo.netease.feedback.DeveloperFeedbackVO
import com.lemon.mcdevmanagermp.data.vo.netease.feedback.FeedbackVO

/**
 * 反馈 UseCase：封装反馈列表获取与回复逻辑
 */
class FeedbackUseCase(
    private val feedbackRepository: FeedbackRepository
) {
    /**
     * 获取反馈列表
     */
    suspend fun loadFeedback(
        from: Int,
        size: Int,
        status: String? = null,
        key: String? = null
    ): NetworkState<FeedbackVO> {
        return feedbackRepository.loadFeedback(
            from = from,
            size = size,
            status = status,
            key = key
        )
    }

    /**
     * 回复反馈
     */
    suspend fun sendReply(feedbackId: String, content: String): NetworkState<NoNeedData> {
        return feedbackRepository.sendReply(feedbackId, ReplyDTO(content))
    }

    /**
     * 提交开发者反馈
     */
    suspend fun submitFeedback(feedback: DeveloperFeedbackDTO): NetworkState<DeveloperFeedbackVO> {
        return feedbackRepository.submitFeedback(feedback)
    }
}
