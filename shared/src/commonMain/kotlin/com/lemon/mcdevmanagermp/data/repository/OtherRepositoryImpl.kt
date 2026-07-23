package com.lemon.mcdevmanagermp.data.repository

import com.lemon.mcdevmanagermp.data.api.OtherApi
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.vo.netease.other.RedSpotsVO
import com.lemon.mcdevmanagermp.domain.other.OtherRepository
import com.lemon.mcdevmanagermp.utils.UnifiedExceptionHandler

class OtherRepositoryImpl : OtherRepository {
    companion object {
        val INSTANCE by lazy { OtherRepositoryImpl() }
        private val api = OtherApi.INSTANCE
    }

    override suspend fun getRedSpots(): NetworkState<RedSpotsVO> =
        UnifiedExceptionHandler.handleRequest { api.getRedSpots() }
}
