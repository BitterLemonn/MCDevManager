package com.lemon.mcdevmanagermp.ui.pages.community.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ComposableImageOptions
import com.lemon.mcdevmanagermp.data.vo.netease.feedback.ConflictModsVO
import com.lemon.mcdevmanagermp.data.vo.netease.feedback.FeedbackData
import com.lemon.mcdevmanagermp.platform.BackHandler
import com.lemon.mcdevmanagermp.ui.components.CollapsingTopBar
import com.lemon.mcdevmanagermp.ui.components.ImagePreviewOverlay
import com.lemon.mcdevmanagermp.ui.components.LocalWindowWidthSizeClass
import com.lemon.mcdevmanagermp.ui.components.collectUiEffect
import com.lemon.mcdevmanagermp.ui.iconpack.IconPack
import com.lemon.mcdevmanagermp.ui.iconpack.Replied
import com.lemon.mcdevmanagermp.ui.pages.community.components.FilterChipItem
import com.lemon.mcdevmanagermp.ui.pages.community.components.FilterGroupDef
import com.lemon.mcdevmanagermp.ui.pages.community.components.ModernFilterBar
import com.lemon.mcdevmanagermp.ui.pages.community.components.ReplyInputBar
import com.lemon.mcdevmanagermp.ui.pages.community.feedback.layout.CompactFeedbackLayout
import com.lemon.mcdevmanagermp.ui.pages.community.feedback.layout.ExpandedFeedbackLayout
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import com.lemon.mcdevmanagermp.utils.extension.toDateTimeString
import kotlinx.serialization.json.Json

// ============================================================
// FeedbackType enum
// ============================================================

enum class FeedbackType(val label: String, val apiValue: String) {
    BUG("故障问题", "0"),
    SUGGESTION("玩法建议", "1"),
    COPYRIGHT("内容侵权", "2"),
    OTHER("其他", "3"),
    CONFLICT("组件冲突", "4"),
    MY_HILL("我的山头", "5"),
    PERFORMANCE("性能反馈", "6");

    companion object {
        fun labelOf(value: String): String = entries.find { it.apiValue == value }?.label ?: "未知"
    }
}

// ============================================================
// JSON parser for conflict content
// ============================================================

private val conflictJson = Json { ignoreUnknownKeys = true }

private fun parseConflictContent(content: String): ConflictModsVO? {
    return try {
        if (content.trimStart().startsWith("{")) conflictJson.decodeFromString<ConflictModsVO>(content) else null
    } catch (_: Exception) { null }
}

// ============================================================
// Main Page
// ============================================================

@Composable
fun FeedbackPage(onBack: () -> Unit) {
    val viewModel = remember { FeedbackViewModel() }
    val state by viewModel.state.collectAsState()
    val colors = LocalAppColors.current
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    BackHandler(enabled = state.selectedFeedback != null) {
        viewModel.dispatch(FeedbackAction.SelectFeedback(null))
    }

    viewModel.effect.collectUiEffect { effect ->
        when (effect) {
            is FeedbackEffect.ShowToast -> showToast(effect.message)
            FeedbackEffect.ReplySuccess -> showToast("回复成功")
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        val widthSizeClass = LocalWindowWidthSizeClass.current

        if (widthSizeClass == WindowWidthSizeClass.Expanded) {
            ExpandedFeedbackLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack,
                statusBarTop = statusBarTop,
                navBarBottom = navBarBottom
            )
        } else {
            CompactFeedbackLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack,
                statusBarTop = statusBarTop,
                navBarBottom = navBarBottom
            )
        }

    }
}

// ============================================================
// Filter bar for feedback
// ============================================================

@Composable
internal fun FeedbackFilterBar(
    state: FeedbackState,
    onAction: (FeedbackAction) -> Unit,
) {
    val colors = LocalAppColors.current

    val filterGroups = buildList {
        add(FilterGroupDef(
            "type", "类型",
            FeedbackType.entries.map { it.label },
            selectedValues = setOfNotNull(state.filterType?.let { apiVal ->
                FeedbackType.entries.find { e -> e.apiValue == apiVal }?.label
            })
        ))
    }

    ModernFilterBar(
        groups = filterGroups,
        searchQuery = state.searchKey,
        onSearchChange = { onAction(FeedbackAction.UpdateSearchKey(it)) },
        searchPlaceholder = "搜索昵称、内容、组件名...",
        onToggleFilter = { key, value ->
            when (key) {
                "type" -> {
                    val typeEntry = FeedbackType.entries.find { it.label == value }
                    val currentType = state.filterType
                    onAction(FeedbackAction.UpdateFilterType(
                        if (currentType == typeEntry?.apiValue) null else typeEntry?.apiValue
                    ))
                }
            }
        },
        onClearAll = { onAction(FeedbackAction.ClearFilters) },
        isExpanded = state.isFilterExpanded,
        onToggleExpanded = { onAction(FeedbackAction.ToggleFilterPanel) },
        expandedExtras = {
            Text(
                text = "回复状态",
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChipItem(
                    text = "已回复",
                    isSelected = state.filterReplied == true,
                    onClick = { onAction(FeedbackAction.UpdateFilterReplied(true)) }
                )
                FilterChipItem(
                    text = "未回复",
                    isSelected = state.filterReplied == false,
                    onClick = { onAction(FeedbackAction.UpdateFilterReplied(false)) }
                )
            }
        },
    )
}

