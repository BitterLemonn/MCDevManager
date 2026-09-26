package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

/**
 * 只读富文本详情区块，与 [RichDetailForm] 视觉对齐（同用 [FormSection] 卡片容器）。
 *
 * 与编辑器版本的关键差异：使用 [RichHtmlText] 渲染，`<img>` 会以真实图片显示，
 * 而不是编辑器里 `data:` URI 被截断成的占位乱码。因此只读查看时不提供「预览」按钮，
 * 内容本身即为所见即所得的最终效果。
 */
@Composable
fun ReadOnlyRichDetail(
    title: String,
    html: String,
    modifier: Modifier = Modifier,
    required: Boolean = false
) {
    val colors = LocalAppColors.current
    FormSection(title = title, modifier = modifier, required = required) {
        if (html.isBlank()) {
            Text(
                text = "—",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurfaceVariant
            )
        } else {
            RichHtmlText(
                html = html,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
