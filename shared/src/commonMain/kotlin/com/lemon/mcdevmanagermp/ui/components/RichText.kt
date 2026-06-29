@file:OptIn(com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi::class)

package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.mohamedrejeb.richeditor.model.LocalImageLoader
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText

/**
 * 只读渲染 HTML 富文本。基于 rich-editor 的 [RichText]，
 * 内部通过 [com.mohamedrejeb.richeditor.model.RichTextState.setHtml] 在协程中解析，
 * [LaunchedEffect] 监听内容变化重新解析。
 */
@Composable
fun RichHtmlText(
    html: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
) {
    val state = rememberRichTextState()
    LaunchedEffect(html) {
        state.setHtml(html)
    }
    // 自包含注入图片加载器：Dialog 有独立 CompositionLocal 树，不继承外层 LocalImageLoader，
    // 此处显式提供 SketchImageLoader，确保 <img> 在任意容器（含 Dialog）内都能渲染
    CompositionLocalProvider(LocalImageLoader provides SketchImageLoader) {
        RichText(
            state = state,
            modifier = modifier,
            style = style,
            color = color,
        )
    }
}

/**
 * 只读渲染 Markdown 富文本。基于 rich-editor 的 [RichText]，
 * 内部通过 [com.mohamedrejeb.richeditor.model.RichTextState.setMarkdown] 在协程中解析，
 * [LaunchedEffect] 监听内容变化重新解析。
 */
@Composable
fun RichMarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
) {
    val state = rememberRichTextState()
    LaunchedEffect(markdown) {
        state.setMarkdown(markdown)
    }
    // 同 RichHtmlText：自包含注入 SketchImageLoader，覆盖 Dialog 等独立 CompositionLocal 场景
    CompositionLocalProvider(LocalImageLoader provides SketchImageLoader) {
        RichText(
            state = state,
            modifier = modifier,
            style = style,
            color = color,
        )
    }
}
