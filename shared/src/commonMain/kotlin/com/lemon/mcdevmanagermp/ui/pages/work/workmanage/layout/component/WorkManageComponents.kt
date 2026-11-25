package com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.consts.enums.WorkItemStatusEnum
import com.lemon.mcdevmanagermp.ui.components.CollapsingTopBar
import com.lemon.mcdevmanagermp.ui.components.FinanceBadgeTone
import com.lemon.mcdevmanagermp.ui.components.FinanceSkeletonCard
import com.lemon.mcdevmanagermp.ui.components.resolveColors
import com.lemon.mcdevmanagermp.ui.components.tabularNumbers
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.WorkManageScope
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.WorkManageState
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.WorkManageSummary
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.WorkStatusFilter
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

/**
 * 资源管理顶部栏：支持吸顶渐变、添加归档、新增作品与刷新操作。
 */
@Composable
internal fun WorkManageTopBar(
    collapseFraction: Float,
    isRefreshing: Boolean,
    canAddArchive: Boolean,
    onBack: () -> Unit,
    onAddWork: () -> Unit,
    onOpenAddArchive: () -> Unit,
    onRefresh: () -> Unit
) {
    CollapsingTopBar(
        title = "资源管理",
        collapseFraction = collapseFraction,
        onBack = onBack,
        actions = {
            if (canAddArchive) {
                IconButton(onClick = onOpenAddArchive) {
                    Icon(
                        imageVector = Icons.Filled.Archive,
                        contentDescription = "添加归档",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            IconButton(onClick = onAddWork) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "新增作品",
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(
                onClick = onRefresh,
                enabled = !isRefreshing
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = LocalContentColor.current
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "刷新",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    )
}

/**
 * 资源管理 Hero 概览卡（用于 Compact / Medium 布局顶部）：
 * 顶部采用 M3 primaryContainer 承载主统计与快捷添加归档入口，下方 2×2 可点击状态统计联动筛选。
 */
@Composable
internal fun WorkManageOverviewCard(
    state: WorkManageState,
    onSelectFilter: (WorkStatusFilter) -> Unit,
    onOpenAddArchive: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val summary = state.summary
    val isArchivedScope = state.scope == WorkManageScope.ARCHIVED

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 顶部主视觉区
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.primaryContainer)
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isArchivedScope) Icons.Filled.Archive else Icons.Filled.Inventory2,
                            contentDescription = null,
                            tint = colors.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isArchivedScope) "已归档资源" else "活跃资源",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.onPrimaryContainer.copy(alpha = 0.82f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${if (isArchivedScope) summary.archivedCount else summary.activeCount}",
                            style = MaterialTheme.typography.headlineMedium.tabularNumbers(),
                            fontWeight = FontWeight.Bold,
                            color = colors.onPrimaryContainer
                        )
                        Text(
                            text = "全部 ${summary.totalCount} · 已归档 ${summary.archivedCount}",
                            style = MaterialTheme.typography.labelMedium.tabularNumbers(),
                            color = colors.onPrimaryContainer.copy(alpha = 0.78f),
                            modifier = Modifier.padding(bottom = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (summary.activeCount > 0) {
                    Spacer(Modifier.width(12.dp))
                    HeroArchiveActionChip(
                        onClick = onOpenAddArchive
                    )
                }
            }

            // 下方 2×2 可点击状态统计网格
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WorkManageStatusSummaryGrid(
                    summary = summary,
                    selectedFilter = state.statusFilter,
                    onSelectFilter = onSelectFilter
                )
            }
        }
    }
}

/**
 * 2×2 可点击状态统计网格，Compact 概览卡与 Expanded 侧边栏复用。
 */
