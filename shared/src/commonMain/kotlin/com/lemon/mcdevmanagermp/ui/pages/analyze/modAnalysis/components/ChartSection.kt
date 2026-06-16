package com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.vo.netease.analyze.NewResAnalyzeData
import com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis.ChartType
import com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis.MetricType
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
 * VICO 图表区域封装
 * 根据指标类型和图表类型渲染折线图或柱状图
 *
 * 使用 key(chartType, metricType) 确保切换图表类型/指标时
 * CartesianChartModelProducer 和 CartesianChartHost 完全重建，
 * 避免 layer 与 model 数据类型不匹配导致内部崩溃。
 */
@Composable
internal fun ChartSection(
    analysisData: List<NewResAnalyzeData>,
    metricType: Int,
    chartType: ChartType,
    modifier: Modifier = Modifier,
) {
    // 从数据中提取指标值
    val (modValues, avgValues, dateLabels) = remember(analysisData, metricType) {
        extractMetricData(analysisData, metricType)
    }

    // 数据为空时不渲染图表
    if (modValues.isEmpty()) return

    // key 确保切换 chartType 或 metricType 时完全重建所有内部状态
    key(chartType, metricType) {
        val modelProducer = remember { CartesianChartModelProducer() }
        val dateLabelsKey = remember { ExtraStore.Key<List<String>>() }

        // 写入图表数据
        LaunchedEffect(modValues, avgValues) {
            modelProducer.runTransaction {
                if (chartType == ChartType.LINE) {
                    lineModel {
                        series(modValues)
                        series(avgValues)
                    }
                } else {
                    columnModel {
                        series(modValues)
                        series(avgValues)
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

        val chartHeight = if (chartType == ChartType.LINE) 220.dp else 240.dp
        val appColors = LocalAppColors.current

        // 使用与图例一致的颜色：primary + onSurfaceVariant(半透明)
        val chartColors = remember(appColors) {
            listOf(appColors.primary, appColors.onSurfaceVariant.copy(alpha = 0.5f))
        }
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
        } // ProvideVicoTheme
    }
}

/**
 * 从分析数据中提取指定指标的模组值、同类均值和日期标签
 */
private fun extractMetricData(
    data: List<NewResAnalyzeData>,
    metricType: Int
): Triple<List<Double>, List<Double>, List<String>> {
    val modValues = mutableListOf<Double>()
    val avgValues = mutableListOf<Double>()
    val dateLabels = mutableListOf<String>()

    for (item in data) {
        dateLabels.add(formatDateLabel(item.dateId))
        when (metricType) {
            MetricType.NEW_PURCHASE -> {
                modValues.add(item.cntBuy.toDouble())
                avgValues.add(item.avgFirstTypeBuy)
            }

            MetricType.DAU -> {
                modValues.add(item.dau.toDouble())
                avgValues.add(item.avgFirstTypeRolePlay)
            }

            MetricType.NEW_FOLLOW -> {
                modValues.add(item.focusCnt.toDouble())
                avgValues.add(item.avgFirstTypeFocus)
            }

            MetricType.AVG_PLAY_TIME -> {
                modValues.add(item.avgPlaytime)
                avgValues.add(item.firstTypeAvgRoleTime)
            }
        }
    }

    return Triple(modValues, avgValues, dateLabels)
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
