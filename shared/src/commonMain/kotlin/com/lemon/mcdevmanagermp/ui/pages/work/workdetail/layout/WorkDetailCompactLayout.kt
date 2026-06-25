package com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.components.CollapsingTopBar
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailAction
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailState
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.BasicInfoForm
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.PcBasicInfoForm
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.PriceInfoForm
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

@Composable
internal fun WorkDetailCompactLayout(
    state: WorkDetailState,
    onAction: (WorkDetailAction) -> Unit,
    onBack: () -> Unit
) {
    val colors = LocalAppColors.current
    val scrollState = rememberScrollState()
    val scrollAlpha = remember {
        derivedStateOf { (scrollState.value / 100f).coerceIn(0f, 1f) }
    }
    val contentColor = lerp(colors.textColor, colors.scheme.onPrimary, scrollAlpha.value)

    Column(modifier = Modifier.fillMaxSize()) {
        CollapsingTopBar(
            title = "作品详情",
            collapseFraction = scrollAlpha.value,
            onBack = onBack,
            actions = {
                TextButton(onClick = { onAction(WorkDetailAction.Submit) }) {
                    Text("更新", color = contentColor)
                }
            }
        )

        when {
            state.isLoading && state.detail == null -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(36.dp),
                    color = colors.primary,
                    strokeWidth = 3.dp
                )
            }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BasicInfoForm(state = state, onAction = onAction)
                if (state.syncPc) {
                    PcBasicInfoForm(state = state, onAction = onAction)
                }
                PriceInfoForm(state = state, onAction = onAction)
            }
        }
    }
}
