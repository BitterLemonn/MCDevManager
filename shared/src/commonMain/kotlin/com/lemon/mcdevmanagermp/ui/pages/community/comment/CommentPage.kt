package com.lemon.mcdevmanagermp.ui.pages.community.comment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.vo.netease.comment.CommentData
import com.lemon.mcdevmanagermp.platform.BackHandler
import com.lemon.mcdevmanagermp.ui.components.CollapsingTopBar
import com.lemon.mcdevmanagermp.ui.components.LocalWindowWidthSizeClass
import com.lemon.mcdevmanagermp.ui.components.collectUiEffect
import com.lemon.mcdevmanagermp.ui.pages.community.comment.layout.CompactCommentLayout
import com.lemon.mcdevmanagermp.ui.pages.community.comment.layout.ExpandedCommentLayout
import com.lemon.mcdevmanagermp.ui.pages.community.components.DateRangeChipGroup
import com.lemon.mcdevmanagermp.ui.pages.community.components.FilterGroupDef
import com.lemon.mcdevmanagermp.ui.pages.community.components.ModernFilterBar
import com.lemon.mcdevmanagermp.ui.pages.community.components.ReplyInputBar
import com.lemon.mcdevmanagermp.ui.pages.community.components.StarChipGroup
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import com.lemon.mcdevmanagermp.utils.extension.toDateTimeString
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_refresh
import mcdevmanagermpr.shared.generated.resources.ic_star
import org.jetbrains.compose.resources.painterResource

@Composable
fun CommentPage(onBack: () -> Unit) {
    val viewModel = remember { CommentViewModel() }
    val state by viewModel.state.collectAsState()
    val colors = LocalAppColors.current
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    viewModel.effect.collectUiEffect { effect ->
        when (effect) {
            is CommentEffect.ShowToast -> showToast(effect.message)
            CommentEffect.ReplySuccess -> showToast("回复成功")
        }
    }

    val allTags = remember(state.commentList) {
        state.commentList.map { it.commentTag }.distinct().filter { it.isNotBlank() }
    }

    BackHandler(enabled = state.selectedComment != null) {
        viewModel.dispatch(CommentAction.SelectComment(null))
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        val widthSizeClass = LocalWindowWidthSizeClass.current

        if (widthSizeClass == WindowWidthSizeClass.Expanded) {
            ExpandedCommentLayout(
                state = state,
                allTags = allTags,
                onAction = viewModel::dispatch,
                onBack = onBack,
                statusBarTop = statusBarTop,
                navBarBottom = navBarBottom
            )
        } else {
            CompactCommentLayout(
                state = state,
                allTags = allTags,
                onAction = viewModel::dispatch,
                onBack = onBack,
                statusBarTop = statusBarTop,
                navBarBottom = navBarBottom
            )
        }

    }
}

// ============================================================
// Filter bar for comments
// ============================================================

@Composable
internal fun CommentFilterBar(
    state: CommentState,
    allTags: List<String>,
    onAction: (CommentAction) -> Unit,
) {
    val filterGroups = buildList {
        if (allTags.isNotEmpty()) {
            add(FilterGroupDef("tag", "组件标签", allTags, setOfNotNull(state.filterTag)))
        }
    }

    ModernFilterBar(
        groups = filterGroups,
        searchQuery = state.searchKey,
        onSearchChange = { onAction(CommentAction.UpdateSearchKey(it)) },
        searchPlaceholder = "搜索昵称、内容、组件名...",
        onToggleFilter = { key, value ->
            when (key) {
                "tag" -> onAction(CommentAction.UpdateFilterTag(
                    if (state.filterTag == value) null else value
                ))
            }
        },
        onClearAll = { onAction(CommentAction.ClearFilters) },
        isExpanded = state.isFilterExpanded,
        onToggleExpanded = { onAction(CommentAction.ToggleFilterPanel) },
        expandedExtras = {
            StarChipGroup(
                selectedStars = state.filterStars,
                onToggle = { onAction(CommentAction.ToggleFilterStars(it)) }
            )
            Spacer(Modifier.height(10.dp))
            DateRangeChipGroup(
                selectedRange = state.dateRange,
                onSelect = { onAction(CommentAction.UpdateDateRange(it)) }
            )
        },
    )
}

// ============================================================
// Shared Components
// ============================================================

