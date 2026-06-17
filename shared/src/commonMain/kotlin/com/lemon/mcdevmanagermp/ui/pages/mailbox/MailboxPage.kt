package com.lemon.mcdevmanagermp.ui.pages.mailbox

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.vo.netease.mailbox.MailListContentVO
import com.lemon.mcdevmanagermp.ui.components.CollapsingTopBar
import com.lemon.mcdevmanagermp.ui.components.LocalWindowWidthSizeClass
import com.lemon.mcdevmanagermp.ui.components.RichHtmlText
import com.lemon.mcdevmanagermp.ui.components.collectUiEffect
import com.lemon.mcdevmanagermp.ui.pages.mailbox.layout.CompactMailboxLayout
import com.lemon.mcdevmanagermp.ui.pages.mailbox.layout.ExpandedMailboxLayout
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import com.lemon.mcdevmanagermp.utils.extension.toReadableTime
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_refresh
import org.jetbrains.compose.resources.painterResource

@Composable
fun MailboxPage(onBack: () -> Unit) {
    val viewModel = remember { MailboxViewModel() }
    val state by viewModel.state.collectAsState()
    val colors = LocalAppColors.current
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    viewModel.effect.collectUiEffect { effect ->
        when (effect) {
            is MailboxEffect.ShowToast -> showToast(effect.message)
            MailboxEffect.NeedReLogin -> onBack()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatch(MailboxAction.LoadData)
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        val widthSizeClass = LocalWindowWidthSizeClass.current

        when (widthSizeClass) {
            WindowWidthSizeClass.Compact, WindowWidthSizeClass.Medium -> CompactMailboxLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack,
                statusBarTop = statusBarTop,
                navBarBottom = navBarBottom
            )

            else -> ExpandedMailboxLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack,
                statusBarTop = statusBarTop,
                navBarBottom = navBarBottom
            )
        }

        // 详情弹层（仅窄屏：Compact/Medium 用 overlay，Expanded 在布局内联展示）
        if (widthSizeClass == WindowWidthSizeClass.Compact ||
            widthSizeClass == WindowWidthSizeClass.Medium
        ) {
            MailDetailOverlay(state = state, onAction = viewModel::dispatch)
        }

        // 列表加载中 overlay
        if (state.isLoading && state.mailList.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center).size(36.dp),
                color = colors.primary,
                strokeWidth = 3.dp
            )
        }

        // 删除已读确认弹窗
        if (state.showDeleteReadConfirm) {
            AlertDialog(
                onDismissRequest = { viewModel.dispatch(MailboxAction.DismissDeleteReadConfirm) },
                title = { Text(text = "删除已读消息", color = colors.textColor) },
                text = {
                    val readCount = state.mailList.count { it.haveRead }
                    Text(
                        text = "确定要删除 $readCount 条已读消息吗？此操作不可撤销。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textColor
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.dispatch(MailboxAction.DeleteReadMails) },
                        enabled = !state.isDeletingRead
                    ) {
                        if (state.isDeletingRead) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = colors.error
                            )
                        } else {
                            Text(text = "删除", color = colors.error)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dispatch(MailboxAction.DismissDeleteReadConfirm) }) {
                        Text(text = "取消", color = colors.onSurfaceVariant)
                    }
                },
                containerColor = colors.surfaceContainerHigh
            )
        }
    }
}

// ============================================================
// Top Bar
// ============================================================

