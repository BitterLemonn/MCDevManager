package com.lemon.mcdevmanagermp.ui.pages.settings.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ComposableImageOptions
import com.github.panpf.sketch.request.error
import com.github.panpf.sketch.request.fallback
import com.github.panpf.sketch.request.placeholder
import com.lemon.mcdevmanagermp.data.db.entity.AccountEntity
import com.lemon.mcdevmanagermp.ui.components.CollapsingTopBar
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import com.lemon.mcdevmanagermp.utils.extension.collectEffect
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_add
import mcdevmanagermpr.shared.generated.resources.ic_del
import mcdevmanagermpr.shared.generated.resources.img_avatar
import org.jetbrains.compose.resources.painterResource

@Composable
fun AccountManagementPage(
    onBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToAddAccount: () -> Unit = {},
    onAccountSwitched: () -> Unit = {}
) {
    val viewModel = remember { AccountViewModel() }
    val state by viewModel.state.collectAsState()
    val colors = LocalAppColors.current

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val scrollState = rememberScrollState()
    val topBarAlpha by remember {
        derivedStateOf {
            (scrollState.value.toFloat() / 100f).coerceIn(0f, 1f)
        }
    }

    // Handle effects
    viewModel.effect.collectEffect {
        when (it) {
            is AccountEffect.ShowToast -> {
                // Toast will be handled by parent
            }

            AccountEffect.NavigateToLogin -> onNavigateToLogin()
            AccountEffect.AccountSwitched -> onAccountSwitched()
        }
    }

    // Delete confirmation dialog
    val accountToDelete = state.accountToDelete
    if (state.showDeleteDialog && accountToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dispatch(AccountAction.DismissDelete) },
            title = { Text("删除账号") },
            text = { Text("确定要删除账号 ${accountToDelete.email} 吗？删除后需要重新登录。") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.dispatch(AccountAction.ConfirmDelete) },
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.error)
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dispatch(AccountAction.DismissDelete) }) {
                    Text("取消")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Spacer(Modifier.height(statusBarTop))
            Spacer(Modifier.height(56.dp))

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(bottom = navBarBottom)
            ) {
                val widthSizeClass = when {
                    maxWidth < 600.dp -> WindowWidthSizeClass.Compact
                    maxWidth < 840.dp -> WindowWidthSizeClass.Medium
                    else -> WindowWidthSizeClass.Expanded
                }

                when (widthSizeClass) {
                    WindowWidthSizeClass.Compact -> CompactAccountLayout(
                        state = state,
                        onAction = viewModel::dispatch,
                        onNavigateToLogin = onNavigateToLogin,
                        onNavigateToAddAccount = onNavigateToAddAccount
                    )

                    WindowWidthSizeClass.Medium -> MediumAccountLayout(
                        state = state,
                        onAction = viewModel::dispatch,
                        onNavigateToLogin = onNavigateToLogin,
                        onNavigateToAddAccount = onNavigateToAddAccount
                    )

                    else -> ExpandedAccountLayout(
                        state = state,
                        onAction = viewModel::dispatch,
                        onNavigateToLogin = onNavigateToLogin,
                        onNavigateToAddAccount = onNavigateToAddAccount
                    )
                }
            }
        }

        CollapsingTopBar(
            title = "账号管理",
            alpha = topBarAlpha,
            onBack = onBack
        )
    }
}

// ============================================================
// Compact Layout
// ============================================================

