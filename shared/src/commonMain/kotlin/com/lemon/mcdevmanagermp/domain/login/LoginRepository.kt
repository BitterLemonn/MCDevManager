package com.lemon.mcdevmanagermp.domain.login

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.dto.netease.login.PVResultStrDTO

interface LoginRepository {

    suspend fun init(topUrl: String): NetworkState<String>

    suspend fun getPower(username: String, topUrl: String): NetworkState<String>

    suspend fun getTicket(username: String, topUrl: String): NetworkState<String>

    suspend fun loginWithTicket(
        username: String,
        password: String,
        ticket: String,
        pvResult: PVResultStrDTO
    ): NetworkState<String>

}