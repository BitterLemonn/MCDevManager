package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import com.lemon.mcdevmanagermp.utils.extension.formatDecimal
import kotlin.math.abs

/**
 * 纯 Kotlin 跨平台千分位金额格式化，避免依赖 JVM 专属格式化 API。
 *
 * 例如：12345.6 → "¥12,345.60"，-89.5 → "-¥89.50"。
 */
internal fun Double.formatCurrency(
    symbol: String = "¥",
    digits: Int = 2,
    showPlusSign: Boolean = false
): String {
    val negative = this < 0.0
    val formatted = abs(this).formatDecimal(digits)
    val withGrouping = insertThousandsSeparator(formatted)
    return when {
        negative -> "-$symbol$withGrouping"
        showPlusSign && this > 0.0 -> "+$symbol$withGrouping"
        else -> "$symbol$withGrouping"
    }
}

/**
 * 将后端返回的金额字符串转为带千分位的货币显示；无法解析时原样兜底。
 */
internal fun String.formatCurrencyString(
    symbol: String = "¥",
    digits: Int = 2,
    showPlusSign: Boolean = false
): String {
    val parsed = trim().toDoubleOrNull() ?: return this
    return parsed.formatCurrency(symbol = symbol, digits = digits, showPlusSign = showPlusSign)
}

/**
 * 纯 Kotlin 跨平台整数千分位格式化（例如钻石数 123456 → "123,456"）。
 */
internal fun Int.formatGrouped(): String = toLong().formatGrouped()

internal fun Long.formatGrouped(): String {
    val negative = this < 0L
    val raw = abs(this).toString()
    val grouped = insertThousandsSeparator(raw)
    return if (negative) "-$grouped" else grouped
}

private fun insertThousandsSeparator(numericText: String): String {
    val dotIndex = numericText.indexOf('.')
    val intPart = if (dotIndex >= 0) numericText.substring(0, dotIndex) else numericText
    val fracPart = if (dotIndex >= 0) numericText.substring(dotIndex) else ""
    if (intPart.length <= 3) return numericText

    val sb = StringBuilder(intPart.length + intPart.length / 3 + fracPart.length)
    val firstGroupLen = intPart.length % 3
    if (firstGroupLen > 0) {
        sb.append(intPart, 0, firstGroupLen)
        if (firstGroupLen < intPart.length) sb.append(',')
    }
    var i = firstGroupLen
    while (i < intPart.length) {
        sb.append(intPart, i, i + 3)
        i += 3
        if (i < intPart.length) sb.append(',')
    }
    sb.append(fracPart)
    return sb.toString()
}

/**
 * 格式化月份字符串（兼容 "2026-05"、"2026-5"、"202605"）为 "2026年5月"。
 */
internal fun formatDisplayMonth(rawMonth: String): String {
    val digits = rawMonth.filter { it.isDigit() }
    if (digits.length < 5) return rawMonth
    val year = digits.substring(0, 4)
    val month = digits.substring(4).take(2).toIntOrNull() ?: return rawMonth
    return "${year}年${month}月"
}

/**
 * 财务数字等宽对齐样式，确保金额在纵向列表与表格中逐位对齐。
 */
internal fun TextStyle.tabularNumbers(): TextStyle = copy(fontFeatureSettings = "tnum")

/**
 * Material 3 语义徽章色调：严格映射到 ColorScheme 的成对 Container / onContainer 角色，
 * 避免脱离主题的固定色，确保在所有 Seed 主题与深浅色模式下对比度一致。
 */
internal enum class FinanceBadgeTone {
    /** 主强调：primaryContainer / onPrimaryContainer */
    PRIMARY,
    /** 次强调（常规已完成/选中态）：secondaryContainer / onSecondaryContainer */
    SECONDARY,
    /** 辅助区分（进行中/关注提示）：tertiaryContainer / onTertiaryContainer */
    TERTIARY,
    /** 中性说明：surfaceContainerHighest / onSurfaceVariant */
    NEUTRAL,
    /** 错误/失败：errorContainer / onErrorContainer */
    ERROR
}

internal data class ToneColorPair(
    val container: Color,
    val content: Color,
    val accent: Color
)

