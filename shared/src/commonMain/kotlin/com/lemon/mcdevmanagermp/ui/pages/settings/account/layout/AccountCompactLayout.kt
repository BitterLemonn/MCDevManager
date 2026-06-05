package com.lemon.mcdevmanagermp.ui.pages.settings.account.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.pages.settings.account.AccountAction
import com.lemon.mcdevmanagermp.ui.pages.settings.account.AccountState
import com.lemon.mcdevmanagermp.ui.pages.settings.account.CurrentAccountSection
import com.lemon.mcdevmanagermp.ui.pages.settings.account.SavedAccountsSection

@Composable
internal fun CompactAccountLayout(
    state: AccountState,
    onAction: (AccountAction) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToAddAccount: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CurrentAccountSection(
            state = state,
            onAction = onAction,
            onNavigateToLogin = onNavigateToLogin
        )
        SavedAccountsSection(
            state = state,
            onAction = onAction,
            onNavigateToAddAccount = onNavigateToAddAccount
        )
    }
}
