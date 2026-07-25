package com.lemon.mcdevmanagermp.data.api

import com.lemon.mcdevmanagermp.data.common.ResponseData
import com.lemon.mcdevmanagermp.data.consts.NETEASE_MC_DEV_LINK
import com.lemon.mcdevmanagermp.data.dto.netease.feedback.ReplyDTO
import com.lemon.mcdevmanagermp.data.vo.netease.comment.CommentListVO
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

interface CommentApi {

    @GET("items/comment/pe/")
    suspend fun getCommentList(
        @Query("start") start: Int = 0,
        @Query("span") span: Int = 20,
        @Query("fuzzy_key") key: String? = null,
        @Query("comment_tag") tag: String? = null,
        @Query("comment_state") state: Int? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): ResponseData<CommentListVO>

    @PUT("items/comment/pe/{id}/reply")
    suspend fun replyComment(
        @Path("id") commentId: String,
        @Body content: ReplyDTO
    ): ResponseData<Unit>

    companion object {
        val INSTANCE: CommentApi by lazy {
            ApiFactory.provideKtorfit(NETEASE_MC_DEV_LINK).createCommentApi()
        }
    }
}