package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

/**
 * 阻塞式加载弹窗：转圈 + 文案。不可由返回键/点击外部关闭（dismissOnBackPress/Outside = false），
 * 用于提交审核/保存等不可中断的写操作，调用方按需 `if (loading) LoadingDialog(...)` 渲染。
 *
 * @param message 提示文案（如「提交审核中…」）
 */
@Composable
fun LoadingDialog(
    message: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh)
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = colors.primary,
                    strokeWidth = 3.dp
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textColor
                )
            }
        }
    }
}
