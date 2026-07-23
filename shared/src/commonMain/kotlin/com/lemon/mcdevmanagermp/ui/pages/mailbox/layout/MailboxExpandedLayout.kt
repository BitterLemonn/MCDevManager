package com.lemon.mcdevmanagermp.ui.pages.mailbox.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.pages.mailbox.MailDetailCard
import com.lemon.mcdevmanagermp.ui.pages.mailbox.MailList
import com.lemon.mcdevmanagermp.ui.pages.mailbox.MailTypeFilter
import com.lemon.mcdevmanagermp.ui.pages.mailbox.MailboxAction
import com.lemon.mcdevmanagermp.ui.pages.mailbox.MailboxState
import com.lemon.mcdevmanagermp.ui.pages.mailbox.MailboxTopBar
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

@Composable
internal fun ExpandedMailboxLayout(
    state: MailboxState,
    onAction: (MailboxAction) -> Unit,
    onBack: () -> Unit,
    statusBarTop: Dp,
    navBarBottom: Dp
) {
    Row(modifier = Modifier.fillMaxSize().padding(bottom = navBarBottom)) {
        // 左侧列表栏
        Column(modifier = Modifier.width(420.dp).fillMaxHeight()) {
            MailboxTopBar(
                onBack = onBack,
                onRefresh = { onAction(MailboxAction.LoadData) },
                onMarkAllRead = { onAction(MailboxAction.MarkAllRead) },
                hasUnread = state.hasUnread,
                isMarkingRead = state.isMarkingRead,
                onDeleteRead = { onAction(MailboxAction.ShowDeleteReadConfirm) },
                hasReadMails = state.hasReadMails,
                isDeletingRead = state.isDeletingRead
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Spacer(Modifier.height(4.dp))
                MailTypeFilter(
                    selected = state.selectedMailType,
                    onSelect = { onAction(MailboxAction.SelectMailType(it)) }
                )
                MailList(
                    list = state.mailList,
                    isLoading = state.isLoading,
                    hasMore = state.hasMore,
                    isLoadingMore = state.isLoadingMore,
                    onOpen = { onAction(MailboxAction.OpenMail(it)) },
                    onLoadMore = { onAction(MailboxAction.LoadMore) },
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
            }
        }

        // 右侧详情栏
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            DetailHeader(
                hasContent = state.currentMailMeta != null,
                onClose = { onAction(MailboxAction.CloseDetail) }
            )
            Spacer(Modifier.height(8.dp))
            MailDetailCard(
                state = state,
                onAction = onAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}

@Composable
private fun DetailHeader(hasContent: Boolean, onClose: () -> Unit) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "消息详情",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = colors.textColor,
            modifier = Modifier.weight(1f)
        )
        if (hasContent) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "关闭详情",
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
