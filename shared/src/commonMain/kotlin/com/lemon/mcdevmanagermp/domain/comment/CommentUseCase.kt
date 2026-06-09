package com.lemon.mcdevmanagermp.domain.comment

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.dto.netease.feedback.ReplyDTO
import com.lemon.mcdevmanagermp.data.vo.netease.comment.CommentListVO

/**
 * 评论 UseCase：封装评论列表获取与回复逻辑
 */
class CommentUseCase(
    private val commentRepository: CommentRepository
) {
    /**
     * 获取评论列表
     */
    suspend fun getCommentList(
        start: Int = 0,
        span: Int = 20,
        key: String? = null,
        tag: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): NetworkState<CommentListVO> {
        return commentRepository.getCommentList(
            start = start,
            span = span,
            key = key,
            tag = tag,
            startDate = startDate,
            endDate = endDate
        )
    }

    /**
     * 回复评论
     */
    suspend fun replyComment(commentId: String, content: String): NetworkState<Unit> {
        return commentRepository.replyComment(commentId, ReplyDTO(content))
    }
}
