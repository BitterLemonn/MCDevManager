package com.lemon.mcdevmanagermp.ui.pages.work.activity.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ActivityReviewItemVO
import com.lemon.mcdevmanagermp.ui.components.CollapsingTopBar
import com.lemon.mcdevmanagermp.ui.pages.work.activity.ActivityAction
import com.lemon.mcdevmanagermp.ui.pages.work.activity.ActivityState
import com.lemon.mcdevmanagermp.ui.pages.work.activity.layout.component.ActivityCard
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_refresh
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun ActivityMediumLayout(
    state: ActivityState,
    onAction: (ActivityAction) -> Unit,
    onBack: () -> Unit,
    onItemClick: (ActivityReviewItemVO) -> Unit
) {
    val colors = LocalAppColors.current
    val gridState = rememberLazyGridState()

    val scrollAlpha = remember {
        derivedStateOf {
            if (gridState.firstVisibleItemIndex > 0) 1f
            else (gridState.firstVisibleItemScrollOffset / 100f).coerceIn(0f, 1f)
        }
    }

    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = gridState.layoutInfo.totalItemsCount
            state.hasMore && !state.isLoading && totalItems > 0 && lastVisibleIndex >= totalItems - 3
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            onAction(ActivityAction.LoadMore)
        }
    }

    // 分组：进行中/审核中 和 已结束
    val (activeActivities, endedActivities) = remember(state.activities) {
        state.activities.partition { it.statusTag != "已结束" }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CollapsingTopBar(
            title = "作品活动",
            collapseFraction = scrollAlpha.value,
            onBack = onBack,
            actions = {
                IconButton(onClick = { onAction(ActivityAction.RefreshData) }) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_refresh),
                        contentDescription = "刷新",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        )

        if (state.isLoading && state.activities.isEmpty()) {
            LoadingOrEmpty(state = state)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 进行中/审核中分组
                if (activeActivities.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SectionHeader(
                            title = "进行中",
                            count = activeActivities.size,
                            isActive = true,
                            colors = colors
                        )
                    }
                    items(activeActivities, key = { "active_${it.id}" }) { activity ->
                        ActivityCard(
                            activity = activity,
                            onClick = { onItemClick(activity) }
                        )
                    }
                }

                // 已结束分组
                if (endedActivities.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SectionHeader(
                            title = "已结束",
                            count = endedActivities.size,
                            isActive = false,
                            colors = colors
                        )
                    }
                    items(endedActivities, key = { "ended_${it.id}" }) { activity ->
                        ActivityCard(
                            activity = activity,
                            onClick = { onItemClick(activity) }
                        )
                    }
                }

                if (state.hasMore && state.isLoading) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        LoadingMoreIndicator(colors)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    isActive: Boolean,
    colors: com.lemon.mcdevmanagermp.ui.theme.AppColors
) {
    val accentColor = if (isActive) colors.primary else colors.onSurfaceVariant
    val bgColor =
        if (isActive) colors.primary.copy(alpha = 0.08f) else colors.onSurfaceVariant.copy(alpha = 0.06f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧竖条
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accentColor)
        )
        Spacer(Modifier.width(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )

        Spacer(Modifier.width(8.dp))

        // 数量标签
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(bgColor)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = accentColor
            )
        }
    }
}

@Composable
private fun LoadingOrEmpty(state: ActivityState) {
    val colors = LocalAppColors.current
    if (state.activities.isEmpty() && !state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "📋", style = MaterialTheme.typography.displayMedium)
                Text(
                    text = "暂无活动数据",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun LoadingMoreIndicator(colors: com.lemon.mcdevmanagermp.ui.theme.AppColors) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = colors.primary,
            strokeWidth = 2.dp
        )
        Text(
            text = "加载中...",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
    }
}
