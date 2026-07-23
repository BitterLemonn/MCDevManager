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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.domain.analyze.SummaryMetrics
import com.lemon.mcdevmanagermp.ui.components.CollapsingTopBar
import com.lemon.mcdevmanagermp.ui.iconpack.BarChart
import com.lemon.mcdevmanagermp.ui.iconpack.IconPack
import com.lemon.mcdevmanagermp.ui.iconpack.LineChart
import com.lemon.mcdevmanagermp.ui.iconpack.Star
import com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis.ChartType
import com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis.MetricType
import com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis.ModAnalysisAction
import com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis.ModAnalysisState
import com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis.components.ChartSection
import com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis.components.ResourceSelector
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

/**
 * Medium 布局（平板 600-840dp）
 * 与 Compact 类似，但四指标横向排列（1x4），图表区域更大
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun ModAnalysisMediumLayout(
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
        CollapsingTopBar(
            title = "模组分析",
            collapseFraction = collapseFraction,
            onBack = onBack,
            actions = {
                if (state.selectedIid.isNotEmpty()) {
                    IconButton(onClick = { onAction(ModAnalysisAction.RefreshData) }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "刷新",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
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

            // 标题卡片
            if (state.selectedIid.isNotEmpty() && state.modName.isNotEmpty()) {
                MediumModTitleCard(
                    modName = state.modName,
                    iid = state.selectedIid,
                    score = state.modScore
                )
            }

            // 四指标横向排列（1x4）
            if (state.selectedIid.isNotEmpty()) {
                MediumMetricsRow(metrics = state.summaryMetrics)
            }

            // 指标筛选标签
            if (state.analysisData.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MediumMetricFilterChip(
                        label = "新增购买",
                        selected = state.metricType == MetricType.NEW_PURCHASE,
                        onClick = { onAction(ModAnalysisAction.SelectMetric(MetricType.NEW_PURCHASE)) }
                    )
                    MediumMetricFilterChip(
                        label = "日活",
                        selected = state.metricType == MetricType.DAU,
                        onClick = { onAction(ModAnalysisAction.SelectMetric(MetricType.DAU)) }
                    )
                    MediumMetricFilterChip(
                        label = "新增粉丝",
                        selected = state.metricType == MetricType.NEW_FOLLOW,
                        onClick = { onAction(ModAnalysisAction.SelectMetric(MetricType.NEW_FOLLOW)) }
                    )
                    MediumMetricFilterChip(
                        label = "人均游玩时间",
                        selected = state.metricType == MetricType.AVG_PLAY_TIME,
                        onClick = { onAction(ModAnalysisAction.SelectMetric(MetricType.AVG_PLAY_TIME)) }
                    )
                }

                // 图表类型切换 + 图例
                MediumChartTypeRow(
                    chartType = state.chartType,
                    onToggle = { onAction(ModAnalysisAction.ToggleChartType) },
                    modName = state.modName
                )

                // VICO 图表（更大）
                ChartSection(
                    analysisData = state.analysisData,
                    metricType = state.metricType,
                    chartType = state.chartType
                )
            } else if (state.selectedIid.isNotEmpty() && !state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
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

@Composable
private fun MediumModTitleCard(
    modName: String,
    iid: String,
    score: Double,
) {
    val colors = LocalAppColors.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh),
        shape = RoundedCornerShape(12.dp)
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
                    color = colors.textColor
                )
                Text(
                    text = "IID: $iid",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = IconPack.Star,
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

@Composable
private fun MediumMetricsRow(metrics: SummaryMetrics) {
    val colors = LocalAppColors.current
    val items = listOf(
        MediumMetricData(
            "新增购买",
            metrics.newPurchaseCount.toString(),
            "${(metrics.newPurchasePercent * 1000).toInt() / 10.0}"
        ),
        MediumMetricData(
            "日活",
            metrics.dau.toString(),
            "${(metrics.dauPercent * 1000).toInt() / 10.0}"
        ),
        MediumMetricData(
            "新增粉丝",
            metrics.newFollowCount.toString(),
            "${(metrics.newFollowPercent * 1000).toInt() / 10.0}"
        ),
        MediumMetricData(
            "游玩(min)",
            "${(metrics.avgPlayTime * 10).toInt() / 10.0}",
            "${(metrics.avgPlayTimePercent * 1000).toInt() / 10.0}"
        ),
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh),
        shape = RoundedCornerShape(12.dp),

        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEach { item ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = item.value,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textColor
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "超过 ${item.percent}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private data class MediumMetricData(
    val title: String,
    val value: String,
    val percent: String,
)

@Composable
private fun MediumMetricFilterChip(
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

@Composable
private fun MediumChartTypeRow(
    chartType: ChartType,
    onToggle: () -> Unit,
    modName: String,
) {
    val colors = LocalAppColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
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

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surfaceContainerLow)
                .padding(2.dp)
        ) {
            MediumChartTypeButton(
                icon = IconPack.LineChart,
                isSelected = chartType == ChartType.LINE,
                onClick = { if (chartType != ChartType.LINE) onToggle() }
            )
            MediumChartTypeButton(
                icon = IconPack.BarChart,
                isSelected = chartType == ChartType.COLUMN,
                onClick = { if (chartType != ChartType.COLUMN) onToggle() }
            )
        }
    }
}

@Composable
private fun MediumChartTypeButton(
    icon: ImageVector,
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
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = if (isSelected) colors.primary else colors.onSurfaceVariant
        )
    }
}
