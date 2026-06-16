package com.lemon.mcdevmanagermp.ui.pages.mailbox.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.pages.mailbox.MailList
import com.lemon.mcdevmanagermp.ui.pages.mailbox.MailTypeFilter
import com.lemon.mcdevmanagermp.ui.pages.mailbox.MailboxAction
import com.lemon.mcdevmanagermp.ui.pages.mailbox.MailboxState
import com.lemon.mcdevmanagermp.ui.pages.mailbox.MailboxTopBar

@Composable
internal fun CompactMailboxLayout(
    state: MailboxState,
    onAction: (MailboxAction) -> Unit,
    onBack: () -> Unit,
    statusBarTop: Dp,
    navBarBottom: Dp
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = navBarBottom)
    ) {
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
                onOpen = { onAction(MailboxAction.OpenMail(it)) },
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
        }
    }
}
