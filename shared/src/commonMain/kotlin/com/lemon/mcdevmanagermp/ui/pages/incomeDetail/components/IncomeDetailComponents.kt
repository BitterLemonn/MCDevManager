package com.lemon.mcdevmanagermp.ui.pages.incomeDetail.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lemon.mcdevmanagermp.data.db.entity.EcoFeeRecordEntity
import com.lemon.mcdevmanagermp.data.repository.monthKey
import com.lemon.mcdevmanagermp.data.repository.parseMonthKey
import com.lemon.mcdevmanagermp.data.repository.reverseInputIncome
import com.lemon.mcdevmanagermp.data.vo.netease.income.IncomeVO
import com.lemon.mcdevmanagermp.domain.main.ProfitMonth
import com.lemon.mcdevmanagermp.ui.components.CollapsingTopBar
import com.lemon.mcdevmanagermp.ui.components.FinanceBadgeTone
import com.lemon.mcdevmanagermp.ui.components.FinanceKeyValueRow
import com.lemon.mcdevmanagermp.ui.components.FinanceMetricTile
import com.lemon.mcdevmanagermp.ui.components.FinanceStatusBadge
import com.lemon.mcdevmanagermp.ui.components.formatCurrency
import com.lemon.mcdevmanagermp.ui.components.formatCurrencyString
import com.lemon.mcdevmanagermp.ui.components.formatDisplayMonth
import com.lemon.mcdevmanagermp.ui.components.resolveColors
import com.lemon.mcdevmanagermp.ui.components.tabularNumbers
import com.lemon.mcdevmanagermp.ui.pages.income.components.statusBadgeTone
import com.lemon.mcdevmanagermp.ui.pages.incomeDetail.ModuleSortMode
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import com.lemon.mcdevmanagermp.utils.ECO_FEE_RATE
import com.lemon.mcdevmanagermp.utils.ModuleIncomeDetail
import com.lemon.mcdevmanagermp.utils.ProfitData
import com.lemon.mcdevmanagermp.utils.extension.formatDecimal
import com.lemon.mcdevmanagermp.utils.getTaxMoney
import com.lemon.mcdevmanagermp.utils.reverseEcoFeeRate
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_money
import org.jetbrains.compose.resources.painterResource

/**
 * 收益详情顶部栏：包含强制刷新与月历选择入口。
 */
@Composable
internal fun IncomeDetailTopBar(
    collapseFraction: Float,
    isLoading: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onOpenMonthPicker: () -> Unit
) {
    CollapsingTopBar(
        title = "收益详情",
        collapseFraction = collapseFraction,
        onBack = onBack,
        actions = {
            IconButton(
                onClick = onRefresh,
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "刷新",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            IconButton(onClick = onOpenMonthPicker) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "选择月份",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    )
}

/**
 * 快捷月度步进导航条：支持上一月 / 下一月单点切换，点击中间月份弹出月历选择器，
 * 同时展示当月结算单状态（严格遵循 M3 Tonal 色调）。
 */
@Composable
internal fun MonthNavigatorBar(
    selectedMonth: ProfitMonth,
    maxMonth: ProfitMonth,
    settlement: IncomeVO?,
    isSettlementLoading: Boolean,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onOpenPicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val canGoNext = selectedMonth < maxMonth
    val centerInteraction = remember { MutableInteractionSource() }
    val isCenterHovered by centerInteraction.collectIsHoveredAsState()

    val (badgeText, badgeTone) = when {
        isSettlementLoading -> "核对账单…" to FinanceBadgeTone.NEUTRAL
        settlement != null -> settlement.status to statusBadgeTone(settlement.status)
        else -> "预估中 · 未出账" to FinanceBadgeTone.NEUTRAL
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPrevMonth) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "上个月",
                    tint = colors.onSurface
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isCenterHovered) colors.surfaceContainerHighest else Color.Transparent
                    )
                    .hoverable(centerInteraction)
                    .clickable(
                        interactionSource = centerInteraction,
                        indication = ripple(),
                        onClick = onOpenPicker
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${selectedMonth.year}年${selectedMonth.month}月",
                    style = MaterialTheme.typography.titleMedium.tabularNumbers(),
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Spacer(Modifier.width(8.dp))
                FinanceStatusBadge(
                    text = badgeText,
                    tone = badgeTone,
                    showDot = settlement != null
                )
            }

            IconButton(
                onClick = onNextMonth,
                enabled = canGoNext
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "下个月",
                    tint = if (canGoNext) colors.onSurface else colors.onSurface.copy(alpha = 0.38f)
                )
            }
        }
    }
}