@Composable
internal fun WorkManageStatusSummaryGrid(
    summary: WorkManageSummary,
    selectedFilter: WorkStatusFilter,
    onSelectFilter: (WorkStatusFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusStatTile(
                label = "已上架",
                count = summary.onlineCount,
                tone = FinanceBadgeTone.SECONDARY,
                selected = selectedFilter == WorkStatusFilter.ONLINE,
                onClick = { onSelectFilter(WorkStatusFilter.ONLINE) },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            StatusStatTile(
                label = "审测/待上架",
                count = summary.inProgressCount,
                tone = FinanceBadgeTone.TERTIARY,
                selected = selectedFilter == WorkStatusFilter.IN_PROGRESS,
                onClick = { onSelectFilter(WorkStatusFilter.IN_PROGRESS) },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusStatTile(
                label = "待提审",
                count = summary.draftCount,
                tone = FinanceBadgeTone.PRIMARY,
                selected = selectedFilter == WorkStatusFilter.DRAFT,
                onClick = { onSelectFilter(WorkStatusFilter.DRAFT) },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            StatusStatTile(
                label = "异常/已下架",
                count = summary.attentionCount,
                tone = FinanceBadgeTone.ERROR,
                selected = selectedFilter == WorkStatusFilter.ATTENTION,
                onClick = { onSelectFilter(WorkStatusFilter.ATTENTION) },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
    }
}

@Composable
private fun StatusStatTile(
    label: String,
    count: Int,
    tone: FinanceBadgeTone,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val toneColors = tone.resolveColors()
    val interaction = remember { MutableInteractionSource() }
    val isHovered by interaction.collectIsHoveredAsState()

    val containerColor by animateColorAsState(
        targetValue = when {
            selected -> toneColors.container
            isHovered -> colors.secondaryContainer.copy(alpha = 0.45f)
            else -> colors.surfaceContainerHighest
        },
        label = "work_stat_tile_bg"
    )
    val valueColor = if (selected) toneColors.content else colors.onSurface
    val labelColor = if (selected) toneColors.content.copy(alpha = 0.82f) else colors.onSurfaceVariant

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .hoverable(interaction)
            .clickable(
                interactionSource = interaction,
                indication = ripple(),
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (selected) toneColors.content else toneColors.accent)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium.tabularNumbers(),
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = toneColors.content,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
internal fun HeroArchiveActionChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val interaction = remember { MutableInteractionSource() }
    val isHovered by interaction.collectIsHoveredAsState()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colors.primary)
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
                    .background(colors.onPrimary.copy(alpha = 0.08f))
            )
        }
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Archive,
                contentDescription = null,
                tint = colors.onPrimary,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = "添加归档",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.onPrimary
            )
        }
    }
}

/**
 * 视图分段切换器：资源列表 / 已归档，对齐新版收益页 M3 分段风格。
 */
