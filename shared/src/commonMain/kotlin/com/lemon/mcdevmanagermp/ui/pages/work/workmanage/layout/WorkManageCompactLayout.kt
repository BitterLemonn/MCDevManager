package com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.consts.enums.WorkItemActionEnum
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.WorkManageAction
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.WorkManageScope
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.WorkManageState
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component.WorkManageCard
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component.WorkManageControlBar
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component.WorkManageEmptyStateCard
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component.WorkManageOverviewCard
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component.WorkManageSkeletonList
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component.WorkManageTopBar

/**
 * Compact (< 600dp) 资源管理布局：
 * 单一 LazyColumn 驱动顶部栏吸顶，自上而下依次展示概览统计卡、归档切换/搜索/筛选控制栏与作品卡片列表。
 */
@Composable
internal fun WorkManageCompactLayout(
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
    val listState = rememberLazyListState()
    val collapseFraction by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 1f
            else (listState.firstVisibleItemScrollOffset / 120f).coerceIn(0f, 1f)
        }
    }
    val filteredItems = state.filteredItems

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = statusBarTop + 60.dp,
                    bottom = navBarBottom + 20.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "overview") {
                    WorkManageOverviewCard(
                        state = state,
                        onSelectFilter = { onAction(WorkManageAction.SelectStatusFilter(it)) },
                        onOpenAddArchive = onOpenAddArchive
                    )
                }

                item(key = "control_bar") {
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
                        item(key = "skeleton") {
                            WorkManageSkeletonList(count = 4)
                        }
                    }

                    filteredItems.isEmpty() -> {
                        item(key = "empty") {
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

                item(key = "bottom_spacer") { Spacer(Modifier.height(4.dp)) }
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
