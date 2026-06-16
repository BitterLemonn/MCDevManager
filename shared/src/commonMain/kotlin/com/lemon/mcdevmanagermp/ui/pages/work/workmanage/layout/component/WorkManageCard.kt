package com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.consts.enums.WorkItemActionEnum
import com.lemon.mcdevmanagermp.data.consts.enums.WorkItemStatusEnum
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import com.lemon.mcdevmanagermp.ui.theme.AppColors
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_correct
import mcdevmanagermpr.shared.generated.resources.ic_diamond
import mcdevmanagermpr.shared.generated.resources.ic_refresh
import mcdevmanagermpr.shared.generated.resources.ic_sale
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * 作品上架卡片：展示名称 / 状态 / 价格 / 时间 / 状态对应的操作按钮
 */
@Composable
internal fun WorkManageCard(
    item: ResourceData,
    onActionClick: (WorkItemActionEnum) -> Unit
) {
    val colors = LocalAppColors.current
    val status = item.getStatus()
    val actions = status.actions(isFree = item.price <= 0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 标题 + 状态标签
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.itemName.ifEmpty { "未命名作品" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                WorkStatusTag(status = status, colors = colors)
            }

            // 价格 + 特色标签
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = priceText(item.price, item.priceType),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.textColor
                )
                if (item.isOriginal) FeatureTag(text = "原创")
            }

            // 时间信息
            Spacer(Modifier.height(6.dp))
            Text(
                text = buildTimeString(item),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant.copy(alpha = 0.7f)
            )

            // 操作按钮
            if (actions.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    actions.forEach { action ->
                        ActionButton(action = action, onClick = { onActionClick(action) })
                    }
                }
            }
        }
    }
}

/**
 * 现代化操作按钮：tonal 胶囊样式（图标 + 文字），统一主色，按操作类型配图标。
 */
@Composable
private fun ActionButton(
    action: WorkItemActionEnum,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    val accent = colors.primary
    val iconRes: DrawableResource = when (action) {
        WorkItemActionEnum.SUBMIT_REVIEW -> Res.drawable.ic_correct
        WorkItemActionEnum.PUBLISH -> Res.drawable.ic_sale
        WorkItemActionEnum.UPDATE -> Res.drawable.ic_refresh
        WorkItemActionEnum.CANCEL_TEST -> Res.drawable.ic_refresh
        WorkItemActionEnum.CANCEL_REVIEW -> Res.drawable.ic_refresh
        WorkItemActionEnum.ADJUST_PRICE -> Res.drawable.ic_diamond
    }
    val interaction = remember { MutableInteractionSource() }
    val isHovered by interaction.collectIsHoveredAsState()
    val bgAlpha = if (isHovered) 0.20f else 0.10f

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(accent.copy(alpha = bgAlpha))
            .hoverable(interaction)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = action.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = accent
        )
    }
}

/**
 * 状态标签：保留语义色（绿/橙/红/灰）以传递状态信息。
 */
@Composable
private fun WorkStatusTag(status: WorkItemStatusEnum, colors: AppColors) {
    val accent = when (status) {
        WorkItemStatusEnum.ONLINE -> colors.online
        WorkItemStatusEnum.SELF_TEST -> colors.warning
        WorkItemStatusEnum.REVIEWING -> colors.warning
        WorkItemStatusEnum.REJECTED -> colors.danger
        WorkItemStatusEnum.ACCEPT -> colors.warning
        WorkItemStatusEnum.OFFLINE -> colors.offline
        WorkItemStatusEnum.SYSTEM_OFFLINE -> colors.offline
        WorkItemStatusEnum.SELF_TEST_PREPARE -> colors.onSurfaceVariant
        WorkItemStatusEnum.PREPARE -> colors.onSurfaceVariant
        WorkItemStatusEnum.UNKNOWN -> colors.onSurfaceVariant
    }
    Text(
        text = status.label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = accent,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(accent.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

/**
 * 特色标签：统一中性灰，不引入额外彩色。
 */
@Composable
private fun FeatureTag(text: String) {
    val colors = LocalAppColors.current
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Medium,
        color = colors.onSurfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(colors.onSurfaceVariant.copy(alpha = 0.10f))
            .padding(horizontal = 5.dp, vertical = 1.dp)
    )
}

// —— 格式化辅助 ——

private fun priceText(price: Int, priceType: String): String {
    if (price <= 0) return "免费"
    val unit = when (priceType) {
        "diamond" -> "钻石"
        "point" -> "绿宝石"
        else -> priceType.ifEmpty { "" }
    }
    return if (unit.isEmpty()) "$price" else "$price $unit"
}

private fun buildTimeString(item: ResourceData): String {
    val created = formatDate(item.createTime)
    val online = if (item.onlineTime.isNotEmpty() && item.onlineTime != "UNKNOWN") {
        formatDate(item.onlineTime)
    } else {
        null
    }
    return buildString {
        append("创建 $created")
        if (online != null) append("  ·  上线 $online")
    }
}

private fun formatDate(iso: String): String {
    if (iso.isEmpty()) return "—"
    return if (iso.length >= 10) iso.substring(0, 10) else iso
}
