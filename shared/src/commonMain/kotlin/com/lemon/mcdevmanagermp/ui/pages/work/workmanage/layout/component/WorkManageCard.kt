package com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.consts.enums.WorkItemActionEnum
import com.lemon.mcdevmanagermp.data.consts.enums.priceTypeLabel
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import com.lemon.mcdevmanagermp.ui.components.tabularNumbers
import com.lemon.mcdevmanagermp.ui.iconpack.Calendar
import com.lemon.mcdevmanagermp.ui.iconpack.Feedback
import com.lemon.mcdevmanagermp.ui.iconpack.IconPack
import com.lemon.mcdevmanagermp.ui.iconpack.Modified
import com.lemon.mcdevmanagermp.ui.iconpack.Sale
import com.lemon.mcdevmanagermp.ui.iconpack.Show
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.splitCardActions
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

/**
 * 重构后的作品资源卡片：
 * - 头部：作品名称（最多 2 行）+ 醒目状态徽章
 * - 中部：价格 + 属性徽标（原创 / 双端 / 前置 / 弱下架 / 已归档）+ 时间/ID 元数据
 * - 底部：主操作 + 次操作（已归档视图为「移出归档」）+ 更多菜单（收纳其余动作与归档开关）
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun WorkManageCard(
    item: ResourceData,
    isArchived: Boolean,
    onActionClick: (WorkItemActionEnum) -> Unit,
    onToggleArchive: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val status = item.getStatus()
    val actionSplit = remember(item.status, item.price, isArchived) {
        item.splitCardActions(isArchived = isArchived)
    }
    var menuExpanded by remember(item.itemId) { mutableStateOf(false) }

    val cardInteraction = remember { MutableInteractionSource() }
    val isCardHovered by cardInteraction.collectIsHoveredAsState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .hoverable(cardInteraction)
            .border(
                width = 1.dp,
                color = if (isCardHovered) {
                    colors.primary.copy(alpha = 0.35f)
                } else {
                    colors.outlineVariant.copy(alpha = 0.35f)
                },
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 顶部：作品标题 + 状态徽章
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = item.itemName.ifBlank { "未命名作品" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                WorkStatusBadge(status = status)
            }

            // 中部：价格与属性徽标
            Spacer(Modifier.height(10.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                itemVerticalAlignment = Alignment.CenterVertically
            ) {
                PricePill(price = item.price, priceType = item.priceType)
                if (item.isOriginal) MetaTag(text = "原创")
                if (item.syncPcFlag) MetaTag(text = "双端同步")
                if (item.isPrerequisite()) MetaTag(text = "前置模组")
                if (item.weakOffline) {
                    MetaTag(
                        text = "弱下架",
                        containerColor = colors.scheme.errorContainer,
                        contentColor = colors.scheme.onErrorContainer
                    )
                }
                if (isArchived) {
                    MetaTag(
                        text = "已归档",
                        containerColor = colors.secondaryContainer,
                        contentColor = colors.onSecondaryContainer
                    )
                }
            }

            // 时间与作品 ID
            Spacer(Modifier.height(10.dp))
            val timeEntries = remember(item.createTime, item.applyReviewTime, item.onlineTime) {
                buildWorkTimeEntries(item)
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                timeEntries.forEach { entry ->
                    Text(
                        text = entry,
                        style = MaterialTheme.typography.bodySmall.tabularNumbers(),
                        color = colors.onSurfaceVariant.copy(alpha = 0.78f),
                        maxLines = 1
                    )
                }
            }
            if (item.itemId.isNotBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "ID ${item.itemId}",
                    style = MaterialTheme.typography.labelSmall.tabularNumbers(),
                    color = colors.onSurfaceVariant.copy(alpha = 0.56f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 分隔线 + 规整单行操作栏
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.45f))
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                actionSplit.primary?.let { primaryAction ->
                    CardPrimaryActionButton(
                        action = primaryAction,
                        onClick = { onActionClick(primaryAction) }
                    )
                }

                if (isArchived) {
                    CardSubtleActionButton(
                        label = "移出归档",
                        icon = Icons.Filled.Unarchive,
                        onClick = onToggleArchive
                    )
                } else {
                    actionSplit.secondary?.let { secondaryAction ->
                        CardSubtleActionButton(
                            label = secondaryAction.label,
                            icon = actionIcon(secondaryAction),
                            onClick = { onActionClick(secondaryAction) }
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                // 更多操作菜单（包含其余动作 + 归档/移出归档）
                Box {
                    val moreInteraction = remember { MutableInteractionSource() }
                    val isMoreHovered by moreInteraction.collectIsHoveredAsState()
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                when {
                                    menuExpanded -> colors.secondaryContainer
                                    isMoreHovered -> colors.surfaceContainerHighest
                                    else -> Color.Transparent
                                }
                            )
                            .hoverable(moreInteraction)
                            .clickable(
                                interactionSource = moreInteraction,
                                indication = ripple(),
                                onClick = { menuExpanded = true }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "更多操作",
                            tint = if (menuExpanded) colors.onSecondaryContainer else colors.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        offset = DpOffset(0.dp, 4.dp),
                        shape = RoundedCornerShape(14.dp),
                        containerColor = colors.surfaceContainerHigh,
                        tonalElevation = 0.dp,
                        shadowElevation = 6.dp,
                        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.65f)),
                        modifier = Modifier.widthIn(min = 156.dp, max = 210.dp)
                    ) {
                        val normalOverflow = actionSplit.overflow.filter { it != WorkItemActionEnum.DELETE }
                        val hasDelete = WorkItemActionEnum.DELETE in actionSplit.overflow

                        normalOverflow.forEach { action ->
                            WorkCardMenuRow(
                                label = action.label,
                                icon = actionIcon(action),
                                onClick = {
                                    menuExpanded = false
                                    onActionClick(action)
                                }
                            )
                        }

                        if (normalOverflow.isNotEmpty()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color = colors.outlineVariant.copy(alpha = 0.5f)
                            )
                        }

                        WorkCardMenuRow(
                            label = if (isArchived) "移出归档" else "加入归档",
                            icon = if (isArchived) Icons.Filled.Unarchive else Icons.Filled.Archive,
                            onClick = {
                                menuExpanded = false
                                onToggleArchive()
                            }
                        )

                        if (hasDelete) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color = colors.outlineVariant.copy(alpha = 0.5f)
                            )
                            WorkCardMenuRow(
                                label = WorkItemActionEnum.DELETE.label,
                                icon = Icons.Filled.Delete,
                                isDestructive = true,
                                onClick = {
                                    menuExpanded = false
                                    onActionClick(WorkItemActionEnum.DELETE)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 卡片悬浮菜单项：圆角内嵌高亮行，与卡片视觉语言保持一致。
 */
