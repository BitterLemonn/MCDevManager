package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 按需开启文本选择复制： [enabled]=true 时用 [SelectionContainer] 包裹 [content]，
 * 使内部所有文本（普通 [androidx.compose.material3.Text]、富文本、只读字段）可长按选中并复制；
 * 否则原样渲染，保持既有编辑手势不变。
 *
 * 仅用于只读查看场景——可写模式下的输入控件有自己的选区与长按行为，不应被外层容器接管。
 */
@Composable
fun SelectableTextContainer(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (enabled) {
        SelectionContainer(modifier = modifier) { content() }
    } else {
        content()
    }
}
