package com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.consts.enums.WorkItemActionEnum
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import com.lemon.mcdevmanagermp.ui.components.FinanceSkeletonCard
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.WorkManageAction
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.WorkManageScope
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.WorkManageState
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component.WorkManageCard
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component.WorkManageControlBar
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component.WorkManageEmptyStateCard
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component.WorkManageOverviewCard
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component.WorkManageTopBar

/**
 * Medium (600~840dp) 资源管理布局：
 * 概览卡与控制栏跨满两列，下方作品卡片以双列等宽网格排布。
 */
@Composable
internal fun WorkManageMediumLayout(
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
    val gridState = rememberLazyGridState()
    val collapseFraction by remember {
        derivedStateOf {
            if (gridState.firstVisibleItemIndex > 0) 1f
            else (gridState.firstVisibleItemScrollOffset / 120f).coerceIn(0f, 1f)
        }
    }
    val filteredItems = state.filteredItems

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = statusBarTop + 60.dp,
                bottom = navBarBottom + 20.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "overview", span = { GridItemSpan(maxLineSpan) }) {
                WorkManageOverviewCard(
                    state = state,
                    onSelectFilter = { onAction(WorkManageAction.SelectStatusFilter(it)) },
                    onOpenAddArchive = onOpenAddArchive
                )
            }

            item(key = "control_bar", span = { GridItemSpan(maxLineSpan) }) {
                WorkManageControlBar(
                    state = state,
                    onSelectScope = { onAction(WorkManageAction.SelectScope(it)) },
                    onSelectFilter = { onAction(WorkManageAction.SelectStatusFilter(it)) },
                    onQueryChange = { onAction(WorkManageAction.UpdateSearchQuery(it)) },
                    onOpenAddArchive = onOpenAddArchive
                )
            }

            when {
                state.isLoading && state.items.isEmpty() -> {
                    items(4, key = { "skeleton_$it" }) {
                        FinanceSkeletonCard(lineCount = 3)
                    }
                }

                filteredItems.isEmpty() -> {
                    item(key = "empty", span = { GridItemSpan(maxLineSpan) }) {
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
                    items(filteredItems, key = { it.itemId }) { item ->
                        WorkManageCard(
                            item = item,
                            isArchived = item.itemId in state.archivedItemIds,
                            onActionClick = { action -> onCardAction(item, action) },
                            onToggleArchive = { onToggleArchive(item) }
                        )
                    }
                }
            }

            item(key = "bottom_spacer", span = { GridItemSpan(maxLineSpan) }) {
                Spacer(Modifier.height(4.dp))
            }
        }

        WorkManageTopBar(
            collapseFraction = collapseFraction,
            isRefreshing = state.isLoading || state.isRefreshing,
            canAddArchive = state.summary.activeCount > 0,
            onBack = onBack,
            onAddWork = onAddWork,
            onOpenAddArchive = onOpenAddArchive,
            onRefresh = { onAction(WorkManageAction.RefreshData) }
        )
    }
}