@Composable
private fun WorkCardMenuRow(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    val colors = LocalAppColors.current
    val interaction = remember { MutableInteractionSource() }
    val isHovered by interaction.collectIsHoveredAsState()

    val hoverBg = if (isDestructive) {
        colors.scheme.errorContainer.copy(alpha = 0.65f)
    } else {
        colors.surfaceContainerHighest
    }
    val contentColor = if (isDestructive) colors.error else colors.onSurface
    val iconColor = if (isDestructive) colors.error else colors.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 1.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isHovered) hoverBg else Color.Transparent)
            .hoverable(interaction)
            .clickable(
                interactionSource = interaction,
                indication = ripple(),
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PricePill(price: Int, priceType: String) {
    val colors = LocalAppColors.current
    val isFree = price <= 0
    val text = priceText(price, priceType)
    val bg = if (isFree) colors.surfaceContainerHighest else colors.secondaryContainer
    val fg = if (isFree) colors.onSurfaceVariant else colors.onSecondaryContainer

    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.tabularNumbers(),
        fontWeight = FontWeight.Bold,
        color = fg,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
private fun MetaTag(
    text: String,
    containerColor: Color = LocalAppColors.current.surfaceContainerHighest,
    contentColor: Color = LocalAppColors.current.onSurfaceVariant
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Medium,
        color = contentColor,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(containerColor)
            .padding(horizontal = 7.dp, vertical = 3.dp)
    )
}

@Composable
private fun CardPrimaryActionButton(
    action: WorkItemActionEnum,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    val interaction = remember { MutableInteractionSource() }
    val isHovered by interaction.collectIsHoveredAsState()
    val containerColor = colors.primaryContainer
    val contentColor = colors.onPrimaryContainer

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(containerColor)
            .hoverable(interaction)
            .clickable(
                interactionSource = interaction,
                indication = ripple(),
                onClick = onClick
            )
    ) {
        if (isHovered) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(contentColor.copy(alpha = 0.08f))
            )
        }
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = actionIcon(action),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = action.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CardSubtleActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    val interaction = remember { MutableInteractionSource() }
    val isHovered by interaction.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surfaceContainerHighest)
            .border(
                1.dp,
                colors.outlineVariant.copy(alpha = 0.55f),
                RoundedCornerShape(10.dp)
            )
            .hoverable(interaction)
            .clickable(
                interactionSource = interaction,
                indication = ripple(),
                onClick = onClick
            )
    ) {
        if (isHovered) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(colors.onSurface.copy(alpha = 0.08f))
            )
        }
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface,
                maxLines = 1
            )
        }
    }
}

private fun actionIcon(action: WorkItemActionEnum): ImageVector = when (action) {
    WorkItemActionEnum.SUBMIT_REVIEW,
    WorkItemActionEnum.SUBMIT_SELF_TEST -> Icons.Default.Check

    WorkItemActionEnum.UPDATE,
    WorkItemActionEnum.CANCEL_TEST,
    WorkItemActionEnum.CANCEL_REVIEW -> Icons.Default.Refresh

    WorkItemActionEnum.PUBLISH -> IconPack.Sale
    WorkItemActionEnum.ADJUST_PRICE -> IconPack.Modified
    WorkItemActionEnum.VIEW_FEEDBACK -> IconPack.Feedback
    WorkItemActionEnum.APPOINT_ONLINE -> IconPack.Calendar
    WorkItemActionEnum.DELETE -> Icons.Default.Delete
    WorkItemActionEnum.VIEW_DETAIL -> IconPack.Show
}

private fun priceText(price: Int, priceType: String): String {
    if (price <= 0) return "免费"
    val unit = priceTypeLabel(priceType)
    return if (unit.isEmpty()) "$price" else "$price $unit"
}

/**
 * 组装卡片展示的时间项列表：创建时间常驻，提审时间与上线时间在有效时依次展示。
 */
internal fun buildWorkTimeEntries(item: ResourceData): List<String> = buildList {
    add("创建 ${formatDate(item.createTime)}")
    if (isValidTime(item.applyReviewTime)) {
        add("提审 ${formatDate(item.applyReviewTime)}")
    }
    if (isValidTime(item.onlineTime)) {
        add("上线 ${formatDate(item.onlineTime)}")
    }
}

private fun isValidTime(raw: String): Boolean {
    val trimmed = raw.trim()
    return trimmed.isNotEmpty() && !trimmed.equals("UNKNOWN", ignoreCase = true)
}

private fun formatDate(iso: String): String {
    val trimmed = iso.trim()
    if (trimmed.isEmpty() || trimmed.equals("UNKNOWN", ignoreCase = true)) return "—"
    return if (trimmed.length >= 10) trimmed.substring(0, 10) else trimmed
}
