package com.lemon.mcdevmanagermp.ui.pages.work.promotion.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.components.CollapsingTopBar
import com.lemon.mcdevmanagermp.ui.pages.work.promotion.PromotionAction
import com.lemon.mcdevmanagermp.ui.pages.work.promotion.PromotionState
import com.lemon.mcdevmanagermp.ui.pages.work.promotion.layout.component.PromotionApplicationsSection
import com.lemon.mcdevmanagermp.ui.pages.work.promotion.layout.component.PromotionApplyForm
import com.lemon.mcdevmanagermp.ui.pages.work.promotion.layout.component.PromotionDatesSection
import com.lemon.mcdevmanagermp.ui.pages.work.promotion.layout.component.PromotionEmptyState
import com.lemon.mcdevmanagermp.ui.pages.work.promotion.layout.component.PromotionLoadingBox
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

/**
 * PE 轮播图申请 - 展开布局（> 840dp）。
 * 双栏：左广告位（约 40%），右申请表单（约 60%）。
 */
@Composable
internal fun PromotionExpandedLayout(
    state: PromotionState,
    onAction: (PromotionAction) -> Unit,
    onBack: () -> Unit
) {
    val colors = LocalAppColors.current
    val scrollState = rememberScrollState()
    val scrollAlpha = remember {
        derivedStateOf { (scrollState.value / 100f).coerceIn(0f, 1f) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CollapsingTopBar(
            title = "PE 轮播图申请",
            collapseFraction = scrollAlpha.value,
            onBack = onBack,
            actions = {
                IconButton(onClick = { onAction(PromotionAction.Refresh) }) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "刷新",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        )

        when {
            state.isLoading && state.permit.isEmpty() -> PromotionLoadingBox(Modifier.fillMaxSize())
            state.permit.isEmpty() && state.reason.isEmpty() -> PromotionEmptyState(
                onRefresh = { onAction(PromotionAction.Refresh) },
                modifier = Modifier.fillMaxSize()
            )

            else -> Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column(modifier = Modifier.weight(2f)) {
                    PromotionDatesSection(state = state, onAction = onAction)
                    Spacer(modifier = Modifier.height(8.dp))
                    PromotionApplicationsSection(state = state, onAction = onAction)
                }
                Column(modifier = Modifier.weight(3f)) {
                    PromotionApplyForm(state = state, onAction = onAction)
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}
