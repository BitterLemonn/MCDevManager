package com.lemon.mcdevmanagermp.platform

import android.media.MediaMetadataRetriever
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path

actual fun validateVideoFile(file: PlatformFile, fileSize: Long): VideoValidationResult {
    // 1. 校验文件大小（50MB）
    val maxFileSize = 50L * 1024 * 1024
    if (fileSize > maxFileSize) {
        return VideoValidationResult(
            isValid = false,
            errorMessage = "视频文件大小不能超过 50MB（当前 ${(fileSize / 1024.0 / 1024.0).let { "${(it * 10).toLong() / 10.0} MB" }}）"
        )
    }

    // 2. 使用 MediaMetadataRetriever 读取视频元数据
    val retriever = MediaMetadataRetriever()
    return try {
        val context = AndroidLogContext.getContext()
        if (context != null) {
            // Android PlatformFile 包装的是 Uri
            retriever.setDataSource(context, file.uri)
        } else {
            // 降级：尝试使用文件路径
            retriever.setDataSource(file.path)
        }

        // 时长校验
        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val durationMs = durationStr?.toLongOrNull() ?: 0L
        val maxDurationMs = 90_000L // 1:30 = 90 秒
        if (durationMs > maxDurationMs) {
            val seconds = durationMs / 1000
            return VideoValidationResult(
                isValid = false,
                errorMessage = "视频时长不能超过 1:30（当前 ${seconds / 60}:${"%02d".format(seconds % 60)}）",
                durationMs = durationMs
            )
        }

        // 宽高比校验（16:9）
        val widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
        val heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
        val width = widthStr?.toIntOrNull() ?: 0
        val height = heightStr?.toIntOrNull() ?: 0
        if (width > 0 && height > 0) {
            // 允许 5% 的误差（考虑旋转等情况）
            val expectedRatio = 16.0 / 9.0
            val actualRatio = width.toDouble() / height.toDouble()
            // 横竖屏都可能是 16:9
            val isLandscape16by9 =
                kotlin.math.abs(actualRatio - expectedRatio) / expectedRatio < 0.05
            val isPortrait9by16 =
                kotlin.math.abs(actualRatio - 1.0 / expectedRatio) / (1.0 / expectedRatio) < 0.05
            if (!isLandscape16by9 && !isPortrait9by16) {
                return VideoValidationResult(
                    isValid = false,
                    errorMessage = "视频宽高比需要为 16:9（当前 ${width}x${height}）",
                    durationMs = durationMs,
                    width = width,
                    height = height
                )
            }
        }

        // 编码校验（H264 / AVC）
        val codec = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
        if (codec != null && !codec.contains("avc", ignoreCase = true) && !codec.contains(
                "h264",
                ignoreCase = true
            ) && !codec.contains("mp4v", ignoreCase = true)
        ) {
            return VideoValidationResult(
                isValid = false,
                errorMessage = "视频编码需要为 H264（当前编码: $codec）",
                durationMs = durationMs,
                width = width,
                height = height
            )
        }

        VideoValidationResult(
            isValid = true,
            durationMs = durationMs,
            width = width,
            height = height
        )
    } catch (e: Exception) {
        // 无法读取元数据时，仅校验文件大小（已在上方完成）
        VideoValidationResult(isValid = true)
    } finally {
        try {
            retriever.release()
        } catch (_: Exception) {
            // 忽略释放异常
        }
    }
}

// 获取 PlatformFile 内部的 Android Uri
private val PlatformFile.uri: android.net.Uri
    get() = runCatching {
        // FileKit 的 Android PlatformFile 内部持有 Uri
        val field = this::class.java.getDeclaredField("uri")
        field.isAccessible = true
        field.get(this) as android.net.Uri
    }.getOrDefault(android.net.Uri.parse(this.path))
