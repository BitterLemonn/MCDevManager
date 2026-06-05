package com.lemon.mcdevmanagermp.ui.pages.income.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.pages.income.IncomeAction
import com.lemon.mcdevmanagermp.ui.pages.income.IncomeList
import com.lemon.mcdevmanagermp.ui.pages.income.IncomeState
import com.lemon.mcdevmanagermp.ui.pages.income.IncomeTopBar
import com.lemon.mcdevmanagermp.ui.pages.income.PlatformToggle
import com.lemon.mcdevmanagermp.ui.pages.income.SettleableBanner
import com.lemon.mcdevmanagermp.ui.pages.income.UnExtractedIncomeCard

@Composable
internal fun CompactIncomeLayout(
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            PlatformToggle(
                selectedPlatform = state.selectedPlatform,
                onSelect = { onAction(IncomeAction.SelectPlatform(it)) }
            )

            UnExtractedIncomeCard(unExtractedIncome = state.unExtractedIncome)

            if (state.hasSettleableIncome) {
                val settleable = state.currentList.firstOrNull { it.status == "未结算" && it.availableIncome != "0.00" }
                if (settleable != null) {
                    SettleableBanner(
                        incomeName = settleable.dataMonth,
                        onGetDetail = { onAction(IncomeAction.GetApplyDetail(settleable.id)) }
                    )
                }
            }

            IncomeList(
                incomeList = state.currentList,
                isLoading = state.isLoading,
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
        }
    }
}
