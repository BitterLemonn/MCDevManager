package com.lemon.mcdevmanagermp.data.api

import com.lemon.mcdevmanagermp.data.api.ApiFactory.provideKtorfit
import com.lemon.mcdevmanagermp.data.common.ResponseData
import com.lemon.mcdevmanagermp.data.consts.NETEASE_MC_DEV_LINK
import com.lemon.mcdevmanagermp.data.vo.netease.other.RedSpotsVO
import de.jensklingenberg.ktorfit.http.GET

interface OtherApi {
    @GET("others/red-spots")
    suspend fun getRedSpots(): ResponseData<RedSpotsVO>

    companion object {
        val INSTANCE by lazy {
            provideKtorfit(NETEASE_MC_DEV_LINK).createOtherApi()
        }
    }
}
