@file:OptIn(com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi::class)

package com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.components.FormSection
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/** 预设色板（文字颜色 / 文字底色共用）。 */
private val PRESET_COLORS = listOf(
    Color.Black, Color(0xFF424242), Color(0xFF757575), Color(0xFFBDBDBD), Color.White,
    Color(0xFFE53935), Color(0xFFFB8C00), Color(0xFFFDD835), Color(0xFF43A047),
    Color(0xFF1E88E5), Color(0xFF8E24AA), Color(0xFF6D4C41)
)

/**
 * HTML 富文本详情编辑器（PE 详情信息 / PC 详细信息共用）。
 *
 * 所见即所得编辑，支持粗体/斜体/下划线/删除线/文字颜色/文字底色/图片，HTML 与调用方 state 双向同步。
 * 基于 rich-editor 的 [RichTextEditor]（移动端原生输入体验）。工具栏 [FlowRow] 自适应窄屏换行；
 * 颜色为预设色板弹层；图片以 base64 data URI 内嵌（编辑器内渲染依赖 rich-editor 能力，HTML 始终正确）。
 *
 * - [html] 为回显源（如 `state.detail?.info` / `state.detail?.syncItemInfo?.info`）；
 *   [echoKey] 仅用于触发首次回显（如 `itemId`），**不可**用 [html] 本身——否则与下方 toHtml 同步形成回写死循环。
 * - [onHtmlChange] 把编辑/规范化后的 HTML 同步回 VM。
 * - [syncFromPeHtml] 非 null 时工具栏显示「同步 PE」按钮：点击将其返回的 HTML 灌入本编辑器（仅 PC 详情用，
 *   返回 `state.peDetail` 即可把 PE 详情一键同步到 PC）。
 */
@OptIn(ExperimentalEncodingApi::class)
@Composable
internal fun RichDetailForm(
    title: String,
    html: String,
    echoKey: Any?,
    onHtmlChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    syncFromPeHtml: (() -> String?)? = null,
    showPreviewButton: Boolean = true
) {
    val colors = LocalAppColors.current
    val richState = rememberRichTextState()
    val scope = rememberCoroutineScope()
    var showPreview by remember { mutableStateOf(false) }
    var previewHtml by remember { mutableStateOf("") }
    // 字符计数：纯文本长度（不含 HTML 标签 / 图片 data），网易详情上限 1000
    val charCount = richState.annotatedString.text.length
    val maxCharCount = 1000
    val isOverLimit = charCount > maxCharCount

    // 回显：仅按 echoKey 变化一次，避免与下方 toHtml 同步形成循环
    LaunchedEffect(echoKey) {
        html.takeIf { it.isNotEmpty() }?.let {
            richState.setHtml(it)
            // setHtml 后光标默认落在末尾，会导致编辑器滚动到底；移到开头以默认显示第一行
            richState.selection = TextRange(0)
        }
    }
    // 编辑/规范化后同步 HTML 到 VM（toHtml 为 suspend，置于协程内）
    LaunchedEffect(richState.annotatedString) {
        onHtmlChange(richState.toHtml())
    }

    // 图片选择：读 bytes → base64 → 插入 <img>
    val imagePicker = rememberFilePickerLauncher(type = FileKitType.Image) { file: PlatformFile? ->
        if (file != null) {
            scope.launch {
                runCatching {
                    val bytes = file.readBytes()
                    val b64 = Base64.encode(bytes)
                    val ext = file.name.substringAfterLast('.', "png").lowercase()
                    val mime = when (ext) {
                        "jpg", "jpeg" -> "jpeg"
                        "png", "gif", "webp", "bmp" -> ext
                        else -> "png"
                    }
                    richState.insertHtmlAfterSelection("""<img src="data:image/$mime;base64,$b64" alt="" />""")
                }
            }
        }
    }

    FormSection(
        title = title,
        modifier = modifier
    ) {
        RichTextToolbar(
            richState = richState,
            onPickImage = { imagePicker.launch() },
            onPreview = { scope.launch { previewHtml = richState.toHtml(); showPreview = true } },
            onSyncFromPe = syncFromPeHtml?.let { fn ->
                {
                    scope.launch {
                        val peHtml = fn()
                        if (!peHtml.isNullOrEmpty()) {
                            richState.setHtml(peHtml)
                            richState.selection = TextRange(0)
                        }
                    }
                }
            },
            showPreviewButton = showPreviewButton,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "图片在编辑时会以乱码占位符显示, 具体效果请点击预览",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 300.dp, max = 480.dp),
            shape = RoundedCornerShape(12.dp),
            color = colors.scheme.surface,
            tonalElevation = 1.dp,
            border = BorderStroke(1.dp, if (isOverLimit) colors.error else colors.outlineVariant)
        ) {
            RichTextEditor(
                state = richState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )
        }
        // 字符计数器
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "$charCount/$maxCharCount",
                style = MaterialTheme.typography.bodySmall,
                color = if (isOverLimit) colors.error else colors.onSurfaceVariant
            )
        }
    }
    if (showPreview) {
        PePreviewDialog(
            html = previewHtml,
            onDismiss = { showPreview = false }
        )
    }
}

/**
 * 富文本工具栏：粗体/斜体/下划线/删除线/文字颜色/文字底色/插入图片。
 * [FlowRow] 自适应窄屏换行，按钮选中态读 [RichTextState.currentSpanStyle] 高亮。
 * 「同步 PE」(仅 [onSyncFromPe] 非 null) 与预览按钮固定在工具栏最右侧，与格式工具用 [Spacer] 隔开。
 */
