package com.lemon.mcdevmanagermp.data.api

import com.lemon.mcdevmanagermp.data.api.ApiFactory.provideKtorfit
import com.lemon.mcdevmanagermp.data.common.NoNeedData
import com.lemon.mcdevmanagermp.data.common.ResponseData
import com.lemon.mcdevmanagermp.data.consts.NETEASE_MC_DEV_LINK
import com.lemon.mcdevmanagermp.data.dto.netease.activity.CancelJoinDiscountDTO
import com.lemon.mcdevmanagermp.data.dto.netease.activity.JoinActivityDTO
import com.lemon.mcdevmanagermp.data.dto.netease.activity.JoinDiscountDTO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ActivityCandidatesVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ActivityItemsVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ActivityReviewVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.DiscountActivityVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.DiscountCandidatesVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.DiscountItemsVO
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
    ): ResponseData<ActivityReviewVO>

    /**
     * 获取作品活动候选模组列表
     */
    @GET("$NETEASE_ACTIVITY_BASE_INTERFACE/{activityId}/modules/{modulesId}/candidates")
    suspend fun getActivityCandidates(
        @Path("activityId") activityId: String,
        @Path("modulesId") modulesId: String
    ): ResponseData<ActivityCandidatesVO>

    /**
     * 获取参与作品活动模组信息
     */
    @GET("$NETEASE_ACTIVITY_BASE_INTERFACE/{activityId}/modules/{modulesId}/items")
    suspend fun getActivityItems(
        @Path("activityId") activityId: String,
        @Path("modulesId") modulesId: String
    ): ResponseData<ActivityItemsVO>

    /**
     * 参与作品活动
     */
    @POST("$NETEASE_ACTIVITY_BASE_INTERFACE/{activityId}/modules/{modulesId}/join")
    suspend fun joinActivity(
        @Path("activityId") activityId: String,
        @Path("modulesId") modulesId: String,
        @Body content: JoinActivityDTO
    ): ResponseData<NoNeedData>

    /**
     * 获取折扣特卖
     */
    @GET("activities/discount_activities/current")
    suspend fun getDiscountActivity(): ResponseData<DiscountActivityVO>

    /**
     * 获取能够参与折扣特卖的项目列表
     */
    @GET("activities/discount_activities/candidates")
    suspend fun getDiscountCandidates(
        @Query("activity_id") activityId: String,
        @Query("module_id") moduleId: String
    ): ResponseData<DiscountCandidatesVO>

    /**
     * 获取已参加折扣特卖的项目
     */
    @GET("activities/discount_activities/current/modules/{moduleId}/items")
    suspend fun getDiscountJoinedItems(
        @Path("moduleId") moduleId: String
    ): ResponseData<DiscountItemsVO>

    /**
     * 参加折扣特卖
     */
    @POST("activities/discount_activities/join")
    suspend fun joinDiscount(
        @Body content: JoinDiscountDTO
    ): ResponseData<NoNeedData>


    /**
     * 取消参加折扣特卖
     */
    @POST("activities/discount_activities/cancel_join")
    suspend fun cancelDiscountJoin(
        @Body content: CancelJoinDiscountDTO
    ): ResponseData<NoNeedData>


    companion object {
        const val NETEASE_ACTIVITY_BASE_INTERFACE = "activities/pe-review-activities"
        val INSTANCE by lazy {
            provideKtorfit(NETEASE_MC_DEV_LINK).createActivityApi()
        }
    }
}