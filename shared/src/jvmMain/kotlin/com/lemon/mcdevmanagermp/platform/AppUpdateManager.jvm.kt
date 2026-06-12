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
    companion object {
        const val TAG = "AppUpdateManager"
    }

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

            Logger.d("$TAG: 下载完成: ${outputFile.absolutePath}")
            Result.success(outputFile.absolutePath)
        } catch (e: Exception) {
            Logger.e("$TAG: 下载失败: ${e.message}", e)
            Result.failure(e)
        }
    }

    actual suspend fun installUpdate(filePath: String): Result<Unit> {
        return try {
            val jarPath =
                UpdateClassRef::class.java.protectionDomain.codeSource.location.toURI().path
            // 使用发行版根目录（JAR 在 app/ 子目录中，需要再上一级）
            val distRoot = File(jarPath).parentFile?.parentFile
                ?: return Result.failure(IllegalStateException("Cannot determine app directory"))

            val zipFile = File(filePath)
            val tempDir = File(distRoot, ".patch_temp")
            if (tempDir.exists()) tempDir.deleteRecursively()
            tempDir.mkdirs()

            // 解压 ZIP 到临时目录
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

            // 删除下载的 ZIP（不被锁定）
            zipFile.delete()

            val isWindows = System.getProperty("os.name", "").lowercase().contains("win")

            if (isWindows) {
                // Windows: JAR 文件被运行中的 JVM 锁定，无法直接覆盖
                // 将文件保留在 .patch_temp/ 中，由 restartApp() 创建辅助脚本在进程退出后完成覆盖
                Logger.d("$TAG: 更新已解压，等待重启时应用补丁")
            } else {
                // Unix: 可以直接覆盖运行中的文件
                val extractedRoot = detectExtractedRoot(tempDir)
                copyDirectory(extractedRoot, distRoot)
                tempDir.deleteRecursively()
                Logger.d("$TAG: 更新已安装")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e("$TAG: 安装更新失败: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * 检测 ZIP 解压后的根目录：
     * - 如果临时目录直接包含 app/ 或 runtime/，说明 ZIP 没有包装目录
     * - 否则取第一个子目录作为根（ZIP 含包装目录 MCDevManager/）
     */
    internal fun detectExtractedRoot(tempDir: File): File {
        return if (File(tempDir, "app").exists() || File(tempDir, "runtime").exists()) {
            tempDir
        } else {
            tempDir.listFiles()?.firstOrNull { it.isDirectory } ?: tempDir
        }
    }

    private fun copyDirectory(source: File, target: File) {
        source.listFiles()?.forEach { file ->
            if (file.name == ".data" || file.name == "logs" || file.name == "downloads" || file.name == ".patch_temp") return@forEach

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
