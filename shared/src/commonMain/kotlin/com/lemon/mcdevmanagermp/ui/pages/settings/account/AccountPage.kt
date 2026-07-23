package com.lemon.mcdevmanagermp.ui.pages.settings.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ComposableImageOptions
import com.github.panpf.sketch.request.error
import com.github.panpf.sketch.request.fallback
import com.github.panpf.sketch.request.placeholder
import com.lemon.mcdevmanagermp.data.consts.getContributeClassName
import com.lemon.mcdevmanagermp.data.consts.getLevelName
import com.lemon.mcdevmanagermp.data.vo.netease.user.LevelInfoVO
import com.lemon.mcdevmanagermp.domain.account.Account
import com.lemon.mcdevmanagermp.ui.components.CollapsingTopBar
import com.lemon.mcdevmanagermp.ui.components.LocalWindowWidthSizeClass
import com.lemon.mcdevmanagermp.ui.components.collectUiEffect
import com.lemon.mcdevmanagermp.ui.pages.settings.account.layout.CompactAccountLayout
import com.lemon.mcdevmanagermp.ui.pages.settings.account.layout.ExpandedAccountLayout
import com.lemon.mcdevmanagermp.ui.pages.settings.account.layout.MediumAccountLayout
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.img_avatar
import kotlin.time.Clock
import kotlin.time.Instant

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

    viewModel.effect.collectUiEffect {
        when (it) {
            is AccountEffect.ShowToast -> showToast(it.message)

            AccountEffect.NavigateToLogin -> onNavigateToLogin()
            AccountEffect.AccountSwitched -> onAccountSwitched()
        }
    }

    val accountToDelete = state.accountToDelete
    if (state.showDeleteDialog && accountToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dispatch(AccountAction.DismissDelete) },
            title = { Text("删除账号") },
            text = { Text("确定要删除账号 ${accountToDelete.nickname} 吗？删除后需要重新登录。") },
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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(bottom = navBarBottom)
            ) {
                val widthSizeClass = LocalWindowWidthSizeClass.current

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
            collapseFraction = topBarAlpha,
            onBack = onBack
        )
    }
}

// ============================================================
// Current Account Section
// ============================================================

@Composable
internal fun CurrentAccountSection(
    state: AccountState,
    onAction: (AccountAction) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val colors = LocalAppColors.current
    val currentAccount = state.accounts.find { it.id == state.currentAccountId }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.primary.copy(alpha = 0.06f)),
        shape = RoundedCornerShape(16.dp),

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
                            text = currentAccount.nickname,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = colors.textColor
                        )
                        val expired = isLoginExpired(currentAccount.lastLoginTime)
                        Text(
                            text = formatLoginTime(currentAccount.lastLoginTime) +
                                    if (expired) " · 登录已过期" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (expired) colors.danger else colors.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                AccountLevelInfoSection(
                    levelInfo = state.levelInfo,
                    isLoading = state.isLoadingLevel
                )

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
// Account Level Info Section
// ============================================================

@Composable
private fun AccountLevelInfoSection(
    levelInfo: LevelInfoVO?,
    isLoading: Boolean
) {
    val colors = LocalAppColors.current

    if (levelInfo == null) {
        if (isLoading) {
            Text(
                text = "加载中…",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
        return
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // 等级 + 升阶任务状态
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${getLevelName(levelInfo.currentClass)} · Lv.${levelInfo.currentLevel}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.textColor
            )
            Text(
                text = "升阶任务 ${if (levelInfo.upgradeClassAchieve) "已完成" else "未完成"}",
                style = MaterialTheme.typography.labelSmall,
                color = if (levelInfo.upgradeClassAchieve) colors.success else colors.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = {
                if (levelInfo.expCeiling > levelInfo.expFloor) {
                    ((levelInfo.totalExp - levelInfo.expFloor) / (levelInfo.expCeiling - levelInfo.expFloor))
                        .coerceIn(0.0, 1.0).toFloat()
                } else 0f
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = colors.primary,
            trackColor = colors.primary.copy(alpha = 0.15f)
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "经验 ${levelInfo.totalExp.toInt()} / ${levelInfo.expCeiling.toInt()}",
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        // 月度贡献
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "月度贡献",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = colors.textColor
            )
            Text(
                text = "统计 ${levelInfo.contributionMonth}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = colors.outlineVariant,
            thickness = 0.5.dp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ContributionColumn(
                modifier = Modifier.weight(1f),
                title = "组件贡献",
                score = levelInfo.contributionScore,
                rank = levelInfo.contributionRank,
                className = getContributeClassName(levelInfo.contributionClass)
            )
            ContributionColumn(
                modifier = Modifier.weight(1f),
                title = "网络游戏",
                score = levelInfo.contributionNetGameScore,
                rank = levelInfo.contributionNetGameRank,
                className = getContributeClassName(levelInfo.contributionNetGameClass)
            )
        }
    }
}

@Composable
private fun ContributionColumn(
    modifier: Modifier = Modifier,
    title: String,
    score: String,
    rank: Int,
    className: String
) {
    val colors = LocalAppColors.current
    val rows = listOf("分数" to score, "排名" to "$rank", "等级" to className)
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        rows.forEach { (label, value) ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = colors.primary
                )
            }
        }
    }
}

// ============================================================
// Saved Accounts Section
// ============================================================

@Composable
internal fun SavedAccountsSection(
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
            Icon(
                imageVector = Icons.Filled.Add,
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
    account: Account,
    isCurrent: Boolean,
    isSwitching: Boolean,
    onSwitch: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalAppColors.current

    Card(
        modifier = Modifier.fillMaxWidth().then(
            if (isCurrent) Modifier.border(
                1.5.dp,
                colors.primary.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            )
            else Modifier
        ),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(12.dp),

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
                            text = account.nickname,
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
                    val expired = isLoginExpired(account.lastLoginTime)
                    Text(
                        text = formatLoginTime(account.lastLoginTime) +
                                if (expired) " · 登录已过期" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (expired) colors.danger else colors.onSurfaceVariant
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
                        IconButton(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape),
                            onClick = onSwitch,
                            shape = CircleShape,
                            enabled = true,
                            interactionSource = remember { MutableInteractionSource() },
                            colors = IconButtonDefaults.iconButtonColors(),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SwitchAccount,
                                contentDescription = "切换",
                                modifier = Modifier.size(16.dp),
                                tint = colors.primary
                            )
                        }

                        Spacer(Modifier.width(4.dp))

                        IconButton(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape),
                            onClick = onDelete,
                            shape = CircleShape,
                            enabled = true,
                            interactionSource = remember { MutableInteractionSource() },
                            colors = IconButtonDefaults.iconButtonColors(),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "删除",
                                modifier = Modifier.size(16.dp),
                                tint = colors.danger
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh),
        shape = RoundedCornerShape(12.dp),

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
    size: Dp,
    iconSize: Dp,
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
    val instant = Instant.fromEpochMilliseconds(epochMillis)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "上次登录: ${localDateTime.year}/${localDateTime.month.number}/${localDateTime.day} " +
            "${localDateTime.hour.toString().padStart(2, '0')}:" +
            localDateTime.minute.toString().padStart(2, '0')
}

private const val LOGIN_EXPIRE_DAYS = 5

private fun isLoginExpired(epochMillis: Long): Boolean {
    val expireMillis = LOGIN_EXPIRE_DAYS * 24L * 60 * 60 * 1000
    return Clock.System.now().toEpochMilliseconds() - epochMillis > expireMillis
}
