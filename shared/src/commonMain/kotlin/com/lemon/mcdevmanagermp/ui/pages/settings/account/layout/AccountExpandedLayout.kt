package com.lemon.mcdevmanagermp.ui.pages.settings.account.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.pages.settings.account.AccountAction
import com.lemon.mcdevmanagermp.ui.pages.settings.account.AccountState
import com.lemon.mcdevmanagermp.ui.pages.settings.account.CurrentAccountSection
import com.lemon.mcdevmanagermp.ui.pages.settings.account.SavedAccountsSection

@Composable
internal fun ExpandedAccountLayout(
    state: AccountState,
    onAction: (AccountAction) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToAddAccount: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            CurrentAccountSection(
                state = state,
                onAction = onAction,
                onNavigateToLogin = onNavigateToLogin
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            SavedAccountsSection(
                state = state,
                onAction = onAction,
                onNavigateToAddAccount = onNavigateToAddAccount
            )
        }
    }
}
