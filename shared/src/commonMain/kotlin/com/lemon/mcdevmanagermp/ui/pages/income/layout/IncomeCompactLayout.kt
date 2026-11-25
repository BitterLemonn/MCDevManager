package com.lemon.mcdevmanagermp.ui.pages.income.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.repository.normalizeMonthKey
import com.lemon.mcdevmanagermp.ui.components.FinanceSkeletonCard
import com.lemon.mcdevmanagermp.ui.pages.income.IncomeAction
import com.lemon.mcdevmanagermp.ui.pages.income.IncomeState
import com.lemon.mcdevmanagermp.ui.pages.income.components.EmptyIncomeStateCard
import com.lemon.mcdevmanagermp.ui.pages.income.components.ExpandableIncomeRecordCard
import com.lemon.mcdevmanagermp.ui.pages.income.components.IncomeControlBar
import com.lemon.mcdevmanagermp.ui.pages.income.components.IncomeHeroSummaryCard
import com.lemon.mcdevmanagermp.ui.pages.income.components.IncomeTopBar

/**
 * Compact (< 600dp) 与 Medium (600~840dp) 自适应收益管理布局。
 * 采用单一 LazyColumn 驱动滚动吸顶；中屏下居中限制内容最大宽度避免过度拉伸。
 */
@Composable
internal fun CompactIncomeLayout(
    state: IncomeState,
    onAction: (IncomeAction) -> Unit,
    onBack: () -> Unit,
    statusBarTop: Dp,
    navBarBottom: Dp
) {
    val listState = rememberLazyListState()
    val collapseFraction by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 1f
            else (listState.firstVisibleItemScrollOffset / 120f).coerceIn(0f, 1f)
        }
    }

    // 展开的条目 ID 集合；默认展开第一条可结算或最新月份
    var expandedIds by remember(state.selectedPlatform) { mutableStateOf<Set<String>>(emptySet()) }
    var hasInitializedExpand by remember(state.selectedPlatform) { mutableStateOf(false) }
    val filteredList = state.filteredList
    if (!hasInitializedExpand && filteredList.isNotEmpty()) {
        val defaultId = state.settleableItem?.id ?: filteredList.first().id
        expandedIds = setOf(defaultId)
        hasInitializedExpand = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = statusBarTop + 60.dp,
                    bottom = navBarBottom + 20.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "hero_summary") {
                    IncomeHeroSummaryCard(
                        state = state,
                        onSelectFilter = { onAction(IncomeAction.SelectStatusFilter(it)) },
                        onStartSettle = { onAction(IncomeAction.GetApplyDetail(it)) }
                    )
                }

                item(key = "control_bar") {
                    IncomeControlBar(
                        selectedPlatform = state.selectedPlatform,
                        peCount = state.peList.size,
                        pcCount = state.pcList.size,
                        statusFilter = state.statusFilter,
                        hasAttentionItems = state.hasAttentionItems,
                        onSelectPlatform = { onAction(IncomeAction.SelectPlatform(it)) },
                        onSelectFilter = { onAction(IncomeAction.SelectStatusFilter(it)) }
                    )
                }

                if (state.isLoading && state.currentList.isEmpty()) {
                    items(3, key = { "skeleton_$it" }) {
                        FinanceSkeletonCard(lineCount = 3)
                    }
                } else if (filteredList.isEmpty()) {
                    item(key = "empty_state") {
                        EmptyIncomeStateCard(
                            message = if (state.currentList.isEmpty()) {
                                "暂无该平台的收益结算记录"
                            } else {
                                "当前筛选条件下暂无结算记录"
                            }
                        )
                    }
                } else {
                    itemsIndexed(
                        items = filteredList,
                        key = { index, item -> item.id.ifBlank { "${item.dataMonth}_$index" } }
                    ) { _, income ->
                        val ecoFeeRate = normalizeMonthKey(income.dataMonth)?.let { state.ecoFeeRates[it] }
                        val isExpanded = income.id in expandedIds
                        ExpandableIncomeRecordCard(
                            data = income,
                            ecoFeeRate = ecoFeeRate,
                            expanded = isExpanded,
                            isLoadingDetail = state.isLoadingApplyDetail,
                            onToggleExpand = {
                                expandedIds = if (isExpanded) {
                                    expandedIds - income.id
                                } else {
                                    expandedIds + income.id
                                }
                            },
                            onStartSettle = { onAction(IncomeAction.GetApplyDetail(income.id)) }
                        )
                    }
                }

                item(key = "bottom_spacer") { Spacer(Modifier.height(4.dp)) }
            }
        }

        IncomeTopBar(
            collapseFraction = collapseFraction,
            isRefreshing = state.isLoading,
            onBack = onBack,
            onRefresh = { onAction(IncomeAction.LoadData) }
        )
    }
}
