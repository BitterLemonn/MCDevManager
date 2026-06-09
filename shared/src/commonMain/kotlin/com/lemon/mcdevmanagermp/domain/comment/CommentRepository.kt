package com.lemon.mcdevmanagermp.domain.comment

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.dto.netease.feedback.ReplyDTO
import com.lemon.mcdevmanagermp.data.vo.netease.comment.CommentListVO

interface CommentRepository {
    suspend fun getCommentList(
        start: Int = 0,
        span: Int = 20,
        key: String? = null,
        tag: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): NetworkState<CommentListVO>

    suspend fun replyComment(commentId: String, content: ReplyDTO): NetworkState<Unit>
}
