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
                        MediaType.parse("application/json; charset=UTF8")!!
                    )
                )
                .client(client)
                .build()
                .create(LoginApi::class.java)
        }
    }
}


//{
//    "un": "yuchu_liu@126.com",
//    "pw": "CKzklt0c/KSwfccBM9DTqD2RtlzzG0Hb5JXCkIojXpGZavrYb1afF3Kew9whVFh+H+MCDjcQbVfTXmBOBw+EYxMOqPRRS8MrZUMrqPqBtWi/zjKIuaKcRiSfIUNdUbC0iueQRMxtVWMN3lrePMvD2z1Si5KBqr8gOJn9ldBFlb0=",
//    "pd": "x19_developer",
//    "l": 0,
//    "d": 10,
//    "t": 1718962642898,
//    "pkid": "kBSLIYY",
//    "domains": "",
//    "tk": "f92501623aafe9da1e9a76cb1b3f89b8",
//    "pwdKeyUp": 1,
//    "pVParam": {
//        "puzzle": "woVmIfMmB3qI6a7ywfvS+/7oyCpQ0cGCf+o2wYqut+j2lrCPuUuKwIVTr04b10hcMz+Edfi9oUAP\r\nWVgMADDnOt7MS3nghZVXgnaihfo6KYk446nprSEkqYT8+EoJDBOK6UKsBkwgYF+XftlA6sNN5L5J\r\nHTODZn1kA+jTMckRBrEc1ig94P+Ej8VmehOuMLNU+xhc0fBfFB8oIdmOY5giDZzFHId08JI7ldRN\r\nEhVo/Fs=",
//        "spendTime": 1000,
//        "runTimes": 389249,
//        "sid": "72e16052-1829-4921-8d9c-e3695c7ee083",
//        "args": "{\"x\":\"3b11d0b6b5bc205bdacaa7667af0ca4a9\",\"t\":389249,\"sign\":2174230325}"
//    }
//}

//{
//    "puzzle": "woVmIfMmB3qI6a7ywfvS+/7oyCpQ0cGCf+o2wYqut+j3zufDvPpmjOHM+NZOLVOP3bcqkTlGepqK\r\nnNAlfyVyXB0tKojilUbXDZdJFVwohU0cFhXdhf1COMxsfJUX1UoyCZpXB9w7kRSLUSSnx6XgbgSH\r\niio9gFtjsFtEZoCFkmOkQ1GfAzg0NdrQ1DUwl0RpuSKemGPAyJ+oecEQHJ8A54Fql32U13OR/PUf\r\nJBR+QnA=",
//    "spendTime": 1000,
//    "runTimes": 388717,
//    "sid": "1c2f7e7d-6a49-4c42-ba9d-f3221ab6c919",
//    "args": "{\"x\":\"6d47cf031d55eb0279dfdc485be638e04b\",\"t\":388717,\"sign\":3617411971}"
//}

