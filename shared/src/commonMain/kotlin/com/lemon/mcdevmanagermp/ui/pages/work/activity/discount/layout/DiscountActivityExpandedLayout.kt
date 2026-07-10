package com.lemon.mcdevmanagermp.ui.pages.work.activity.discount.layout

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.components.CollapsingTopBar
import com.lemon.mcdevmanagermp.ui.pages.work.activity.discount.DiscountActivityAction
import com.lemon.mcdevmanagermp.ui.pages.work.activity.discount.DiscountActivityState
import com.lemon.mcdevmanagermp.ui.pages.work.activity.discount.layout.component.DiscountCandidateSection
import com.lemon.mcdevmanagermp.ui.pages.work.activity.discount.layout.component.DiscountDescCard
import com.lemon.mcdevmanagermp.ui.pages.work.activity.discount.layout.component.DiscountEmptyState
import com.lemon.mcdevmanagermp.ui.pages.work.activity.discount.layout.component.DiscountInfoHeader
import com.lemon.mcdevmanagermp.ui.pages.work.activity.discount.layout.component.DiscountJoinedSection
import com.lemon.mcdevmanagermp.ui.pages.work.activity.discount.layout.component.DiscountLoadingBox
import com.lemon.mcdevmanagermp.ui.pages.work.activity.discount.layout.component.DiscountModuleSelector
import com.lemon.mcdevmanagermp.ui.pages.work.activity.discount.layout.component.DiscountPartitionRulesCard
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_refresh
import org.jetbrains.compose.resources.painterResource

/**
 * 折扣特卖 - 展开布局（> 840dp，桌面）。
 * 左右双栏：左侧活动信息/描述/分区规则（约 40%），右侧赛道/已参加/候选与表单（约 60%）。
 */
@Composable
internal fun DiscountActivityExpandedLayout(
    state: DiscountActivityState,
    onAction: (DiscountActivityAction) -> Unit,
    onBack: () -> Unit
) {
    val colors = LocalAppColors.current
    val scrollState = rememberScrollState()
    val scrollAlpha = remember {
        derivedStateOf { (scrollState.value / 100f).coerceIn(0f, 1f) }
    }
    val activity = state.activity

    Column(modifier = Modifier.fillMaxSize()) {
        CollapsingTopBar(
            title = "折扣特卖",
            collapseFraction = scrollAlpha.value,
            onBack = onBack,
            actions = {
                IconButton(onClick = { onAction(DiscountActivityAction.Refresh) }) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_refresh),
                        contentDescription = "刷新",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        )

        when {
            state.isLoading && activity == null -> DiscountLoadingBox(Modifier.fillMaxSize())
            activity == null -> DiscountEmptyState(
                onRefresh = { onAction(DiscountActivityAction.Refresh) },
                modifier = Modifier.fillMaxSize()
            )

            else -> Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 左栏：活动信息（约 40%）
                Column(
                    modifier = Modifier.weight(2f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DiscountInfoHeader(activity = activity)
                    DiscountDescCard(activity = activity)
                    DiscountPartitionRulesCard(activity = activity)
                }

                // 右栏：赛道与参与（约 60%）
                Column(
                    modifier = Modifier.weight(3f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DiscountModuleSelector(state = state, onAction = onAction)
                    DiscountJoinedSection(state = state, onAction = onAction)
                    DiscountCandidateSection(state = state, onAction = onAction)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
