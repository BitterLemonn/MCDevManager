package com.lemon.mcdevmanagermp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState

private val BAR_HEIGHT = 32.dp
private val BUTTON_WIDTH = 46.dp
private val ICON_STROKE = 1.dp
private val ICON_CORNER = 1.dp

/**
 * 自绘桌面标题栏。undecorated 窗口三端统一外观。
 *
 * 拖拽：整条标题栏 (WindowDragArea)；双击切换最大化。
 * 配色：跟随 MaterialTheme.colorScheme。
 */
@Composable
fun FrameWindowScope.DesktopTitleBar(
    title: String,
    state: WindowState,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val maximized = state.placement == WindowPlacement.Maximized

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(BAR_HEIGHT)
            .background(scheme.surface),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WindowDraggableArea(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            state.placement =
                                if (state.placement == WindowPlacement.Maximized) WindowPlacement.Floating
                                else WindowPlacement.Maximized
                        }
                    )
                },
        )

        CaptionButton(onClick = { state.isMinimized = true }) { fg ->
            MinimizeIcon(fg)
        }
        CaptionButton(
            onClick = {
                state.placement =
                    if (maximized) WindowPlacement.Floating else WindowPlacement.Maximized
            }
        ) { fg ->
            if (maximized) RestoreIcon(fg) else MaximizeIcon(fg)
        }
        CaptionButton(onClick = onClose, isClose = true) { fg ->
            CloseIcon(fg)
        }
    }
}

@Composable
private fun CaptionButton(
    onClick: () -> Unit,
    isClose: Boolean = false,
    content: @Composable (fg: Color) -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val scheme = MaterialTheme.colorScheme

    val (bg, fg) = when {
        isClose && hovered -> Color(0xFFC42B1C) to Color.White
        hovered -> scheme.surfaceVariant to scheme.onSurface
        else -> Color.Transparent to scheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .size(width = BUTTON_WIDTH, height = BAR_HEIGHT)
            .hoverable(interaction)
            .background(bg)
            .pointerInput(onClick) { detectTapGestures(onTap = { onClick() }) },
        contentAlignment = Alignment.Center,
    ) {
        content(fg)
    }
}

@Composable
private fun MinimizeIcon(color: Color) {
    Box(
        Modifier.size(12.dp).drawBehind {
            drawLine(
                color = color,
                start = Offset(size.width * 0.2f, size.height * 0.5f),
                end = Offset(size.width * 0.8f, size.height * 0.5f),
                strokeWidth = ICON_STROKE.toPx(),
                cap = StrokeCap.Round,
            )
        }
    )
}

@Composable
private fun MaximizeIcon(color: Color) {
    Box(
        Modifier.size(12.dp).drawBehind {
            drawRoundRect(
                color = color,
                topLeft = Offset(size.width * 0.2f, size.height * 0.2f),
                size = Size(size.width * 0.6f, size.height * 0.6f),
                style = Stroke(width = ICON_STROKE.toPx()),
                cornerRadius = CornerRadius(ICON_CORNER.toPx()),
            )
        }
    )
}

@Composable
private fun RestoreIcon(color: Color) {
    Box(
        Modifier.size(12.dp).drawBehind {
            val s = size.width * 0.6f
            // ponytail: 后画的覆盖前画的；先画背(右下)再画前(左上)，让左上角完整
            drawRoundRect(
                color = color,
                topLeft = Offset(size.width * 0.2f, size.height * 0.2f),
                size = Size(s, s),
                style = Stroke(width = ICON_STROKE.toPx()),
                cornerRadius = CornerRadius(ICON_CORNER.toPx()),
            )
            drawRoundRect(
                color = color,
                topLeft = Offset(0f, 0f),
                size = Size(s, s),
                style = Stroke(width = ICON_STROKE.toPx()),
                cornerRadius = CornerRadius(ICON_CORNER.toPx()),
            )
        }
    )
}

@Composable
private fun CloseIcon(color: Color) {
    Box(
        Modifier.size(12.dp).drawBehind {
            val pad = size.width * 0.2f
            drawLine(
                color = color,
                start = Offset(pad, pad),
                end = Offset(size.width - pad, size.height - pad),
                strokeWidth = ICON_STROKE.toPx(),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = color,
                start = Offset(size.width - pad, pad),
                end = Offset(pad, size.height - pad),
                strokeWidth = ICON_STROKE.toPx(),
                cap = StrokeCap.Round,
            )
        }
    )
}
