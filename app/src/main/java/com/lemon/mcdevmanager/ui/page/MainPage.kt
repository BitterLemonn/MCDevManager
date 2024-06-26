package com.lemon.mcdevmanager.ui.page

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lemon.mcdevmanager.data.global.AppContext
import com.lemon.mcdevmanager.ui.widget.HeaderWidget
import com.lemon.mcdevmanager.ui.widget.MainUserCard
import com.lemon.mcdevmanager.ui.widget.ProfitWidget
import com.lemon.mcdevmanager.ui.widget.SNACK_ERROR
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

    LaunchedEffect(key1 = Unit) {
        viewModel.viewEvents.observeEvent(lifecycleOwner) { event ->
            when (event) {
                is MainViewEvent.ShowToast -> showToast(event.msg, SNACK_ERROR)
                is MainViewEvent.RouteToPath -> navController.navigate(event.path)
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
                halfAvgDl = states.halfAvgDl
            )
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