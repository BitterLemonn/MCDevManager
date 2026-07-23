package com.lemon.mcdevmanagermp.ui.pages.work.promotion.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import com.lemon.mcdevmanagermp.ui.pages.work.promotion.layout.component.PromotionTabbedContent
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

/**
 * PE 轮播图申请 - 中等布局（600~840dp）。与 Compact 共用 Tab 分发内容。
 */
@Composable
internal fun PromotionMediumLayout(
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
        PromotionTabbedContent(
            state = state,
            onAction = onAction,
            scrollState = scrollState,
            modifier = Modifier.fillMaxSize()
        )
    }
}
