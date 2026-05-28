package com.lemon.mcdevmanagermp.data.repository

import com.lemon.mcdevmanagermp.data.api.InfoApi
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.vo.netease.user.LevelInfoVO
import com.lemon.mcdevmanagermp.data.vo.netease.user.OverviewVO
import com.lemon.mcdevmanagermp.data.vo.netease.user.UserInfoVO
import com.lemon.mcdevmanagermp.domain.user.UserRepository
import com.lemon.mcdevmanagermp.utils.UnifiedExceptionHandler

class UserRepositoryImpl : UserRepository {
    companion object {
        val INSTANCE by lazy { UserRepositoryImpl() }
        private val infoApi = InfoApi.INSTANCE
    }

    override suspend fun getUserInfo(): NetworkState<UserInfoVO> {
        return UnifiedExceptionHandler.handleRequest {
            infoApi.getUserInfo()
        }
    }

    override suspend fun getOverview(): NetworkState<OverviewVO> {
        return UnifiedExceptionHandler.handleRequest {
            infoApi.getOverview()
        }
    }

    override suspend fun getLevelInfo(): NetworkState<LevelInfoVO> {
        return UnifiedExceptionHandler.handleRequest {
            infoApi.getLevelInfo()
        }
    }
}