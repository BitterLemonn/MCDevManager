package com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lemon.mcdevmanagermp.ui.components.BinarySelector
import com.lemon.mcdevmanagermp.ui.components.FormSection
import com.lemon.mcdevmanagermp.ui.components.ReadOnlyField
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

/**
 * 上架设置（弱下架）区块：PE / PC 各自独立卡片，[title] 区分。
 * 弱下架开启后资源不再在列表公开展示，但已获取的玩家仍可正常使用。
 * 开启弱下架时显示下架理由输入框（必填，提交时由后端校验）。
 *
 * [readOnly]=true 时改为纯文本展示（选择器置灰保留选中态，理由以可换行只读字段呈现）。
 */
@Composable
internal fun ShelfSettingsForm(
    title: String,
    weakOffline: Boolean,
    reason: String,
    onToggleWeakOffline: (Boolean) -> Unit,
    onReasonChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
) {
    val colors = LocalAppColors.current
    FormSection(title = title, modifier = modifier) {
        BinarySelector(
            label = "下架设置",
            optionTrue = "弱下架",
            optionFalse = "正常上架",
            value = weakOffline,
            onValueChange = onToggleWeakOffline,
            modifier = Modifier.fillMaxWidth(),
            enabled = !readOnly
        )
        Text(
            text = "弱下架后资源不在列表展示，已获取的玩家仍可正常使用",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
        if (weakOffline) {
            if (readOnly) {
                ReadOnlyField(
                    label = "下架理由",
                    value = reason,
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                OutlinedTextField(
                    value = reason,
                    onValueChange = onReasonChange,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    label = { Text("下架理由") }
                )
            }
        }
    }
}
