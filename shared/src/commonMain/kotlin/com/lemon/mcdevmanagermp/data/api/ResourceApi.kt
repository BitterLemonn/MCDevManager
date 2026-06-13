package com.lemon.mcdevmanagermp.data.api

import com.lemon.mcdevmanagermp.data.common.ResponseData
import com.lemon.mcdevmanagermp.data.consts.NETEASE_MC_DEV_LINK
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailVO
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path

interface ResourceApi {

    @GET("/items/categories/pe/{itemId}")
    suspend fun getResourceDetail(@Path("itemId") itemId: String): ResponseData<ResourceDetailVO>

    companion object {
        val INSTANCE: ResourceApi by lazy {
            ApiFactory.provideKtorfit(NETEASE_MC_DEV_LINK).createResourceApi()
        }
    }
}