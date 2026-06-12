package com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail.layout

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.components.CollapsingTopBar
import com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail.CHART_COLORS
import com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail.DayDetailAction
import com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail.DayDetailMetricType
import com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail.DayDetailState
import com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail.components.DateRangePicker
import com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail.components.DayDetailChart
import com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail.components.ResourceMultiSelector
import com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis.ChartType
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_bar_chart
import mcdevmanagermpr.shared.generated.resources.ic_line_chart
import mcdevmanagermpr.shared.generated.resources.ic_refresh
import org.jetbrains.compose.resources.painterResource

/**
 * Compact 布局（手机 < 600dp）
 * 垂直滚动，所有内容纵向排列
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun DayDetailCompactLayout(
    state: DayDetailState,
    onAction: (DayDetailAction) -> Unit,
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
            title = "数据追踪",
            collapseFraction = collapseFraction,
            onBack = onBack,
            actions = {
                if (state.selectedIIDs.isNotEmpty()) {
                    IconButton(onClick = { onAction(DayDetailAction.RefreshData) }) {
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

            // 平台切换
            PlatformToggle(
                platform = state.platform,
                onPlatformChange = { onAction(DayDetailAction.SetPlatform(it)) }
            )

            // 资源选择器
            ResourceMultiSelector(
                resList = state.resList,
                selectedIIDs = state.selectedIIDs,
                isExpanded = state.isResSelectorVisible,
                onToggle = { onAction(DayDetailAction.ToggleResSelector) },
                onToggleResource = { onAction(DayDetailAction.ToggleResource(it)) }
            )

            // 日期范围
            if (state.selectedIIDs.isNotEmpty()) {
                DateRangePicker(
                    startDate = state.startDate,
                    endDate = state.endDate,
                    onSelectRange = { s, e ->
                        onAction(DayDetailAction.SetDateRange(s, e))
                        onAction(DayDetailAction.LoadData(state.selectedIIDs))
                    }
                )

                // 查询按钮
                androidx.compose.material3.FilledTonalButton(
                    onClick = { onAction(DayDetailAction.LoadData(state.selectedIIDs)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                        containerColor = colors.primary.copy(alpha = 0.12f),
                        contentColor = colors.primary
                    )
                ) {
                    Text("查询数据", fontWeight = FontWeight.SemiBold)
                }
            }

            // 指标筛选 + 图表
            if (state.detailData.isNotEmpty()) {
                // 指标筛选 Chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MetricChip("新增购买", state.metricType == DayDetailMetricType.NEW_PURCHASE) {
                        onAction(DayDetailAction.SelectMetric(DayDetailMetricType.NEW_PURCHASE))
                    }
                    MetricChip("下载量", state.metricType == DayDetailMetricType.DOWNLOAD) {
                        onAction(DayDetailAction.SelectMetric(DayDetailMetricType.DOWNLOAD))
                    }
                    MetricChip("钻石收益", state.metricType == DayDetailMetricType.DIAMOND) {
                        onAction(DayDetailAction.SelectMetric(DayDetailMetricType.DIAMOND))
                    }
                    MetricChip("绿宝石", state.metricType == DayDetailMetricType.POINTS) {
                        onAction(DayDetailAction.SelectMetric(DayDetailMetricType.POINTS))
                    }
                    MetricChip("日活", state.metricType == DayDetailMetricType.DAU) {
                        onAction(DayDetailAction.SelectMetric(DayDetailMetricType.DAU))
                    }
                    MetricChip("退款率", state.metricType == DayDetailMetricType.REFUND_RATE) {
                        onAction(DayDetailAction.SelectMetric(DayDetailMetricType.REFUND_RATE))
                    }
                }

                // 图例 + 图表类型切换
                ChartHeaderRow(
                    chartType = state.chartType,
                    detailData = state.detailData,
                    resNameMap = buildResNameMap(state),
                    onToggle = { onAction(DayDetailAction.ToggleChartType) }
                )

                // 多资源图表
                DayDetailChart(
                    detailData = state.detailData,
                    resNameMap = buildResNameMap(state),
                    metricType = state.metricType,
                    chartType = state.chartType
                )

                // 数据明细
                DetailDataSection(state = state)
            } else if (state.selectedIIDs.isNotEmpty() && !state.isLoading) {
                EmptyDataHint()
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * 平台切换 PE/PC
 */
@Composable
internal fun PlatformToggle(
    platform: String,
    onPlatformChange: (String) -> Unit,
) {
    val colors = LocalAppColors.current

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surfaceContainerLow)
            .padding(2.dp)
    ) {
        PlatformButton("PE", platform == "pe") { onPlatformChange("pe") }
        PlatformButton("PC", platform == "comp") { onPlatformChange("comp") }
    }
}

