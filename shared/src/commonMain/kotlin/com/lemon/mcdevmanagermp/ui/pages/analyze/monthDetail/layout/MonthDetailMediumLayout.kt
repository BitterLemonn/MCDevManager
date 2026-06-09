package com.lemon.mcdevmanagermp.ui.pages.analyze.monthDetail.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import com.lemon.mcdevmanagermp.ui.pages.analyze.monthDetail.MonthDetailAction
import com.lemon.mcdevmanagermp.ui.pages.analyze.monthDetail.MonthDetailState

/**
 * Medium 布局（平板 600-840dp）
 * 月度卡片两列展示，其余与 Compact 一致
 */
@Composable
internal fun MonthDetailMediumLayout(
    state: MonthDetailState,
    onAction: (MonthDetailAction) -> Unit,
    onBack: () -> Unit,
    navBarBottom: Dp,
) {
    // Medium 与 Compact 共用主布局，通过自定义卡片区域实现两列
    MonthDetailCompactLayout(
        state = state,
        onAction = onAction,
        onBack = onBack,
        navBarBottom = navBarBottom,
    )
}
