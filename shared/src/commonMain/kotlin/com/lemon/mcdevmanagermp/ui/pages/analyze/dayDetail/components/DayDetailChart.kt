package com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.vo.netease.analyze.ResAnalyzeData
import com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail.CHART_COLORS
import com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail.DayDetailMetricType
import com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis.ChartType
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberFadingEdges
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.patrykandpatrick.vico.compose.common.vicoTheme

/**
 * 多资源对比图表
 * 每个资源一条线/一组柱，使用不同颜色区分
 */
@Composable
internal fun DayDetailChart(
    detailData: Map<String, List<ResAnalyzeData>>,
    metricType: Int,
    chartType: ChartType,
    modifier: Modifier = Modifier,
) {
    if (detailData.isEmpty()) return

    // 提取所有日期标签（取第一个资源的日期列表）
    val dateLabels = remember(detailData) {
        detailData.values.firstOrNull()?.map { formatDateLabel(it.dateId) } ?: emptyList()
    }
    if (dateLabels.isEmpty()) return

    // 提取各资源的指标值
    val seriesData = remember(detailData, metricType) {
        detailData.entries.mapIndexed { index, (_, dataList) ->
            val values = dataList.map { getMetricValue(it, metricType) }
            Pair(index, values)
        }
    }

    // 图表颜色：按资源顺序分配
    val chartColors = remember(detailData.size) {
        val colorCount = detailData.size.coerceAtMost(CHART_COLORS.size)
        (0 until colorCount).map { CHART_COLORS[it] }
    }

    key(chartType, metricType, detailData.keys) {
        val modelProducer = remember { CartesianChartModelProducer() }
        val dateLabelsKey = remember { ExtraStore.Key<List<String>>() }

        LaunchedEffect(seriesData) {
            modelProducer.runTransaction {
                if (chartType == ChartType.LINE) {
                    lineModel {
                        seriesData.forEach { (_, values) -> series(values) }
                    }
                } else {
                    columnModel {
                        seriesData.forEach { (_, values) -> series(values) }
                    }
                }
                extras { it[dateLabelsKey] = dateLabels }
            }
        }

        val bottomValueFormatter = CartesianValueFormatter { context, x, _ ->
            val labels = context.model.extraStore[dateLabelsKey]
            val index = x.toInt()
            if (index >= 0 && index < labels.size) labels[index] else ""
        }

        val startValueFormatter = CartesianValueFormatter { _, value, _ ->
            formatChartValue(value)
        }

        val chartHeight = if (chartType == ChartType.LINE) 240.dp else 260.dp

        val appColors = LocalAppColors.current
        val vicoTheme = vicoTheme
        ProvideVicoTheme(
            vicoTheme.copy(
                columnCartesianLayerColors = chartColors,
                lineCartesianLayerColors = chartColors,
                textColor = appColors.onSurface,
            )
        ) {
            CartesianChartHost(
                chart = rememberCartesianChart(
                    if (chartType == ChartType.LINE) rememberLineCartesianLayer()
                    else rememberColumnCartesianLayer(),
                    startAxis = VerticalAxis.rememberStart(
                        valueFormatter = startValueFormatter,
                        itemPlacer = VerticalAxis.ItemPlacer.count({ 5 }),
                    ),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        valueFormatter = bottomValueFormatter,
                        itemPlacer = HorizontalAxis.ItemPlacer.segmented(),
                    ),
                    fadingEdges = rememberFadingEdges(),
                ),
                modelProducer = modelProducer,
                modifier = modifier
                    .fillMaxWidth()
                    .height(chartHeight)
                    .padding(horizontal = 8.dp),
            )
        }
    }
}

/**
 * 从 ResDetailData 提取指定指标值
 */
private fun getMetricValue(data: ResAnalyzeData, metricType: Int): Double {
    return when (metricType) {
        DayDetailMetricType.NEW_PURCHASE -> data.cntBuy.toDouble()
        DayDetailMetricType.DOWNLOAD -> data.downloadNum.toDouble()
        DayDetailMetricType.DIAMOND -> data.diamond.toDouble()
        DayDetailMetricType.POINTS -> data.points.toDouble()
        DayDetailMetricType.DAU -> data.dau.toDouble()
        DayDetailMetricType.REFUND_RATE -> data.refundRate * 100
        DayDetailMetricType.WISHLIST_ADDS -> data.wishlistAddsUv.toDouble()
        DayDetailMetricType.WISHLIST_GIFTS -> data.wishlistGifts.toDouble()
        DayDetailMetricType.WISHLIST_PURCHASES -> data.wishlistPurchases.toDouble()
        DayDetailMetricType.WISHLIST_REMOVES -> data.wishlistRemovesUv.toDouble()
        else -> 0.0
    }
}

/**
 * 格式化日期标签：yyyyMMdd → MM/dd
 */
private fun formatDateLabel(dateId: String): String {
    return try {
        if (dateId.length == 8) {
            val month = dateId.substring(4, 6).toIntOrNull() ?: return dateId
            val day = dateId.substring(6, 8).toIntOrNull() ?: return dateId
            "${month}/${day}"
        } else dateId
    } catch (_: Exception) {
        dateId
    }
}

/**
 * 格式化图表数值：大数用 k/m 缩写
 */
private fun formatChartValue(value: Double): String {
    return when {
        value >= 1_000_000 -> "${(value / 100_000).toInt() / 10.0}m"
        value >= 1_000 -> "${(value / 100).toInt() / 10.0}k"
        value == value.toInt().toDouble() -> value.toInt().toString()
        else -> "${(value * 10).toInt() / 10.0}"
    }
}
