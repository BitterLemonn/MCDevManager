package com.lemon.mcdevmanagermp.ui.pages.analyze.monthDetail.layout

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.unit.Dp
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
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_refresh
import org.jetbrains.compose.resources.painterResource

/**
 * Expanded 布局（桌面 > 840dp）
 * 左侧趋势图 + 右侧统计摘要，下方卡片网格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MonthDetailExpandedLayout(
    state: MonthDetailState,
    onAction: (MonthDetailAction) -> Unit,
    onBack: () -> Unit,
    navBarBottom: Dp,
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
                        painter = painterResource(Res.drawable.ic_refresh),
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // 控制区：平台 + 时间筛选
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PlatformToggle(
                    platform = state.platform,
                    onPlatformChange = { onAction(MonthDetailAction.SetPlatform(it)) }
                )
                QuickTimeFilter(
                    selectedRange = state.quickTimeRange,
                    onSelectRange = { onAction(MonthDetailAction.SetQuickTimeRange(it)) }
                )
            }

            if (state.monthData.isNotEmpty()) {
                // 上部：趋势图 + 统计摘要
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 左侧：趋势图 + 指标选择
                    Column(
                        modifier = Modifier.weight(2f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 趋势图指标 Chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TrendMetricChip("钻石", state.selectedMetric == MonthMetricType.TOTAL_DIAMOND) {
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

                        MonthTrendChart(
                            monthData = state.monthData,
                            selectedMetric = state.selectedMetric
                        )
                    }

                    // 右侧：统计摘要
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val totalDiamond = state.monthData.sumOf { it.totalDiamond }
                        val totalPoints = state.monthData.sumOf { it.totalPoints }
                        val totalDownloads = state.monthData.sumOf { it.downloadNum }
                        val totalMau = state.monthData.maxOfOrNull { it.mau } ?: 0

                        SummaryStatCard("总钻石收益", formatNum(totalDiamond))
                        SummaryStatCard("总绿宝石收益", formatNum(totalPoints))
                        SummaryStatCard("总下载量", formatNum(totalDownloads))
                        SummaryStatCard("最高月活", formatNum(totalMau))
                    }
                }

                // 下部：月度卡片网格（每行2列）
                val grouped = state.monthData.groupBy { it.monthId }
                grouped.keys.sortedDescending().forEach { monthId ->
                    Text(
                        text = formatMonthTitle(monthId),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    val monthItems = grouped[monthId] ?: emptyList()
                    // 按2个一组分行排列
                    monthItems.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { data ->
                                MonthCard(
                                    data = data,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // 奇数个时填充空白
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else if (!state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
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

@Composable
private fun SummaryStatCard(label: String, value: String) {
    val colors = LocalAppColors.current

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = colors.surfaceContainerHigh),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
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

private fun formatMonthTitle(monthId: String): String {
    return try {
        if (monthId.length == 6) {
            "${monthId.substring(0, 4)}年${monthId.substring(4, 6).toInt()}月"
        } else monthId
    } catch (_: Exception) {
        monthId
    }
}
