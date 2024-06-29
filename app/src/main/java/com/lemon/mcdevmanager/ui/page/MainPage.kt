package com.lemon.mcdevmanager.ui.page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.data.global.AppContext
import com.lemon.mcdevmanager.ui.widget.MainUserCard
import com.lemon.mcdevmanager.ui.widget.ProfitWidget
import com.lemon.mcdevmanager.ui.widget.SNACK_ERROR
import com.lemon.mcdevmanager.ui.widget.TipsCard
import com.lemon.mcdevmanager.viewModel.MainViewAction
import com.lemon.mcdevmanager.viewModel.MainViewEvent
import com.lemon.mcdevmanager.viewModel.MainViewModel
import com.zj.mvi.core.observeEvent

@Composable
fun MainPage(
    navController: NavController,
    viewModel: MainViewModel = viewModel(),
    showToast: (String, String) -> Unit = { _, _ -> },
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val states = viewModel.viewStates.collectAsState().value
    var isShowNotice by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        viewModel.viewEvents.observeEvent(lifecycleOwner) { event ->
            when (event) {
                is MainViewEvent.ShowToast -> showToast(event.msg, SNACK_ERROR)
                is MainViewEvent.RouteToPath -> navController.navigate(event.path)
                is MainViewEvent.MaybeDataNoRefresh -> isShowNotice = true
            }
        }

        viewModel.dispatch(MainViewAction.LoadData(AppContext.nowNickname))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            MainUserCard(
                username = states.username,
                avatarUrl = states.avatarUrl,
                mainLevel = states.mainLevel,
                subLevel = states.subLevel,
                levelText = states.levelText
            )
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
                enter = fadeIn(animationSpec = tween(300))
                        + slideInHorizontally(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300)) +
                        slideOutHorizontally(animationSpec = tween(300))
            ) {
                TipsCard(
                    headerIcon = R.drawable.ic_notice,
                    content = "昨日数据可能未更新",
                    dismissText = "知道了"
                ) { isShowNotice = false }
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