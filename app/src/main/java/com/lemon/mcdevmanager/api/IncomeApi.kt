package com.lemon.mcdevmanager.api

import com.lemon.mcdevmanager.data.AddCookiesInterceptor
import com.lemon.mcdevmanager.data.CommonInterceptor
import com.lemon.mcdevmanager.data.common.JSONConverter
import com.lemon.mcdevmanager.data.common.NETEASE_MC_DEV_LINK
import com.lemon.mcdevmanager.utils.NoNeedData
import com.lemon.mcdevmanager.utils.ResponseData
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.PUT
import java.util.concurrent.TimeUnit

interface IncomeApi {

    // 结算收益
    @PUT("/incomes/apply")
    suspend fun applyIncome(
        @Body request: RequestBody
    ): ResponseData<NoNeedData>

    companion object {
        /**
         * 获取接口实例用于调用对接方法
         * @return ServerApi
         */
        fun create(): IncomeApi {
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
                .create(IncomeApi::class.java)
        }
    }
}