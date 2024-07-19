package com.lemon.mcdevmanager.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.orhanobut.logger.Logger
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

fun copyFileToDownloadFolder(
    context: Context,
    sourcePath: String,
    fileName: String,
    targetPath: String,
    onSuccess: () -> Unit,
    onFail: () -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(
                MediaStore.Downloads.DISPLAY_NAME,
                "MCDevManager" + File.pathSeparator + "Log" + File.pathSeparator + fileName
            )
            put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val uri = resolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            contentValues
        )
        uri?.let {
            try {
                resolver.openOutputStream(it)?.use { outputStream ->
                    val file = File(sourcePath)
                    FileInputStream(file).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                onSuccess()
            } catch (e: IOException) {
                Logger.e(e, "文件复制至下载目录失败: ${e.message}")
                onFail()
            }
        } ?: run {
            onFail()
            Logger.e("文件复制至下载目录失败: uri is null")
        }
    } else {
        val sourceFile = File(sourcePath)
        val downloadsDir = Environment.getExternalStoragePublicDirectory(targetPath)
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }
        val destinationFile = File(downloadsDir, fileName)

        try {
            FileInputStream(sourceFile).use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
            onSuccess()
        } catch (e: IOException) {
            Logger.e(e, "日志文件导出失败: ${e.message}")
            onFail()
        }
    }
}

fun getFileSizeFormat(size: Long): String {
    val kb = size / 1024
    return if (kb < 1024) {
        "$kb KB"
    } else if (kb < 1024 * 1024) {
        val mb = kb / 1024
        "$mb MB"
    } else {
        val gb = kb / 1024
        "$gb GB"
    }
}