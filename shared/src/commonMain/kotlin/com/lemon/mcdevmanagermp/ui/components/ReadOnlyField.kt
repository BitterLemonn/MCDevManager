package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

/**
 * 只读字段：上方标签，下方值（值缺省显示 "—"）。可选 [trailing] 尾部插槽（如复制按钮）。
 *
 * [singleLine]=false 时值可换行完整展示（用于只读详情中的长文本，如简介 / 下架理由）。
 */
@Composable
fun ReadOnlyField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    singleLine: Boolean = true,
    trailing: @Composable (() -> Unit)? = null
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = if (singleLine) Alignment.Bottom else Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Column(modifier = Modifier.weight(1f, fill = false)) {
            FieldLabel(text = label, required = required)
            Spacer(Modifier.height(4.dp))
            Text(
                text = value.ifEmpty { "—" },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = colors.textColor,
                maxLines = if (singleLine) 1 else Int.MAX_VALUE,
                overflow = if (singleLine) TextOverflow.Ellipsis else TextOverflow.Clip
            )
        }
        trailing?.invoke()
    }
}
