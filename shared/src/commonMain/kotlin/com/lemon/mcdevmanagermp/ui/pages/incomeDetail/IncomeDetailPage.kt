package com.lemon.mcdevmanagermp.ui.pages.incomeDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.lemon.mcdevmanagermp.domain.main.ProfitMonth
import com.lemon.mcdevmanagermp.domain.main.profitMonthWindow
import com.lemon.mcdevmanagermp.platform.BackHandler
import com.lemon.mcdevmanagermp.ui.components.AppScaffold
import com.lemon.mcdevmanagermp.ui.components.LocalSnackbarHostState
import com.lemon.mcdevmanagermp.ui.components.LocalToastScope
import com.lemon.mcdevmanagermp.ui.components.LocalWindowWidthSizeClass
import com.lemon.mcdevmanagermp.ui.pages.incomeDetail.components.ProfitMonthPickerDialog
import com.lemon.mcdevmanagermp.ui.pages.incomeDetail.layout.IncomeDetailCompactLayout
import com.lemon.mcdevmanagermp.ui.pages.incomeDetail.layout.IncomeDetailExpandedLayout
import com.lemon.mcdevmanagermp.ui.pages.main.MainViewModel
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import com.lemon.mcdevmanagermp.utils.toModuleIncomeDetails
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@Composable
fun IncomeDetailPage(initialMonthOffset: Int = 0, onBack: () -> Unit) {
    val colors = LocalAppColors.current
    val today = remember {
        val now = Clock.System.now().toLocalDateTime(TimeZone.of("Asia/Shanghai"))
        LocalDate(now.year, now.month.number, now.day)
    }
    val monthWindow = remember(today) { profitMonthWindow(today) }
    val initialMonth = remember(initialMonthOffset, monthWindow) {
        monthWindow.current.shift(initialMonthOffset)
    }
    val initialData = remember(initialMonthOffset) {
        when (initialMonthOffset) {
            -1 -> MainViewModel.cachedLastMonthProfitData
            1 -> MainViewModel.cachedNextMonthProfitData
            else -> MainViewModel.cachedProfitData
        }
    }
    val viewModel = remember(initialMonth) {
        IncomeDetailViewModel(initialMonth, initialData, MainViewModel.cachedUserInfo)
    }
    val state by viewModel.state.collectAsState()
    val maxSelectableMonth = if (monthWindow.showNextMonth) {
        monthWindow.current.shift(1)
    } else {
        monthWindow.current
    }

    val sortedModules = remember(state.profitData, state.moduleSortMode) {
        val base = state.profitData.toModuleIncomeDetails()
        when (state.moduleSortMode) {
            ModuleSortMode.BY_FLOW -> base.sortedByDescending { it.flowIncome }
            ModuleSortMode.BY_TOTAL -> base.sortedByDescending { it.totalIncome }
        }
    }
    val ratedMonthKeys = remember(state.ecoFeeHistory) {
        state.ecoFeeHistory.mapTo(mutableSetOf()) { it.dataMonth }
    }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    var showMonthPicker by remember { mutableStateOf(false) }

    val snackbarHostState = LocalSnackbarHostState.current
    val toastScope = LocalToastScope.current
    LaunchedEffect(state.selectedMonth) {
        if (state.selectedMonth < ProfitMonth(2026, 1)) {
            toastScope.launch {
                snackbarHostState.showSnackbar("2026.1以前的收益算法与现在不一致，数据可能产生偏差")
            }
        }
    }

    BackHandler(enabled = showMonthPicker) {
        showMonthPicker = false
    }

    AppScaffold {
        Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
            val widthSizeClass = LocalWindowWidthSizeClass.current
            when (widthSizeClass) {
                WindowWidthSizeClass.Compact, WindowWidthSizeClass.Medium -> IncomeDetailCompactLayout(
                    state = state,
                    sortedModules = sortedModules,
                    maxSelectableMonth = maxSelectableMonth,
                    moduleColumns = if (widthSizeClass == WindowWidthSizeClass.Medium) 2 else 1,
                    statusBarTop = statusBarTop,
                    navBarBottom = navBarBottom,
                    onBack = onBack,
                    onRefresh = viewModel::retry,
                    onSelectMonth = viewModel::selectMonth,
                    onSelectSortMode = viewModel::setModuleSortMode,
                    onOpenMonthPicker = { showMonthPicker = true }
                )

                else -> IncomeDetailExpandedLayout(
                    state = state,
                    sortedModules = sortedModules,
                    maxSelectableMonth = maxSelectableMonth,
                    navBarBottom = navBarBottom,
                    onBack = onBack,
                    onRefresh = viewModel::retry,
                    onSelectMonth = viewModel::selectMonth,
                    onSelectSortMode = viewModel::setModuleSortMode,
                    onOpenMonthPicker = { showMonthPicker = true }
                )
            }
        }
    }

    if (showMonthPicker) {
        ProfitMonthPickerDialog(
            selectedMonth = state.selectedMonth,
            maxMonth = maxSelectableMonth,
            ratedMonthKeys = ratedMonthKeys,
            onSelect = {
                viewModel.selectMonth(it)
                showMonthPicker = false
            },
            onDismiss = { showMonthPicker = false }
        )
    }
}
