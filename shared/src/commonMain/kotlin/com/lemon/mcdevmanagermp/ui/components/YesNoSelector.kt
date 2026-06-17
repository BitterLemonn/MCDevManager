package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_correct
import org.jetbrains.compose.resources.painterResource

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
            OptionPill(text = optionTrue, selected = value) { onValueChange(true) }
            OptionPill(text = optionFalse, selected = !value) { onValueChange(false) }
        }
    }
}

/**
 * 单个选项胶囊：选中时主色填充 + 勾图标 + 加粗；未选中时描边轮廓，hover 有轻量背景反馈。
 */
@Composable
private fun OptionPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    val shape = RoundedCornerShape(50)
    val interaction = remember { MutableInteractionSource() }
    val isHovered by interaction.collectIsHoveredAsState()

    val content = if (selected) colors.scheme.onPrimary else colors.textColor
    val container = when {
        selected -> colors.primary
        isHovered -> colors.primary.copy(alpha = 0.06f)
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .clip(shape)
            .background(container)
            .border(
                width = 1.dp,
                color = if (selected) colors.primary else colors.outlineVariant,
                shape = shape
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selected) {
            Icon(
                painter = painterResource(Res.drawable.ic_correct),
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = text,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = content
        )
    }
}
