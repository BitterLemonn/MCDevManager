package com.lemon.mcdevmanagermp.ui.pages.work.promotion.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_refresh
import org.jetbrains.compose.resources.painterResource

/**
 * PE 轮播图申请 - 紧凑布局（< 600dp）。
 * TabBar 切换"申请 / 我的申请"：申请 tab=可申请周+表单，历史 tab=申请记录。
 */
@Composable
internal fun PromotionCompactLayout(
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
                        painter = painterResource(Res.drawable.ic_refresh),
                        contentDescription = "刷新",
                        tint = colors.onSurfaceVariant,
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
