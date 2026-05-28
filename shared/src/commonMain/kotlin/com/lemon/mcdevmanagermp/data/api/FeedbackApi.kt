package com.lemon.mcdevmanagermp.data.api

import com.lemon.mcdevmanagermp.data.consts.NETEASE_MC_DEV_LINK
import com.lemon.mcdevmanagermp.data.common.NoNeedData
import com.lemon.mcdevmanagermp.data.common.ResponseData
import com.lemon.mcdevmanagermp.data.dto.netease.feedback.DeveloperFeedbackDTO
import com.lemon.mcdevmanagermp.data.vo.netease.feedback.DeveloperFeedbackVO
import com.lemon.mcdevmanagermp.data.vo.netease.feedback.FeedbackVO
import com.lemon.mcdevmanagermp.data.dto.netease.feedback.ReplyDTO
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

interface FeedbackApi {

    @GET("/items/feedback/pe/")
    suspend fun loadFeedback(
        @Query("start") from: Int,
        @Query("span") size: Int,
        @Query("sort") sort: String? = null,
        @Query("order") order: String? = null,
        @Query("type") status: String? = null,
        @Query("fuzzy_key") key: String? = null,
        @Query("reply_count") replyCount: Int? = null
    ): ResponseData<FeedbackVO>

    @PUT("/items/feedback/pe/{id}/reply")
    suspend fun sendReply(
        @Path("id") feedbackId: String,
        @Body content: ReplyDTO
    ): ResponseData<NoNeedData>

    @POST("/developer/feedback/add_feedback")
    suspend fun seedFeedback(@Body feedbackBean: DeveloperFeedbackDTO): ResponseData<DeveloperFeedbackVO>

    companion object {
        val INSTANCE: FeedbackApi by lazy {
            ApiFactory.provideKtorfit(NETEASE_MC_DEV_LINK).createFeedbackApi()
        }
    }
}