@Composable
internal fun PlatformButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current

    Box(
        modifier = Modifier
            .size(width = 48.dp, height = 30.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) colors.primary.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) colors.primary else colors.onSurfaceVariant
        )
    }
}

@Composable
internal fun MetricChip(
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

/**
 * 图表头部：图例 + 图表类型切换
 * 图例使用 FlowRow 自动换行，图表类型切换按钮固定在右侧
 */
@Composable
internal fun ChartHeaderRow(
    chartType: ChartType,
    detailData: Map<String, List<*>>,
    resNameMap: Map<String, String>,
    onToggle: () -> Unit,
) {
    val colors = LocalAppColors.current

    Column(modifier = Modifier.fillMaxWidth()) {
        // 图例（FlowRow 自动换行）
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            detailData.entries.forEachIndexed { index, (iid, _) ->
                val color = CHART_COLORS[index.coerceAtMost(CHART_COLORS.size - 1)]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(color)
                    )
                    Text(
                        text = resNameMap[iid]?.take(6) ?: iid.take(6),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // 图表类型切换（靠右）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
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
}

@Composable
internal fun ChartTypeButton(
    icon: org.jetbrains.compose.resources.DrawableResource,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current

    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) colors.primary.copy(alpha = 0.12f) else Color.Transparent)
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
            modifier = Modifier.size(16.dp),
            tint = if (isSelected) colors.primary else colors.onSurfaceVariant
        )
    }
}

/**
 * 数据明细区域
 */
@Composable
internal fun DetailDataSection(state: DayDetailState) {
    val colors = LocalAppColors.current

    // 按日期分组所有资源数据
    val allDates = remember(state.detailData) {
        state.detailData.values
            .firstOrNull()
            ?.map { it.dateId }
            ?: emptyList()
    }

    Text(
        text = "数据明细",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = colors.textColor,
        modifier = Modifier.padding(top = 4.dp)
    )

    allDates.reversed().forEach { dateId ->
        DateGroupCard(
            dateId = dateId,
            detailData = state.detailData,
            metricType = state.metricType
        )
    }
}

@Composable
private fun DateGroupCard(
    dateId: String,
    detailData: Map<String, List<com.lemon.mcdevmanagermp.data.vo.netease.resource.ResAnalyzeData>>,
    metricType: Int,
) {
    val colors = LocalAppColors.current

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = colors.surfaceContainerHigh),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = formatDateDisplay(dateId),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.primary
            )
            Spacer(Modifier.height(6.dp))

            detailData.entries.forEachIndexed { index, (iid, dataList) ->
                val item = dataList.find { it.dateId == dateId }
                if (item != null) {
                    val color = CHART_COLORS[index.coerceAtMost(CHART_COLORS.size - 1)]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(color)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = item.resName.take(10),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textColor,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        Text(
                            text = formatMetricValue(item, metricType),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = color
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                }
            }
        }
    }
}

/**
 * 构建资源 IID → 名称的映射
 */
internal fun buildResNameMap(state: DayDetailState): Map<String, String> {
    val map = mutableMapOf<String, String>()
    state.detailData.forEach { (iid, dataList) ->
        if (dataList.isNotEmpty()) {
            map[iid] = dataList.first().resName
        }
    }
    return map
}

private fun formatMetricValue(
    data: com.lemon.mcdevmanagermp.data.vo.netease.resource.ResAnalyzeData,
    metricType: Int
): String {
    return when (metricType) {
        DayDetailMetricType.NEW_PURCHASE -> data.cntBuy.toString()
        DayDetailMetricType.DOWNLOAD -> data.downloadNum.toString()
        DayDetailMetricType.DIAMOND -> data.diamond.toString()
        DayDetailMetricType.POINTS -> data.points.toString()
        DayDetailMetricType.DAU -> data.dau.toString()
        DayDetailMetricType.REFUND_RATE -> "${(data.refundRate * 1000).toInt() / 10.0}%"
        else -> "-"
    }
}

private fun formatDateDisplay(dateId: String): String {
    return try {
        if (dateId.length == 8) {
            "${dateId.substring(0, 4)}-${dateId.substring(4, 6)}-${dateId.substring(6, 8)}"
        } else dateId
    } catch (_: Exception) {
        dateId
    }
}

@Composable
internal fun EmptyDataHint() {
    val colors = LocalAppColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "选择资源并查询数据",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "支持同时对比最多 5 个资源",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
