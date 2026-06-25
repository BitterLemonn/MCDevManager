package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.AsyncImage
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

/**
 * 全屏图片预览覆盖层：移动端双指缩放 / 拖动，桌面端滚轮缩放（以光标为中心），点击任意处关闭。
 *
 * 自身**非 Dialog** —— 仅提供内容层（fillMaxSize）。调用方负责提供全屏容器：
 * - 页面级 Box overlay：直接放入最上层 Box（如反馈页）。
 * - 弹窗：用 [androidx.compose.ui.window.Dialog]（`DialogProperties(usePlatformDefaultWidth = false)`）包装。
 */
@Composable
fun ImagePreviewOverlay(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background.copy(alpha = 0.92f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 5f)
                    offset = Offset(
                        offset.x + pan.x,
                        offset.y + pan.y
                    )
                }
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Scroll) {
                            val change = event.changes.firstOrNull() ?: continue
                            val scrollDelta = change.scrollDelta.y
                            if (scrollDelta != 0f) {
                                val cursorPos = change.position
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val zoomFactor = if (scrollDelta > 0f) 0.9f else 1.1f
                                val newScale = (scale * zoomFactor).coerceIn(0.5f, 5f)
                                val ratio = newScale / scale
                                offset = Offset(
                                    offset.x * ratio + (cursorPos.x - center.x) * (1f - ratio),
                                    offset.y * ratio + (cursorPos.y - center.y) * (1f - ratio)
                                )
                                scale = newScale
                            }
                            change.consume()
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            uri = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentScale = ContentScale.Fit
        )

        Text(
            text = "点击任意处关闭",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}
