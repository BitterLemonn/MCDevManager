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
import com.lemon.mcdevmanagermp.data.consts.enums.PriceRankEnum
import com.lemon.mcdevmanagermp.data.consts.enums.PriceTypeEnum
import com.lemon.mcdevmanagermp.data.consts.enums.priceTypeLabel
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import com.lemon.mcdevmanagermp.ui.components.OptionChips

/**
 * 调整定价对话框：输入新价格。
 */
@Composable
internal fun PriceAdjustDialog(
    item: ResourceData,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val isDiamond = PriceTypeEnum.fromStringType(item.priceType) == PriceTypeEnum.DIAMOND
    var selectedRank by remember(item.itemId) {
        mutableStateOf(
            PriceRankEnum.fromIntType(item.priceRank).takeIf { it.type in 0..6 }
                ?: PriceRankEnum.fromDiamondPrice(item.price)
        )
    }
    var priceInput by remember(item.itemId) {
        mutableStateOf(item.price.coerceAtLeast(0).toString())
    }
    val name = item.itemName.ifEmpty { "未命名" }
    val unit = priceTypeLabel(item.priceType)
    val currentLabel = if (item.price <= 0) "免费" else "${item.price} $unit".trim()
    val newPrice = if (isDiamond) selectedRank.diamondPrice else priceInput.toIntOrNull()

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
                if (isDiamond) {
                    OptionChips(
                        label = "新定价档位",
                        options = PriceRankEnum.entries
                            .filter { it.type in 0..6 }
                            .map { it.diamondPrice.toString() to it },
                        selected = selectedRank.takeIf { it.type in 0..6 },
                        onSelect = { selectedRank = it },
                        required = true
                    )
                } else {
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { s -> priceInput = s.filter { it.isDigit() }.take(9) },
                        label = { Text(if (unit.isNotEmpty()) "新价格（$unit）" else "新价格") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(newPrice ?: 0) },
                enabled = newPrice != null && newPrice > 0 && newPrice != item.price
            ) { Text("确认") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
