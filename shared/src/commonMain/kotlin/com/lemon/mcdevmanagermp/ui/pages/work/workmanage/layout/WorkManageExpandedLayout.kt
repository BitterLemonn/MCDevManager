package com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.consts.enums.WorkItemActionEnum
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import com.lemon.mcdevmanagermp.ui.components.FinanceSkeletonCard
import com.lemon.mcdevmanagermp.ui.components.tabularNumbers
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.WorkManageAction
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.WorkManageScope
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.WorkManageState
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.WorkStatusFilter
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component.ArchiveInfoBanner
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component.HeroArchiveActionChip
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component.WorkManageCard
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component.WorkManageEmptyStateCard
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component.WorkManageScopeSwitch
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component.WorkManageSearchField
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component.WorkManageStatusSummaryGrid
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component.WorkManageTopBar
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

/**
 * Expanded (>= 840dp) 宽屏双栏布局（对齐新版收益管理页视觉结构）：
 * 左栏（340dp）：紧凑总览头卡、资源列表/已归档视图切换、2×2 状态统计与状态筛选导航；
 * 右栏（自适应多列网格）：固定搜索与结果计数工具栏 + 自适应多列资源卡片网格。
 */
@Composable
internal fun WorkManageExpandedLayout(
    state: WorkManageState,
    onAction: (WorkManageAction) -> Unit,
    onBack: () -> Unit,
    onAddWork: () -> Unit,
    onCardAction: (ResourceData, WorkItemActionEnum) -> Unit,
    onToggleArchive: (ResourceData) -> Unit,
    onOpenAddArchive: () -> Unit,
    statusBarTop: Dp,
    navBarBottom: Dp
) {
    val colors = LocalAppColors.current
    val summary = state.summary
    val filteredItems = state.filteredItems
    val isArchivedScope = state.scope == WorkManageScope.ARCHIVED

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = navBarBottom)
    ) {
        WorkManageTopBar(
            collapseFraction = 0f,
            isRefreshing = state.isLoading || state.isRefreshing,
            canAddArchive = summary.activeCount > 0,
            onBack = onBack,
            onAddWork = onAddWork,
            onOpenAddArchive = onOpenAddArchive,
            onRefresh = { onAction(WorkManageAction.RefreshData) }
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 左侧概览与筛选侧栏
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(340.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 紧凑主视觉摘要卡
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.primaryContainer)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Icon(
                                imageVector = if (isArchivedScope) Icons.Filled.Archive else Icons.Filled.Inventory2,
                                contentDescription = null,
                                tint = colors.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isArchivedScope) "已归档资源" else "活跃资源",
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.onPrimaryContainer.copy(alpha = 0.84f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "${if (isArchivedScope) summary.archivedCount else summary.activeCount}",
                            style = MaterialTheme.typography.headlineSmall.tabularNumbers(),
                            fontWeight = FontWeight.Bold,
                            color = colors.onPrimaryContainer
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "全部 ${summary.totalCount} · 已归档 ${summary.archivedCount}",
                            style = MaterialTheme.typography.labelSmall.tabularNumbers(),
                            color = colors.onPrimaryContainer.copy(alpha = 0.76f)
                        )
                        if (summary.activeCount > 0) {
                            HeroArchiveActionChip(onClick = onOpenAddArchive)
                        }
                    }
                }

                // 资源列表 / 已归档 分段切换
                WorkManageScopeSwitch(
                    selectedScope = state.scope,
                    activeCount = summary.activeCount,
                    archivedCount = summary.archivedCount,
                    onSelectScope = { onAction(WorkManageAction.SelectScope(it)) }
                )

                // 2×2 状态统计块（点击联动筛选）
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surfaceContainerHigh)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "状态概览",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurfaceVariant
                    )
                    WorkManageStatusSummaryGrid(
                        summary = summary,
                        selectedFilter = state.statusFilter,
                        onSelectFilter = { onAction(WorkManageAction.SelectStatusFilter(it)) }
                    )
                }

                // 纵向状态筛选菜单
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surfaceContainerHigh)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "快捷筛选",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                    WorkStatusFilter.entries.forEach { option ->
                        SidebarFilterRow(
                            label = option.label,
                            count = state.countForFilter(option),
                            selected = state.statusFilter == option,
                            isAttention = option == WorkStatusFilter.ATTENTION,
                            onClick = { onAction(WorkManageAction.SelectStatusFilter(option)) }
                        )
                    }
                }

                if (isArchivedScope) {
                    ArchiveInfoBanner(
                        canAddArchive = summary.activeCount > 0,
                        onOpenAddArchive = onOpenAddArchive
                    )
                }

                Spacer(Modifier.height(8.dp))
            }

            VerticalDivider(
                modifier = Modifier.fillMaxHeight().padding(vertical = 4.dp),
                color = colors.outlineVariant.copy(alpha = 0.6f),
                thickness = 1.dp
            )

            // 右侧主内容区：搜索工具栏 + 自适应多列网格
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    WorkManageSearchField(
                        query = state.searchQuery,
                        onQueryChange = { onAction(WorkManageAction.UpdateSearchQuery(it)) },
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "共 ${filteredItems.size} 项",
                        style = MaterialTheme.typography.labelLarge.tabularNumbers(),
                        color = colors.onSurfaceVariant
                    )

                    if (state.hasActiveFilters) {
                        OutlinedButton(
                            onClick = { onAction(WorkManageAction.ClearFilters) },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, colors.outlineVariant),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("清空筛选", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                when {
                    state.isLoading && state.items.isEmpty() -> {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 310.dp),
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(6, key = { "skeleton_$it" }) {
                                FinanceSkeletonCard(lineCount = 3)
                            }
                        }
                    }

                    filteredItems.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            WorkManageEmptyStateCard(
                                state = state,
                                onClearFilters = { onAction(WorkManageAction.ClearFilters) },
                                onOpenAddArchive = onOpenAddArchive,
                                onSwitchToArchived = {
                                    onAction(WorkManageAction.SelectScope(WorkManageScope.ARCHIVED))
                                },
                                onAddWork = onAddWork
                            )
                        }
                    }

                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 310.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(filteredItems, key = { it.itemId }) { item ->
                                WorkManageCard(
                                    item = item,
                                    isArchived = item.itemId in state.archivedItemIds,
                                    onActionClick = { action -> onCardAction(item, action) },
                                    onToggleArchive = { onToggleArchive(item) }
                                )
                            }
                            item(key = "bottom_spacer", span = { GridItemSpan(maxLineSpan) }) {
                                Spacer(Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SidebarFilterRow(
    label: String,
    count: Int,
    selected: Boolean,
    isAttention: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    val interaction = remember { MutableInteractionSource() }
    val isHovered by interaction.collectIsHoveredAsState()

    val bg by animateColorAsState(
        targetValue = when {
            selected && isAttention -> colors.scheme.errorContainer
            selected -> colors.secondaryContainer
            isHovered -> colors.surfaceContainerHighest
            else -> Color.Transparent
        },
        label = "sidebar_filter_bg"
    )
    val fg by animateColorAsState(
        targetValue = when {
            selected && isAttention -> colors.scheme.onErrorContainer
            selected -> colors.onSecondaryContainer
            else -> colors.onSurface
        },
        label = "sidebar_filter_fg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .hoverable(interaction)
            .clickable(
                interactionSource = interaction,
                indication = ripple(),
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = fg,
                    modifier = Modifier.size(15.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = fg
            )
        }
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelMedium.tabularNumbers(),
            fontWeight = FontWeight.SemiBold,
            color = fg.copy(alpha = if (selected) 0.9f else 0.7f),
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(
                    if (selected) fg.copy(alpha = 0.12f)
                    else colors.surfaceContainerHighest
                )
                .padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}
