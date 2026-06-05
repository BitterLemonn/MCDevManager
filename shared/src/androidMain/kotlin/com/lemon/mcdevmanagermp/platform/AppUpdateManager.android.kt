package com.lemon.mcdevmanagermp.platform

import android.content.Intent
import androidx.core.content.FileProvider
import com.lemon.mcdevmanagermp.BuiltInVersion
import com.lemon.mcdevmanagermp.data.api.DownloadApi
import io.ktor.client.call.body
import java.io.File

actual class AppUpdateManager actual constructor() {

    actual fun getCurrentVersion(): String {
        return try {
            val context = AndroidLogContext.getContext() ?: return BuiltInVersion.VERSION
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo?.versionName ?: BuiltInVersion.VERSION
        } catch (_: Exception) {
            BuiltInVersion.VERSION
        }
    }

    actual fun getPlatformUpdateStrategy(): UpdateStrategy = UpdateStrategy.DOWNLOAD_AND_INSTALL

    actual fun getPlatformAssetMatcher(): String = ".apk"

    actual fun getDownloadDirectory(): String {
        val context = AndroidLogContext.getContext()
            ?: throw IllegalStateException("Context not initialized")
        val dir = context.getExternalFilesDir("updates") ?: context.filesDir
        return dir.absolutePath
    }

    actual suspend fun downloadFile(
        downloadUrl: String,
        fileName: String,
        onProgress: (Float) -> Unit
    ): Result<String> {
        return try {
            val outputDir = File(getDownloadDirectory())
            if (!outputDir.exists()) outputDir.mkdirs()
            val outputFile = File(outputDir, fileName)

            val statement = DownloadApi.INSTANCE.downloadFile(downloadUrl)
            statement.execute { response ->
                val bytes = response.body<ByteArray>()
                outputFile.writeBytes(bytes)
                onProgress(1f)
            }

            Result.success(outputFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual suspend fun installUpdate(filePath: String): Result<Unit> {
        return try {
            val context = AndroidLogContext.getContext()
                ?: return Result.failure(IllegalStateException("Context not initialized"))

            val file = File(filePath)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
