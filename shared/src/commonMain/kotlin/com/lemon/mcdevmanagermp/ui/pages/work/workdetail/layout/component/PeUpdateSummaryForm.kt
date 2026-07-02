package com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lemon.mcdevmanagermp.ui.components.FormSection
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailAction
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailState
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

/** PE 更新纪要最大字数。 */
private const val MAX_LENGTH = 200

/**
 * PE 更新纪要：纯文本多行输入（自动高度），**不允许空格/换行字符**，最大 [MAX_LENGTH] 字，带计数器。
 * 回显来自 `ResourceDetailVO.updateSummary`，编辑通过 [WorkDetailAction.UpdatePeUpdateSummary] 同步。
 */
@Composable
internal fun PeUpdateSummaryForm(
    state: WorkDetailState,
    onAction: (WorkDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    FormSection(title = "PE 更新纪要", modifier = modifier) {
        OutlinedTextField(
            value = state.peUpdateSummary,
            onValueChange = { raw ->
                // 过滤所有空白字符（空格 / 制表 / 换行等），并截断到上限
                val filtered = raw.filter { !it.isWhitespace() }.take(MAX_LENGTH)
                onAction(WorkDetailAction.UpdatePeUpdateSummary(filtered))
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            label = { Text("更新纪要") },
            supportingText = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "${state.peUpdateSummary.length}/$MAX_LENGTH",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        )
    }
}
