package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.ContentScale
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.sketch.SingletonSketch
import com.github.panpf.sketch.rememberAsyncImagePainter
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.resize.Precision
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.model.ImageData
import com.mohamedrejeb.richeditor.model.ImageLoader

/**
 * rich-editor 的 Sketch 图片加载器。
 *
 * 根因：rich-editor 渲染 `<img>` 时会读取 [ImageData.painter] 的 intrinsicSize 来计算占位尺寸
 * （intrinsicSize == Unspecified 时占位停在初始值，而网易富文本 `<img>` 不带 width/height，即 0×0）。
 * 而 Sketch 的 [rememberAsyncImagePainter] 是 lower-level API——其加载依赖 onDraw 被调用 + size 解析，
 * 在「占位 0×0 → Image 不绘制 → onDraw 不触发 → 不加载 → intrinsicSize 永远 Unspecified」里死锁
 * （Sketch 官方文档：AsyncImagePainter will not finish loading if onDraw is not called；
 * 设 ImageRequest.Builder.size 可让加载在组合阶段完成）。
 */
@OptIn(ExperimentalRichTextApi::class)
object SketchImageLoader : ImageLoader {
    @Composable
    override fun load(model: Any): ImageData? {
        val uri = model as? String ?: return null
        val context = LocalPlatformContext.current
        val sketch = remember(context) { SingletonSketch.get(context) }
        val state = rememberAsyncImageState()
        val request = remember(uri) {
            ImageRequest(context, uri) {
                crossfade()
                size(1080, 1080)
                precision(Precision.LESS_PIXELS)
            }
        }
        return ImageData(
            painter = rememberAsyncImagePainter(
                request = request,
                sketch = sketch,
                state = state,
                contentScale = ContentScale.Fit
            )
        )
    }
}
