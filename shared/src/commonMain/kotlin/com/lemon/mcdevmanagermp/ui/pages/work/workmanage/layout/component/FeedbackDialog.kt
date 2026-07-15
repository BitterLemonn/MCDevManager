package com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.vo.netease.work.ReviewFeedbackVO
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

/**
 * 审核反馈弹窗：展示纯文本反馈（rich_feedback 去 HTML 标签，其次 feedback）+ 操作时间。
 * 内容可滚动（反馈可能较长）。
 */
@Composable
internal fun FeedbackDialog(
    itemName: String,
    feedback: ReviewFeedbackVO,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    val raw = feedback.richFeedback.ifEmpty { feedback.feedback }
    val body = if (raw.isEmpty()) "暂无审核反馈" else stripHtml(raw)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("审核反馈 · ${itemName.ifEmpty { "未命名" }}") },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textColor
                )
                if (feedback.opTime.isNotEmpty()) {
                    Text(
                        text = "操作时间：${formatTime(feedback.opTime)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

/** 去 HTML 标签：先把 <br> 转换行，再剥除其余标签（不解码实体，保持轻量）。 */
private fun stripHtml(html: String): String =
    html.replace(Regex("(?i)<br\\s*/?>"), "\n").replace(Regex("<[^>]*>"), "").trim()

/** ISO 8601 时间取 "YYYY-MM-DD HH:mm:ss" 段展示；非标准格式原样返回。 */
private fun formatTime(iso: String): String {
    if (iso.length < 19) return iso
    return iso.substring(0, 19).replace('T', ' ')
}
