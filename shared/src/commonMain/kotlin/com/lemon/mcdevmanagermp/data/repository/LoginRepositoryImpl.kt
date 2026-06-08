package com.lemon.mcdevmanagermp.data.repository

import com.lemon.mcdevmanagermp.data.api.LoginApi
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.consts.NETEASE_TOP_URL
import com.lemon.mcdevmanagermp.data.consts.RSAKey
import com.lemon.mcdevmanagermp.data.consts.SM4Key
import com.lemon.mcdevmanagermp.data.dto.netease.login.BaseLoginResult
import com.lemon.mcdevmanagermp.data.dto.netease.login.CapIdResult
import com.lemon.mcdevmanagermp.data.dto.netease.login.EncParamsDTO
import com.lemon.mcdevmanagermp.data.dto.netease.login.GetCapIdDTO
import com.lemon.mcdevmanagermp.data.dto.netease.login.GetPowerDTO
import com.lemon.mcdevmanagermp.data.dto.netease.login.LoginDTO
import com.lemon.mcdevmanagermp.data.dto.netease.login.PVResultStrDTO
import com.lemon.mcdevmanagermp.data.dto.netease.login.PowerResult
import com.lemon.mcdevmanagermp.data.dto.netease.login.TicketDTO
import com.lemon.mcdevmanagermp.data.dto.netease.login.TicketResult
import com.lemon.mcdevmanagermp.domain.login.LoginRepository
import com.lemon.mcdevmanagermp.platform.rsaEncrypt
import com.lemon.mcdevmanagermp.utils.UnifiedExceptionHandler
import com.lemon.mcdevmanagermp.utils.encrpy.dataJsonToString
import com.lemon.mcdevmanagermp.utils.encrpy.sm4Encrypt

class LoginRepositoryImpl : LoginRepository {

    private constructor()

    companion object {
        val INSTANCE by lazy { LoginRepositoryImpl() }
        private val loginApi = LoginApi.INSTANCE
    }

    override suspend fun init(topUrl: String): NetworkState<String> {
        return UnifiedExceptionHandler.handleNeteaseLoginRequest {
            val initRequest = GetCapIdDTO(topURL = topUrl)
            val encode = sm4Encrypt(dataJsonToString(initRequest), SM4Key)
            val encParams = EncParamsDTO(encode)
            CapIdResult(loginApi.init(encParams))
        }
    }

    override suspend fun getPower(
        username: String,
        topUrl: String
    ): NetworkState<String> {
        return UnifiedExceptionHandler.handleNeteaseLoginRequest {
            val powerRequest = GetPowerDTO(un = username, topURL = topUrl)
            val encode = sm4Encrypt(dataJsonToString(powerRequest), SM4Key)
            val encParams = EncParamsDTO(encode)
            PowerResult(loginApi.getPower(encParams))
        }
    }

    override suspend fun getTicket(
        username: String,
        topUrl: String
    ): NetworkState<String> {
        return UnifiedExceptionHandler.handleNeteaseLoginRequest {
            val tkRequest = TicketDTO(username, topURL = topUrl)
            val encParams = EncParamsDTO(sm4Encrypt(dataJsonToString(tkRequest), SM4Key))
            TicketResult(loginApi.getTicket(encParams))
        }
    }

    override suspend fun loginWithTicket(
        username: String,
        password: String,
        ticket: String,
        pvResult: PVResultStrDTO
    ): NetworkState<String> {
        return UnifiedExceptionHandler.handleNeteaseLoginRequest {
            val encodePw = rsaEncrypt(password, RSAKey)
            val loginRequest = LoginDTO(
                un = username,
                pw = encodePw,
                tk = ticket,
                topURL = NETEASE_TOP_URL,
                pvParam = pvResult
            )
            val encode = sm4Encrypt(dataJsonToString(loginRequest), SM4Key)
            val encParams = EncParamsDTO(encode)
            BaseLoginResult(loginApi.safeLogin(encParams))
        }
    }

}