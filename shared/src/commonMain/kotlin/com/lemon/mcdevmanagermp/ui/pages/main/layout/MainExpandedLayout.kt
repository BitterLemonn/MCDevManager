package com.lemon.mcdevmanagermp.ui.pages.main.layout

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.ui.components.ExpandableNavigateItem
import com.lemon.mcdevmanagermp.ui.components.IncomeManagementCard
import com.lemon.mcdevmanagermp.ui.components.MailboxCard
import com.lemon.mcdevmanagermp.ui.components.MultiLevelRankingCard
import com.lemon.mcdevmanagermp.ui.components.ProfitCard
import com.lemon.mcdevmanagermp.ui.components.ProfitSplitWidget
import com.lemon.mcdevmanagermp.ui.navigation.Route
import com.lemon.mcdevmanagermp.ui.pages.analyze.AnalyzeTabContent
import com.lemon.mcdevmanagermp.ui.pages.community.CommunityContent
import com.lemon.mcdevmanagermp.ui.pages.main.MainAction
import com.lemon.mcdevmanagermp.ui.pages.main.MainState
import com.lemon.mcdevmanagermp.ui.pages.main.MainTab
import com.lemon.mcdevmanagermp.ui.pages.settings.SettingsContent
import com.lemon.mcdevmanagermp.ui.pages.work.WorkContent
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import com.lemon.mcdevmanagermp.utils.ProfitData
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_menu

private val CollapsedWidth = 80.dp
private val ExpandedWidth = 240.dp

@Composable
internal fun ExpandedLayout(
    state: MainState,
    onAction: (MainAction) -> Unit,
    onNavigateToSubPage: (Route) -> Unit,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToAddAccount: () -> Unit = {},
    onAccountSwitched: () -> Unit = {},
    onCheckUpdate: () -> Unit = {}
) {
    val colors = LocalAppColors.current
    var isExpanded by remember { mutableStateOf(false) }
    val userNickname = (state.userInfo as? NetworkState.Success)?.data?.nickname
    val userHeadImg = (state.userInfo as? NetworkState.Success)?.data?.headImg

    val sidebarWidth by animateDpAsState(
        targetValue = if (isExpanded) ExpandedWidth else CollapsedWidth,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "SidebarWidth"
    )

    Row(modifier = Modifier.fillMaxSize().background(colors.surface)) {
        NavigationRail(
            modifier = Modifier
                .width(sidebarWidth)
                .fillMaxHeight(),
            containerColor = colors.surface
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.Start
            ) {
                ExpandableNavigateItem(
                    title = "MCDEV",
                    titleWeight = FontWeight.ExtraBold,
                    titleColor = colors.primary,
                    icon = Res.drawable.ic_menu,
                    expanded = isExpanded
                ) { isExpanded = !isExpanded }

                HorizontalDivider(color = colors.outline, thickness = 1.dp)
                Spacer(Modifier.height(12.dp))

                MainTab.entries.filter { it != MainTab.Settings }.forEachIndexed { index, tab ->
                    ExpandableNavigateItem(
                        title = tab.label,
                        icon = tab.icon,
                        expanded = isExpanded,
                        selected = state.selectedTab == tab,
                        titleWeight = if (tab == MainTab.Home) FontWeight.SemiBold else FontWeight.Normal
                    ) {
                        onAction(MainAction.SelectTab(tab))
                    }
                }

                Spacer(Modifier.weight(1f))

                ExpandableNavigateItem(
                    title = "设置",
                    icon = MainTab.Settings.icon,
                    expanded = isExpanded,
                    selected = state.selectedTab == MainTab.Settings
                ) {
                    onAction(MainAction.SelectTab(MainTab.Settings))
                }

                Spacer(Modifier.height(4.dp))

                HorizontalDivider(color = colors.outline, thickness = 1.dp)

                Spacer(Modifier.height(4.dp))

                ExpandableNavigateItem(
                    title = userNickname ?: "开发者",
                    icon = userHeadImg,
                    isTinted = false,
                    expanded = isExpanded
                ) {}

                Spacer(Modifier.height(4.dp))
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                .background(colors.background)
        ) {
            AnimatedContent(
                targetState = state.selectedTab,
                transitionSpec = {
                    val direction = if (
                        MainTab.entries.indexOf(targetState) > MainTab.entries.indexOf(initialState)
                    ) 1 else -1
                    (fadeIn(tween(300)) + slideInHorizontally(tween(300)) { direction * it })
                        .togetherWith(fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { -direction * it })
                },
                label = "tab_content_expanded"
            ) { targetTab ->
                when (targetTab) {
                    MainTab.Home -> ExpandedHomeTabContent(
                        state = state,
                        onAction = onAction,
                        onNavigateToIncomeDetail = {
                            onNavigateToSubPage(Route.IncomeDetail())
                        },
                        onNavigateToLastMonthDetail = {
                            onNavigateToSubPage(Route.IncomeDetail(isLastMonth = true))
                        },
                        onNavigateToIncome = {
                            onNavigateToSubPage(Route.Income)
                        },
                        onNavigateToMailbox = {
                            onNavigateToSubPage(Route.Mailbox)
                        }
                    )

                    MainTab.Analyze -> AnalyzeTabContent(onNavigateToSubPage = onNavigateToSubPage)
                    MainTab.Community -> CommunityContent()
                    MainTab.Work -> WorkContent()
                    MainTab.Settings -> SettingsContent(
                        onNavigateToLogin = onNavigateToLogin,
                        onNavigateToAddAccount = onNavigateToAddAccount,
                        onAccountSwitched = onAccountSwitched,
                        onCheckUpdate = onCheckUpdate
                    )
                }
            }
        }
    }
}

