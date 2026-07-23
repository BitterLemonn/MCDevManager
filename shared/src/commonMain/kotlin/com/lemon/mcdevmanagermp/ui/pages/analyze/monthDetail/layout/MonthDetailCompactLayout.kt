package com.lemon.mcdevmanagermp.ui.pages.analyze.monthDetail.layout

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.components.CollapsingTopBar
import com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail.layout.PlatformToggle
import com.lemon.mcdevmanagermp.ui.pages.analyze.monthDetail.MonthDetailAction
import com.lemon.mcdevmanagermp.ui.pages.analyze.monthDetail.MonthDetailState
import com.lemon.mcdevmanagermp.ui.pages.analyze.monthDetail.MonthMetricType
import com.lemon.mcdevmanagermp.ui.pages.analyze.monthDetail.components.MonthCard
import com.lemon.mcdevmanagermp.ui.pages.analyze.monthDetail.components.MonthTrendChart
import com.lemon.mcdevmanagermp.ui.pages.analyze.monthDetail.components.QuickTimeFilter
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

/**
 * Compact 布局（手机 < 600dp）
 * 垂直滚动，卡片纵向排列
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun MonthDetailCompactLayout(
    state: MonthDetailState,
    onAction: (MonthDetailAction) -> Unit,
    onBack: () -> Unit,
    navBarBottom: androidx.compose.ui.unit.Dp,
) {
    val colors = LocalAppColors.current
    val scrollState = remember { ScrollState(0) }
    val collapseFraction by remember {
        derivedStateOf { (scrollState.value / 100f).coerceIn(0f, 1f) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = navBarBottom)
    ) {
        // TopBar
        CollapsingTopBar(
            title = "数据汇总",
            collapseFraction = collapseFraction,
            onBack = onBack,
            actions = {
                IconButton(onClick = { onAction(MonthDetailAction.RefreshData) }) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "刷新",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // 平台切换
            PlatformToggle(
                platform = state.platform,
                onPlatformChange = { onAction(MonthDetailAction.SetPlatform(it)) }
            )

            // 快捷时间筛选
            QuickTimeFilter(
                selectedRange = state.quickTimeRange,
                onSelectRange = { onAction(MonthDetailAction.SetQuickTimeRange(it)) }
            )

            // 汇总统计
            if (state.monthData.isNotEmpty()) {
                SummaryRow(state = state)
            }

            // 趋势图指标选择
            if (state.monthData.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TrendMetricChip("钻石收益", state.selectedMetric == MonthMetricType.TOTAL_DIAMOND) {
                        onAction(MonthDetailAction.SelectMetric(MonthMetricType.TOTAL_DIAMOND))
                    }
                    TrendMetricChip("绿宝石", state.selectedMetric == MonthMetricType.TOTAL_POINTS) {
                        onAction(MonthDetailAction.SelectMetric(MonthMetricType.TOTAL_POINTS))
                    }
                    TrendMetricChip("日活", state.selectedMetric == MonthMetricType.AVG_DAU) {
                        onAction(MonthDetailAction.SelectMetric(MonthMetricType.AVG_DAU))
                    }
                    TrendMetricChip("购买", state.selectedMetric == MonthMetricType.AVG_DAY_BUY) {
                        onAction(MonthDetailAction.SelectMetric(MonthMetricType.AVG_DAY_BUY))
                    }
                    TrendMetricChip("下载", state.selectedMetric == MonthMetricType.DOWNLOAD) {
                        onAction(MonthDetailAction.SelectMetric(MonthMetricType.DOWNLOAD))
                    }
                    TrendMetricChip("月活", state.selectedMetric == MonthMetricType.MAU) {
                        onAction(MonthDetailAction.SelectMetric(MonthMetricType.MAU))
                    }
                }

                // 月度趋势图
                MonthTrendChart(
                    monthData = state.monthData,
                    selectedMetric = state.selectedMetric
                )
            }

            // 月度卡片列表
            state.monthData.forEach { data ->
                MonthCard(data = data)
            }

            if (state.monthData.isEmpty() && !state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无月度数据",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * 汇总统计行
 */
@Composable
private fun SummaryRow(state: MonthDetailState) {
    val colors = LocalAppColors.current
    val totalDiamond = state.monthData.sumOf { it.totalDiamond }
    val totalPoints = state.monthData.sumOf { it.totalPoints }
    val totalDownloads = state.monthData.sumOf { it.downloadNum }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryItem("总钻石", formatNum(totalDiamond), Modifier.weight(1f))
        SummaryItem("总绿宝石", formatNum(totalPoints), Modifier.weight(1f))
        SummaryItem("总下载", formatNum(totalDownloads), Modifier.weight(1f))
    }
}

@Composable
private fun SummaryItem(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colors.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TrendMetricChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label, style = MaterialTheme.typography.labelMedium) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = colors.primary.copy(alpha = 0.12f),
            selectedLabelColor = colors.primary,
            containerColor = colors.surfaceContainerLow,
            labelColor = colors.onSurfaceVariant
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = colors.outlineVariant,
            selectedBorderColor = colors.primary,
            enabled = true,
            selected = selected
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

private fun formatNum(num: Int): String {
    return when {
        num >= 1_000_000 -> "${(num / 100_000).toDouble() / 10.0}m"
        num >= 10_000 -> "${(num / 1_000).toDouble() / 10.0}w"
        num >= 1_000 -> "${(num / 100).toDouble() / 10.0}k"
        else -> num.toString()
    }
}
