package com.lemon.mcdevmanagermp.ui.pages.analyze.monthDetail.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResMonthAnalyzeData
import com.lemon.mcdevmanagermp.ui.pages.analyze.monthDetail.MonthMetricType
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberFadingEdges
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.patrykandpatrick.vico.compose.common.vicoTheme

/**
 * 月度趋势折线图
 * 按月份聚合展示选定指标的趋势
 */
@Composable
internal fun MonthTrendChart(
    monthData: List<ResMonthAnalyzeData>,
    selectedMetric: Int,
    modifier: Modifier = Modifier,
) {
    if (monthData.isEmpty()) return

    // 按月份聚合
    val trendData = remember(monthData, selectedMetric) {
        val grouped = monthData.groupBy { it.monthId }
        val vals = grouped.values.map { list ->
            list.sumOf { getMetricValue(it, selectedMetric).toDouble() }
        }
        val lbls = grouped.keys.map { formatMonthLabel(it) }
        Pair(vals, lbls)
    }
    val values = trendData.first
    val labels = trendData.second

    if (values.isEmpty()) return

    val appColors = LocalAppColors.current

    key(selectedMetric) {
        val modelProducer = remember { CartesianChartModelProducer() }
        val labelsKey = remember { ExtraStore.Key<List<String>>() }

        LaunchedEffect(values) {
            modelProducer.runTransaction {
                lineModel { series(values) }
                extras { it[labelsKey] = labels }
            }
        }

        val bottomFormatter = CartesianValueFormatter { context, x, _ ->
            val lbls = context.model.extraStore[labelsKey]
            val index = x.toInt()
            if (index >= 0 && index < lbls.size) lbls[index] else ""
        }

        val startFormatter = CartesianValueFormatter { _, value, _ ->
            formatChartValue(value)
        }

        val vicoTheme = vicoTheme
        ProvideVicoTheme(
            vicoTheme.copy(
                lineCartesianLayerColors = listOf(appColors.primary),
                textColor = appColors.onSurface,
            )
        ) {
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(),
                    startAxis = VerticalAxis.rememberStart(
                        valueFormatter = startFormatter,
                        itemPlacer = VerticalAxis.ItemPlacer.count({ 5 }),
                    ),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        valueFormatter = bottomFormatter,
                        itemPlacer = HorizontalAxis.ItemPlacer.segmented(),
                    ),
                    fadingEdges = rememberFadingEdges(),
                ),
                modelProducer = modelProducer,
                modifier = modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 8.dp),
            )
        }
    }
}

private fun getMetricValue(data: ResMonthAnalyzeData, metricType: Int): Number {
    return when (metricType) {
        MonthMetricType.TOTAL_DIAMOND -> data.totalDiamond
        MonthMetricType.TOTAL_POINTS -> data.totalPoints
        MonthMetricType.AVG_DAU -> data.avgDau
        MonthMetricType.AVG_DAY_BUY -> data.avgDayBuy
        MonthMetricType.DOWNLOAD -> data.downloadNum
        MonthMetricType.MAU -> data.mau
        else -> 0
    }
}

private fun formatMonthLabel(monthId: String): String {
    return try {
        if (monthId.length == 6) {
            val month = monthId.substring(4, 6).toIntOrNull() ?: return monthId
            "${month}月"
        } else monthId
    } catch (_: Exception) {
        monthId
    }
}

private fun formatChartValue(value: Double): String {
    return when {
        value >= 1_000_000 -> "${(value / 100_000).toInt() / 10.0}m"
        value >= 1_000 -> "${(value / 100).toInt() / 10.0}k"
        value == value.toInt().toDouble() -> value.toInt().toString()
        else -> "${(value * 10).toInt() / 10.0}"
    }
}
