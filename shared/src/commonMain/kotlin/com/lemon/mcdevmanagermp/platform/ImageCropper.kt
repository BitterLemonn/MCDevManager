package com.lemon.mcdevmanagermp.platform

import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.size

/**
 * 裁剪后的图片信息（写入各平台临时目录，由 OS 清理）。
 */
data class ProcessedImage(
    val file: PlatformFile,
    val fileName: String,
    val mimeType: String
)

/**
 * 宣传图校验：仅 png/jpg/jpeg，≤ 10MB。通过返回 null，否则返回错误文案。
 */
suspend fun validatePromoImage(file: PlatformFile): String? {
    val size = runCatching { file.size() }.getOrDefault(0L)
    if (size <= 0L) return "无法读取图片文件"
    val max = 10L * 1024 * 1024
    if (size > max) {
        val mb = (size * 10 / 1024 / 1024).toInt() / 10.0
        return "图片大小不能超过 10MB（当前 $mb MB）"
    }
    val ext = file.name.substringAfterLast('.', "").lowercase()
    val mime = runCatching { file.mimeType()?.toString() }.getOrNull().orEmpty().lowercase()
    val ok = ext in setOf("png", "jpg", "jpeg") ||
            mime in setOf("image/png", "image/jpeg", "image/jpg")
    if (!ok) return "仅支持 PNG / JPG / JPEG 格式"
    return null
}

/**
 * 读取图片原始像素尺寸（用于裁剪 UI 计算坐标映射）。无法解码返回 null。
 */
expect suspend fun imageSize(file: PlatformFile): IntSize?

/**
 * 按 [srcRect]（原图像素坐标系）裁剪，再缩放到精确 [targetWidth]×[targetHeight]，
 * 写入临时文件返回。png 输入保 png，jpg/jpeg 输出 jpeg。失败返回 null。
 */
expect suspend fun cropImageToRect(
    file: PlatformFile,
    srcRect: IntRect,
    targetWidth: Int,
    targetHeight: Int
): ProcessedImage?
