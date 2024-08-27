package com.lemon.mcdevmanager.data.repository

import com.lemon.mcdevmanager.api.InfoApi
import com.lemon.mcdevmanager.data.netease.user.LevelInfoBean
import com.lemon.mcdevmanager.data.netease.user.OverviewBean
import com.lemon.mcdevmanager.data.netease.user.UserInfoBean
import com.lemon.mcdevmanager.utils.NetworkState
import com.lemon.mcdevmanager.utils.UnifiedExceptionHandler

class MainRepository {
    companion object {
        @Volatile
        private var instance: MainRepository? = null
        fun getInstance() = instance ?: synchronized(this) {
            instance ?: MainRepository().also { instance = it }
        }
    }

    suspend fun getUserInfo(): NetworkState<UserInfoBean> {
        return UnifiedExceptionHandler.handleSuspendWithCall {
            InfoApi.create().getUserInfo()
        }
    }

    suspend fun getOverview(): NetworkState<OverviewBean> {
        return UnifiedExceptionHandler.handleSuspendWithCall {
            InfoApi.create().getOverview()
        }
    }

    suspend fun getLevelInfo(): NetworkState<LevelInfoBean> {
        return UnifiedExceptionHandler.handleSuspendWithCall {
            InfoApi.create().getLevelInfo()
        }
    }

    fun stopAllCalls() {
        UnifiedExceptionHandler.stopAllCalls()
    }
}