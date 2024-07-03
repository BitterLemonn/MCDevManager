package com.lemon.mcdevmanager.api

import com.lemon.mcdevmanager.data.AddCookiesInterceptor
import com.lemon.mcdevmanager.data.CommonInterceptor
import com.lemon.mcdevmanager.data.common.JSONConverter
import com.lemon.mcdevmanager.data.common.NETEASE_MC_DEV_LINK
import com.lemon.mcdevmanager.data.netease.feedback.FeedbackResponseBean
import com.lemon.mcdevmanager.data.netease.feedback.ReplyBean
import com.lemon.mcdevmanager.utils.NoNeedData
import com.lemon.mcdevmanager.utils.ResponseData
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface FeedbackApi {

    @GET("/items/feedback/pe/")
    suspend fun loadFeedback(
        @Query("start") from: Int,
        @Query("span") size: Int,
        @Query("sort") sort: String? = null,
        @Query("order") order: String? = null,
        @Query("type") status: String? = null,
        @Query("fuzzy_key") key: String? = null,
        @Query("reply_count") replyCount: Int? = null
    ): ResponseData<FeedbackResponseBean>

    @PUT("/items/feedback/pe/{id}/reply")
    suspend fun sendReply(
        @Path("id") feedbackId: String,
        @Body content: RequestBody
    ): ResponseData<NoNeedData>

    companion object {
        /**
         * 获取接口实例用于调用对接方法
         * @return ServerApi
         */
        fun create(): FeedbackApi {
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(AddCookiesInterceptor())
                .addInterceptor(CommonInterceptor())
                .build()
            return Retrofit.Builder()
                .baseUrl(NETEASE_MC_DEV_LINK)
                .addConverterFactory(
                    JSONConverter.asConverterFactory(
                        "application/json; charset=UTF8".toMediaTypeOrNull()!!
                    )
                )
                .client(client)
                .build()
                .create(FeedbackApi::class.java)
        }
    }
}