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
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGImageCreateWithImageInRect
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
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

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual suspend fun imageSize(file: PlatformFile): IntSize? {
    val bytes = runCatching { file.readBytes() }.getOrNull() ?: return null
    return bytes.usePinned { pinned ->
        val data = NSData.create(pinned.addressOf(0), bytes.size.convert())
        val image = UIImage(data = data) ?: return@usePinned null
        val cg = image.CGImage ?: return@usePinned null
        val w = CGImageGetWidth(cg)
        val h = CGImageGetHeight(cg)
        if (w > 0u && h > 0u) IntSize(w.toInt(), h.toInt()) else null
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
        val srcImage = UIImage(data = data) ?: return@usePinned null
        val cg = srcImage.CGImage ?: return@usePinned null
        val origW = CGImageGetWidth(cg)
        val origH = CGImageGetHeight(cg)
        if (origW <= 0u || origH <= 0u) return@usePinned null

        // 钳制 srcRect 到原图边界
        val l = srcRect.left.coerceIn(0, (origW - 1.toUInt()).toInt()).toUInt()
        val t = srcRect.top.coerceIn(0, (origH - 1.toUInt()).toInt())
        val r = srcRect.right.coerceIn((l + 1.toUInt()).toInt(), origW.toInt())
        val b = srcRect.bottom.coerceIn(t + 1, origH.toInt())
        // CG 坐标系原点在左下，srcRect 是左上原点 → Y 需翻转
        val croppedCG = CGImageCreateWithImageInRect(
            image = cg,
            rect = CGRectMake(
                l.toDouble(),
                (origH - b.toUInt()).toDouble(),
                (r.toUInt() - l).toDouble(),
                (b - t).toDouble()
            )
        ) ?: return@usePinned null

        // 缩放到精确 target×target（单参数 imageWithCGImage 默认 scale=1、orientation=up）
        val croppedImage = UIImage.imageWithCGImage(croppedCG)
        UIGraphicsBeginImageContextWithOptions(
            size = CGSizeMake(targetWidth.toDouble(), targetHeight.toDouble()),
            opaque = !isPng,
            scale = 1.0
        )
        croppedImage.drawInRect(
            CGRectMake(
                0.0,
                0.0,
                targetWidth.toDouble(),
                targetHeight.toDouble()
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
