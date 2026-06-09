package com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail.DayDetailAction
import com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail.DayDetailState

/**
 * Medium 布局（平板 600-840dp）
 * 与 Compact 类似，但指标筛选标签横排一行，图表更大
 */
@Composable
internal fun DayDetailMediumLayout(
    state: DayDetailState,
    onAction: (DayDetailAction) -> Unit,
    onBack: () -> Unit,
    navBarBottom: Dp,
) {
    // Medium 与 Compact 共用布局，差异通过内部自适应处理
    DayDetailCompactLayout(
        state = state,
        onAction = onAction,
        onBack = onBack,
        navBarBottom = navBarBottom,
    )
}
