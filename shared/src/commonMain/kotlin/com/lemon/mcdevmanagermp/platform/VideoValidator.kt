package com.lemon.mcdevmanagermp.platform

import io.github.vinceglb.filekit.PlatformFile

/**
 * 视频元数据校验结果
 */
data class VideoValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val durationMs: Long = 0,
    val width: Int = 0,
    val height: Int = 0,
)

/**
 * 校验视频文件是否符合要求：
 * - 时长 ≤ 90 秒
 * - 宽高比 16:9
 * - H264 编码
 * - 文件大小 ≤ 50MB
 *
 * 各平台实现不同的校验逻辑。
 * @param file 由 FileKit 选择的平台文件
 * @param fileSize 文件大小（字节）
 */
expect fun validateVideoFile(file: PlatformFile, fileSize: Long): VideoValidationResult
