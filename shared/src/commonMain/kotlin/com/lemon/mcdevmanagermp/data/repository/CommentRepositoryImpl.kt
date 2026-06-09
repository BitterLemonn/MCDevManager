package com.lemon.mcdevmanagermp.data.repository

import com.lemon.mcdevmanagermp.data.api.CommentApi
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.dto.netease.feedback.ReplyDTO
import com.lemon.mcdevmanagermp.data.vo.netease.comment.CommentListVO
import com.lemon.mcdevmanagermp.domain.comment.CommentRepository
import com.lemon.mcdevmanagermp.utils.UnifiedExceptionHandler

class CommentRepositoryImpl : CommentRepository {
    companion object {
        val INSTANCE by lazy { CommentRepositoryImpl() }
        private val commentApi = CommentApi.INSTANCE
    }

    override suspend fun getCommentList(
        start: Int,
        span: Int,
        key: String?,
        tag: String?,
        startDate: String?,
        endDate: String?
    ): NetworkState<CommentListVO> {
        return UnifiedExceptionHandler.handleRequest {
            commentApi.getCommentList(
                start = start,
                span = span,
                key = key,
                tag = tag,
                startDate = startDate,
                endDate = endDate
            )
        }
    }

    override suspend fun replyComment(commentId: String, content: ReplyDTO): NetworkState<Unit> {
        return UnifiedExceptionHandler.handleRequest {
            commentApi.replyComment(commentId, content)
        }
    }
}
