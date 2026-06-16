package com.lemon.mcdevmanagermp.ui.pages.analyze.monthDetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.vo.netease.analyze.ResMonthAnalyzeData
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

/**
 * 月度汇总卡片
 * 展示单个资源在某月的数据汇总
 */
@Composable
internal fun MonthCard(
    data: ResMonthAnalyzeData,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = colors.surfaceContainerHigh),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // 头部：资源名 + 月份
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = data.resName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textColor,
                        maxLines = 1
                    )
                    Text(
                        text = "IID: ${data.iid}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant
                    )
                }
                Text(
                    text = formatMonthId(data.monthId),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primary
                )
            }

            Spacer(Modifier.height(10.dp))

            // 指标网格 3x2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricItem("日均购买", formatNumber(data.avgDayBuy), modifier = Modifier.weight(1f))
                MetricItem("月均日活", formatNumber(data.avgDau), modifier = Modifier.weight(1f))
                MetricItem("月活", formatNumber(data.mau), modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricItem("钻石收益", formatNumber(data.totalDiamond), modifier = Modifier.weight(1f))
                MetricItem("绿宝石收益", formatNumber(data.totalPoints), modifier = Modifier.weight(1f))
                MetricItem("下载量", formatNumber(data.downloadNum), modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.textColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant
        )
    }
}

/**
 * 格式化 monthId: yyyyMM → yyyy年MM月
 */
private fun formatMonthId(monthId: String): String {
    return try {
        if (monthId.length == 6) {
            val year = monthId.substring(0, 4)
            val month = monthId.substring(4, 6).toIntOrNull() ?: return monthId
            "${year}年${month}月"
        } else monthId
    } catch (_: Exception) {
        monthId
    }
}

/**
 * 格式化数字：大数用 k/m 缩写
 */
private fun formatNumber(num: Int): String {
    return when {
        num >= 1_000_000 -> "${(num / 100_000).toDouble() / 10.0}m"
        num >= 10_000 -> "${(num / 1_000).toDouble() / 10.0}w"
        num >= 1_000 -> "${(num / 100).toDouble() / 10.0}k"
        else -> num.toString()
    }
}