@Composable
internal fun WorkManageScopeSwitch(
    selectedScope: WorkManageScope,
    activeCount: Int,
    archivedCount: Int,
    onSelectScope: (WorkManageScope) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceContainerHigh)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ScopeSegmentItem(
            label = WorkManageScope.ACTIVE.label,
            count = activeCount,
            selected = selectedScope == WorkManageScope.ACTIVE,
            onClick = { onSelectScope(WorkManageScope.ACTIVE) },
            modifier = Modifier.weight(1f)
        )
        ScopeSegmentItem(
            label = WorkManageScope.ARCHIVED.label,
            count = archivedCount,
            selected = selectedScope == WorkManageScope.ARCHIVED,
            onClick = { onSelectScope(WorkManageScope.ARCHIVED) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ScopeSegmentItem(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val interaction = remember { MutableInteractionSource() }
    val isHovered by interaction.collectIsHoveredAsState()
    val containerColor by animateColorAsState(
        targetValue = when {
            selected -> colors.secondaryContainer
            isHovered -> colors.surfaceContainerHighest
            else -> Color.Transparent
        },
        label = "work_scope_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) colors.onSecondaryContainer else colors.onSurfaceVariant,
        label = "work_scope_fg"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(containerColor)
            .hoverable(interaction)
            .clickable(
                interactionSource = interaction,
                indication = ripple(),
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(5.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = contentColor,
            maxLines = 1
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelSmall.tabularNumbers(),
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(
                    if (selected) colors.onSecondaryContainer.copy(alpha = 0.14f)
                    else colors.surfaceContainerHighest
                )
                .padding(horizontal = 6.dp, vertical = 1.dp)
        )
    }
}

/**
 * 现代圆角搜索框：支持按作品名称或作品 ID 搜索，带一键清空按钮。
 */
@Composable
internal fun WorkManageSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "搜索作品名称或 ID",
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceContainerHigh)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (query.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.textColor),
                cursorBrush = SolidColor(colors.primary),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (query.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable(onClick = { onQueryChange("") }),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "清空搜索",
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * 状态筛选横滑 Chips 行。
 */
@Composable
internal fun WorkManageStatusFilterRow(
    state: WorkManageState,
    onSelectFilter: (WorkStatusFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WorkStatusFilter.entries.forEach { option ->
            val selected = state.statusFilter == option
            val count = state.countForFilter(option)
            val isAttention = option == WorkStatusFilter.ATTENTION
            val interaction = remember { MutableInteractionSource() }
            val isHovered by interaction.collectIsHoveredAsState()

            val bg by animateColorAsState(
                targetValue = when {
                    selected && isAttention -> colors.scheme.errorContainer
                    selected -> colors.secondaryContainer
                    isHovered -> colors.surfaceContainerHighest
                    else -> Color.Transparent
                },
                label = "work_chip_bg"
            )
            val fg by animateColorAsState(
                targetValue = when {
                    selected && isAttention -> colors.scheme.onErrorContainer
                    selected -> colors.onSecondaryContainer
                    isAttention && count > 0 -> colors.error
                    else -> colors.onSurfaceVariant
                },
                label = "work_chip_fg"
            )
            val borderColor = when {
                selected -> Color.Transparent
                isAttention && count > 0 -> colors.error.copy(alpha = 0.5f)
                else -> colors.outlineVariant
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(bg)
                    .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                    .hoverable(interaction)
                    .clickable(
                        interactionSource = interaction,
                        indication = ripple(),
                        onClick = { onSelectFilter(option) }
                    )
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = fg,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = fg,
                    maxLines = 1
                )
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelSmall.tabularNumbers(),
                    fontWeight = FontWeight.SemiBold,
                    color = fg.copy(alpha = 0.8f)
                )
            }
        }
    }
}

/**
 * 组合控制栏（Compact / Medium 用）：视图分段切换 + 搜索框 + 状态筛选 + 已归档视图说明条。
 */
@Composable
internal fun WorkManageControlBar(
    state: WorkManageState,
    onSelectScope: (WorkManageScope) -> Unit,
    onSelectFilter: (WorkStatusFilter) -> Unit,
    onQueryChange: (String) -> Unit,
    onOpenAddArchive: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        WorkManageScopeSwitch(
            selectedScope = state.scope,
            activeCount = state.summary.activeCount,
            archivedCount = state.summary.archivedCount,
            onSelectScope = onSelectScope
        )
        WorkManageSearchField(
            query = state.searchQuery,
            onQueryChange = onQueryChange
        )
        WorkManageStatusFilterRow(
            state = state,
            onSelectFilter = onSelectFilter
        )
        if (state.scope == WorkManageScope.ARCHIVED) {
            ArchiveInfoBanner(
                canAddArchive = state.summary.activeCount > 0,
                onOpenAddArchive = onOpenAddArchive
            )
        }
    }
}

/**
 * 已归档视图说明横幅，说明归档仅本地生效，并提供添加归档快捷入口。
 */
