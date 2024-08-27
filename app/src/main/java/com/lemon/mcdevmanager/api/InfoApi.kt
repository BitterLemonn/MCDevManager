package com.lemon.mcdevmanager.api

import com.lemon.mcdevmanager.data.AddCookiesInterceptor
import com.lemon.mcdevmanager.data.CommonInterceptor
import com.lemon.mcdevmanager.data.common.JSONConverter
import com.lemon.mcdevmanager.data.common.NETEASE_MC_DEV_LINK
import com.lemon.mcdevmanager.data.netease.resource.ResourceResponseBean
import com.lemon.mcdevmanager.data.netease.user.LevelInfoBean
import com.lemon.mcdevmanager.data.netease.user.OverviewBean
import com.lemon.mcdevmanager.data.netease.user.UserInfoBean
import com.lemon.mcdevmanager.utils.ResponseData
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface InfoApi {

    @GET("/users/me")
    fun getUserInfo(): Call<ResponseData<UserInfoBean>>

    @GET("/data_analysis/overview")
    fun getOverview(): Call<ResponseData<OverviewBean>>

    @GET("/new_level")
    fun getLevelInfo(): Call<ResponseData<LevelInfoBean>>

    @GET("/items/categories/{platform}")
    suspend fun getResInfoList(
        @Path("platform") platform: String = "pe",
        @Query("start") start: Int = 0,
        @Query("span") span: Int = Int.MAX_VALUE
    ): ResponseData<ResourceResponseBean>

    companion object {
        /**
         * 获取接口实例用于调用对接方法
         * @return ServerApi
         */
        fun create(): InfoApi {
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
                .create(InfoApi::class.java)
        }
    }
}