package com.lemon.mcdevmanagermp.ui.pages.income.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.vo.netease.income.IncomeVO
import com.lemon.mcdevmanagermp.ui.pages.income.DetailRow
import com.lemon.mcdevmanagermp.ui.pages.income.IncomeAction
import com.lemon.mcdevmanagermp.ui.pages.income.IncomeList
import com.lemon.mcdevmanagermp.ui.pages.income.IncomeState
import com.lemon.mcdevmanagermp.ui.pages.income.IncomeTopBar
import com.lemon.mcdevmanagermp.ui.pages.income.PlatformToggle
import com.lemon.mcdevmanagermp.ui.pages.income.SettleableBanner
import com.lemon.mcdevmanagermp.ui.pages.income.UnExtractedIncomeCard
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

@Composable
internal fun ExpandedIncomeLayout(
    state: IncomeState,
    onAction: (IncomeAction) -> Unit,
    onBack: () -> Unit,
    statusBarTop: Dp,
    navBarBottom: Dp
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = navBarBottom)
    ) {
        IncomeTopBar(onBack = onBack, onRefresh = { onAction(IncomeAction.LoadData) })

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxHeight().width(420.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                PlatformToggle(
                    selectedPlatform = state.selectedPlatform,
                    onSelect = { onAction(IncomeAction.SelectPlatform(it)) }
                )

                Spacer(Modifier.height(10.dp))

                UnExtractedIncomeCard(unExtractedIncome = state.unExtractedIncome)

                if (state.hasSettleableIncome) {
                    Spacer(Modifier.height(10.dp))
                    val settleable = state.currentList.firstOrNull { it.status == "未结算" && it.availableIncome != "0.00" }
                    if (settleable != null) {
                        SettleableBanner(
                            incomeName = settleable.dataMonth,
                            onGetDetail = { onAction(IncomeAction.GetApplyDetail(settleable.id)) }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                IncomeList(
                    incomeList = state.currentList,
                    isLoading = state.isLoading,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(
                modifier = Modifier.width(1.dp).fillMaxHeight(),
                color = LocalAppColors.current.outlineVariant,
                thickness = 1.dp
            )

            Box(modifier = Modifier.weight(1f).fillMaxHeight().padding(horizontal = 16.dp).padding(top = 8.dp)) {
                // Detail / summary area — shows available balance summary
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val currentList = state.currentList
                    if (currentList.isNotEmpty()) {
                        val unsettledList = currentList.filter { it.status == "未结算" }
                        val settledList = currentList.filter { it.status != "未结算" && it.status != "结算中" }
                        val processingList = currentList.filter { it.status == "结算中" }

                        SummaryBlock("未结算收益", unsettledList)
                        SummaryBlock("结算中", processingList)
                        SummaryBlock("已结算/已打款", settledList)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryBlock(
    title: String,
    items: List<IncomeVO>
) {
    val colors = LocalAppColors.current
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh),

    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Text(
                text = "$title (${items.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.textColor
            )
            if (items.isEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "暂无数据",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            } else {
                Spacer(Modifier.height(8.dp))
                items.forEach { item ->
                    DetailRow(
                        "${item.dataMonth}",
                        item.monthlyNetIncome,
                        colors.textColor,
                        bold = true
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
    Spacer(Modifier.height(0.dp))
}
