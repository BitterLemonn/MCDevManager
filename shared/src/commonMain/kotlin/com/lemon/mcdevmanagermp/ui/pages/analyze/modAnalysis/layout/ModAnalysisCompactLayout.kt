package com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis.layout

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.domain.analyze.SummaryMetrics
import com.lemon.mcdevmanagermp.ui.components.CollapsingTopBar
import com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis.ChartType
import com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis.MetricType
import com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis.ModAnalysisAction
import com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis.ModAnalysisState
import com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis.components.ChartSection
import com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis.components.MetricCard
import com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis.components.ResourceSelector
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_bar_chart
import mcdevmanagermpr.shared.generated.resources.ic_line_chart
import mcdevmanagermpr.shared.generated.resources.ic_refresh
import mcdevmanagermpr.shared.generated.resources.ic_star
import org.jetbrains.compose.resources.painterResource

/**
 * Compact 布局（手机 < 600dp）和 Medium 布局（平板 600-840dp）
 * 垂直滚动，所有内容纵向排列
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun ModAnalysisCompactLayout(
    state: ModAnalysisState,
    onAction: (ModAnalysisAction) -> Unit,
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
            title = "模组分析",
            collapseFraction = collapseFraction,
            onBack = onBack,
            actions = {
                if (state.selectedIid.isNotEmpty()) {
                    IconButton(onClick = { onAction(ModAnalysisAction.RefreshData) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_refresh),
                            contentDescription = "刷新",
                            modifier = Modifier.size(20.dp),
                            tint = colors.textColor
                        )
                    }
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

            // 资源选择器
            ResourceSelector(
                resList = state.resList,
                selectedIid = state.selectedIid,
                isExpanded = state.isResourceSelectorExpanded,
                onToggle = { onAction(ModAnalysisAction.ToggleResourceSelector) },
                onSelect = { onAction(ModAnalysisAction.SelectResource(it)) }
            )

            // 标题卡片（选中资源后显示）
            if (state.selectedIid.isNotEmpty() && state.modName.isNotEmpty()) {
                ModTitleCard(
                    modName = state.modName,
                    iid = state.selectedIid,
                    score = state.modScore
                )
            }

            // 四指标网格（2x2）
            if (state.selectedIid.isNotEmpty()) {
                MetricsGrid(
                    metrics = state.summaryMetrics,
                    columns = 2
                )
            }

            // 指标筛选标签
            if (state.analysisData.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MetricFilterChip(
                        label = "新增购买",
                        selected = state.metricType == MetricType.NEW_PURCHASE,
                        onClick = { onAction(ModAnalysisAction.SelectMetric(MetricType.NEW_PURCHASE)) }
                    )
                    MetricFilterChip(
                        label = "日活",
                        selected = state.metricType == MetricType.DAU,
                        onClick = { onAction(ModAnalysisAction.SelectMetric(MetricType.DAU)) }
                    )
                    MetricFilterChip(
                        label = "新增粉丝",
                        selected = state.metricType == MetricType.NEW_FOLLOW,
                        onClick = { onAction(ModAnalysisAction.SelectMetric(MetricType.NEW_FOLLOW)) }
                    )
                    MetricFilterChip(
                        label = "人均游玩时间",
                        selected = state.metricType == MetricType.AVG_PLAY_TIME,
                        onClick = { onAction(ModAnalysisAction.SelectMetric(MetricType.AVG_PLAY_TIME)) }
                    )
                }

                // 图表类型切换 + 图例
                ChartTypeRow(
                    chartType = state.chartType,
                    onToggle = { onAction(ModAnalysisAction.ToggleChartType) },
                    modName = state.modName
                )

                // VICO 图表
                ChartSection(
                    analysisData = state.analysisData,
                    metricType = state.metricType,
                    chartType = state.chartType
                )
            } else if (state.selectedIid.isNotEmpty() && !state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无分析数据",
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
 * 模组标题卡片
 */
@Composable
private fun ModTitleCard(
    modName: String,
    iid: String,
    score: Double,
) {
    val colors = LocalAppColors.current

    androidx.compose.material3.ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
            containerColor = colors.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = modName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textColor,
                    maxLines = 1
                )
                Text(
                    text = "IID: $iid",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(Res.drawable.ic_star),
                    contentDescription = "评分",
                    modifier = Modifier.size(16.dp),
                    tint = colors.primary
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "${(score * 10).toInt() / 10.0}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
            }
        }
    }
}

/**
 * 四指标网格
 */
@Composable
private fun MetricsGrid(
    metrics: SummaryMetrics,
    columns: Int = 2,
) {
    val items = listOf(
        CompactMetricItem(
            Res.drawable.ic_line_chart,
            "新增购买",
            metrics.newPurchaseCount.toString(),
            "${(metrics.newPurchasePercent * 1000).toInt() / 10.0}"
        ),
        CompactMetricItem(
            Res.drawable.ic_line_chart,
            "日活",
            metrics.dau.toString(),
            "${(metrics.dauPercent * 1000).toInt() / 10.0}"
        ),
        CompactMetricItem(
            Res.drawable.ic_line_chart,
            "新增粉丝",
            metrics.newFollowCount.toString(),
            "${(metrics.newFollowPercent * 1000).toInt() / 10.0}"
        ),
        CompactMetricItem(
            Res.drawable.ic_line_chart,
            "人均游玩(min)",
            "${(metrics.avgPlayTime * 10).toInt() / 10.0}",
            "${(metrics.avgPlayTimePercent * 1000).toInt() / 10.0}"
        ),
    )

    if (columns == 2) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { item ->
                        MetricCard(
                            icon = item.icon,
                            title = item.title,
                            value = item.value,
                            percent = item.percent,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // 奇数个时填充空白
                    if (row.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.forEach { item ->
                MetricCard(
                    icon = item.icon,
                    title = item.title,
                    value = item.value,
                    percent = item.percent,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private data class CompactMetricItem(
    val icon: org.jetbrains.compose.resources.DrawableResource,
    val title: String,
    val value: String,
    val percent: String,
)

/**
 * 指标筛选 Chip
 */
@Composable
private fun MetricFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium
            )
        },
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

/**
 * 图表类型切换行 + 图例
 */
@Composable
private fun ChartTypeRow(
    chartType: ChartType,
    onToggle: () -> Unit,
    modName: String,
) {
    val colors = LocalAppColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 图例
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(colors.primary)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = modName,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(colors.onSurfaceVariant.copy(alpha = 0.5f))
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "同类均值",
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant
        )

        Spacer(Modifier.weight(1f))

        // 图表类型切换按钮
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surfaceContainerLow)
                .padding(2.dp)
        ) {
            ChartTypeButton(
                icon = Res.drawable.ic_line_chart,
                isSelected = chartType == ChartType.LINE,
                onClick = { if (chartType != ChartType.LINE) onToggle() }
            )
            ChartTypeButton(
                icon = Res.drawable.ic_bar_chart,
                isSelected = chartType == ChartType.COLUMN,
                onClick = { if (chartType != ChartType.COLUMN) onToggle() }
            )
        }
    }
}

@Composable
private fun ChartTypeButton(
    icon: org.jetbrains.compose.resources.DrawableResource,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) colors.primary.copy(alpha = 0.12f) else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = if (isSelected) colors.primary else colors.onSurfaceVariant
        )
    }
}
