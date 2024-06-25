package com.lemon.mcdevmanager.viewModel

import androidx.lifecycle.ViewModel
import com.zj.mvi.core.SharedFlowEvents
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {
    private val _viewStates = MutableStateFlow(MainViewState())
    val viewStates = _viewStates.asStateFlow()
    private val _viewEvents = SharedFlowEvents<MainViewEvent>()
    val viewEvents = _viewEvents.asSharedFlow()
}

data class MainViewState(
    val curMonthProfit: Int = 0,
    val curMonthDl: Int = 0,
    val lastMonthProfit: Int = 0,
    val lastMonthDl: Int = 0,
    val yesterdayProfit: Int = 0,
    val halfAvgProfit: Int = 0,
    val yesterdayDl: Int = 0,
    val halfAvgDl: Int = 0,
    val isLoading: Boolean = false,

    val username: String = "",
    val avatarUrl: String = ""
)

sealed class MainViewEvent {
    data class RouteToPath(val path: String) : MainViewEvent()
    data object ShowToast : MainViewEvent()
}

sealed class MainViewAction {
    data class LoadData(val nickname: String) : MainViewAction()
}