/**
 * 2026 年 1 月以前收益口径提示条：使用 M3 secondaryContainer / onSecondaryContainer。
 */
@Composable
internal fun AlgorithmVersionNoticeBanner(modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.secondaryContainer)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = colors.onSecondaryContainer,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "2026 年 1 月以前的收益算法与当前不一致，预估数据可能存在偏差",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSecondaryContainer
        )
    }
}

/**
 * 加载错误提示卡片：使用 M3 errorContainer / onErrorContainer。
 */
@Composable
internal fun LoadErrorBanner(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.scheme.errorContainer)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = colors.scheme.onErrorContainer,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = colors.scheme.onErrorContainer,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onRetry) {
            Text(
                text = "重试",
                color = colors.scheme.onErrorContainer,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * 收益汇总 Hero 卡片：顶部使用纯色 primaryContainer / onPrimaryContainer，
 * 收益构成采用主题派生的 primary / tertiary / secondary 色系保持视觉和谐。
 *
 * @param showTaxEstimate 是否展示按个人劳务报酬预估的税后收益；企业开发者传 false 隐藏
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ProfitHeroSummaryCard(
    profitData: ProfitData,
    modules: List<ModuleIncomeDetail>,
    showTaxEstimate: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val totalShareReturn = remember(modules) { modules.sumOf { it.shareReturn } }
    val subsidyTotal = remember(profitData) { profitData.subsidyProfit.values.sum() }
    val developerShareRmb = profitData.developerProfit / 100.0
    val baseShareRmb = (developerShareRmb - totalShareReturn).coerceAtLeast(0.0)
    val taxAmount = remember(profitData.totalProfit) { getTaxMoney(profitData.totalProfit) }
    val afterTaxProfit = (profitData.totalProfit - taxAmount).coerceAtLeast(0.0)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 顶部主视觉区：纯色 primaryContainer + onPrimaryContainer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.primaryContainer)
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.ic_money),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "预估税前总收益",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                    if (!showTaxEstimate) {
                        FinanceStatusBadge(
                            text = "企业 · 自行开票缴税",
                            containerColor = colors.onPrimaryContainer.copy(alpha = 0.14f),
                            contentColor = colors.onPrimaryContainer
                        )
                    } else if (profitData.subsidyPercent > 0.0) {
                        FinanceStatusBadge(
                            text = "返还 ${(profitData.subsidyPercent * 100).toInt()}%",
                            containerColor = colors.onPrimaryContainer.copy(alpha = 0.14f),
                            contentColor = colors.onPrimaryContainer
                        )
                    }
                }

                Text(
                    text = profitData.totalProfit.formatCurrency(),
                    style = MaterialTheme.typography.headlineMedium.tabularNumbers(),
                    fontWeight = FontWeight.Bold,
                    color = colors.onPrimaryContainer
                )

                if (showTaxEstimate) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.onPrimaryContainer.copy(alpha = 0.10f))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "预估税后到手",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onPrimaryContainer.copy(alpha = 0.78f)
                            )
                            Text(
                                text = afterTaxProfit.formatCurrency(),
                                style = MaterialTheme.typography.titleMedium.tabularNumbers(),
                                fontWeight = FontWeight.Bold,
                                color = colors.onPrimaryContainer
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "预估劳务个税",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onPrimaryContainer.copy(alpha = 0.78f)
                            )
                            Text(
                                text = "-${taxAmount.formatCurrency()}",
                                style = MaterialTheme.typography.bodyMedium.tabularNumbers(),
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onPrimaryContainer.copy(alpha = if (taxAmount > 0.0) 0.92f else 0.72f)
                            )
                        }
                    }
                }
            }

            // 下半部分：收益构成比例条 + 核心数据网格
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 收益构成可视化：使用主题派生的 primary / tertiary / secondary / primary浅阶
                val segments = remember(
                    baseShareRmb,
                    totalShareReturn,
                    subsidyTotal,
                    profitData.profitSubsidy,
                    colors.primary,
                    colors.tertiary,
                    colors.secondary
                ) {
                    listOf(
                        CompositionSegment("基础分成", baseShareRmb, colors.primary),
                        CompositionSegment("分成返还", totalShareReturn, colors.tertiary),
                        CompositionSegment("模组激励", subsidyTotal, colors.secondary),
                        CompositionSegment("流水激励", profitData.profitSubsidy, colors.primary.copy(alpha = 0.48f))
                    ).filter { it.amount > 0.005 }
                }

                if (segments.isNotEmpty()) {
                    val segmentSum = segments.sumOf { it.amount }.coerceAtLeast(0.01)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "收益构成",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(colors.surfaceContainerHighest),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            segments.forEach { seg ->
                                val weight = (seg.amount / segmentSum).toFloat().coerceAtLeast(0.02f)
                                Box(
                                    modifier = Modifier
                                        .weight(weight)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(seg.color)
                                )
                            }
                        }
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            segments.forEach { seg ->
                                val pct = seg.amount / segmentSum * 100.0
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(seg.color)
                                    )
                                    Text(
                                        text = "${seg.label} ${pct.formatDecimal(1)}%",
                                        style = MaterialTheme.typography.labelSmall.tabularNumbers(),
                                        color = colors.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // 2x2 核心财务指标块
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FinanceMetricTile(
                            label = "月总流水",
                            value = (profitData.sumProfit / 100.0).formatCurrency(),
                            modifier = Modifier.weight(1f)
                        )
                        FinanceMetricTile(
                            label = "开发者分成",
                            value = developerShareRmb.formatCurrency(),
                            subLabel = if (totalShareReturn > 0.0) {
                                "含返还 ${totalShareReturn.formatCurrency()}"
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FinanceMetricTile(
                            label = "模组激励合计",
                            value = subsidyTotal.formatCurrency(),
                            subLabel = if (profitData.subsidyProfit.isNotEmpty()) {
                                "${profitData.subsidyProfit.size} 个模组达标"
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                        FinanceMetricTile(
                            label = "流水激励",
                            value = profitData.profitSubsidy.formatCurrency(),
                            subLabel = if (profitData.subsidyPercent > 0.0) {
                                "分成返还 ${(profitData.subsidyPercent * 100).toInt()}%"
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

private data class CompositionSegment(
    val label: String,
    val amount: Double,
    val color: Color
)

/**
 * 实际收益反推生态费卡片：将实际费率与 30% 基准可视化对比，并列出完整核对账目。
 */
@Composable
internal fun EcoFeeReverseCard(
    profitData: ProfitData,
    settlement: IncomeVO?,
    isSettlementLoading: Boolean,
    settlementError: String?,
    dataReady: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val ecoFeeRate = remember(profitData, settlement, dataReady) {
        if (!dataReady || !profitData.isFlowComplete || settlement == null) null
        else profitData.reverseEcoFeeRate(settlement.reverseInputIncome)
    }

    val (badgeText, badgeTone) = when {
        !dataReady -> "等待数据" to FinanceBadgeTone.NEUTRAL
        !profitData.isFlowComplete -> "数据不完整" to FinanceBadgeTone.TERTIARY
        settlementError != null -> "获取失败" to FinanceBadgeTone.ERROR
        isSettlementLoading -> "核对中" to FinanceBadgeTone.NEUTRAL
        settlement == null -> "未出账" to FinanceBadgeTone.NEUTRAL
        ecoFeeRate == null -> "无流水" to FinanceBadgeTone.NEUTRAL
        ecoFeeRate !in 0.0..1.0 -> "需核对" to FinanceBadgeTone.TERTIARY
        else -> "已反推" to FinanceBadgeTone.SECONDARY
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Analytics,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "实际收益反推生态费",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface
                    )
                }
                FinanceStatusBadge(text = badgeText, tone = badgeTone)
            }

            if (!dataReady) return@Card

            when {
                !profitData.isFlowComplete -> InlineToneBox(
                    message = "部分作品流水获取失败，数据不完整，暂不反推生态费",
                    tone = FinanceBadgeTone.TERTIARY,
                    isAlert = true,
                    actionText = "重试",
                    onAction = onRetry
                )

                settlementError != null -> InlineToneBox(
                    message = settlementError,
                    tone = FinanceBadgeTone.ERROR,
                    isAlert = true,
                    actionText = "重试",
                    onAction = onRetry
                )

                isSettlementLoading -> Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceContainerHighest)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = colors.primary
                    )
                    Text(
                        text = "正在拉取结算单并反推生态费…",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }

                settlement == null -> InlineInfoBox(
                    message = "该月结算单尚未生成，待平台出账后将自动反推实际生态费率"
                )

                ecoFeeRate == null -> InlineInfoBox(
                    message = "该月无有效流水，无法反推生态费"
                )

                ecoFeeRate !in 0.0..1.0 -> InlineToneBox(
                    message = "实际收益超出预估公式可解释的范围，可能存在预估未计入的收益或调整项",
                    tone = FinanceBadgeTone.TERTIARY,
                    isAlert = true
                )

                else -> {
                    val deltaPercent = (ecoFeeRate - ECO_FEE_RATE) * 100.0
                    val isLowerOrEqual = deltaPercent <= 0.0
                    val deltaText = "${if (deltaPercent > 0) "+" else ""}${deltaPercent.formatDecimal(2)}%"
                    val actualFeeAmount = profitData.sumProfit / 100.0 * ecoFeeRate

                    // 顶部实际费率 vs 预估基准对比（纯色 M3 Container）
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.primaryContainer)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "实际生态费率",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.onPrimaryContainer.copy(alpha = 0.82f)
                                )
                                Text(
                                    text = deltaText,
                                    style = MaterialTheme.typography.labelSmall.tabularNumbers(),
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isLowerOrEqual) colors.onPrimaryContainer else colors.scheme.onTertiaryContainer,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            if (isLowerOrEqual) colors.onPrimaryContainer.copy(alpha = 0.14f)
                                            else colors.scheme.tertiaryContainer
                                        )
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                            Text(
                                text = "${(ecoFeeRate * 100).formatDecimal(2)}%",
                                style = MaterialTheme.typography.headlineSmall.tabularNumbers(),
                                fontWeight = FontWeight.Bold,
                                color = colors.onPrimaryContainer
                            )
                        }

                        FinanceMetricTile(
                            label = "实际生态费",
                            value = actualFeeAmount.formatCurrency(),
                            subLabel = "预估基准 ${(ECO_FEE_RATE * 100).formatDecimal(0)}%",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 刻度对比可视化条
                    EcoFeeRateGauge(
                        actualRate = ecoFeeRate,
                        referenceRate = ECO_FEE_RATE
                    )

                    // 账目对照表
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceContainerHighest)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        FinanceKeyValueRow(
                            label = "税前实际收益",
                            value = settlement.income.formatCurrencyString(),
                            bold = true
                        )
                        val playPlan = settlement.playPlanIncome.toDoubleOrNull() ?: 0.0
                        if (playPlan > 0.0) {
                            FinanceKeyValueRow(
                                label = "剔除畅玩计划",
                                value = "-${playPlan.formatCurrency()}",
                                valueColor = colors.onSurfaceVariant
                            )
                        }
                        FinanceKeyValueRow(
                            label = "预估总收益",
                            value = profitData.totalProfit.formatCurrency()
                        )
                        FinanceKeyValueRow(
                            label = "预估生态费率",
                            value = "${(ECO_FEE_RATE * 100).formatDecimal(2)}%",
                            valueColor = colors.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * 纯 Compose Canvas 绘制的生态费率对比条：≤基准使用 primary，>基准使用主题派生 tertiary。
 */
@Composable
private fun EcoFeeRateGauge(
    actualRate: Double,
    referenceRate: Double
) {
    val colors = LocalAppColors.current
    val maxScale = if (actualRate > 0.48) 1.0 else 0.5
    val actualFraction = (actualRate / maxScale).toFloat().coerceIn(0f, 1f)
    val refFraction = (referenceRate / maxScale).toFloat().coerceIn(0f, 1f)
    val barColor = if (actualRate <= referenceRate) colors.primary else colors.tertiary

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
        ) {
            val trackHeight = 8.dp.toPx()
            val trackTop = (size.height - trackHeight) / 2f
            val corner = CornerRadius(trackHeight / 2f, trackHeight / 2f)

            drawRoundRect(
                color = colors.surfaceContainerHighest,
                topLeft = Offset(0f, trackTop),
                size = Size(size.width, trackHeight),
                cornerRadius = corner
            )
            drawRoundRect(
                color = barColor,
                topLeft = Offset(0f, trackTop),
                size = Size(size.width * actualFraction, trackHeight),
                cornerRadius = corner
            )

            // 30% 基准线刻度
            val refX = size.width * refFraction
            drawLine(
                color = colors.onSurface.copy(alpha = 0.65f),
                start = Offset(refX, 0f),
                end = Offset(refX, size.height),
                strokeWidth = 2.dp.toPx()
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "0%",
                style = MaterialTheme.typography.labelSmall.tabularNumbers(),
                color = colors.onSurfaceVariant
            )
            Text(
                text = "基准 ${(referenceRate * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall.tabularNumbers(),
                color = colors.onSurfaceVariant
            )
            Text(
                text = "${(maxScale * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall.tabularNumbers(),
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InlineToneBox(
    message: String,
    tone: FinanceBadgeTone,
    isAlert: Boolean = false,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    val pair = tone.resolveColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(pair.container)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (isAlert) Icons.Filled.WarningAmber else Icons.Filled.Info,
            contentDescription = null,
            tint = pair.content,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = pair.content,
            modifier = Modifier.weight(1f)
        )
        if (actionText != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionText, color = pair.content, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun InlineInfoBox(message: String) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceContainerHighest)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
    }
}

/**
 * 历史生态费卡片：展示历史均值、迷你趋势柱图与可点击跳转的历史明细列表。
 */
@Composable
internal fun EcoFeeHistoryCard(
    history: List<EcoFeeRecordEntity>,
    selectedMonth: ProfitMonth,
    onSelectMonth: (ProfitMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val selectedKey = remember(selectedMonth) { monthKey(selectedMonth) }
    val avgRate = remember(history) {
        val rates = history.mapNotNull { it.ecoFeeRate }
        if (rates.isEmpty()) null else rates.average()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "历史生态费",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface
                    )
                }

                if (avgRate != null) {
                    FinanceStatusBadge(
                        text = "均值 ${(avgRate * 100).formatDecimal(2)}%",
                        tone = FinanceBadgeTone.SECONDARY
                    )
                }
            }

            if (history.isEmpty()) {
                InlineInfoBox("暂无记录，结算单出账后浏览对应月份将自动反推并保存")
            } else {
                // 最近最多 8 个月的趋势柱状图（按时间正序）
                val chartRecords = remember(history) {
                    history.take(8).reversed()
                }
                EcoFeeTrendChart(
                    records = chartRecords,
                    selectedKey = selectedKey,
                    onSelectMonth = onSelectMonth
                )

                // 历史明细列表（支持点击跳转）
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceContainerHighest)
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    history.forEach { record ->
                        val isCurrent = record.dataMonth == selectedKey
                        val rate = record.ecoFeeRate ?: 0.0
                        val targetMonth = remember(record.dataMonth) { parseMonthKey(record.dataMonth) }
                        val interaction = remember { MutableInteractionSource() }
                        val isHovered by interaction.collectIsHoveredAsState()

                        val rowBg = when {
                            isCurrent -> colors.secondaryContainer
                            isHovered -> colors.surfaceContainerHigh
                            else -> Color.Transparent
                        }
                        val titleColor = if (isCurrent) colors.onSecondaryContainer else colors.onSurface
                        val subColor = if (isCurrent) colors.onSecondaryContainer.copy(alpha = 0.78f) else colors.onSurfaceVariant

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(rowBg)
                                .hoverable(interaction)
                                .clickable(
                                    enabled = targetMonth != null,
                                    interactionSource = interaction,
                                    indication = ripple(),
                                    onClick = { targetMonth?.let(onSelectMonth) }
                                )
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = formatDisplayMonth(record.dataMonth),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                                    color = titleColor
                                )
                                if (isCurrent) {
                                    Text(
                                        text = "当前",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.onSecondaryContainer,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(colors.onSecondaryContainer.copy(alpha = 0.14f))
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                record.ecoFeeAmount?.let { feeAmount ->
                                    Text(
                                        text = feeAmount.formatCurrency(),
                                        style = MaterialTheme.typography.bodySmall.tabularNumbers(),
                                        color = subColor
                                    )
                                }
                                Text(
                                    text = "${(rate * 100).formatDecimal(2)}%",
                                    style = MaterialTheme.typography.bodyMedium.tabularNumbers(),
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        isCurrent -> colors.onSecondaryContainer
                                        rate > ECO_FEE_RATE -> colors.tertiary
                                        else -> colors.primary
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 纯 Compose 迷你生态费率柱状趋势图：严格采用主题色系（当前月 primary，正常 secondary，偏高 tertiary）。
 */
@Composable
private fun EcoFeeTrendChart(
    records: List<EcoFeeRecordEntity>,
    selectedKey: String,
    onSelectMonth: (ProfitMonth) -> Unit
) {
    val colors = LocalAppColors.current
    val maxRate = remember(records) {
        val peak = records.mapNotNull { it.ecoFeeRate }.maxOrNull() ?: ECO_FEE_RATE
        maxOf(peak * 1.15, ECO_FEE_RATE * 1.35, 0.40).coerceAtMost(1.0)
    }
    val refFraction = (ECO_FEE_RATE / maxRate).toFloat().coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceContainerHighest)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
        ) {
            // 30% 参考虚线
            Canvas(modifier = Modifier.fillMaxSize()) {
                val refY = size.height * (1f - refFraction)
                drawLine(
                    color = colors.outline.copy(alpha = 0.55f),
                    start = Offset(0f, refY),
                    end = Offset(size.width, refY),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                )
            }

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                records.forEach { record ->
                    val rate = record.ecoFeeRate ?: 0.0
                    val fraction = (rate / maxRate).toFloat().coerceIn(0.06f, 1f)
                    val isCurrent = record.dataMonth == selectedKey
                    val targetMonth = remember(record.dataMonth) { parseMonthKey(record.dataMonth) }
                    val barColor = when {
                        isCurrent -> colors.primary
                        rate > ECO_FEE_RATE -> colors.tertiary.copy(alpha = 0.78f)
                        else -> colors.secondary.copy(alpha = 0.62f)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(enabled = targetMonth != null) {
                                targetMonth?.let(onSelectMonth)
                            },
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.58f)
                                .fillMaxHeight(fraction)
                                .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                                .background(barColor)
                        )
                    }
                }
            }
        }

        // 底部月份标签
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            records.forEach { record ->
                val isCurrent = record.dataMonth == selectedKey
                val shortMonth = remember(record.dataMonth) {
                    record.dataMonth.substringAfter("-", record.dataMonth).trimStart('0').ifEmpty { "0" } + "月"
                }
                Text(
                    text = shortMonth,
                    style = MaterialTheme.typography.labelSmall.tabularNumbers(),
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCurrent) colors.primary else colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 模组收益区标题栏：展示模组数与 M3 风格排序切换胶囊（secondaryContainer 选中态）。
 */
@Composable
internal fun ModuleSectionHeader(
    moduleCount: Int,
    sortMode: ModuleSortMode,
    onSelectSortMode: (ModuleSortMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "模组收益明细",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )
            FinanceStatusBadge(
                text = "$moduleCount",
                tone = FinanceBadgeTone.SECONDARY
            )
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(colors.surfaceContainerHigh)
                .border(1.dp, colors.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            ModuleSortMode.entries.forEach { mode ->
                val selected = sortMode == mode
                val bg by animateColorAsState(
                    targetValue = if (selected) colors.secondaryContainer else Color.Transparent,
                    label = "sort_chip_bg"
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(bg)
                        .clickable { onSelectSortMode(mode) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = mode.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selected) colors.onSecondaryContainer else colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 单个模组收益卡片：排名徽章严格映射 M3 primary/secondary/tertiary/neutral 容器色。
 */
@Composable
internal fun ModuleIncomeCard(
    rank: Int,
    module: ModuleIncomeDetail,
    totalFlowIncome: Double,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val flowShare = if (totalFlowIncome > 0.0) {
        (module.flowIncome / totalFlowIncome).coerceIn(0.0, 1.0)
    } else 0.0
    val rankTone = when (rank) {
        1 -> FinanceBadgeTone.PRIMARY
        2 -> FinanceBadgeTone.SECONDARY
        3 -> FinanceBadgeTone.TERTIARY
        else -> FinanceBadgeTone.NEUTRAL
    }
    val rankColors = rankTone.resolveColors()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 顶部：排名 + 模组名 + 小计金额
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(rankColors.container),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$rank",
                        style = MaterialTheme.typography.labelSmall.tabularNumbers(),
                        fontWeight = FontWeight.Bold,
                        color = rankColors.content
                    )
                }

                Text(
                    text = module.moduleName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = module.totalIncome.formatCurrency(),
                    style = MaterialTheme.typography.titleSmall.tabularNumbers(),
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
            }

            // 流水占比进度条
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "流水占比",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant
                    )
                    Text(
                        text = "${(flowShare * 100).formatDecimal(1)}%",
                        style = MaterialTheme.typography.labelSmall.tabularNumbers(),
                        fontWeight = FontWeight.Medium,
                        color = colors.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(colors.surfaceContainerHighest)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(flowShare.toFloat().coerceAtLeast(0.02f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(colors.primary)
                    )
                }
            }

            // 关键数据双列块
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FinanceMetricTile(
                    label = "流水收益",
                    value = module.flowIncome.formatCurrency(),
                    containerColor = colors.surfaceContainerHighest,
                    modifier = Modifier.weight(1f)
                )
                FinanceMetricTile(
                    label = "开发者分成",
                    value = module.developerShare.formatCurrency(),
                    containerColor = colors.surfaceContainerHighest,
                    modifier = Modifier.weight(1f)
                )
            }

            // 额外返还 / 激励明细行（非零时展示）
            if (module.shareReturn > 0.0 || module.subsidyAmount > 0.0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surfaceContainerHighest)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (module.shareReturn > 0.0) {
                        FinanceKeyValueRow(
                            label = "其中分成返还",
                            value = module.shareReturn.formatCurrency(),
                            valueColor = colors.tertiary
                        )
                    }
                    if (module.subsidyAmount > 0.0) {
                        FinanceKeyValueRow(
                            label = "模组激励",
                            value = module.subsidyAmount.formatCurrency(showPlusSign = true),
                            valueColor = colors.primary,
                            bold = true
                        )
                    }
                }
            }
        }
    }
}

