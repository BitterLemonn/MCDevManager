package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 通用单选胶囊组（N 选 1）：按内容宽度自动换行排列。
 *
 * 复用 [FieldLabel] + [OptionChip]，与 [YesNoSelector] 视觉完全一致。
 * 适用于定价类型（3 选 1）、钻石档位（7 选 1）等多选项场景。
 *
 * @param options 选项列表，每项为 (显示文案, 对应值)
 * @param selected 当前选中值（为 null 表示无选中）
 * @param onSelect 选中回调
 * @param required 是否必填（标签前加红色 *）
 */
@Composable
fun <T> OptionChips(
    label: String,
    options: List<Pair<String, T>>,
    selected: T?,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    enabled: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        FieldLabel(text = label, required = required)
        Spacer(Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { (text, value) ->
                OptionChip(
                    text = text,
                    selected = value == selected,
                    onClick = { onSelect(value) },
                    enabled = enabled
                )
            }
        }
    }
}
