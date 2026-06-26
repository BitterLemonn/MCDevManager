@file:OptIn(com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi::class)

package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.runtime.Composable
import com.github.panpf.sketch.rememberAsyncImagePainter
import com.mohamedrejeb.richeditor.model.ImageData
import com.mohamedrejeb.richeditor.model.ImageLoader

/**
 * 用 Sketch 加载富文本内联图片（rich-editor rc14 的 [ImageLoader] 实现）。
 *
 * rc14 起 [com.mohamedrejeb.richeditor.model.RichSpanStyle.Image] 通过
 * [com.mohamedrejeb.richeditor.model.LocalImageLoader] 注入的 [ImageLoader] 加载，
 * 核心库默认实现不加载任何图片。本实现改用项目既有的 Sketch 图片栈
 * （[rememberAsyncImagePainter]），支持网络 URL 与 base64 data URI，
 * model 取自 HTML `<img src>`，无需引入 Coil3。
 *
 * 用法：在应用根部 `CompositionLocalProvider(LocalImageLoader provides SketchImageLoader)`。
 */
object SketchImageLoader : ImageLoader {
    @Composable
    override fun load(model: Any): ImageData? {
        val uri = model as? String ?: return null
        return ImageData(painter = rememberAsyncImagePainter(uri = uri))
    }
}
