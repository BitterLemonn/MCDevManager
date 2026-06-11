package com.lemon.mcdevmanagermp.platform

import io.github.vinceglb.filekit.PlatformFile

actual fun validateVideoFile(file: PlatformFile, fileSize: Long): VideoValidationResult {
    // iOS 端仅校验文件大小（50MB）
    // 详细的视频元数据校验待后续实现
    val maxFileSize = 50L * 1024 * 1024
    if (fileSize > maxFileSize) {
        return VideoValidationResult(
            isValid = false,
            errorMessage = "视频文件大小不能超过 50MB"
        )
    }

    return VideoValidationResult(isValid = true)
}
