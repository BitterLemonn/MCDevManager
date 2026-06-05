package com.lemon.mcdevmanagermp.platform

enum class UpdateStrategy {
    DOWNLOAD_AND_INSTALL,
    DOWNLOAD_AND_PATCH,
    OPEN_BROWSER
}

expect class AppUpdateManager() {
    fun getCurrentVersion(): String
    fun getPlatformUpdateStrategy(): UpdateStrategy
    fun getPlatformAssetMatcher(): String
    fun getDownloadDirectory(): String
    suspend fun downloadFile(
        downloadUrl: String,
        fileName: String,
        onProgress: (Float) -> Unit
    ): Result<String>

    suspend fun installUpdate(filePath: String): Result<Unit>
}
