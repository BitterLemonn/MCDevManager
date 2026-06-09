package com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

/**
 * 快捷日期范围选择器
 * 提供 7天 / 14天 / 30天 / 90天 快捷选项
 */
@Composable
internal fun DateRangePicker(
    startDate: String,
    endDate: String,
    onSelectRange: (start: String, end: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "日期:",
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.width(4.dp))

        val ranges = listOf("7天" to 7, "14天" to 14, "30天" to 30, "90天" to 90)
        ranges.forEach { (label, days) ->
            val isSelected = isRangeSelected(startDate, endDate, days)
            DateChip(
                label = label,
                isSelected = isSelected,
                onClick = {
                    val end = parseDate(endDate)
                    val start = end.minusDays(days - 1)
                    onSelectRange(formatDate(start), formatDate(end))
                }
            )
        }
    }
}

@Composable
private fun DateChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current
    val bgColor = if (isSelected) colors.primary.copy(alpha = 0.12f) else colors.surfaceContainerLow
    val textColor = if (isSelected) colors.primary else colors.onSurfaceVariant

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

/**
 * 判断当前日期范围是否与指定天数匹配
 */
private fun isRangeSelected(startDate: String, endDate: String, days: Int): Boolean {
    if (startDate.length != 8 || endDate.length != 8) return false
    return try {
        val start = parseDate(startDate)
        val end = parseDate(endDate)
        val diff = end.toEpochDays() - start.toEpochDays() + 1
        diff == days
    } catch (_: Exception) {
        false
    }
}

/**
 * 简易日期解析 yyyyMMdd
 */
private fun parseDate(dateStr: String): SimpleDate {
    val year = dateStr.substring(0, 4).toInt()
    val month = dateStr.substring(4, 6).toInt()
    val day = dateStr.substring(6, 8).toInt()
    return SimpleDate(year, month, day)
}

/**
 * 简易日期格式化 → yyyyMMdd
 */
private fun formatDate(date: SimpleDate): String {
    return "${date.year}${date.month.toString().padStart(2, '0')}${date.day.toString().padStart(2, '0')}"
}

/**
 * 简易日期类（避免依赖 kotlinx.datetime）
 */
private class SimpleDate(val year: Int, val month: Int, val day: Int) {
    fun toEpochDays(): Int {
        // 简易 Julian Day 计算
        val a = (14 - month) / 12
        val y = year + 4800 - a
        val m = month + 12 * a - 3
        return day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
    }

    fun minusDays(days: Int): SimpleDate {
        val epoch = toEpochDays() - days
        return fromEpochDays(epoch)
    }

    companion object {
        fun fromEpochDays(epoch: Int): SimpleDate {
            val a = epoch + 32044
            val b = (4 * a + 3) / 146097
            val c = a - (146097 * b) / 4
            val d = (4 * c + 3) / 1461
            val e = c - (1461 * d) / 4
            val m = (5 * e + 2) / 153
            val day = e - (153 * m + 2) / 5 + 1
            val month = m + 3 - 12 * (m / 10)
            val year = 100 * b + d - 4800 + m / 10
            return SimpleDate(year, month, day)
        }
    }
}
