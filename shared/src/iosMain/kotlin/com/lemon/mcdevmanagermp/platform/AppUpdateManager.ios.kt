package com.lemon.mcdevmanagermp.platform

import com.lemon.mcdevmanagermp.BuiltInVersion
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

actual class AppUpdateManager actual constructor() {

    @OptIn(ExperimentalForeignApi::class)
    actual fun getCurrentVersion(): String {
        return try {
            val version =
                NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String
            version ?: BuiltInVersion.VERSION
        } catch (_: Exception) {
            BuiltInVersion.VERSION
        }
    }

    actual fun getPlatformUpdateStrategy(): UpdateStrategy = UpdateStrategy.OPEN_BROWSER

    actual fun getPlatformAssetMatcher(): String = ".ipa"

    actual fun getDownloadDirectory(): String {
        return NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        ).first() as String
    }

    actual suspend fun downloadFile(
        downloadUrl: String,
        fileName: String,
        onProgress: (Float) -> Unit
    ): Result<String> {
        return Result.failure(UnsupportedOperationException("iOS does not support in-app download"))
    }

    actual suspend fun installUpdate(filePath: String): Result<Unit> {
        return Result.failure(UnsupportedOperationException("iOS does not support in-app installation"))
    }
}