@Composable
internal fun MailboxTopBar(
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onMarkAllRead: () -> Unit,
    hasUnread: Boolean,
    isMarkingRead: Boolean,
    onDeleteRead: () -> Unit = {},
    hasReadMails: Boolean = false,
    isDeletingRead: Boolean = false
) {
    val colors = LocalAppColors.current
    CollapsingTopBar(
        title = "消息中心",
        collapseFraction = 0f,
        onBack = onBack,
        actions = {
            if (hasUnread) {
                TextButton(
                    onClick = onMarkAllRead,
                    enabled = !isMarkingRead,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    if (isMarkingRead) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = colors.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "全部已读",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
            if (hasReadMails) {
                TextButton(
                    onClick = onDeleteRead,
                    enabled = !isDeletingRead,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    if (isDeletingRead) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = colors.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "删除已读",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
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
// 类型筛选
// ============================================================

@Composable
internal fun MailTypeFilter(
    selected: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MAIL_TYPE_OPTIONS.forEach { option ->
            MailTypeChip(
                label = option.label,
                isSelected = selected == option.key,
                onClick = { onSelect(option.key) }
            )
        }
    }
}

@Composable
private fun MailTypeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) colors.primary else colors.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) colors.onPrimary else colors.onSurfaceVariant
        )
    }
}

// ============================================================
// 消息列表
// ============================================================

@Composable
internal fun MailList(
    list: List<MailListContentVO>,
    isLoading: Boolean,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    onOpen: (String) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val listState = rememberLazyListState()

    // 接近底部（剩余 ≤ 5 条）时触发加载下一页。
    val shouldLoadMore by remember(list.size, hasMore, isLoadingMore, isLoading) {
        derivedStateOf {
            if (isLoading || isLoadingMore || !hasMore || list.isEmpty()) return@derivedStateOf false
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= list.size - 5
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    if (!isLoading && list.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "暂无消息",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(list) { _, mail ->
            MailItemCard(mail = mail, onOpen = onOpen)
        }
        // 加载中：底部小转圈；到底后完全静默（无任何 footer）
        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = colors.primary,
                        strokeWidth = 3.dp
                    )
                }
            }
        }
    }
}

@Composable
internal fun MailItemCard(mail: MailListContentVO, onOpen: (String) -> Unit) {
    val colors = LocalAppColors.current
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onOpen(mail.id) }
            ),
        colors = CardDefaults.elevatedCardColors(containerColor = colors.surfaceContainerHigh),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 未读小圆点
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (mail.haveRead) colors.outlineVariant else colors.primary)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mail.title.ifBlank { "（无标题）" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (mail.haveRead) FontWeight.Normal else FontWeight.SemiBold,
                    color = if (mail.haveRead) colors.onSurfaceVariant else colors.textColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = mailTypeLabel(mail.mailType),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.primary.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = mail.time.toReadableTime(),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ============================================================
// 详情弹层
// ============================================================

// ============================================================
// 详情卡片内容（overlay 与 Expanded 右栏共用）
// ============================================================

@Composable
internal fun MailDetailCard(
    state: MailboxState,
    onAction: (MailboxAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val meta = state.currentMailMeta
    val content = state.currentMailContent

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = colors.surfaceContainerHigh),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        if (meta == null) {
            // 空态占位
            Box(
                modifier = Modifier.fillMaxWidth().height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "选择左侧消息查看详情",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }
            return@ElevatedCard
        }

        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text(
                text = meta.title.ifBlank { "（无标题）" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.textColor
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "来自：${content?.sender?.ifBlank { "—" } ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
                Text(
                    text = meta.time.toReadableTime(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant
                )
            }
            HorizontalDivider(
                color = colors.outlineVariant,
                modifier = Modifier.padding(vertical = 10.dp)
            )
            if (state.isDetailLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = colors.primary,
                        strokeWidth = 3.dp
                    )
                }
            } else if (content != null) {
                RichHtmlText(
                    html = content.detail.ifBlank { "（无内容）" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textColor
                )
                if (content.extraList.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "附加信息",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    content.extraList.forEach { extra ->
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "• $extra",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textColor
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onAction(MailboxAction.DeleteMail(meta.id)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                enabled = !state.isDeleting,
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                if (state.isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = colors.onPrimary
                    )
                } else {
                    Text(
                        text = "删除该消息",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onPrimary
                    )
                }
            }
        }
    }
}

// ============================================================
// 详情弹层（窄屏用）
// ============================================================

@Composable
internal fun MailDetailOverlay(
    state: MailboxState,
    onAction: (MailboxAction) -> Unit
) {
    val colors = LocalAppColors.current
    val meta = state.currentMailMeta ?: return

    AnimatedVisibility(
        visible = state.showDetail,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onAction(MailboxAction.CloseDetail) }
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
            ) {
                MailDetailCard(
                    state = state,
                    onAction = onAction,
                    modifier = Modifier.widthIn(min = 280.dp, max = 420.dp)
                )
            }
        }
    }
}