// ============================================================
// Shared Components
// ============================================================

@Composable
internal fun FeedbackTopBar(onBack: () -> Unit, onRefresh: () -> Unit) {
    CollapsingTopBar(
        title = "玩家反馈",
        collapseFraction = 0f,
        onBack = onBack,
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "刷新",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    )
}

@Composable
internal fun FeedbackListContent(
    state: FeedbackState,
    onAction: (FeedbackAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    val filteredList = remember(state.feedbackList, state.filterReplied) {
        var list = state.feedbackList
        if (state.filterReplied != null) {
            list = list.filter { (it.reply != null) == state.filterReplied }
        }
        list
    }

    if (state.isLoading && state.feedbackList.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(32.dp), color = colors.primary, strokeWidth = 3.dp)
        }
        return
    }

    if (state.feedbackList.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("暂无反馈数据", style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
        }
        return
    }

    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= listState.layoutInfo.totalItemsCount - 3
        }.collect { nearEnd ->
            val hasClientFilter = state.filterReplied != null
            if (nearEnd && !state.isLoadOver && !state.isLoading && !hasClientFilter) {
                onAction(FeedbackAction.LoadMore)
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filteredList.size) { index ->
            val feedback = filteredList[index]
            FeedbackSummaryCard(
                feedback = feedback,
                isSelected = state.selectedFeedback?.id == feedback.id,
                onClick = { onAction(FeedbackAction.SelectFeedback(feedback)) }
            )
        }

        if (state.isLoading && !state.isLoadOver) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colors.primary, strokeWidth = 2.dp)
                }
            }
        }

        if (state.isLoadOver && filteredList.isNotEmpty()) {
            item {
                Text(
                    text = "已加载全部反馈",
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
private fun FeedbackSummaryCard(
    feedback: FeedbackData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current

    val conflictData = parseConflictContent(feedback.content)
    val displayContent = if (conflictData != null) {
        "冲突模组: ${conflictData.itemList.joinToString(", ") { it.name }}"
    } else {
        feedback.content
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val cardColor = when {
        isSelected -> colors.primary.copy(alpha = 0.08f)
        isHovered -> colors.surfaceContainerHighest
        else -> colors.surfaceContainerHigh
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = feedback.commitNickname,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = feedback.createTime.toDateTimeString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = displayContent,
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
                if (feedback.resName.isNotBlank()) {
                    ChipLabel(feedback.resName, colors.primary)
                }
                ChipLabel(feedback.type, colors.secondary)
                if (feedback.reply != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = IconPack.Replied,
                            contentDescription = "已回复",
                            tint = colors.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = "已回复",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                if (feedback.picList.isNotEmpty()) {
                    Text(
                        text = "${feedback.picList.size}图",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
internal fun FeedbackDetailPanel(
    feedback: FeedbackData,
    replyText: String,
    isReplying: Boolean,
    onReplyTextChange: (String) -> Unit,
    onSubmitReply: () -> Unit,
    onBack: () -> Unit,
    navBarBottom: Dp,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val conflictData = parseConflictContent(feedback.content)
    var viewerImageUrl by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(colors.background)) {
            CollapsingTopBar(
                title = feedback.resName,
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
                            text = feedback.commitNickname,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textColor
                        )
                        Text(
                            text = "UID: ${feedback.commitUid}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                    Text(
                        text = feedback.createTime.toDateTimeString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (feedback.resName.isNotBlank()) {
                        ChipLabel(feedback.resName, colors.primary)
                    }
                    ChipLabel(feedback.type, colors.secondary)
                }

                if (conflictData != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.surfaceContainerHighest, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "冲突模组",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        conflictData.itemList.forEach { mod ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = mod.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textColor
                                )
                                if (mod.iid != null) {
                                    Text(
                                        text = "IID: ${mod.iid}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        if (conflictData.detail != null) {
                            Text(
                                text = conflictData.detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                }

                HorizontalDivider(color = colors.outlineVariant, thickness = 1.dp)

                if (conflictData == null) {
                    Text(
                        text = feedback.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textColor,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4
                    )
                }

                if (feedback.picList.isNotEmpty()) {
                    Text(
                        text = "附件图片",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        feedback.picList.forEach { url ->
                            AsyncImage(
                                uri = url,
                                state = rememberAsyncImageState(ComposableImageOptions {
                                    sizeMultiplier(5.0f)
                                }),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewerImageUrl = url },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                if (feedback.reply != null) {
                    HorizontalDivider(color = colors.outlineVariant, thickness = 1.dp)
                    Text(
                        text = "开发者回复",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = feedback.reply,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textColor.copy(alpha = 0.85f)
                    )
                }
            }

            if (!feedback.forbidReply) {
                HorizontalDivider(color = colors.outlineVariant, thickness = 1.dp)
                ReplyInputBar(
                    value = replyText,
                    onValueChange = onReplyTextChange,
                    onSubmit = onSubmitReply,
                    placeholder = "回复 ${feedback.commitNickname}...",
                    isEnabled = !isReplying
                )
            } else {
                Surface(color = colors.surfaceContainerHigh) {
                    Text(
                        text = "该反馈不允许回复",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(navBarBottom))
        }

        val currentViewerUrl = viewerImageUrl
        if (currentViewerUrl != null) {
            ImagePreviewOverlay(
                imageUrl = currentViewerUrl,
                onDismiss = { viewerImageUrl = null }
            )
        }
    }
}


// ============================================================
// Utility
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