@Composable
internal fun ArchiveInfoBanner(
    canAddArchive: Boolean,
    onOpenAddArchive: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceContainerHighest)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Archive,
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "归档仅保存在当前账号本地，不影响平台线上状态，可随时移出归档。",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        if (canAddArchive) {
            OutlinedButton(
                onClick = onOpenAddArchive,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, colors.outlineVariant),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "添加归档",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * 作品状态对应的 Material 3 语义色调，严格复用 ColorScheme 成对色，避免高饱和固定色在深浅色下刺眼。
 */
internal fun WorkItemStatusEnum.toBadgeTone(): FinanceBadgeTone = when (this) {
    WorkItemStatusEnum.ONLINE -> FinanceBadgeTone.SECONDARY
    WorkItemStatusEnum.INIT -> FinanceBadgeTone.PRIMARY
    WorkItemStatusEnum.SELF_TEST,
    WorkItemStatusEnum.SELF_TEST_PREPARE,
    WorkItemStatusEnum.REVIEWING,
    WorkItemStatusEnum.PREPARING,
    WorkItemStatusEnum.ACCEPT,
    WorkItemStatusEnum.ONLINE_PREPARING -> FinanceBadgeTone.TERTIARY

    WorkItemStatusEnum.REJECTED -> FinanceBadgeTone.ERROR
    WorkItemStatusEnum.OFFLINE,
    WorkItemStatusEnum.SYSTEM_OFFLINE,
    WorkItemStatusEnum.UNKNOWN -> FinanceBadgeTone.NEUTRAL
}

/**
 * 作品状态胶囊徽章：采用 Material 3 成对 Container / onContainer 配色，带柔和状态圆点。
 */
@Composable
internal fun WorkStatusBadge(
    status: WorkItemStatusEnum,
    modifier: Modifier = Modifier
) {
    val pair = status.toBadgeTone().resolveColors()

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(pair.container)
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(pair.accent)
        )
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = pair.content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 空态卡片：根据当前是否有筛选条件、是否处于归档视图提供精准引导操作。
 */
@Composable
internal fun WorkManageEmptyStateCard(
    state: WorkManageState,
    onClearFilters: () -> Unit,
    onOpenAddArchive: () -> Unit,
    onSwitchToArchived: () -> Unit,
    onAddWork: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val isArchivedScope = state.scope == WorkManageScope.ARCHIVED

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isArchivedScope) Icons.Filled.Archive else Icons.Filled.Inventory2,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            when {
                state.hasActiveFilters -> {
                    Text(
                        text = "未找到匹配的作品",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textColor
                    )
                    Text(
                        text = "尝试调整搜索关键词或清空当前状态筛选条件",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(Modifier.height(2.dp))
                    OutlinedButton(
                        onClick = onClearFilters,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, colors.outlineVariant),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary)
                    ) {
                        Text("清空筛选")
                    }
                }

                isArchivedScope -> {
                    Text(
                        text = "暂无已归档作品",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textColor
                    )
                    Text(
                        text = "可将停更、已下架或不常维护的作品加入归档，保持主列表清爽",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                    if (state.summary.activeCount > 0) {
                        Spacer(Modifier.height(2.dp))
                        Button(
                            onClick = onOpenAddArchive,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = colors.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Archive,
                                contentDescription = null,
                                tint = colors.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("添加归档")
                        }
                    }
                }

                state.summary.archivedCount > 0 -> {
                    Text(
                        text = "所有作品均已归档",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textColor
                    )
                    Text(
                        text = "当前有 ${state.summary.archivedCount} 个作品位于已归档列表",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(Modifier.height(2.dp))
                    OutlinedButton(
                        onClick = onSwitchToArchived,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, colors.outlineVariant),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary)
                    ) {
                        Text("查看已归档")
                    }
                }

                else -> {
                    Text(
                        text = "暂无作品",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textColor
                    )
                    Text(
                        text = "点击下方按钮创建你的第一个作品",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(Modifier.height(2.dp))
                    Button(
                        onClick = onAddWork,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = colors.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("新增作品")
                    }
                }
            }
        }
    }
}

/**
 * 加载骨架占位列表。
 */
@Composable
internal fun WorkManageSkeletonList(
    count: Int = 4,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(count) {
            FinanceSkeletonCard(lineCount = 3)
        }
    }
}
