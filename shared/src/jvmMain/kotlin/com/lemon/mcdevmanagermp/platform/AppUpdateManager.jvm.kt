package com.lemon.mcdevmanagermp.platform

import com.lemon.mcdevmanagermp.BuiltInVersion
import com.lemon.mcdevmanagermp.data.api.DownloadApi
import io.ktor.client.call.body
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
