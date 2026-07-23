package com.lemon.mcdevmanagermp.data.api

import com.lemon.mcdevmanagermp.data.common.ResponseData
import com.lemon.mcdevmanagermp.data.consts.NETEASE_MC_DEV_LINK
import com.lemon.mcdevmanagermp.data.vo.netease.files.GetFileTokenVO
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query

interface FilesApi {

    @GET("filepicker/file_token")
    suspend fun getFileToken(
        @Query("file_type") fileType: String,
        @Query("secure") secure: String = "false"
    ): ResponseData<GetFileTokenVO>

    companion object {
        val INSTANCE by lazy {
            ApiFactory.provideLoggerKtorfit(NETEASE_MC_DEV_LINK).createFilesApi()
        }
    }
}