@Composable
private fun CompactAccountLayout(
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

// ============================================================
// Medium Layout
// ============================================================

@Composable
private fun MediumAccountLayout(
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

// ============================================================
// Expanded Layout
// ============================================================

@Composable
private fun ExpandedAccountLayout(
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

// ============================================================
// Current Account Section
// ============================================================

@Composable
private fun CurrentAccountSection(
    state: AccountState,
    onAction: (AccountAction) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val colors = LocalAppColors.current
    val currentAccount = state.accounts.find { it.id == state.currentAccountId }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = colors.primary.copy(alpha = 0.06f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "当前账号",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.textColor
            )
            Spacer(Modifier.height(12.dp))
            if (currentAccount != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AccountAvatar(
                        headImg = currentAccount.headImg,
                        size = 44.dp,
                        iconSize = 22.dp,
                        isCurrent = true
                    )

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentAccount.email,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = colors.textColor
                        )
                        Text(
                            text = formatLoginTime(currentAccount.lastLoginTime),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { onAction(AccountAction.Logout) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error)
                ) {
                    Text("退出登录")
                }
            } else {
                Text(
                    text = "未登录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text("登录账号")
                }
            }
        }
    }
}

// ============================================================
// Saved Accounts Section
// ============================================================

@Composable
private fun SavedAccountsSection(
    state: AccountState,
    onAction: (AccountAction) -> Unit,
    onNavigateToAddAccount: () -> Unit
) {
    val colors = LocalAppColors.current

    SettingsSectionCard(title = "已保存的账号") {
        if (state.accounts.isEmpty()) {
            Text(
                text = "暂无保存的账号",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        } else {
            state.accounts.forEachIndexed { index, account ->
                if (index > 0) {
                    Spacer(Modifier.height(8.dp))
                }
                AccountCard(
                    account = account,
                    isCurrent = account.id == state.currentAccountId,
                    isSwitching = account.id == state.isSwitching,
                    onSwitch = { onAction(AccountAction.SwitchAccount(account)) },
                    onDelete = { onAction(AccountAction.RequestDelete(account)) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Add account button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(colors.primary.copy(alpha = 0.08f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onNavigateToAddAccount
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_add),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "添加账号",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colors.primary
            )
        }
    }
}

// ============================================================
// Account Card
// ============================================================

@Composable
private fun AccountCard(
    account: AccountEntity,
    isCurrent: Boolean,
    isSwitching: Boolean,
    onSwitch: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalAppColors.current

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().then(
            if (isCurrent) Modifier.border(
                1.5.dp,
                colors.primary.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            )
            else Modifier
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = colors.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AccountAvatar(
                    headImg = account.headImg,
                    size = 32.dp,
                    iconSize = 16.dp,
                    isCurrent = isCurrent
                )

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = account.email,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = colors.textColor
                        )
                        if (isCurrent) {
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "当前",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Text(
                        text = formatLoginTime(account.lastLoginTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }

                if (!isCurrent) {
                    if (isSwitching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = colors.primary
                        )
                    } else {
                        // Switch button
                        TextButton(
                            onClick = onSwitch,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 12.dp,
                                vertical = 4.dp
                            )
                        ) {
                            Text(
                                text = "切换",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.primary
                            )
                        }

                        Spacer(Modifier.width(4.dp))

                        // Delete button
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onDelete
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.ic_del),
                                contentDescription = "删除",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// Shared: Section Card
// ============================================================

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    val colors = LocalAppColors.current

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = colors.surfaceContainerHigh),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.textColor
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

// ============================================================
// Account Avatar
// ============================================================

@Composable
private fun AccountAvatar(
    headImg: String?,
    size: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp,
    isCurrent: Boolean = false
) {
    val colors = LocalAppColors.current
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                if (isCurrent) colors.primary else colors.primary.copy(alpha = 0.12f)
            ),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            uri = headImg,
            state = rememberAsyncImageState(ComposableImageOptions {
                placeholder(Res.drawable.img_avatar)
                fallback(Res.drawable.img_avatar)
                crossfade()
                error(Res.drawable.img_avatar)
                sizeMultiplier(2.0f)
            }),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

// ============================================================
// Utility
// ============================================================

private fun formatLoginTime(epochMillis: Long): String {
    val instant = kotlin.time.Instant.fromEpochMilliseconds(epochMillis)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "上次登录: ${localDateTime.year}/${localDateTime.month.number}/${localDateTime.day} " +
            "${localDateTime.hour.toString().padStart(2, '0')}:" +
            localDateTime.minute.toString().padStart(2, '0')
}
