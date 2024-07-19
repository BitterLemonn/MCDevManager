package com.lemon.mcdevmanager.api

import com.lemon.mcdevmanager.data.AddCookiesInterceptor
import com.lemon.mcdevmanager.data.CommonInterceptor
import com.lemon.mcdevmanager.data.common.GITHUB_RESTFUL_LINK
import com.lemon.mcdevmanager.data.common.JSONConverter
import com.lemon.mcdevmanager.data.github.update.LatestReleaseBean
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface GithubUpdateApi {

    @GET("/repos/{author}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("author") author: String = "BitterLemonn",
        @Path("repo") repo: String = "MCDevManager"
    ): LatestReleaseBean

    companion object {
        /**
         * 获取接口实例用于调用对接方法
         * @return ServerApi
         */
        fun create(): GithubUpdateApi {
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(AddCookiesInterceptor())
                .addInterceptor(CommonInterceptor())
                .build()
            return Retrofit.Builder()
                .baseUrl(GITHUB_RESTFUL_LINK)
                .addConverterFactory(
                    JSONConverter.asConverterFactory(
                        "application/json; charset=UTF8".toMediaTypeOrNull()!!
                    )
                )
                .client(client)
                .build()
                .create(GithubUpdateApi::class.java)
        }
    }
}