package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ComposableImageOptions
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

/**
 * 可视化图片裁剪对话框。
 *
 * - 裁剪框比例锁定为 [aspectRatio]（宽/高），用户可拖动框移动、用滑块缩放；
 * - 框外区域半透明遮罩，框边高亮；
 * - 确认时回调 [onConfirm]，传入**原图像素坐标系**的裁剪矩形。
 *
 * @param imageUri 原图可被 Sketch 加载的 URI（PlatformFile.path）
 * @param origSize 原图像素尺寸（调用方预先通过 [com.lemon.mcdevmanagermp.platform.imageSize] 获取）
 */
@Composable
fun ImageCropDialog(
    imageUri: String,
    origSize: IntSize,
    aspectRatio: Float,
    onConfirm: (IntRect) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
    ) {
        CropContent(imageUri, origSize, aspectRatio, onConfirm, onDismiss)
    }
}

@Composable
private fun CropContent(
    imageUri: String,
    origSize: IntSize,
    aspectRatio: Float,
    onConfirm: (IntRect) -> Unit,
    onCancel: () -> Unit
) {
    val colors = LocalAppColors.current
    val density = LocalDensity.current
    // 框缩放（1 = 最大覆盖），框中心相对显示区的比例位置
    var frameScale by remember { mutableStateOf(1f) }
    var cxRatio by remember { mutableStateOf(0.5f) }
    var cyRatio by remember { mutableStateOf(0.5f) }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(colors.background)
    ) {
        val headerPx = with(density) { 56.dp.toPx() }
        val footerPx = with(density) { 88.dp.toPx() }
        val hPadPx = with(density) { 16.dp.toPx() }
        val availW = (constraints.maxWidth - hPadPx * 2).coerceAtLeast(1f)
        val availH = (constraints.maxHeight - headerPx - footerPx).coerceAtLeast(1f)

        val origRatio =
            if (origSize.height > 0) origSize.width.toFloat() / origSize.height else aspectRatio
        // 图片按 Fit 适配到可用区后的显示尺寸
        val dispW: Float
        val dispH: Float
        if (origRatio > availW / availH) {
            dispW = availW; dispH = availW / origRatio
        } else {
            dispH = availH; dispW = availH * origRatio
        }
        // 框最大宽（受显示区宽 + 比例约束的高 共同限制）
        val maxFrameW = minOf(dispW, dispH * aspectRatio).coerceAtLeast(1f)
        val frameW = (maxFrameW * frameScale).coerceIn(1f, maxFrameW)
        val frameH = (frameW / aspectRatio).coerceAtLeast(1f)
        val halfW = frameW / 2f
        val halfH = frameH / 2f
        // 框中心允许范围（保证框不溢出显示区）
        val minCxRatio = (halfW / dispW).coerceIn(0f, 1f)
        val maxCxRatio = (1f - halfW / dispW).coerceIn(0f, 1f)
        val minCyRatio = (halfH / dispH).coerceIn(0f, 1f)
        val maxCyRatio = (1f - halfH / dispH).coerceIn(0f, 1f)
        val cx = cxRatio.coerceIn(minCxRatio, maxCxRatio) * dispW
        val cy = cyRatio.coerceIn(minCyRatio, maxCyRatio) * dispH
        val frameLeft = cx - halfW
        val frameTop = cy - halfH

        val strokePx = with(density) { 2.dp.toPx() }
        val maskColor = colors.background.copy(alpha = 0.65f)
        val frameColor = colors.primary

        Column(Modifier.fillMaxSize()) {
            // 顶部栏：取消 / 标题 / 确定
            Row(
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCancel) { Text("取消", color = colors.textColor) }
                Spacer(Modifier.weight(1f))
                Text(
                    "裁剪图片",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textColor
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = {
                    val left =
                        ((frameLeft / dispW) * origSize.width).toInt().coerceIn(0, origSize.width)
                    val top =
                        ((frameTop / dispH) * origSize.height).toInt().coerceIn(0, origSize.height)
                    val right = (((frameLeft + frameW) / dispW) * origSize.width).toInt()
                        .coerceIn(left + 1, origSize.width)
                    val bottom = (((frameTop + frameH) / dispH) * origSize.height).toInt()
                        .coerceIn(top + 1, origSize.height)
                    onConfirm(IntRect(left, top, right, bottom))
                }) {
                    Text("确定", color = frameColor, fontWeight = FontWeight.SemiBold)
                }
            }

            // 裁剪区
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(with(density) { dispW.toDp() }, with(density) { dispH.toDp() })
                        .pointerInput(Unit) {
                            detectDragGestures { change, drag ->
                                change.consume()
                                cxRatio =
                                    (cxRatio + drag.x / dispW).coerceIn(minCxRatio, maxCxRatio)
                                cyRatio =
                                    (cyRatio + drag.y / dispH).coerceIn(minCyRatio, maxCyRatio)
                            }
                        }
                ) {
                    AsyncImage(
                        uri = imageUri,
                        state = rememberAsyncImageState(ComposableImageOptions { crossfade() }),
                        contentDescription = "裁剪预览",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize()
                    )
                    Canvas(Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        // 框外四块遮罩
                        drawRect(maskColor, topLeft = Offset(0f, 0f), size = Size(frameLeft, h))
                        drawRect(
                            maskColor,
                            topLeft = Offset(frameLeft + frameW, 0f),
                            size = Size((w - frameLeft - frameW).coerceAtLeast(0f), h)
                        )
                        drawRect(
                            maskColor,
                            topLeft = Offset(frameLeft, 0f),
                            size = Size(frameW, frameTop)
                        )
                        drawRect(
                            maskColor,
                            topLeft = Offset(frameLeft, frameTop + frameH),
                            size = Size(frameW, (h - frameTop - frameH).coerceAtLeast(0f))
                        )
                        // 框线
                        drawRect(
                            color = frameColor,
                            topLeft = Offset(frameLeft, frameTop),
                            size = Size(frameW, frameH),
                            style = Stroke(width = strokePx)
                        )
                    }
                }
            }

            // 缩放滑块
            Column(
                modifier = Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 16.dp)
            ) {
                Text(
                    "缩放",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant
                )
                Slider(
                    value = frameScale,
                    onValueChange = { frameScale = it },
                    valueRange = 0.2f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = colors.primary,
                        activeTrackColor = colors.primary
                    )
                )
            }
        }
    }
}
