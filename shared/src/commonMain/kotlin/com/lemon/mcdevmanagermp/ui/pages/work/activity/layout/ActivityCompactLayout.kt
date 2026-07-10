package com.lemon.mcdevmanagermp.ui.pages.work.activity.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.lemon.mcdevmanagermp.utils.extension.toDateString
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_refresh
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun ActivityCompactLayout(
    state: ActivityState,
    onAction: (ActivityAction) -> Unit,
    onBack: () -> Unit,
    onItemClick: (ActivityReviewItemVO) -> Unit
) {
    val colors = LocalAppColors.current
    val listState = rememberLazyListState()

    val scrollAlpha = remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 1f
            else (listState.firstVisibleItemScrollOffset / 100f).coerceIn(0f, 1f)
        }
    }

    // 触发加载更多
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            state.hasMore && !state.isLoading && totalItems > 0 && lastVisibleIndex >= totalItems - 2
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            onAction(ActivityAction.LoadMore)
        }
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
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(36.dp),
                    color = colors.primary,
                    strokeWidth = 3.dp
                )
            }
        } else if (state.activities.isEmpty() && !state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📋",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Text(
                        text = "暂无活动数据",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.activities, key = { it.id }) { activity ->
                    ActivityCard(
                        activity = activity,
                        onClick = { onItemClick(activity) }
                    )
                }

                if (state.hasMore && state.isLoading) {
                    item {
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
                }
            }
        }
    }
}

@Composable
internal fun ActivityStatusTag(status: String) {
    val colors = LocalAppColors.current
    val (bgColor, textColor, label) = when (status) {
        "active", "进行中" -> Triple(
            colors.primary.copy(alpha = 0.15f),
            colors.primary,
            "进行中"
        )

        "ended", "已结束" -> Triple(
            colors.onSurfaceVariant.copy(alpha = 0.15f),
            colors.onSurfaceVariant,
            "已结束"
        )

        "upcoming", "未开始" -> Triple(
            colors.textColor.copy(alpha = 0.10f),
            colors.textColor,
            "未开始"
        )

        else -> Triple(
            colors.onSurfaceVariant.copy(alpha = 0.15f),
            colors.onSurfaceVariant,
            status.ifEmpty { "未知" }
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

internal fun formatTimeRange(beginAt: Int, endAt: Int): String {
    if (beginAt == 0 && endAt == 0) return ""
    val begin = if (beginAt > 0) beginAt.toLong().toDateString() else "未知"
    val end = if (endAt > 0) endAt.toLong().toDateString() else "未知"
    return "$begin ~ $end"
}

