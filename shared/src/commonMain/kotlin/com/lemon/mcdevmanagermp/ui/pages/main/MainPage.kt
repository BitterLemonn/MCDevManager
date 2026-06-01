package com.lemon.mcdevmanagermp.ui.pages.main

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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.lemon.mcdevmanagermp.ui.components.AppScaffold
import com.lemon.mcdevmanagermp.ui.components.ExpandableNavigateItem
import com.lemon.mcdevmanagermp.ui.components.MultiLevelRankingCard
import com.lemon.mcdevmanagermp.ui.components.ProfitCard
import com.lemon.mcdevmanagermp.ui.components.ProfitSplitWidget
import com.lemon.mcdevmanagermp.ui.navigation.Route
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import com.lemon.mcdevmanagermp.utils.ProfitData
import kotlinx.coroutines.launch
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_menu
import mcdevmanagermpr.shared.generated.resources.img_avatar
import org.jetbrains.compose.resources.painterResource

private val CollapsedWidth = 80.dp
private val ExpandedWidth = 240.dp

@Composable
fun MainPage(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToSubPage: (Route) -> Unit = {}
) {
    val viewModel = remember { MainViewModel() }
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    AppScaffold(
        viewEffect = viewModel.effect,
        onEffect = { effect ->
            when (effect) {
                is MainEffect.ShowToast -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                }

                is MainEffect.NavigateTo -> onNavigateToSubPage(effect.route)
                MainEffect.SessionExpired -> onNavigateToLogin()
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val colors = LocalAppColors.current
                Snackbar(
                    snackbarData = data,
                    shape = RoundedCornerShape(8.dp),
                    containerColor = colors.surface,
                    contentColor = colors.onSurface
                )
            }
        }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val widthSizeClass = when {
                maxWidth < 600.dp -> WindowWidthSizeClass.Compact
                maxWidth < 840.dp -> WindowWidthSizeClass.Medium
                else -> WindowWidthSizeClass.Expanded
            }
            when (widthSizeClass) {
                WindowWidthSizeClass.Compact -> CompactLayout(
                    state = state,
                    onAction = viewModel::dispatch,
                    onNavigateToSubPage = onNavigateToSubPage
                )

                WindowWidthSizeClass.Medium -> MediumLayout(
                    state = state,
                    onAction = viewModel::dispatch,
                    onNavigateToSubPage = onNavigateToSubPage
                )

                else -> ExpandedLayout(
                    state = state,
                    onAction = viewModel::dispatch,
                    onNavigateToSubPage = onNavigateToSubPage
                )
            }
        }
    }
}

// ============================================================
// Compact Layout (mobile) — Bottom NavigationBar + Drawer
// ============================================================

