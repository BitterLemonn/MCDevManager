package com.lemon.mcdevmanagermp.data.api

import com.lemon.mcdevmanagermp.data.consts.NETEASE_UPLOAD_LINK
import com.lemon.mcdevmanagermp.data.vo.netease.upload.UploadFileVO
import de.jensklingenberg.ktorfit.http.Multipart
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Part
import io.ktor.http.content.PartData

interface UploadApi {

    @Multipart
    @POST("file/new")
    suspend fun uploadFile(
        @Part("Authorization") auth: String,
        @Part("") fpfile: List<PartData>
    ): UploadFileVO

    companion object {
        val INSTANCE by lazy {
            ApiFactory.provideUploadKtorfit(NETEASE_UPLOAD_LINK).createUploadApi()
        }
    }
}