/**
 * 模组空态占位。
 */
@Composable
internal fun EmptyModuleStateCard(modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "该月暂无模组收益数据",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        }
    }
}

/**
 * 精修月份选择弹窗：采用 Material3 Dialog，原生支持 Esc/返回键关闭，
 * 并用主题 primary 小圆点标记已保存生态费记录的历史月份。
 */
@Composable
internal fun ProfitMonthPickerDialog(
    selectedMonth: ProfitMonth,
    maxMonth: ProfitMonth,
    ratedMonthKeys: Set<String>,
    onSelect: (ProfitMonth) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    var displayedYear by remember(selectedMonth) { mutableIntStateOf(selectedMonth.year) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .widthIn(min = 280.dp, max = 380.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = colors.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 年份步进头
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { displayedYear-- }) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "上一年",
                            tint = colors.onSurface
                        )
                    }
                    Text(
                        text = "${displayedYear}年",
                        style = MaterialTheme.typography.titleMedium.tabularNumbers(),
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    IconButton(
                        onClick = { displayedYear++ },
                        enabled = displayedYear < maxMonth.year
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "下一年",
                            tint = if (displayedYear < maxMonth.year) colors.onSurface else colors.onSurface.copy(alpha = 0.38f)
                        )
                    }
                }

                // 3x4 月份网格
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..12).chunked(3).forEach { rowMonths ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowMonths.forEach { month ->
                                val value = ProfitMonth(displayedYear, month)
                                val isSelected = value == selectedMonth
                                val enabled = value <= maxMonth
                                val hasRatedRecord = monthKey(value) in ratedMonthKeys
                                val interaction = remember { MutableInteractionSource() }
                                val isHovered by interaction.collectIsHoveredAsState()

                                val bg = when {
                                    isSelected -> colors.primary
                                    !enabled -> Color.Transparent
                                    isHovered -> colors.secondaryContainer
                                    else -> colors.surfaceContainerHighest
                                }
                                val fg = when {
                                    isSelected -> colors.onPrimary
                                    !enabled -> colors.onSurface.copy(alpha = 0.38f)
                                    else -> colors.onSurface
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(bg)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) Color.Transparent
                                            else colors.outlineVariant.copy(alpha = if (enabled) 0.5f else 0.2f),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .hoverable(interaction)
                                        .clickable(
                                            enabled = enabled,
                                            interactionSource = interaction,
                                            indication = ripple(),
                                            onClick = { onSelect(value) }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${month}月",
                                        style = MaterialTheme.typography.bodyMedium.tabularNumbers(),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = fg
                                    )
                                    if (hasRatedRecord) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(6.dp)
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) colors.onPrimary else colors.primary)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 图例与关闭按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (ratedMonthKeys.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(colors.primary)
                            )
                            Text(
                                text = "已记录实际生态费",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }
                    TextButton(onClick = onDismiss) {
                        Text("关闭")
                    }
                }
            }
        }
    }
}
