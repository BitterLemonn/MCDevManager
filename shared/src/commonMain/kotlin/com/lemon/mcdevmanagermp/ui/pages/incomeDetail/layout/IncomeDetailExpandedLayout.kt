package com.lemon.mcdevmanagermp.ui.pages.incomeDetail.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import com.lemon.mcdevmanagermp.utils.ModuleIncomeDetail

/**
 * Expanded (>= 840dp) 宽屏双栏仪表盘布局。
 * 左栏（420dp 独立滚动）：月份切换、收益总览 Hero、生态费反推与历史趋势；
 * 右栏（弹性宽度 LazyColumn）：模组收益明细 2 列自适应网格与排序控制。
 */
@Composable
internal fun IncomeDetailExpandedLayout(
    state: IncomeDetailState,
    sortedModules: List<ModuleIncomeDetail>,
    maxSelectableMonth: ProfitMonth,
    navBarBottom: Dp,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onSelectMonth: (ProfitMonth) -> Unit,
    onSelectSortMode: (ModuleSortMode) -> Unit,
    onOpenMonthPicker: () -> Unit
) {
    val colors = LocalAppColors.current
    val totalFlowIncome = state.profitData.sumProfit / 100.0
    val dataReady = !state.isLoading && state.errorMessage == null
    val moduleRows = remember(sortedModules) { sortedModules.chunked(2) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = navBarBottom)
    ) {
        IncomeDetailTopBar(
            collapseFraction = 0f,
            isLoading = state.isLoading,
            onBack = onBack,
            onRefresh = onRefresh,
            onOpenMonthPicker = onOpenMonthPicker
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 左侧摘要与生态费分析栏
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(420.dp)
                    .verticalScroll(rememberScrollState()),
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
                }

                Spacer(Modifier.height(8.dp))
            }

            VerticalDivider(
                modifier = Modifier.fillMaxHeight().padding(vertical = 4.dp),
                color = colors.outlineVariant.copy(alpha = 0.6f),
                thickness = 1.dp
            )

            // 右侧模组收益明细双列网格
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModuleSectionHeader(
                    moduleCount = sortedModules.size,
                    sortMode = state.moduleSortMode,
                    onSelectSortMode = onSelectSortMode
                )

                if (state.isLoading) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        repeat(3) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FinanceSkeletonCard(lineCount = 2, modifier = Modifier.weight(1f))
                                FinanceSkeletonCard(lineCount = 2, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else if (sortedModules.isEmpty()) {
                    EmptyModuleStateCard()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentPadding = PaddingValues(bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(
                            items = moduleRows,
                            key = { _, row -> row.joinToString("|") { it.moduleName } }
                        ) { rowIndex, rowModules ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowModules.forEachIndexed { colIndex, module ->
                                    ModuleIncomeCard(
                                        rank = rowIndex * 2 + colIndex + 1,
                                        module = module,
                                        totalFlowIncome = totalFlowIncome,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowModules.size == 1) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
