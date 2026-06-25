package com.lemon.mcdevmanagermp.data.api

import com.lemon.mcdevmanagermp.data.common.NoNeedData
import com.lemon.mcdevmanagermp.data.common.ResponseData
import com.lemon.mcdevmanagermp.data.consts.NETEASE_MC_DEV_LINK
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ItemTagVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.MCConstsVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceListVO
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

interface ResourceApi {
    @GET("items/categories/{platform}/")
    suspend fun getAllResource(
        @Path("platform") platform: String = "pe",
        @Query("start") start: Int = 0,
        @Query("span") span: Int = Int.MAX_VALUE,
        @Query("mc_status") mcStatus: Int? = null
    ): ResponseData<ResourceListVO>

    @GET("items/categories/pe/{itemId}")
    suspend fun getResourceDetail(@Path("itemId") itemId: String): ResponseData<ResourceDetailVO>

    @POST("items/categories/pe/{itemId}/cancel_review")
    suspend fun cancelReview(@Path("itemId") itemId: String): ResponseData<NoNeedData>

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