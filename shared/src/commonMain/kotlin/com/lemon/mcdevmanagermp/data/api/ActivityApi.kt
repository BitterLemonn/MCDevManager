package com.lemon.mcdevmanagermp.data.api

import com.lemon.mcdevmanagermp.data.api.ApiFactory.provideLoggerKtorfit
import com.lemon.mcdevmanagermp.data.common.NoNeedData
import com.lemon.mcdevmanagermp.data.common.ResponseData
import com.lemon.mcdevmanagermp.data.consts.NETEASE_MC_DEV_LINK
import com.lemon.mcdevmanagermp.data.dto.netease.activity.JoinActivityDTO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ActivityItemsVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.CandidatesVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ReviewActivityVO
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

interface ActivityApi {

    /**
     * 获取作品活动
     */
    @GET(NETEASE_ACTIVITY_BASE_INTERFACE)
    suspend fun getReviewActivity(
        @Query("start") start: Int,
        @Query("span") span: Int = 10
    ): ResponseData<ReviewActivityVO>

    /**
     * 获取活动候选模组列表
     */
    @GET("$NETEASE_ACTIVITY_BASE_INTERFACE/{activityId}/modules/{modulesId}/candidates")
    suspend fun getActivityCandidates(
        @Path("activityId") activityId: String,
        @Path("modulesId") modulesId: String
    ): ResponseData<CandidatesVO>

    /**
     * 获取参与活动模组信息
     */
    @GET("$NETEASE_ACTIVITY_BASE_INTERFACE/{activityId}/modules/{modulesId}/items")
    suspend fun getActivityItems(
        @Path("activityId") activityId: String,
        @Path("modulesId") modulesId: String
    ): ResponseData<ActivityItemsVO>

    /**
     * 参与活动
     */
    @POST("$NETEASE_ACTIVITY_BASE_INTERFACE/{activityId}/modules/{modulesId}/join")
    suspend fun joinActivity(
        @Path("activityId") activityId: String,
        @Path("modulesId") modulesId: String,
        @Body content: JoinActivityDTO
    ): ResponseData<NoNeedData>

    companion object {
        const val NETEASE_ACTIVITY_BASE_INTERFACE = "/activities/pe-review-activities"
        val INSTANCE by lazy {
            provideLoggerKtorfit(NETEASE_MC_DEV_LINK).createActivityApi()
        }
    }
}