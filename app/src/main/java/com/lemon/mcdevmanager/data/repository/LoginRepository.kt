package com.lemon.mcdevmanager.data.repository

import com.lemon.mcdevmanager.api.LoginApi
import com.lemon.mcdevmanager.utils.NetworkState
import com.lemon.mcdevmanager.utils.UnifiedExceptionHandler

class LoginRepository {

    companion object {
        @Volatile
        private var instance: LoginRepository? = null
        fun getInstance() = instance ?: synchronized(this) {
            instance ?: LoginRepository().also { instance = it }
        }
    }

    suspend fun getTicket(username: String): NetworkState<String> {
        return UnifiedExceptionHandler.handleSuspendWithNeteaseData {
            LoginApi.create().getTicket(username)
        }
    }

    suspend fun loginWithTicket(ticket: String): NetworkState<String> {
        return UnifiedExceptionHandler.handleSuspendWithNeteaseData {
            LoginApi.create().loginWithTicket(ticket)
        }
    }

}