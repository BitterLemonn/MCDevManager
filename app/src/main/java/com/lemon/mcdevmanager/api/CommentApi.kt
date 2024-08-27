package com.lemon.mcdevmanager.api

import com.lemon.mcdevmanager.data.AddCookiesInterceptor
import com.lemon.mcdevmanager.data.CommonInterceptor
import com.lemon.mcdevmanager.data.common.JSONConverter
import com.lemon.mcdevmanager.data.common.NETEASE_MC_DEV_LINK
import com.lemon.mcdevmanager.data.netease.comment.CommentList
import com.lemon.mcdevmanager.utils.ResponseData
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface CommentApi {

    @GET("/items/comment/pe/")
    suspend fun getCommentList(
        @Query("start") start: Int = 0,
        @Query("span") span: Int = 20,
        @Query("fuzzy_key") key: String? = null,
        @Query("comment_tag") tag: String? = null,
        @Query("comment_state") state: Int? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): ResponseData<CommentList>

    companion object {
        /**
         * 获取接口实例用于调用对接方法
         * @return CommentApi
         */
        fun create(): CommentApi {
            val client = OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS).addInterceptor(AddCookiesInterceptor())
                .addInterceptor(CommonInterceptor()).build()
            return Retrofit.Builder().baseUrl(NETEASE_MC_DEV_LINK).addConverterFactory(
                JSONConverter.asConverterFactory(
                    "application/json; charset=UTF8".toMediaTypeOrNull()!!
                )
            ).client(client).build().create(CommentApi::class.java)
        }
    }
}