@Composable
private fun RichTextToolbar(
    richState: RichTextState,
    onPickImage: () -> Unit,
    onPreview: () -> Unit,
    modifier: Modifier = Modifier,
    onSyncFromPe: (() -> Unit)? = null,
    showPreviewButton: Boolean = true
) {
    val colors = LocalAppColors.current
    val current = richState.currentSpanStyle
    var colorPickerFor by remember { mutableStateOf<ColorPickerTarget?>(null) }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = colors.surfaceContainerHigh,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FlowRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                ToolButton(
                    icon = Icons.Filled.FormatBold,
                    desc = "粗体",
                    selected = current.fontWeight == FontWeight.Bold,
                    onClick = { richState.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) }
                )
                ToolButton(
                    icon = Icons.Filled.FormatItalic,
                    desc = "斜体",
                    selected = current.fontStyle == FontStyle.Italic,
                    onClick = { richState.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic)) }
                )
                ToolButton(
                    icon = Icons.Filled.FormatUnderlined,
                    desc = "下划线",
                    selected = current.textDecoration == TextDecoration.Underline,
                    onClick = { richState.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline)) }
                )
                ToolButton(
                    icon = Icons.Filled.FormatStrikethrough,
                    desc = "删除线",
                    selected = current.textDecoration == TextDecoration.LineThrough,
                    onClick = { richState.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) }
                )
                // 文字颜色
                Box {
                    ToolButton(
                        icon = Icons.Filled.FormatColorText,
                        desc = "文字颜色",
                        selected = colorPickerFor == ColorPickerTarget.Text,
                        iconTint = current.color.takeIf { it != Color.Unspecified }
                            ?: colors.textColor,
                        onClick = { colorPickerFor = ColorPickerTarget.Text }
                    )
                    ColorPopover(
                        expanded = colorPickerFor == ColorPickerTarget.Text,
                        onDismiss = { colorPickerFor = null },
                        onPick = { c ->
                            richState.toggleSpanStyle(SpanStyle(color = c))
                            colorPickerFor = null
                        }
                    )
                }
                // 文字底色
                Box {
                    ToolButton(
                        icon = Icons.Filled.FormatColorFill,
                        desc = "文字底色",
                        selected = colorPickerFor == ColorPickerTarget.Background,
                        iconTint = current.background.takeIf { it != Color.Unspecified }
                            ?: colors.textColor,
                        onClick = { colorPickerFor = ColorPickerTarget.Background }
                    )
                    ColorPopover(
                        expanded = colorPickerFor == ColorPickerTarget.Background,
                        onDismiss = { colorPickerFor = null },
                        onPick = { c ->
                            richState.toggleSpanStyle(SpanStyle(background = c))
                            colorPickerFor = null
                        }
                    )
                }
                ToolButton(
                    icon = Icons.Filled.Image,
                    desc = "插入图片",
                    selected = false,
                    onClick = onPickImage
                )
            }
            // 与格式工具隔开
            Spacer(Modifier.width(8.dp))
            // 同步 PE：仅 PC 详情显示（PE 详情不传 onSyncFromPe）
            if (onSyncFromPe != null) {
                ToolButton(
                    icon = Icons.Filled.Sync,
                    desc = "同步 PE",
                    selected = false,
                    onClick = onSyncFromPe
                )
                Spacer(Modifier.width(4.dp))
            }
            // 预览：固定在工具栏最右侧（PC 详情隐藏，仅 PE 详情显示）
            if (showPreviewButton) {
                ToolButton(
                    icon = Icons.Filled.Preview,
                    desc = "预览",
                    selected = false,
                    onClick = onPreview
                )
            }
        }
    }
}

@Composable
private fun ToolButton(
    icon: ImageVector,
    desc: String,
    selected: Boolean,
    onClick: () -> Unit,
    iconTint: Color? = null
) = ToolButton(
    icon = rememberVectorPainter(icon),
    desc = desc,
    selected = selected,
    onClick = onClick,
    iconTint = iconTint
)

@Composable
private fun ToolButton(
    icon: Painter,
    desc: String,
    selected: Boolean,
    onClick: () -> Unit,
    iconTint: Color? = null
) {
    val colors = LocalAppColors.current
    IconButton(
        onClick = onClick,
        modifier = Modifier
            // 不抢占焦点：保持富文本编辑器焦点与选区，使 toggleSpanStyle 等格式操作作用于选区
            .focusProperties { canFocus = false }
            .size(40.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (selected) colors.primary.copy(alpha = 0.15f) else Color.Transparent
        )
    ) {
        Icon(
            painter = icon,
            contentDescription = desc,
            tint = iconTint ?: if (selected) colors.primary else colors.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

/** 颜色选择弹层（预设色板）。锚定到工具栏按钮下方。 */
@Composable
private fun ColorPopover(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onPick: (Color) -> Unit
) {
    val colors = LocalAppColors.current
    androidx.compose.material3.DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                maxItemsInEachRow = 6
            ) {
                PRESET_COLORS.forEach { c ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(c)
                            .border(1.dp, colors.outlineVariant, CircleShape)
                            .clickable { onPick(c) }
                    )
                }
            }
        }
    }
}

private enum class ColorPickerTarget { Text, Background }
