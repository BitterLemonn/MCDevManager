package com.lemon.mcdevmanager.api

import com.lemon.mcdevmanager.data.AddCookiesInterceptor
import com.lemon.mcdevmanager.data.CommonInterceptor
import com.lemon.mcdevmanager.data.common.JSONConverter
import com.lemon.mcdevmanager.data.common.NETEASE_LOGIN_LINK
import com.lemon.mcdevmanager.data.netease.login.BaseLoginBean
import com.lemon.mcdevmanager.data.netease.login.CapIdBean
import com.lemon.mcdevmanager.data.netease.login.EncParams
import com.lemon.mcdevmanager.data.netease.login.PowerBean
import com.lemon.mcdevmanager.data.netease.login.TicketBean
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface LoginApi {

    @POST("/dl/zj/mail/ini")
    suspend fun init(@Body encParams: EncParams): CapIdBean

    @POST("/dl/zj/mail/powGetP")
    suspend fun getPower(@Body encParams: EncParams): PowerBean

    @POST("/dl/zj/mail/gt")
    suspend fun getTicket(@Body encParams: EncParams): TicketBean

    @POST("/dl/zj/mail/l")
    suspend fun safeLogin(@Body encParams: EncParams): BaseLoginBean

    companion object {
        /**
         * 获取接口实例用于调用对接方法
         * @return ServerApi
         */
        fun create(): LoginApi {
            val client = OkHttpClient.Builder()
                .addInterceptor(AddCookiesInterceptor())
                .addInterceptor(CommonInterceptor())
                .build()
            return Retrofit.Builder()
                .baseUrl(NETEASE_LOGIN_LINK)
                .addConverterFactory(
                    JSONConverter.asConverterFactory(
                        "application/json; charset=UTF8".toMediaTypeOrNull()!!
                    )
                )
                .client(client)
                .build()
                .create(LoginApi::class.java)
        }
    }
}

