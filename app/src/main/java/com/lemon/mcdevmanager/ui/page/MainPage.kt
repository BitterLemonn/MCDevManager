package com.lemon.mcdevmanager.ui.page

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DrawerValue
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalDrawer
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.data.common.FEEDBACK_PAGE
import com.lemon.mcdevmanager.data.common.LOGIN_PAGE
import com.lemon.mcdevmanager.data.common.MAIN_PAGE
import com.lemon.mcdevmanager.data.global.AppContext
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.widget.AccountManagerDrawer
import com.lemon.mcdevmanager.ui.widget.FunctionCard
import com.lemon.mcdevmanager.ui.widget.MainUserCard
import com.lemon.mcdevmanager.ui.widget.ProfitCard
import com.lemon.mcdevmanager.ui.widget.ProfitWidget
import com.lemon.mcdevmanager.ui.widget.SNACK_ERROR
import com.lemon.mcdevmanager.ui.widget.SNACK_INFO
import com.lemon.mcdevmanager.ui.widget.TipsCard
import com.lemon.mcdevmanager.viewModel.MainViewAction
import com.lemon.mcdevmanager.viewModel.MainViewEvent
import com.lemon.mcdevmanager.viewModel.MainViewModel
import com.orhanobut.logger.Logger
import com.zj.mvi.core.observeEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MainPage(
    navController: NavController,
    viewModel: MainViewModel = viewModel(),
    showToast: (String, String) -> Unit = { _, _ -> },
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val states = viewModel.viewStates.collectAsState().value
    var isShowNotice by remember { mutableStateOf(false) }
    var isShowLastMonthProfit by remember { mutableStateOf(false) }

    val clipboardManager =
        LocalContext.current.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    LaunchedEffect(key1 = Unit) {
        viewModel.viewEvents.observeEvent(lifecycleOwner) { event ->
            when (event) {
                is MainViewEvent.ShowToast -> showToast(event.msg, SNACK_ERROR)
                is MainViewEvent.RouteToPath -> navController.navigate(event.path) {
                    if (event.needPop) popUpTo(MAIN_PAGE) { inclusive = true }
                }

                is MainViewEvent.MaybeDataNoRefresh -> isShowNotice = true
                is MainViewEvent.ShowLastMonthProfit -> isShowLastMonthProfit = true
            }
        }

        viewModel.dispatch(MainViewAction.LoadData(AppContext.nowNickname))
    }
    ModalDrawer(
        modifier = Modifier.fillMaxWidth(0.5f),
        drawerState = drawerState,
        drawerBackgroundColor = Color.Transparent,
        drawerElevation = 0.dp,
        drawerContent = {
            AccountManagerDrawer(
                accountList = AppContext.accountList,
                onClick = { viewModel.dispatch(MainViewAction.ChangeAccount(it)) },
                onDismiss = { viewModel.dispatch(MainViewAction.DeleteAccount(it)) },
                onLogout = { viewModel.dispatch(MainViewAction.DeleteAccount(AppContext.nowNickname)) },
                onRightClick = { navController.navigate(LOGIN_PAGE) }
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .animateContentSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .animateContentSize()
            ) {
                MainUserCard(
                    username = states.username,
                    avatarUrl = states.avatarUrl,
                    mainLevel = states.mainLevel,
                    subLevel = states.subLevel,
                    levelText = states.levelText,
                    maxLevelExp = states.maxLevelExp,
                    currentExp = states.currentExp,
                    canLevelUp = states.canLevelUp,
                    contributeScore = states.contributionScore,
                    contributeRank = states.contributionRank,
                    contributeClass = states.contributionClass,
                    netGameScore = states.netGameScore,
                    netGameRank = states.netGameRank,
                    netGameClass = states.netGameClass,
                    dataDate = states.contributionMonth
                ) {
                    val clip = ClipData.newPlainText(
                        "cookies",
                        AppContext.cookiesStore[AppContext.nowNickname]
                    )
                    clipboardManager.setPrimaryClip(clip)
                    coroutineScope.launch { showToast("已将cookies复制至剪贴板", SNACK_INFO) }
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .animateContentSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    ProfitWidget(
                        curMonthProfit = states.curMonthProfit,
                        curMonthDl = states.curMonthDl,
                        lastMonthProfit = states.lastMonthProfit,
                        lastMonthDl = states.lastMonthDl,
                        yesterdayDl = states.yesterdayDl,
                        yesterdayProfit = states.yesterdayProfit,
                        halfAvgProfit = states.halfAvgProfit,
                        halfAvgDl = states.halfAvgDl,
                        isLoading = states.isLoadingOverview
                    )
                    AnimatedVisibility(
                        visible = isShowNotice,
                        enter = fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                            animationSpec = tween(300)
                        ),
                        exit = fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                            animationSpec = tween(300)
                        )
                    ) {
                        TipsCard(
                            headerIcon = R.drawable.ic_notice,
                            content = "昨日数据可能未更新",
                            dismissText = "知道了"
                        ) { isShowNotice = false }
                    }
                    ProfitCard(
                        title = "本月收益速算",
                        realMoney = states.realMoney,
                        taxMoney = states.taxMoney,
                        isLoading = states.isLoadingOverview
                    )
                    AnimatedVisibility(
                        visible = isShowLastMonthProfit,
                        enter = fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                            animationSpec = tween(300)
                        ),
                        exit = fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                            animationSpec = tween(300)
                        )
                    ) {
                        ProfitCard(
                            title = "上月收益速算",
                            realMoney = states.lastRealMoney,
                            taxMoney = states.lastTaxMoney,
                            isLoading = states.isLoadingOverview
                        )
                    }
                    FunctionCard(icon = R.drawable.ic_analyze, title = "数据分析") {
                        coroutineScope.launch { showToast("更多功能请期待未来更新", SNACK_INFO) }
                    }
                    FunctionCard(icon = R.drawable.ic_feedback, title = "玩家反馈") {
                        navController.navigate(FEEDBACK_PAGE)
                    }
                    FunctionCard(icon = R.drawable.ic_profit, title = "收益管理") {
                        coroutineScope.launch { showToast("更多功能请期待未来更新", SNACK_INFO) }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainPagePreview() {
    val context = LocalContext.current
    val navController = NavController(context)
    MainPage(navController)
}