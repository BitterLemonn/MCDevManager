package com.lemon.mcdevmanagermp.ui.pages.incomeDetail.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.domain.main.ProfitMonth
import com.lemon.mcdevmanagermp.ui.components.FinanceSkeletonCard
import com.lemon.mcdevmanagermp.ui.pages.incomeDetail.IncomeDetailState
import com.lemon.mcdevmanagermp.ui.pages.incomeDetail.ModuleSortMode
import com.lemon.mcdevmanagermp.ui.pages.incomeDetail.components.AlgorithmVersionNoticeBanner
import com.lemon.mcdevmanagermp.ui.pages.incomeDetail.components.EcoFeeHistoryCard
import com.lemon.mcdevmanagermp.ui.pages.incomeDetail.components.EcoFeeReverseCard
import com.lemon.mcdevmanagermp.ui.pages.incomeDetail.components.EmptyModuleStateCard
import com.lemon.mcdevmanagermp.ui.pages.incomeDetail.components.IncomeDetailTopBar
import com.lemon.mcdevmanagermp.ui.pages.incomeDetail.components.LoadErrorBanner
import com.lemon.mcdevmanagermp.ui.pages.incomeDetail.components.ModuleIncomeCard
import com.lemon.mcdevmanagermp.ui.pages.incomeDetail.components.ModuleSectionHeader
import com.lemon.mcdevmanagermp.ui.pages.incomeDetail.components.MonthNavigatorBar
import com.lemon.mcdevmanagermp.ui.pages.incomeDetail.components.ProfitHeroSummaryCard
import com.lemon.mcdevmanagermp.utils.ModuleIncomeDetail

/**
 * Compact (< 600dp) 与 Medium (600~840dp) 收益详情自适应布局。
 * Compact 采用单列流式布局；Medium 居中限宽并将模组明细排布为 2 列网格。
 */
@Composable
internal fun IncomeDetailCompactLayout(
    state: IncomeDetailState,
    sortedModules: List<ModuleIncomeDetail>,
    maxSelectableMonth: ProfitMonth,
    moduleColumns: Int,
    statusBarTop: Dp,
    navBarBottom: Dp,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onSelectMonth: (ProfitMonth) -> Unit,
    onSelectSortMode: (ModuleSortMode) -> Unit,
    onOpenMonthPicker: () -> Unit
) {
    val scrollState = rememberScrollState()
    val collapseFraction by remember {
        derivedStateOf { (scrollState.value / 120f).coerceIn(0f, 1f) }
    }
    val totalFlowIncome = state.profitData.sumProfit / 100.0
    val dataReady = !state.isLoading && state.errorMessage == null

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 800.dp)
                    .padding(horizontal = 16.dp)
                    .padding(top = statusBarTop + 60.dp, bottom = navBarBottom + 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MonthNavigatorBar(
                    selectedMonth = state.selectedMonth,
                    maxMonth = maxSelectableMonth,
                    settlement = state.settlement,
                    isSettlementLoading = state.isSettlementLoading,
                    onPrevMonth = { onSelectMonth(state.selectedMonth.shift(-1)) },
                    onNextMonth = { onSelectMonth(state.selectedMonth.shift(1)) },
                    onOpenPicker = onOpenMonthPicker
                )

                if (state.selectedMonth < ProfitMonth(2026, 1)) {
                    AlgorithmVersionNoticeBanner()
                }

                state.errorMessage?.let { message ->
                    LoadErrorBanner(message = message, onRetry = onRefresh)
                }

                if (state.isLoading) {
                    FinanceSkeletonCard(lineCount = 4)
                    FinanceSkeletonCard(lineCount = 3)
                } else {
                    ProfitHeroSummaryCard(
                        profitData = state.profitData,
                        modules = sortedModules,
                        showTaxEstimate = !state.isEnterprise
                    )

                    EcoFeeReverseCard(
                        profitData = state.profitData,
                        settlement = state.settlement,
                        isSettlementLoading = state.isSettlementLoading,
                        settlementError = state.settlementError,
                        dataReady = dataReady,
                        onRetry = onRefresh
                    )

                    EcoFeeHistoryCard(
                        history = state.ecoFeeHistory,
                        selectedMonth = state.selectedMonth,
                        onSelectMonth = onSelectMonth
                    )

                    Spacer(Modifier.height(2.dp))

                    ModuleSectionHeader(
                        moduleCount = sortedModules.size,
                        sortMode = state.moduleSortMode,
                        onSelectSortMode = onSelectSortMode
                    )

                    if (sortedModules.isEmpty()) {
                        EmptyModuleStateCard()
                    } else if (moduleColumns <= 1) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            sortedModules.forEachIndexed { index, module ->
                                ModuleIncomeCard(
                                    rank = index + 1,
                                    module = module,
                                    totalFlowIncome = totalFlowIncome
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            sortedModules.chunked(moduleColumns).forEachIndexed { rowIndex, rowModules ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    rowModules.forEachIndexed { colIndex, module ->
                                        ModuleIncomeCard(
                                            rank = rowIndex * moduleColumns + colIndex + 1,
                                            module = module,
                                            totalFlowIncome = totalFlowIncome,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    repeat(moduleColumns - rowModules.size) {
                                        Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        IncomeDetailTopBar(
            collapseFraction = collapseFraction,
            isLoading = state.isLoading,
            onBack = onBack,
            onRefresh = onRefresh,
            onOpenMonthPicker = onOpenMonthPicker
        )
    }
}
