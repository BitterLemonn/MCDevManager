package com.lemon.mcdevmanagermp.platform

import com.lemon.mcdevmanagermp.BuiltInVersion
import com.lemon.mcdevmanagermp.data.api.DownloadApi
import com.lemon.mcdevmanagermp.utils.Logger
import io.ktor.client.call.body
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import java.io.File
import java.util.zip.ZipInputStream

private object UpdateClassRef

actual class AppUpdateManager actual constructor() {

    actual fun getCurrentVersion(): String = BuiltInVersion.VERSION

    actual fun getPlatformUpdateStrategy(): UpdateStrategy = UpdateStrategy.DOWNLOAD_AND_PATCH

    actual fun getPlatformAssetMatcher(): String = "-portable.zip"

    actual fun getDownloadDirectory(): String {
        val jarPath = UpdateClassRef::class.java.protectionDomain.codeSource.location.toURI().path
        val appDir = File(File(jarPath).parentFile, "downloads")
        if (!appDir.exists()) appDir.mkdirs()
        return appDir.absolutePath
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
                val contentLength = response.headers["Content-Length"]?.toLongOrNull() ?: -1L
                val channel: ByteReadChannel = response.body()

                outputFile.outputStream().buffered(64 * 1024).use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var totalBytesRead = 0L
                    var lastReportedProgress = 0f

                    while (!channel.isClosedForRead) {
                        val bytesRead = channel.readAvailable(buffer, 0, buffer.size)
                        if (bytesRead <= 0) break
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead

                        if (contentLength > 0) {
                            val progress =
                                (totalBytesRead.toFloat() / contentLength).coerceIn(0f, 1f)
                            if (progress - lastReportedProgress >= 0.01f) {
                                onProgress(progress)
                                lastReportedProgress = progress
                            }
                        }
                    }
                }
                onProgress(1f)
            }

            Logger.d("下载完成: ${outputFile.absolutePath}")
            Result.success(outputFile.absolutePath)
        } catch (e: Exception) {
            Logger.e("下载失败: ${e.message}", e)
            Result.failure(e)
        }
    }

    actual suspend fun installUpdate(filePath: String): Result<Unit> {
        return try {
            val jarPath =
                UpdateClassRef::class.java.protectionDomain.codeSource.location.toURI().path
            val appDir = File(jarPath).parentFile
                ?: return Result.failure(IllegalStateException("Cannot determine app directory"))

            val zipFile = File(filePath)
            val tempDir = File(appDir, ".patch_temp")
            if (tempDir.exists()) tempDir.deleteRecursively()
            tempDir.mkdirs()

            ZipInputStream(zipFile.inputStream()).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val entryFile = File(tempDir, entry.name)
                    if (entry.isDirectory) {
                        entryFile.mkdirs()
                    } else {
                        entryFile.parentFile?.mkdirs()
                        entryFile.outputStream().use { fos ->
                            zis.copyTo(fos)
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }

            val extractedRoot = tempDir.listFiles()?.firstOrNull { it.isDirectory } ?: tempDir
            copyDirectory(extractedRoot, appDir)

            tempDir.deleteRecursively()
            zipFile.delete()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun copyDirectory(source: File, target: File) {
        source.listFiles()?.forEach { file ->
            if (file.name == ".data" || file.name == "logs" || file.name == "downloads") return@forEach

            val destFile = File(target, file.name)
            if (file.isDirectory) {
                if (!destFile.exists()) destFile.mkdirs()
                copyDirectory(file, destFile)
            } else {
                file.copyTo(destFile, overwrite = true)
            }
        }
    }
}