@Composable
internal fun CommentTopBar(
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    val colors = LocalAppColors.current
    CollapsingTopBar(
        title = "组件评论",
        collapseFraction = 0f,
        onBack = onBack,
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(
                    painter = painterResource(Res.drawable.ic_refresh),
                    contentDescription = "刷新",
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    )
}

// ============================================================
// List & Cards (with client-side filtering)
// ============================================================

@Composable
internal fun CommentListContent(
    state: CommentState,
    onAction: (CommentAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    // Apply client-side filters
    val filteredList = remember(state.commentList, state.filterStars) {
        var list = state.commentList
        if (state.filterStars.isNotEmpty()) {
            list = list.filter { it.stars in state.filterStars }
        }
        list
    }

    if (state.isLoading && state.commentList.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp), color = colors.primary, strokeWidth = 3.dp
            )
        }
        return
    }

    if (state.commentList.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = "暂无评论数据",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurfaceVariant
            )
        }
        return
    }

    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleItem >= totalItems - 3
        }.collect { nearEnd ->
            if (nearEnd && !state.isLoadOver && !state.isLoading && state.filterStars.isEmpty()) {
                onAction(CommentAction.LoadMore)
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filteredList.size) { index ->
            val comment = filteredList[index]
            CommentSummaryCard(
                comment = comment,
                isSelected = state.selectedComment?.id == comment.id,
                onClick = { onAction(CommentAction.SelectComment(comment)) }
            )
        }

        if (state.isLoading && !state.isLoadOver) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp), color = colors.primary, strokeWidth = 2.dp
                    )
                }
            }
        }

        if (state.isLoadOver && filteredList.isNotEmpty()) {
            item {
                Text(
                    text = "已加载全部评论",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CommentSummaryCard(
    comment: CommentData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val cardColor = when {
        isSelected -> colors.primary.copy(alpha = 0.08f)
        isHovered -> colors.surfaceContainerHighest
        else -> colors.surfaceContainerHigh
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        colors = CardDefaults.elevatedCardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.nickname,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = comment.publishTime.toDateTimeString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = comment.userComment,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textColor.copy(alpha = 0.85f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChipLabel(comment.resName, colors.primary)
                if (comment.commentTag.isNotBlank()) {
                    ChipLabel(comment.commentTag, colors.secondary)
                }
                StarRating(comment.stars)
            }
        }
    }
}

@Composable
internal fun CommentDetailPanel(
    comment: CommentData,
    replyText: String,
    isReplying: Boolean,
    onReplyTextChange: (String) -> Unit,
    onSubmitReply: () -> Unit,
    onBack: () -> Unit,
    statusBarTop: androidx.compose.ui.unit.Dp,
    navBarBottom: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    Column(modifier = modifier.fillMaxSize().background(colors.background)) {
        CollapsingTopBar(
            title = comment.resName,
            collapseFraction = 0f,
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = comment.nickname,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textColor
                    )
                    Text(
                        text = "UID: ${comment.uid}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = comment.publishTime.toDateTimeString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    StarRating(comment.stars)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ChipLabel(comment.resName, colors.primary)
                if (comment.commentTag.isNotBlank()) {
                    ChipLabel(comment.commentTag, colors.secondary)
                }
            }

            HorizontalDivider(color = colors.outlineVariant, thickness = 1.dp)

            Text(
                text = comment.userComment,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textColor,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4
            )
        }

        HorizontalDivider(color = colors.outlineVariant, thickness = 1.dp)
        ReplyInputBar(
            value = replyText,
            onValueChange = onReplyTextChange,
            onSubmit = onSubmitReply,
            placeholder = "回复 ${comment.nickname}...",
            isEnabled = !isReplying
        )
        Spacer(Modifier.height(navBarBottom))
    }
}

// ============================================================
// Utility composables
// ============================================================

@Composable
private fun ChipLabel(text: String, color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StarRating(stars: String) {
    val starCount = stars.toIntOrNull() ?: 0
    if (starCount <= 0) return
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(Res.drawable.ic_star),
            contentDescription = null,
            tint = androidx.compose.ui.graphics.Color(0xFFFFA000),
            modifier = Modifier.size(12.dp)
        )
        Spacer(Modifier.width(2.dp))
        Text(
            text = "$starCount",
            style = MaterialTheme.typography.labelSmall,
            color = androidx.compose.ui.graphics.Color(0xFFFFA000),
            fontWeight = FontWeight.Medium
        )
    }
}

