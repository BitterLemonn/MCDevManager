package com.lemon.mcdevmanagermp.domain.update

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.vo.github.AssetBean
import com.lemon.mcdevmanagermp.data.vo.github.LatestReleaseVO

interface UpdateRepository {
    suspend fun checkForUpdate(): NetworkState<LatestReleaseVO>
    fun selectBestAsset(release: LatestReleaseVO, matcher: String): AssetBean?
}
