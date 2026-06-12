package com.lemon.mcdevmanagermp.domain.update

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.repository.UpdateRepositoryImpl
import com.lemon.mcdevmanagermp.platform.AppUpdateManager
import com.lemon.mcdevmanagermp.platform.UpdateStrategy
import com.lemon.mcdevmanagermp.utils.VersionComparator

class CheckUpdateUseCase(
    private val updateRepository: UpdateRepositoryImpl = UpdateRepositoryImpl.INSTANCE,
    private val updateManager: AppUpdateManager = AppUpdateManager()
) {
    suspend operator fun invoke(): CheckUpdateResult {
        val currentVersion = updateManager.getCurrentVersion()
        return when (val result = updateRepository.checkForUpdate()) {
            is NetworkState.Success -> {
                val release =
                    result.data ?: return CheckUpdateResult.Error("Failed to parse version info")
                if (VersionComparator.needsUpdate(currentVersion, release.tagName)) {
                    val assetHint = updateManager.getPlatformAssetMatcher()
                    val asset = updateRepository.selectBestAsset(release, assetHint)
                    CheckUpdateResult.UpdateAvailable(
                        currentVersion = currentVersion,
                        latestVersion = release.tagName.trimStart('v'),
                        releaseNotes = release.body,
                        downloadUrl = asset?.url ?: "",
                        fileName = asset?.name ?: "",
                        fileSize = asset?.size ?: 0L,
                        strategy = updateManager.getPlatformUpdateStrategy()
                    )
                } else {
                    CheckUpdateResult.UpToDate(currentVersion)
                }
            }

            is NetworkState.Error -> CheckUpdateResult.Error(result.msg)
        }
    }
}

sealed class CheckUpdateResult {
    data class UpToDate(val version: String) : CheckUpdateResult()
    data class UpdateAvailable(
        val currentVersion: String,
        val latestVersion: String,
        val releaseNotes: String,
        val downloadUrl: String,
        val fileName: String,
        val fileSize: Long,
        val strategy: UpdateStrategy
    ) : CheckUpdateResult()

    data class Error(val message: String) : CheckUpdateResult()
}
