package com.lemon.mcdevmanagermp.ui.pages.main.layout

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ComposableImageOptions
import com.github.panpf.sketch.request.error
import com.github.panpf.sketch.request.fallback
import com.github.panpf.sketch.request.placeholder
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.consts.getLevelName
import com.lemon.mcdevmanagermp.ui.components.IncomeManagementCard
import com.lemon.mcdevmanagermp.ui.components.MultiLevelRankingCard
import com.lemon.mcdevmanagermp.ui.components.ProfitCard
import com.lemon.mcdevmanagermp.ui.components.ProfitSplitWidget
import com.lemon.mcdevmanagermp.ui.navigation.Route
import com.lemon.mcdevmanagermp.ui.pages.community.CommunityContent
import com.lemon.mcdevmanagermp.ui.pages.main.MainAction
import com.lemon.mcdevmanagermp.ui.pages.main.MainState
import com.lemon.mcdevmanagermp.ui.pages.main.MainTab
import com.lemon.mcdevmanagermp.ui.pages.analyze.AnalyzeContent
import com.lemon.mcdevmanagermp.ui.pages.main.PlaceholderTabContent
import com.lemon.mcdevmanagermp.ui.pages.settings.SettingsContent
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import com.lemon.mcdevmanagermp.utils.ProfitData
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.img_avatar
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun CompactLayout(
    state: MainState,
    onAction: (MainAction) -> Unit,
    onNavigateToSubPage: (Route) -> Unit,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToAddAccount: () -> Unit = {},
    onAccountSwitched: () -> Unit = {}
) {
    val colors = LocalAppColors.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "账号管理",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "暂无多账号功能",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize().background(colors.background)) {
            AnimatedContent(
                targetState = state.selectedTab,
                transitionSpec = {
                    val direction = if (
                        MainTab.entries.indexOf(targetState) > MainTab.entries.indexOf(initialState)
                    ) 1 else -1
                    (fadeIn(tween(300)) + slideInHorizontally(tween(300)) { direction * it })
                        .togetherWith(fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { -direction * it })
                },
                label = "tab_content",
                modifier = Modifier.weight(1f)
            ) { targetTab ->
                Box(modifier = Modifier.fillMaxSize()) {
                    when (targetTab) {
                        MainTab.Home -> CompactHomeTabContent(
                            state = state,
                            onAction = onAction,
                            onAvatarClick = {
                                // TODO: Open drawer
                            },
                            onNavigateToIncomeDetail = {
                                onNavigateToSubPage(Route.IncomeDetail())
                            },
                            onNavigateToLastMonthDetail = {
                                onNavigateToSubPage(Route.IncomeDetail(isLastMonth = true))
                            },
                            onNavigateToIncome = {
                                onNavigateToSubPage(Route.Income)
                            }
                        )

                        MainTab.Analyze -> AnalyzeContent(onNavigateToSubPage = onNavigateToSubPage)
                        MainTab.Community -> CommunityContent()
                        MainTab.Settings -> SettingsContent(
                            onNavigateToLogin = onNavigateToLogin,
                            onNavigateToAddAccount = onNavigateToAddAccount,
                            onAccountSwitched = onAccountSwitched
                        )
                    }
                }
            }
            TabNavigationBar(
                selectedTab = state.selectedTab,
                onTabSelect = { onAction(MainAction.SelectTab(it)) }
            )
        }
    }
}

@Composable
private fun TabNavigationBar(
    selectedTab: MainTab,
    onTabSelect: (MainTab) -> Unit
) {
    NavigationBar {
        MainTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelect(tab) },
                icon = {
                    Icon(
                        painter = painterResource(tab.icon),
                        contentDescription = tab.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(tab.label, fontSize = 11.sp) }
            )
        }
    }
}

@Composable
internal fun CompactHomeTabContent(
    state: MainState,
    onAction: (MainAction) -> Unit,
    onAvatarClick: () -> Unit,
    onNavigateToIncomeDetail: () -> Unit = {},
    onNavigateToLastMonthDetail: () -> Unit = {},
    onNavigateToIncome: () -> Unit = {}
) {
    val colors = LocalAppColors.current
    val user = (state.userInfo as? NetworkState.Success)?.data
    val level = (state.levelInfo as? NetworkState.Success)?.data
    val userNickname = user?.nickname
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp + statusBarTop)
                    .background(
                        colors.primary,
                        RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                    )
            )

            Column(modifier = Modifier.fillMaxWidth().padding(top = statusBarTop).padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hi, ${userNickname ?: "开发者"}!",
                            fontWeight = FontWeight.Bold,
                            fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                            color = colors.onPrimary
                        )
                        Spacer(Modifier.height(4.dp))
                        val lv = level?.currentLevel ?: user?.level ?: 0
                        val levelText = if (level != null) {
                            "${getLevelName(level.currentClass)} · Lv.$lv"
                        } else {
                            "Lv.$lv"
                        }
                        Text(
                            text = levelText,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onPrimary.copy(alpha = 0.8f)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    ) {
                        AsyncImage(
                            uri = user?.headImg,
                            state = rememberAsyncImageState(ComposableImageOptions {
                                placeholder(Res.drawable.img_avatar)
                                fallback(Res.drawable.img_avatar)
                                crossfade()
                                error(Res.drawable.img_avatar)
                                sizeMultiplier(2.0f)
                            }),
                            contentDescription = "头像",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                if (level != null) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = {
                            val progress = if (level.expCeiling > level.expFloor) {
                                ((level.totalExp - level.expFloor) / (level.expCeiling - level.expFloor))
                                    .coerceIn(0.0, 1.0).toFloat()
                            } else 0f
                            progress
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = colors.textColor.copy(alpha = 0.9f),
                        trackColor = colors.textColor.copy(alpha = 0.2f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                ProfitSplitWidget(
                    overview = state.overview,
                    isLoading = state.isRefreshing,
                    onClick = { onAction(MainAction.RefreshData) }
                )

                Spacer(Modifier.height(12.dp))

                ProfitCard(
                    title = "本月收益速算",
                    profitData = state.profitData ?: ProfitData(),
                    isLoading = state.isProfitLoading,
                    expanded = state.profitExpanded,
                    onToggleExpand = { onAction(MainAction.ToggleProfitExpand) },
                    onNavigateToDetail = onNavigateToIncomeDetail
                )

                if (state.showLastMonthProfit) {
                    Spacer(Modifier.height(12.dp))

                    ProfitCard(
                        title = "上月收益速算",
                        profitData = state.lastProfitData ?: ProfitData(),
                        isLoading = state.isProfitLoading,
                        expanded = state.lastProfitExpanded,
                        onToggleExpand = { onAction(MainAction.ToggleLastProfitExpand) },
                        onNavigateToDetail = onNavigateToLastMonthDetail
                    )
                }

                Spacer(Modifier.height(12.dp))

                IncomeManagementCard(onClick = onNavigateToIncome)

                Spacer(Modifier.height(12.dp))

                MultiLevelRankingCard(
                    data = state.rankListData,
                    onChange = { category, subCategory ->
                        onAction(MainAction.GetRankData(category, subCategory))
                    }
                )

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
