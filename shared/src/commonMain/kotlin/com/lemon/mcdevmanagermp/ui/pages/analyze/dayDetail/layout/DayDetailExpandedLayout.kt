package com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail.layout

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
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
import com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail.DayDetailAction
import com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail.DayDetailMetricType
import com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail.DayDetailState
import com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail.components.DateRangePicker
import com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail.components.DayDetailChart
import com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail.components.ResourceMultiSelector
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_refresh
import org.jetbrains.compose.resources.painterResource

/**
 * Expanded 布局（桌面 > 840dp）
 * 左侧控制面板 + 右侧图表/数据区域
 */
@Composable
internal fun DayDetailExpandedLayout(
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

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            // 左侧控制面板（固定宽度 300dp）
            Column(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(end = 12.dp),
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

                    // 指标筛选
                    Text(
                        text = "指标类型",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        MetricChipRow("新增购买", state.metricType == DayDetailMetricType.NEW_PURCHASE) {
                            onAction(DayDetailAction.SelectMetric(DayDetailMetricType.NEW_PURCHASE))
                        }
                        MetricChipRow("下载量", state.metricType == DayDetailMetricType.DOWNLOAD) {
                            onAction(DayDetailAction.SelectMetric(DayDetailMetricType.DOWNLOAD))
                        }
                        MetricChipRow("钻石收益", state.metricType == DayDetailMetricType.DIAMOND) {
                            onAction(DayDetailAction.SelectMetric(DayDetailMetricType.DIAMOND))
                        }
                        MetricChipRow("绿宝石", state.metricType == DayDetailMetricType.POINTS) {
                            onAction(DayDetailAction.SelectMetric(DayDetailMetricType.POINTS))
                        }
                        MetricChipRow("日活", state.metricType == DayDetailMetricType.DAU) {
                            onAction(DayDetailAction.SelectMetric(DayDetailMetricType.DAU))
                        }
                        MetricChipRow("退款率", state.metricType == DayDetailMetricType.REFUND_RATE) {
                            onAction(DayDetailAction.SelectMetric(DayDetailMetricType.REFUND_RATE))
                        }
                        MetricChipRow("愿望单新增", state.metricType == DayDetailMetricType.WISHLIST_ADDS) {
                            onAction(DayDetailAction.SelectMetric(DayDetailMetricType.WISHLIST_ADDS))
                        }
                        MetricChipRow("愿望单赠送", state.metricType == DayDetailMetricType.WISHLIST_GIFTS) {
                            onAction(DayDetailAction.SelectMetric(DayDetailMetricType.WISHLIST_GIFTS))
                        }
                        MetricChipRow("愿望单购买", state.metricType == DayDetailMetricType.WISHLIST_PURCHASES) {
                            onAction(DayDetailAction.SelectMetric(DayDetailMetricType.WISHLIST_PURCHASES))
                        }
                        MetricChipRow("愿望单移除", state.metricType == DayDetailMetricType.WISHLIST_REMOVES) {
                            onAction(DayDetailAction.SelectMetric(DayDetailMetricType.WISHLIST_REMOVES))
                        }
                    }

                    // 查询按钮
                    FilledTonalButton(
                        onClick = { onAction(DayDetailAction.LoadData(state.selectedIIDs)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = colors.primary.copy(alpha = 0.12f),
                            contentColor = colors.primary
                        )
                    ) {
                        Text("查询数据", fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            // 右侧图表 + 数据区域
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                if (state.detailData.isNotEmpty()) {
                    // 图例 + 图表类型切换
                    ChartHeaderRow(
                        chartType = state.chartType,
                        detailData = state.detailData,
                        resNameMap = buildResNameMap(state),
                        onToggle = { onAction(DayDetailAction.ToggleChartType) }
                    )

                    // 大图表
                    DayDetailChart(
                        detailData = state.detailData,
                        metricType = state.metricType,
                        chartType = state.chartType
                    )

                    // 数据明细
                    DetailDataSection(state = state)
                } else if (state.selectedIIDs.isNotEmpty() && !state.isLoading) {
                    EmptyDataHint()
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "选择资源并查询数据以查看图表",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun MetricChipRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (selected) colors.primary.copy(alpha = 0.12f)
            else colors.surfaceContainerLow
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) colors.primary else colors.onSurfaceVariant
            )
        }
    }
}
