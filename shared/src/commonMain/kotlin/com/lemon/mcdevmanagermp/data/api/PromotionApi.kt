package com.lemon.mcdevmanagermp.data.api

import com.lemon.mcdevmanagermp.data.common.NoNeedData
import com.lemon.mcdevmanagermp.data.common.ResponseData
import com.lemon.mcdevmanagermp.data.consts.NETEASE_MC_DEV_LINK
import com.lemon.mcdevmanagermp.data.consts.TRAILING_SLASH_MARKER
import com.lemon.mcdevmanagermp.data.dto.netease.promotion.ApplyPromotionDTO
import com.lemon.mcdevmanagermp.data.vo.netease.promotion.CanApplyPromotionVO
import com.lemon.mcdevmanagermp.data.vo.netease.promotion.UserApplyVO
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

interface PromotionApi {

    @GET("promotion-banner/pe/can_apply")
    suspend fun getCanApplyPromotion(): ResponseData<CanApplyPromotionVO>

    @GET("promotion-banner/user-applications")
    suspend fun getUserApply(
        @Query("start") start: Int,
        @Query("span") span: Int = 10,
        @Query("platform") platform: String = "pe",
        @Query("type") type: String = "apply"
    ): ResponseData<UserApplyVO>

    @POST("promotion-banner/apply/")
    @Headers("$TRAILING_SLASH_MARKER: true")
    suspend fun applyPromotion(
        @Body content: ApplyPromotionDTO
    ): ResponseData<NoNeedData>

    @PUT("promotion-banner/apply/modify/{applicationId}")
    suspend fun modifyApplyPromotion(
        @Path("applicationId") applicationId: String,
        @Body content: ApplyPromotionDTO
    ): ResponseData<NoNeedData>

    companion object {
        val INSTANCE by lazy {
            ApiFactory.provideKtorfit(NETEASE_MC_DEV_LINK).createPromotionApi()
        }
    }
}