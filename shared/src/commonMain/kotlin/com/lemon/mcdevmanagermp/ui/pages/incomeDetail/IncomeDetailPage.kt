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
import com.lemon.mcdevmanagermp.data.repository.isEcoFeeReverseSupported
import com.lemon.mcdevmanagermp.domain.main.profitMonthWindow
import com.lemon.mcdevmanagermp.platform.BackHandler
import com.lemon.mcdevmanagermp.platform.copyTextToClipboard
import com.lemon.mcdevmanagermp.ui.components.AppScaffold
import com.lemon.mcdevmanagermp.ui.components.LocalSnackbarHostState
import com.lemon.mcdevmanagermp.ui.components.LocalToastScope
import com.lemon.mcdevmanagermp.ui.components.LocalWindowWidthSizeClass
import com.lemon.mcdevmanagermp.ui.pages.incomeDetail.components.ProfitMonthPickerDialog
import com.lemon.mcdevmanagermp.ui.pages.incomeDetail.components.formatActualModuleSummaryText
import com.lemon.mcdevmanagermp.ui.pages.incomeDetail.layout.IncomeDetailCompactLayout
import com.lemon.mcdevmanagermp.ui.pages.incomeDetail.layout.IncomeDetailExpandedLayout
import com.lemon.mcdevmanagermp.ui.pages.main.MainViewModel
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import com.lemon.mcdevmanagermp.utils.ActualModuleIncomeSummary
import com.lemon.mcdevmanagermp.utils.ECO_FEE_RATE
import com.lemon.mcdevmanagermp.utils.computeActualModuleSummary
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

    val estimatedModules = remember(state.profitData) {
        state.profitData.toModuleIncomeDetails(ECO_FEE_RATE)
    }
    val actualSummary = remember(
        state.selectedMonth,
        state.profitData,
        state.settlement,
        state.isLoading,
        state.errorMessage
    ) {
        if (!isEcoFeeReverseSupported(state.selectedMonth)) return@remember null
        val settlement = state.settlement ?: return@remember null
        if (state.isLoading || state.errorMessage != null) return@remember null
        state.profitData.computeActualModuleSummary(
            settlementIncome = settlement.income.toDoubleOrNull() ?: return@remember null,
            playPlanIncome = settlement.playPlanIncome.toDoubleOrNull() ?: 0.0,
            settlementNetIncome = settlement.monthlyNetIncome.toDoubleOrNull() ?: 0.0
        )
    }
    val sortedModules = remember(state.profitData, actualSummary) {
        val rate = actualSummary?.actualEcoFeeRate ?: ECO_FEE_RATE
        state.profitData.toModuleIncomeDetails(rate)
    }
    val ratedMonthKeys = remember(state.ecoFeeHistory) {
        state.ecoFeeHistory.mapTo(mutableSetOf()) { it.dataMonth }
    }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    var showMonthPicker by remember { mutableStateOf(false) }

    val snackbarHostState = LocalSnackbarHostState.current
    val toastScope = LocalToastScope.current
    val onCopyActualSummary: (ActualModuleIncomeSummary) -> Unit = { summary ->
        copyTextToClipboard(formatActualModuleSummaryText(state.selectedMonth, summary))
        toastScope.launch {
            snackbarHostState.showSnackbar("已复制实际收益模组小结")
        }
    }

    LaunchedEffect(state.selectedMonth) {
        if (!isEcoFeeReverseSupported(state.selectedMonth)) {
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
                    estimatedModules = estimatedModules,
                    sortedModules = sortedModules,
                    actualSummary = actualSummary,
                    maxSelectableMonth = maxSelectableMonth,
                    moduleColumns = if (widthSizeClass == WindowWidthSizeClass.Medium) 2 else 1,
                    statusBarTop = statusBarTop,
                    navBarBottom = navBarBottom,
                    onBack = onBack,
                    onRefresh = viewModel::retry,
                    onSelectMonth = viewModel::selectMonth,
                    onCopyActualSummary = onCopyActualSummary,
                    onOpenMonthPicker = { showMonthPicker = true }
                )

                else -> IncomeDetailExpandedLayout(
                    state = state,
                    estimatedModules = estimatedModules,
                    sortedModules = sortedModules,
                    actualSummary = actualSummary,
                    maxSelectableMonth = maxSelectableMonth,
                    navBarBottom = navBarBottom,
                    onBack = onBack,
                    onRefresh = viewModel::retry,
                    onSelectMonth = viewModel::selectMonth,
                    onCopyActualSummary = onCopyActualSummary,
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
