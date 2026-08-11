package com.lemon.mcdevmanagermp.data.api

import com.lemon.mcdevmanagermp.data.common.NoNeedData
import com.lemon.mcdevmanagermp.data.common.ResponseData
import com.lemon.mcdevmanagermp.data.consts.NETEASE_MC_DEV_LINK
import com.lemon.mcdevmanagermp.data.dto.netease.work.ApplyReviewDTO
import com.lemon.mcdevmanagermp.data.dto.netease.work.ApplySelfTestDTO
import com.lemon.mcdevmanagermp.data.dto.netease.work.AppointOnlineDTO
import com.lemon.mcdevmanagermp.data.dto.netease.work.ChangePriceDTO
import com.lemon.mcdevmanagermp.data.dto.netease.work.OnlineItemDTO
import com.lemon.mcdevmanagermp.data.dto.netease.work.WorkCreateDTO
import com.lemon.mcdevmanagermp.data.dto.netease.work.WorkUpdateDTO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ItemTagVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.MCConstsVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.RequirementVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceListVO
import com.lemon.mcdevmanagermp.data.vo.netease.work.ReviewApplyResultVO
import com.lemon.mcdevmanagermp.data.vo.netease.work.ReviewFeedbackVO
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

interface ResourceApi {
    @GET("items/categories/{platform}")
    suspend fun getAllResource(
        @Path("platform") platform: String = "pe",
        @Query("start") start: Int = 0,
        @Query("span") span: Int = Int.MAX_VALUE,
        @Query("item_name") itemName: String? = null,
        @Query("mc_status") mcStatus: Int? = null
    ): ResponseData<ResourceListVO>

    @GET("/items/categories/comp/requirements")
    suspend fun getRequirements(@Query("query_str") itemName: String): ResponseData<RequirementVO>

    @GET("items/categories/pe/{itemId}")
    suspend fun getResourceDetail(@Path("itemId") itemId: String): ResponseData<ResourceDetailVO>

    @DELETE("items/categories/pe/{itemId}")
    suspend fun deleteItem(@Path("itemId") itemId: String): ResponseData<NoNeedData>

    @POST("items/categories/pe/{itemId}/update")
    suspend fun updateItem(
        @Path("itemId") itemId: String,
        @Body item: WorkUpdateDTO
    ): ResponseData<NoNeedData>

    @POST("items/categories/pe/upload")
    suspend fun createItem(@Body body: WorkCreateDTO): ResponseData<NoNeedData>

    @PUT("items/categories/pe/{itemId}/apply_review")
    suspend fun applyReview(
        @Path("itemId") itemId: String,
        @Body content: ApplyReviewDTO
    ): ResponseData<ReviewApplyResultVO>

    @PUT("items/categories/pe/{itemId}/cancel_review")
    suspend fun cancelReview(@Path("itemId") itemId: String): ResponseData<NoNeedData>

    @PUT("items/categories/pe/{itemId}/self-test-apply")
    suspend fun applySelfTest(
        @Path("itemId") itemId: String,
        @Body content: ApplySelfTestDTO
    ): ResponseData<NoNeedData>

    @PUT("items/categories/pe/{itemId}/cancel_self_test")
    suspend fun cancelSelfTest(@Path("itemId") itemId: String): ResponseData<NoNeedData>

    @GET("items/categories/pe/{itemId}/feedback")
    suspend fun getReviewFeedback(@Path("itemId") itemId: String): ResponseData<ReviewFeedbackVO>

    @PUT("items/categories/pe/{itemId}/online")
    suspend fun onlineItem(
        @Path("itemId") itemId: String,
        @Body content: OnlineItemDTO
    ): ResponseData<NoNeedData>

    @PUT("items/categories/pe/{itemId}/appoint_online")
    suspend fun appointOnlineItem(
        @Path("itemId") itemId: String,
        @Body content: AppointOnlineDTO
    ): ResponseData<NoNeedData>

    @POST("items/categories/pe/{itemId}/change_price")
    suspend fun changePrice(
        @Path("itemId") itemId: String,
        @Body content: ChangePriceDTO
    ): ResponseData<NoNeedData>

    @GET("item-tag")
    suspend fun getItemTag(): ResponseData<ItemTagVO>

    @GET("items/mc_consts")
    suspend fun getMCConsts(): ResponseData<MCConstsVO>

    companion object {
        val INSTANCE: ResourceApi by lazy {
            ApiFactory.provideKtorfit(NETEASE_MC_DEV_LINK).createResourceApi()
        }
    }
}
