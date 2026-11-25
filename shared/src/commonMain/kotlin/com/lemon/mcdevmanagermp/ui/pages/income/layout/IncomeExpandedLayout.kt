package com.lemon.mcdevmanagermp.ui.pages.income.layout

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.repository.normalizeMonthKey
import com.lemon.mcdevmanagermp.ui.components.FinanceSkeletonCard
import com.lemon.mcdevmanagermp.ui.components.formatCurrencyString
import com.lemon.mcdevmanagermp.ui.components.tabularNumbers
import com.lemon.mcdevmanagermp.ui.pages.income.IncomeAction
import com.lemon.mcdevmanagermp.ui.pages.income.IncomeState
import com.lemon.mcdevmanagermp.ui.pages.income.components.EmptyIncomeStateCard
import com.lemon.mcdevmanagermp.ui.pages.income.components.IncomeControlBar
import com.lemon.mcdevmanagermp.ui.pages.income.components.IncomeDetailPane
import com.lemon.mcdevmanagermp.ui.pages.income.components.IncomeMasterListItem
import com.lemon.mcdevmanagermp.ui.pages.income.components.IncomeTopBar
import com.lemon.mcdevmanagermp.ui.pages.income.components.SettleActionBanner
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

/**
 * Expanded (>= 840dp) 宽屏 Master-Detail 双栏布局。
 * 左栏：未提取余额摘要、平台与状态筛选、可滚动月度账单列表；
 * 右栏：选中月份的完整结算单拆解、一键结算操作与平台累计统计。
 */
@Composable
internal fun ExpandedIncomeLayout(
    state: IncomeState,
    onAction: (IncomeAction) -> Unit,
    onBack: () -> Unit,
    statusBarTop: Dp,
    navBarBottom: Dp
) {
    val colors = LocalAppColors.current
    val filteredList = state.filteredList
    val selectedIncome = state.effectiveSelectedIncome

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = navBarBottom)
    ) {
        IncomeTopBar(
            collapseFraction = 0f,
            isRefreshing = state.isLoading,
            onBack = onBack,
            onRefresh = { onAction(IncomeAction.LoadData) }
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 左侧 Master 列表栏
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 紧凑账户未提取余额卡：纯色 primaryContainer + onPrimaryContainer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.primaryContainer)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountBalanceWallet,
                            contentDescription = null,
                            tint = colors.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "未提取收益",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.onPrimaryContainer.copy(alpha = 0.82f)
                        )
                    }
                    Text(
                        text = state.unExtractedIncome.formatCurrencyString(),
                        style = MaterialTheme.typography.titleLarge.tabularNumbers(),
                        fontWeight = FontWeight.Bold,
                        color = colors.onPrimaryContainer
                    )
                }

                // 若当前平台有可结算记录且当前选中的不是它，左侧顶部保留一键入口
                val settleable = state.settleableItem
                if (settleable != null && selectedIncome?.id != settleable.id) {
                    SettleActionBanner(
                        item = settleable,
                        isLoadingDetail = state.isLoadingApplyDetail,
                        onStartSettle = { onAction(IncomeAction.GetApplyDetail(settleable.id)) }
                    )
                }

                IncomeControlBar(
                    selectedPlatform = state.selectedPlatform,
                    peCount = state.peList.size,
                    pcCount = state.pcList.size,
                    statusFilter = state.statusFilter,
                    hasAttentionItems = state.hasAttentionItems,
                    onSelectPlatform = { onAction(IncomeAction.SelectPlatform(it)) },
                    onSelectFilter = { onAction(IncomeAction.SelectStatusFilter(it)) }
                )

                if (state.isLoading && state.currentList.isEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        repeat(3) { FinanceSkeletonCard(lineCount = 2) }
                    }
                } else if (filteredList.isEmpty()) {
                    EmptyIncomeStateCard(
                        message = if (state.currentList.isEmpty()) {
                            "暂无该平台的收益结算记录"
                        } else {
                            "当前筛选条件下暂无结算记录"
                        }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentPadding = PaddingValues(bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(
                            items = filteredList,
                            key = { index, item -> item.id.ifBlank { "${item.dataMonth}_$index" } }
                        ) { _, income ->
                            val ecoFeeRate = normalizeMonthKey(income.dataMonth)?.let { state.ecoFeeRates[it] }
                            IncomeMasterListItem(
                                data = income,
                                ecoFeeRate = ecoFeeRate,
                                selected = income.id == selectedIncome?.id,
                                onClick = { onAction(IncomeAction.SelectIncome(income.id)) }
                            )
                        }
                        item { Spacer(Modifier.height(4.dp)) }
                    }
                }
            }

            VerticalDivider(
                modifier = Modifier.fillMaxHeight().padding(vertical = 4.dp),
                color = colors.outlineVariant.copy(alpha = 0.6f),
                thickness = 1.dp
            )

            // 右侧 Detail 详情面板
            IncomeDetailPane(
                selectedIncome = selectedIncome,
                state = state,
                onStartSettle = { onAction(IncomeAction.GetApplyDetail(it)) },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
    }
}
