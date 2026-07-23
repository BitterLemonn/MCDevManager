package com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.consts.enums.priceTypeLabel
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData

/**
 * 调整定价对话框：输入新价格。
 *
 * 注意：当前为占位实现，确认后仅通过 [onConfirm] 触发提示，不真实联网。
 */
@Composable
internal fun PriceAdjustDialog(
    item: ResourceData,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var priceInput by remember { mutableStateOf(item.price.coerceAtLeast(0).toString()) }
    val name = item.itemName.ifEmpty { "未命名" }
    val unit = priceTypeLabel(item.priceType)
    val currentLabel = if (item.price <= 0) "免费" else "${item.price} $unit".trim()
    val newPrice = priceInput.toIntOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("调整定价") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "作品：$name",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "当前价格：$currentLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { s -> priceInput = s.filter { it.isDigit() }.take(9) },
                    label = { Text(if (unit.isNotEmpty()) "新价格（$unit）" else "新价格") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text(
                    text = "（操作接口占位，当前不会真实提交）",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(newPrice ?: 0) },
                enabled = newPrice != null && newPrice > 0
            ) { Text("确认") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