@Composable
internal fun FinanceBadgeTone.resolveColors(): ToneColorPair {
    val scheme = LocalAppColors.current.scheme
    return when (this) {
        FinanceBadgeTone.PRIMARY -> ToneColorPair(
            container = scheme.primaryContainer,
            content = scheme.onPrimaryContainer,
            accent = scheme.primary
        )

        FinanceBadgeTone.SECONDARY -> ToneColorPair(
            container = scheme.secondaryContainer,
            content = scheme.onSecondaryContainer,
            accent = scheme.secondary
        )

        FinanceBadgeTone.TERTIARY -> ToneColorPair(
            container = scheme.tertiaryContainer,
            content = scheme.onTertiaryContainer,
            accent = scheme.tertiary
        )

        FinanceBadgeTone.NEUTRAL -> ToneColorPair(
            container = scheme.surfaceContainerHighest,
            content = scheme.onSurfaceVariant,
            accent = scheme.onSurfaceVariant
        )

        FinanceBadgeTone.ERROR -> ToneColorPair(
            container = scheme.errorContainer,
            content = scheme.onErrorContainer,
            accent = scheme.error
        )
    }
}

/**
 * Material 3 Tonal 胶囊徽章：使用纯色 Container + onContainer 成对配色。
 */
@Composable
internal fun FinanceStatusBadge(
    text: String,
    tone: FinanceBadgeTone,
    modifier: Modifier = Modifier,
    showDot: Boolean = false
) {
    val pair = tone.resolveColors()
    FinanceStatusBadge(
        text = text,
        containerColor = pair.container,
        contentColor = pair.content,
        dotColor = pair.accent,
        modifier = modifier,
        showDot = showDot
    )
}

/**
 * 支持显式指定成对颜色的胶囊徽章（例如放置在 primaryContainer / secondaryContainer 之上时使用同色阶叠层）。
 */
@Composable
internal fun FinanceStatusBadge(
    text: String,
    containerColor: Color,
    contentColor: Color,
    dotColor: Color = contentColor,
    modifier: Modifier = Modifier,
    showDot: Boolean = false
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (showDot) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 财务核心指标块：标签 + 等宽加粗数值 + 可选副标签。
 */
@Composable
internal fun FinanceMetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    subLabel: String? = null,
    labelColor: Color = LocalAppColors.current.onSurfaceVariant,
    valueColor: Color = LocalAppColors.current.onSurface,
    containerColor: Color = LocalAppColors.current.surfaceContainerHighest
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = labelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.tabularNumbers(),
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (!subLabel.isNullOrBlank()) {
            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelSmall.tabularNumbers(),
                color = labelColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 账目键值对行：左侧说明，右侧等宽数字对齐。
 */
@Composable
internal fun FinanceKeyValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = LocalAppColors.current.onSurface,
    labelColor: Color = LocalAppColors.current.onSurfaceVariant,
    bold: Boolean = false,
    largeValue: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (bold) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (bold) FontWeight.Medium else FontWeight.Normal,
            color = labelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            style = (if (largeValue) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium).tabularNumbers(),
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            color = valueColor,
            maxLines = 1
        )
    }
}

/**
 * 统一的跨平台 Shimmer 骨架占位条。
 */
@Composable
internal fun FinanceShimmerBlock(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp
) {
    val colors = LocalAppColors.current
    val transition = rememberInfiniteTransition(label = "finance_shimmer")
    val offset by transition.animateFloat(
        initialValue = -300f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "finance_shimmer_offset"
    )
    val brush = Brush.linearGradient(
        colors = listOf(
            colors.surfaceContainerHighest,
            colors.surfaceContainer,
            colors.surfaceContainerHighest
        ),
        start = Offset(offset, 0f),
        end = Offset(offset + 300f, 0f)
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush)
    )
}

/**
 * 统一的卡片加载骨架屏。
 */
@Composable
internal fun FinanceSkeletonCard(
    modifier: Modifier = Modifier,
    lineCount: Int = 3
) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceContainerHigh)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FinanceShimmerBlock(
            modifier = Modifier.fillMaxWidth(0.38f).height(16.dp),
            cornerRadius = 6.dp
        )
        FinanceShimmerBlock(
            modifier = Modifier.fillMaxWidth(0.55f).height(28.dp),
            cornerRadius = 8.dp
        )
        Spacer(Modifier.height(2.dp))
        repeat(lineCount) { idx ->
            FinanceShimmerBlock(
                modifier = Modifier
                    .fillMaxWidth(if (idx == lineCount - 1) 0.72f else 1f)
                    .height(14.dp),
                cornerRadius = 6.dp
            )
        }
    }
}
