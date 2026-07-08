package com.lemon.mcdevmanagermp.domain.other

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.vo.netease.other.RedSpotsVO

interface OtherRepository {
    suspend fun getRedSpots(): NetworkState<RedSpotsVO>
}
