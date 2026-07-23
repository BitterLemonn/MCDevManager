package com.lemon.mcdevmanagermp.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import java.io.File

actual suspend fun imageSize(file: PlatformFile): IntSize? {
    val bytes = runCatching { file.readBytes() }.getOrNull() ?: return null
    // ponytail: inJustDecodeBounds 只读元数据，不分配像素内存
    val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
    return if (opts.outWidth > 0 && opts.outHeight > 0) IntSize(
        opts.outWidth,
        opts.outHeight
    ) else null
}

actual suspend fun cropImageToRect(
    file: PlatformFile,
    srcRect: IntRect,
    targetWidth: Int,
    targetHeight: Int
): ProcessedImage? {
    val context = AndroidLogContext.getContext() ?: return null
    val bytes = runCatching { file.readBytes() }.getOrNull() ?: return null
    val src = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
    val isPng = file.name.substringAfterLast('.', "").lowercase() == "png"
    return runCatching {
        val rect = srcRect.coerceIn(src.width, src.height)
        val cropped = Bitmap.createBitmap(src, rect.left, rect.top, rect.width, rect.height)
        val scaled = if (cropped.width == targetWidth && cropped.height == targetHeight) {
            cropped
        } else {
            Bitmap.createScaledBitmap(cropped, targetWidth, targetHeight, true)
        }
        if (cropped !== scaled) cropped.recycle()
        val cf = if (isPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        val outExt = if (isPng) "png" else "jpg"
        val tmpName = "promo_${targetWidth}x${targetHeight}_${System.nanoTime()}.$outExt"
        val tmpFile = File(context.cacheDir, tmpName)
        tmpFile.outputStream().use { scaled.compress(cf, 90, it) }
        scaled.recycle()
        ProcessedImage(
            file = PlatformFile(tmpFile),
            fileName = tmpName,
            mimeType = if (isPng) "image/png" else "image/jpeg"
        )
    }.also { src.recycle() }.getOrNull()
}

/** 把 rect 钳制到图片边界内，保证 width/height ≥ 1。 */
private fun IntRect.coerceIn(maxW: Int, maxH: Int): IntRect {
    val l = left.coerceIn(0, maxW - 1)
    val t = top.coerceIn(0, maxH - 1)
    val r = right.coerceIn(l + 1, maxW)
    val b = bottom.coerceIn(t + 1, maxH)
    return IntRect(l, t, r, b)
}
