package com.lemon.mcdevmanagermp.data.api

import com.lemon.mcdevmanagermp.data.api.ApiFactory.provideDownloadKtorfit
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Streaming
import de.jensklingenberg.ktorfit.http.Url
import io.ktor.client.statement.HttpStatement
import kotlin.getValue

interface DownloadApi {

    @Streaming
    @GET
    suspend fun downloadFile(@Url fileUrl: String): HttpStatement

    companion object {
        val INSTANCE by lazy {
            provideDownloadKtorfit().createDownloadApi()
        }
    }
}