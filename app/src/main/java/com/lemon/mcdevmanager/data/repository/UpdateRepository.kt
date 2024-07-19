package com.lemon.mcdevmanager.data.repository

import com.lemon.mcdevmanager.api.DownloadApi
import com.lemon.mcdevmanager.api.GithubUpdateApi
import com.lemon.mcdevmanager.data.github.update.LatestReleaseBean
import com.lemon.mcdevmanager.utils.NetworkState
import com.lemon.mcdevmanager.utils.UnifiedExceptionHandler
import okhttp3.ResponseBody

class UpdateRepository {
    companion object {
        @Volatile
        private var instance: UpdateRepository? = null
        fun getInstance() = instance ?: synchronized(this) {
            instance ?: UpdateRepository().also { instance = it }
        }
    }

    suspend fun getLatestRelease(): NetworkState<LatestReleaseBean> {
        return UnifiedExceptionHandler.handleSuspendWithGithubData {
            GithubUpdateApi.create().getLatestRelease()
        }
    }

    fun downloadAsset(
        baseUrl: String,
        fileUrl: String
    ): ResponseBody? {
        val call = DownloadApi.create(baseUrl).downloadFile(fileUrl)
        return call.execute().body()
    }
}