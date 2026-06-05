package com.lemon.mcdevmanagermp.ui.pages.settings.account.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.pages.settings.account.AccountAction
import com.lemon.mcdevmanagermp.ui.pages.settings.account.AccountState
import com.lemon.mcdevmanagermp.ui.pages.settings.account.CurrentAccountSection
import com.lemon.mcdevmanagermp.ui.pages.settings.account.SavedAccountsSection

@Composable
internal fun MediumAccountLayout(
    state: AccountState,
    onAction: (AccountAction) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToAddAccount: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.width(560.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
}
