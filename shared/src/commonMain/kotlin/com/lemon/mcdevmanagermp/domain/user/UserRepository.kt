package com.lemon.mcdevmanagermp.domain.user

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.vo.netease.user.LevelInfoVO
import com.lemon.mcdevmanagermp.data.vo.netease.user.OverviewVO
import com.lemon.mcdevmanagermp.data.vo.netease.user.UserInfoVO

interface UserRepository {
    suspend fun getUserInfo(): NetworkState<UserInfoVO>

    suspend fun getOverview(): NetworkState<OverviewVO>

    suspend fun getLevelInfo(): NetworkState<LevelInfoVO>
}