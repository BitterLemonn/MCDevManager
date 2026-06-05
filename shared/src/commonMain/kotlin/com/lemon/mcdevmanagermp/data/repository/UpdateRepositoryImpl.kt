package com.lemon.mcdevmanagermp.data.repository

import com.lemon.mcdevmanagermp.data.api.GithubUpdateApi
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.vo.github.AssetBean
import com.lemon.mcdevmanagermp.data.vo.github.LatestReleaseVO
import com.lemon.mcdevmanagermp.utils.UnifiedExceptionHandler

class UpdateRepositoryImpl {

    companion object {
        val INSTANCE by lazy { UpdateRepositoryImpl() }
    }

    suspend fun checkForUpdate(): NetworkState<LatestReleaseVO> {
        return UnifiedExceptionHandler.handleGithubRequest {
            GithubUpdateApi.INSTANCE.getLatestRelease()
        }
    }

    fun selectBestAsset(release: LatestReleaseVO, matcher: String): AssetBean? {
        return release.assets.firstOrNull { it.name.contains(matcher, ignoreCase = true) }
    }
}
