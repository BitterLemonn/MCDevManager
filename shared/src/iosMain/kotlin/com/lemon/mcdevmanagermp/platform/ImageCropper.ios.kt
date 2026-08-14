package com.lemon.mcdevmanagermp.platform

import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.create
import platform.Foundation.date
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToFile
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePNGRepresentation
import kotlin.math.roundToInt

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual suspend fun imageSize(file: PlatformFile): IntSize? {
    val bytes = runCatching { file.readBytes() }.getOrNull() ?: return null
    return bytes.usePinned { pinned ->
        val data = NSData.create(pinned.addressOf(0), bytes.size.convert())
        val image = UIImage(data = data)
        image.pixelSize()
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual suspend fun cropImageToRect(
    file: PlatformFile,
    srcRect: IntRect,
    targetWidth: Int,
    targetHeight: Int
): ProcessedImage? {
    val bytes = runCatching { file.readBytes() }.getOrNull() ?: return null
    val isPng = file.name.substringAfterLast('.', "").lowercase() == "png"
    return bytes.usePinned { pinned ->
        val data = NSData.create(pinned.addressOf(0), bytes.size.convert())
        val srcImage = UIImage(data = data)
        val originalSize = srcImage.pixelSize() ?: return@usePinned null
        val origW = originalSize.width
        val origH = originalSize.height
        if (targetWidth <= 0 || targetHeight <= 0) return@usePinned null

        // 钳制 srcRect 到原图边界
        val l = srcRect.left.coerceIn(0, origW - 1)
        val t = srcRect.top.coerceIn(0, origH - 1)
        val r = srcRect.right.coerceIn(l + 1, origW)
        val b = srcRect.bottom.coerceIn(t + 1, origH)
        val cropWidth = r - l
        val cropHeight = b - t
        val scaleX = targetWidth.toDouble() / cropWidth
        val scaleY = targetHeight.toDouble() / cropHeight

        // UIKit 使用左上原点。把整张图平移、缩放后绘制到输出上下文，
        // 上下文边界自然裁掉 srcRect 之外的区域，同时避免暴露 opaque CGImage 类型。
        UIGraphicsBeginImageContextWithOptions(
            size = CGSizeMake(targetWidth.toDouble(), targetHeight.toDouble()),
            opaque = !isPng,
            scale = 1.0
        )
        srcImage.drawInRect(
            CGRectMake(
                -l * scaleX,
                -t * scaleY,
                origW * scaleX,
                origH * scaleY
            )
        )
        val scaled = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        if (scaled == null) return@usePinned null

        val outData: NSData? = if (isPng) UIImagePNGRepresentation(scaled)
        else UIImageJPEGRepresentation(scaled, 0.9)
        if (outData == null) return@usePinned null

        val outExt = if (isPng) "png" else "jpg"
        val stamp = NSDate.date().timeIntervalSince1970().toString().replace(".", "")
        val tmpName = "promo_${targetWidth}x${targetHeight}_$stamp.$outExt"
        val tmpPath = NSTemporaryDirectory() + tmpName
        outData.writeToFile(tmpPath, atomically = true)
        ProcessedImage(
            file = PlatformFile(tmpPath),
            fileName = tmpName,
            mimeType = if (isPng) "image/png" else "image/jpeg"
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun UIImage.pixelSize(): IntSize? {
    val imageScale = scale
    return size.useContents {
        val pixelWidth = width * imageScale
        val pixelHeight = height * imageScale
        if (
            !pixelWidth.isFinite() || !pixelHeight.isFinite() ||
            pixelWidth <= 0.0 || pixelHeight <= 0.0 ||
            pixelWidth > Int.MAX_VALUE || pixelHeight > Int.MAX_VALUE
        ) {
            null
        } else {
            IntSize(pixelWidth.roundToInt(), pixelHeight.roundToInt())
        }
    }
}