@Composable
internal fun ExpandedHomeTabContent(
    state: MainState,
    onAction: (MainAction) -> Unit,
    onNavigateToIncomeDetail: () -> Unit = {},
    onNavigateToLastMonthDetail: () -> Unit = {},
    onNavigateToIncome: () -> Unit = {},
    onNavigateToMailbox: () -> Unit = {}
) {
    val colors = LocalAppColors.current
    val userNickname = (state.userInfo as? NetworkState.Success)?.data?.nickname
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp + statusBarTop)
                .background(colors.primary)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = statusBarTop)
                .padding(16.dp)
                .padding(bottom = navBarBottom)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Hi, ${userNickname ?: "开发者"}!",
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                    color = colors.onPrimary,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            ProfitSplitWidget(
                overview = state.overview,
                isLoading = state.isRefreshing,
                onClick = { onAction(MainAction.RefreshData) }
            )

            Spacer(Modifier.height(8.dp))

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfitCard(
                        title = "本月收益速算",
                        profitData = state.profitData ?: ProfitData(),
                        isLoading = state.isProfitLoading,
                        expanded = state.profitExpanded,
                        onToggleExpand = { onAction(MainAction.ToggleProfitExpand) },
                        onNavigateToDetail = onNavigateToIncomeDetail
                    )
                    if (state.showLastMonthProfit) {
                        ProfitCard(
                            title = "上月收益速算",
                            profitData = state.lastProfitData ?: ProfitData(),
                            isLoading = state.isProfitLoading,
                            expanded = state.lastProfitExpanded,
                            onToggleExpand = { onAction(MainAction.ToggleLastProfitExpand) },
                            onNavigateToDetail = onNavigateToLastMonthDetail
                        )
                    }
                    IncomeManagementCard(onClick = onNavigateToIncome)
                    MailboxCard(
                        onClick = onNavigateToMailbox,
                        unreadCount = state.mailboxUnreadCount
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    MultiLevelRankingCard(
                        data = state.rankListData,
                        onChange = { category, subCategory ->
                            onAction(MainAction.GetRankData(category, subCategory))
                        }
                    )
                }
            }
        }
    }
}