@Composable
private fun CompactLayout(
    state: MainState,
    onAction: (MainAction) -> Unit,
    onNavigateToSubPage: (Route) -> Unit
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
                            }
                        )

                        MainTab.Analyze -> PlaceholderTabContent("数据分析")
                        MainTab.Feedback -> PlaceholderTabContent("玩家反馈")
                        MainTab.Comment -> PlaceholderTabContent("组件评论")
                        MainTab.Settings -> PlaceholderTabContent("设置")
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
private fun CompactHomeTabContent(
    state: MainState,
    onAction: (MainAction) -> Unit,
    onAvatarClick: () -> Unit,
    onNavigateToIncomeDetail: () -> Unit = {},
    onNavigateToLastMonthDetail: () -> Unit = {}
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
                            color = colors.textColor
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
                            color = colors.textColor.copy(alpha = 0.8f)
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

// ============================================================
// Medium Layout (tablet) — NavigationRail + Single-column content
// ============================================================

@Composable
private fun MediumLayout(
    state: MainState,
    onAction: (MainAction) -> Unit,
    onNavigateToSubPage: (Route) -> Unit
) {
    val colors = LocalAppColors.current
    val userNickname = (state.userInfo as? NetworkState.Success)?.data?.nickname
    val userHeadImg = (state.userInfo as? NetworkState.Success)?.data?.headImg

    Row(modifier = Modifier.fillMaxSize().background(colors.surface)) {
        NavigationRail(
            modifier = Modifier
                .width(CollapsedWidth)
                .fillMaxHeight(),
            containerColor = colors.surface
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(Modifier.height(12.dp))

                MainTab.entries.filter { it != MainTab.Settings }.forEach { tab ->
                    ExpandableNavigateItem(
                        title = tab.label,
                        icon = tab.icon,
                        expanded = false,
                        showLabel = true,
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
                    expanded = false,
                    showLabel = true,
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
                    expanded = false
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
                label = "tab_content_medium"
            ) { targetTab ->
                when (targetTab) {
                    MainTab.Home -> MediumHomeTabContent(
                        state = state,
                        onAction = onAction,
                        onNavigateToIncomeDetail = {
                            onNavigateToSubPage(Route.IncomeDetail())
                        },
                        onNavigateToLastMonthDetail = {
                            onNavigateToSubPage(Route.IncomeDetail(isLastMonth = true))
                        }
                    )

                    MainTab.Analyze -> PlaceholderTabContent("数据分析")
                    MainTab.Feedback -> PlaceholderTabContent("玩家反馈")
                    MainTab.Comment -> PlaceholderTabContent("组件评论")
                    MainTab.Settings -> PlaceholderTabContent("设置")
                }
            }
        }
    }
}

@Composable
private fun MediumHomeTabContent(
    state: MainState,
    onAction: (MainAction) -> Unit,
    onNavigateToIncomeDetail: () -> Unit = {},
    onNavigateToLastMonthDetail: () -> Unit = {}
) {
    val colors = LocalAppColors.current
    val userNickname = (state.userInfo as? NetworkState.Success)?.data?.nickname
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = navBarBottom)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp + statusBarTop)
                    .background(colors.primary)
            )

            Column(modifier = Modifier.fillMaxWidth().padding(top = statusBarTop).padding(16.dp)) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Hi, ${userNickname ?: "开发者"}!",
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                        color = colors.textColor,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

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

                Spacer(Modifier.height(12.dp))

                if (state.showLastMonthProfit) {
                    ProfitCard(
                        title = "上月收益速算",
                        profitData = state.lastProfitData ?: ProfitData(),
                        isLoading = state.isProfitLoading,
                        expanded = state.lastProfitExpanded,
                        onToggleExpand = { onAction(MainAction.ToggleLastProfitExpand) },
                        onNavigateToDetail = onNavigateToLastMonthDetail
                    )

                    Spacer(Modifier.height(12.dp))
                }

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

// ============================================================
// Expanded Layout (desktop/PC) — NavigationRail + Two-column content
// ============================================================

@Composable
private fun ExpandedLayout(
    state: MainState,
    onAction: (MainAction) -> Unit,
    onNavigateToSubPage: (Route) -> Unit
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
                        }
                    )

                    MainTab.Analyze -> PlaceholderTabContent("数据分析")
                    MainTab.Feedback -> PlaceholderTabContent("玩家反馈")
                    MainTab.Comment -> PlaceholderTabContent("组件评论")
                    MainTab.Settings -> PlaceholderTabContent("设置")
                }
            }
        }
    }
}

@Composable
private fun ExpandedHomeTabContent(
    state: MainState,
    onAction: (MainAction) -> Unit,
    onNavigateToIncomeDetail: () -> Unit = {},
    onNavigateToLastMonthDetail: () -> Unit = {}
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
                    color = colors.textColor,
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

// ============================================================
// Shared utilities
// ============================================================

@Composable
private fun PlaceholderTabContent(name: String) {
    val colors = LocalAppColors.current
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onSurface.copy(alpha = 0.5f)
        )
    }
}
