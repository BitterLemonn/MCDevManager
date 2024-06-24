package com.lemon.mcdevmanager.data.repository

import com.lemon.mcdevmanager.api.LoginApi
import com.lemon.mcdevmanager.data.common.RSAKey
import com.lemon.mcdevmanager.data.common.SM4Key
import com.lemon.mcdevmanager.data.netease.login.EncParams
import com.lemon.mcdevmanager.data.netease.login.GetCapIdRequestBean
import com.lemon.mcdevmanager.data.netease.login.GetPowerRequestBean
import com.lemon.mcdevmanager.data.netease.login.LoginRequestBean
import com.lemon.mcdevmanager.data.netease.login.PVResultBean
import com.lemon.mcdevmanager.data.netease.login.PVResultStrBean
import com.lemon.mcdevmanager.data.netease.login.TicketRequestBean
import com.lemon.mcdevmanager.utils.NetworkState
import com.lemon.mcdevmanager.utils.UnifiedExceptionHandler
import com.lemon.mcdevmanager.utils.dataJsonToString
import com.lemon.mcdevmanager.utils.rsaEncrypt
import com.lemon.mcdevmanager.utils.sm4Encrypt
import com.orhanobut.logger.Logger

class LoginRepository {

    companion object {
        @Volatile
        private var instance: LoginRepository? = null
        fun getInstance() = instance ?: synchronized(this) {
            instance ?: LoginRepository().also { instance = it }
        }
    }

    suspend fun init(topUrl: String): NetworkState<String> {
        return UnifiedExceptionHandler.handleSuspendWithNeteaseData {
            val initRequest = GetCapIdRequestBean(topURL = topUrl)
            val encode = sm4Encrypt(dataJsonToString(initRequest), SM4Key)
            val encParams = EncParams(encode)
            LoginApi.create().init(encParams)
        }
    }

    suspend fun getPower(username: String, topUrl: String): NetworkState<String> {
        return UnifiedExceptionHandler.handleSuspendWithNeteaseData {
            val powerRequest = GetPowerRequestBean(un = username, topURL = topUrl)
            val encode = sm4Encrypt(dataJsonToString(powerRequest), SM4Key)
            val encParams = EncParams(encode)
            LoginApi.create().getPower(encParams)
        }
    }

    suspend fun getTicket(username: String, topUrl: String): NetworkState<String> {
        return UnifiedExceptionHandler.handleSuspendWithNeteaseData {
            val tkRequest = TicketRequestBean(username, topURL = topUrl)
            val encParams = EncParams(sm4Encrypt(dataJsonToString(tkRequest), SM4Key))
            LoginApi.create()
                .getTicket(encParams)
        }
    }

    suspend fun loginWithTicket(
        username: String,
        password: String,
        ticket: String,
        pvResultBean: PVResultStrBean
    ): NetworkState<String> {
        return UnifiedExceptionHandler.handleSuspendWithNeteaseData {
            val encodePw = rsaEncrypt(password, RSAKey)
            val loginRequest = LoginRequestBean(
                un = username,
                pw = encodePw,
                tk = ticket,
                topURL = "https://mcdev.webapp.163.com/#/login",
                pvParam = pvResultBean
            )
            val encode = sm4Encrypt(dataJsonToString(loginRequest), SM4Key)
            val encParams = EncParams(encode)
            LoginApi.create().safeLogin(encParams)
        }
    }

}