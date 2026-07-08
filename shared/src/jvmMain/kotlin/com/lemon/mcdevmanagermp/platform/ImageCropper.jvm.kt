package com.lemon.mcdevmanagermp.platform

import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO
import javax.imageio.stream.MemoryCacheImageInputStream

actual suspend fun imageSize(file: PlatformFile): IntSize? {
    val bytes = runCatching { file.readBytes() }.getOrNull() ?: return null
    // ponytail: 用 ImageReader 只读元数据，避免大图全解码占内存
    return runCatching {
        val stream = MemoryCacheImageInputStream(ByteArrayInputStream(bytes))
        val readers = ImageIO.getImageReaders(stream)
        if (!readers.hasNext()) {
            stream.close()
            return null
        }
        val reader = readers.next()
        reader.input = stream
        val w = reader.getWidth(0)
        val h = reader.getHeight(0)
        reader.dispose()
        stream.close()
        if (w > 0 && h > 0) IntSize(w, h) else null
    }.getOrNull()
}

actual suspend fun cropImageToRect(
    file: PlatformFile,
    srcRect: IntRect,
    targetWidth: Int,
    targetHeight: Int
): ProcessedImage? {
    val bytes = runCatching { file.readBytes() }.getOrNull() ?: return null
    val src = ImageIO.read(ByteArrayInputStream(bytes)) ?: return null
    val isPng = file.name.substringAfterLast('.', "").lowercase() == "png"
    return runCatching {
        val rect = srcRect.coerceIn(src.width, src.height)
        val cropped = src.getSubimage(rect.left, rect.top, rect.width, rect.height)
        val type = if (isPng) BufferedImage.TYPE_INT_ARGB else BufferedImage.TYPE_INT_RGB
        val scaled = BufferedImage(targetWidth, targetHeight, type)
        val g: Graphics2D = scaled.createGraphics()
        g.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR
        )
        g.drawImage(cropped, 0, 0, targetWidth, targetHeight, null)
        g.dispose()
        val outBytes = ByteArrayOutputStream().also { baos ->
            val fmt = if (isPng) "png" else "jpeg"
            ImageIO.write(scaled, fmt, baos)
        }.toByteArray()
        val outExt = if (isPng) "png" else "jpg"
        val tmpFile = File.createTempFile("promo_${targetWidth}x${targetHeight}_", ".$outExt")
        tmpFile.outputStream().use { it.write(outBytes) }
        ProcessedImage(
            file = PlatformFile(tmpFile),
            fileName = tmpFile.name,
            mimeType = if (isPng) "image/png" else "image/jpeg"
        )
    }.getOrNull()
}

private fun IntRect.coerceIn(maxW: Int, maxH: Int): IntRect {
    val l = left.coerceIn(0, maxW - 1)
    val t = top.coerceIn(0, maxH - 1)
    val r = right.coerceIn(l + 1, maxW)
    val b = bottom.coerceIn(t + 1, maxH)
    return IntRect(l, t, r, b)
}
