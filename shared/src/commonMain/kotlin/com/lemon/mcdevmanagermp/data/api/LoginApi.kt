package com.lemon.mcdevmanagermp.data.api

import com.lemon.mcdevmanagermp.data.consts.NETEASE_LOGIN_LINK
import com.lemon.mcdevmanagermp.data.dto.netease.login.EncParamsDTO
import com.lemon.mcdevmanagermp.data.vo.netease.login.BaseLoginVO
import com.lemon.mcdevmanagermp.data.vo.netease.login.CapIdVO
import com.lemon.mcdevmanagermp.data.vo.netease.login.PowerVO
import com.lemon.mcdevmanagermp.data.vo.netease.login.TicketVO
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST

interface LoginApi {

    @Headers("Content-Type: application/json")
    @POST("dl/zj/mail/ini")
    suspend fun init(@Body encParams: EncParamsDTO): CapIdVO

    @Headers("Content-Type: application/json")
    @POST("dl/zj/mail/powGetP")
    suspend fun getPower(@Body encParams: EncParamsDTO): PowerVO

    @Headers("Content-Type: application/json")
    @POST("dl/zj/mail/gt")
    suspend fun getTicket(@Body encParams: EncParamsDTO): TicketVO

    @Headers("Content-Type: application/json")
    @POST("dl/zj/mail/l")
    suspend fun safeLogin(@Body encParams: EncParamsDTO): BaseLoginVO

    companion object {
        val INSTANCE by lazy {
            ApiFactory.provideKtorfit(NETEASE_LOGIN_LINK).createLoginApi()
        }
    }
}

