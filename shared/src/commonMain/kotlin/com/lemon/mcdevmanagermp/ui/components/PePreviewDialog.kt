package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.model.LocalImageLoader
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.img_pe_preview
import org.jetbrains.compose.resources.painterResource

// 网易平台详情页截图（1920×1080）中「组件详情」展示框的比例
// 原始像素坐标 (1176,461) → (1814,889)
private const val FRAME_LEFT = 1176f / 1920f
private const val FRAME_TOP = 461f / 1080f
private const val FRAME_WIDTH = (1814f - 1176f) / 1920f
private const val FRAME_HEIGHT = (889f - 461f) / 1080f
private const val TARGET_RATIO = 16f / 9f

/**
 * PE 详情「网页预览」：全屏弹窗，以网易 MC 平台详情页截图为背景（16:9 居中适配），
 * 将 [html] 渲染在截图右下角的「组件详情」展示框内（按 [FRAME_*] 比例定位），长文滚动，
 * 直观预览富文本在平台真实详情页里的呈现。点击遮罩任意处关闭。
 */
@OptIn(ExperimentalRichTextApi::class)
@Composable
fun PePreviewDialog(
    html: String,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    CompositionLocalProvider(LocalImageLoader provides SketchImageLoader) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.scheme.background.copy(alpha = 0.92f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    ),
                contentAlignment = Alignment.Center
            ) {
                // 16:9 适配窗口（等价 ContentScale.Fit，手动算出图片显示尺寸以精确定位展示框）
                val maxW = maxWidth
                val maxH = maxHeight
                var imgW = maxW
                var imgH = maxW / TARGET_RATIO
                if (imgH > maxH) {
                    imgH = maxH
                    imgW = maxH * TARGET_RATIO
                }

                // 居中图片容器：截图 + 叠加的 HTML 展示框
                Box(modifier = Modifier.size(width = imgW, height = imgH)) {
                    Image(
                        painter = painterResource(Res.drawable.img_pe_preview),
                        contentDescription = "网易平台详情页预览",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    val scrollState = rememberScrollState()
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (imgW * FRAME_LEFT).roundToPx(),
                                    (imgH * FRAME_TOP).roundToPx()
                                )
                            }
                            .size(width = imgW * FRAME_WIDTH, height = imgH * FRAME_HEIGHT)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White)
                            // 拦截点击：避免滚动/点选展示框时误触外层关闭
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {}
                            )
                            .verticalScroll(scrollState)
                            .padding(4.dp)
                    ) {
                        // 强制白底黑字，无视系统颜色主题（模拟平台真实浅色呈现）；
                        // color 仅作用于未显式设色的文字，编辑器内手动设的彩色字/底色仍保留
                        RichHtmlText(
                            html = html,
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.Black
                        )
                    }
                }

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
    }
}
