package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 是/否二选一选择器：[BinarySelector] 的「是/否」特例。
 */
@Composable
fun YesNoSelector(
    label: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    required: Boolean = false
) {
    BinarySelector(
        label = label,
        optionTrue = "是",
        optionFalse = "否",
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        required = required
    )
}

/**
 * 通用二选一胶囊选择器（选项文字可自定义）。是/否、主包/副包等均可复用。
 *
 * 按内容宽度排列、不占满整行，在宽屏布局中保持精致。
 */
@Composable
internal fun BinarySelector(
    label: String,
    optionTrue: String,
    optionFalse: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    required: Boolean = false
) {
    Column(modifier = modifier.fillMaxWidth()) {
        FieldLabel(text = label, required = required)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OptionChip(text = optionTrue, selected = value, onClick = { onValueChange(true) })
            OptionChip(text = optionFalse, selected = !value, onClick = { onValueChange(false) })
        }
    }
}
