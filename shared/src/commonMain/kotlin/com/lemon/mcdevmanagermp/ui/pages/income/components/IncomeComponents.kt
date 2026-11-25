package com.lemon.mcdevmanagermp.ui.pages.income.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lemon.mcdevmanagermp.data.repository.normalizeMonthKey
import com.lemon.mcdevmanagermp.data.vo.netease.income.ApplyIncomeDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.income.IncomeVO
import com.lemon.mcdevmanagermp.ui.components.CollapsingTopBar
import com.lemon.mcdevmanagermp.ui.components.FinanceBadgeTone
import com.lemon.mcdevmanagermp.ui.components.FinanceKeyValueRow
import com.lemon.mcdevmanagermp.ui.components.FinanceMetricTile
import com.lemon.mcdevmanagermp.ui.components.FinanceStatusBadge
import com.lemon.mcdevmanagermp.ui.components.formatCurrency
import com.lemon.mcdevmanagermp.ui.components.formatCurrencyString
import com.lemon.mcdevmanagermp.ui.components.formatDisplayMonth
import com.lemon.mcdevmanagermp.ui.components.formatGrouped
import com.lemon.mcdevmanagermp.ui.components.resolveColors
import com.lemon.mcdevmanagermp.ui.components.tabularNumbers
import com.lemon.mcdevmanagermp.ui.pages.income.IncomeState
import com.lemon.mcdevmanagermp.ui.pages.income.IncomeStatusFilter
import com.lemon.mcdevmanagermp.ui.pages.income.isSettleable
import com.lemon.mcdevmanagermp.ui.theme.AppColors
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import com.lemon.mcdevmanagermp.utils.extension.formatDecimal
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_diamond
import org.jetbrains.compose.resources.painterResource

/**
 * 结算状态对应的 Material 3 色调角色。
 */
internal fun statusBadgeTone(status: String): FinanceBadgeTone = when (status) {
    "未结算" -> FinanceBadgeTone.PRIMARY
    "结算中" -> FinanceBadgeTone.TERTIARY
    "已打款" -> FinanceBadgeTone.SECONDARY
    "结算失败", "打款失败" -> FinanceBadgeTone.ERROR
    "结算信息待更正" -> FinanceBadgeTone.TERTIARY
    else -> FinanceBadgeTone.NEUTRAL
}

/**
 * 收益管理顶部栏：标题统一为「收益管理」，支持滚动吸顶与加载态反馈。
 */
@Composable
internal fun IncomeTopBar(
    collapseFraction: Float,
    isRefreshing: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    CollapsingTopBar(
        title = "收益管理",
        collapseFraction = collapseFraction,
        onBack = onBack,
        actions = {
            IconButton(
                onClick = onRefresh,
                enabled = !isRefreshing
            ) {
                if (isRefreshing) {
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
        }
    )
}

/**
 * 收益管理顶部 Hero 概览卡：遵循 Material 3 Tonal Container 配色，
 * 顶部使用纯色 primaryContainer / onPrimaryContainer，下方提供可联动筛选的状态汇总。
 */
@Composable
internal fun IncomeHeroSummaryCard(
    state: IncomeState,
    onSelectFilter: (IncomeStatusFilter) -> Unit,
    onStartSettle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val summary = remember(state.currentList) {
        computePlatformSummary(state.currentList)
    }
    val settleable = state.settleableItem

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 顶部未提取余额主视觉区：纯色 primaryContainer + onPrimaryContainer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.primaryContainer)
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccountBalanceWallet,
                                contentDescription = null,
                                tint = colors.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "未提取收益",
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.onPrimaryContainer.copy(alpha = 0.82f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = state.unExtractedIncome.formatCurrencyString(),
                            style = MaterialTheme.typography.headlineMedium.tabularNumbers(),
                            fontWeight = FontWeight.Bold,
                            color = colors.onPrimaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    FinanceStatusBadge(
                        text = if (state.selectedPlatform == "pe") "PE 平台" else "PC 平台",
                        containerColor = colors.onPrimaryContainer.copy(alpha = 0.14f),
                        contentColor = colors.onPrimaryContainer
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 三列核心状态统计，点击可快速筛选对应状态
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ClickableSummaryTile(
                        label = "未结算",
                        amount = summary.unsettledTotal.formatCurrency(),
                        countText = "${summary.unsettledCount} 笔",
                        tone = FinanceBadgeTone.PRIMARY,
                        isSelected = state.statusFilter == IncomeStatusFilter.UNSETTLED,
                        onClick = {
                            onSelectFilter(
                                if (state.statusFilter == IncomeStatusFilter.UNSETTLED) {
                                    IncomeStatusFilter.ALL
                                } else {
                                    IncomeStatusFilter.UNSETTLED
                                }
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ClickableSummaryTile(
                        label = "结算中",
                        amount = summary.processingTotal.formatCurrency(),
                        countText = "${summary.processingCount} 笔",
                        tone = FinanceBadgeTone.TERTIARY,
                        isSelected = state.statusFilter == IncomeStatusFilter.PROCESSING,
                        onClick = {
                            onSelectFilter(
                                if (state.statusFilter == IncomeStatusFilter.PROCESSING) {
                                    IncomeStatusFilter.ALL
                                } else {
                                    IncomeStatusFilter.PROCESSING
                                }
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ClickableSummaryTile(
                        label = "已打款",
                        amount = summary.settledNetTotal.formatCurrency(),
                        countText = "${summary.settledCount} 笔实收",
                        tone = FinanceBadgeTone.SECONDARY,
                        isSelected = state.statusFilter == IncomeStatusFilter.SETTLED,
                        onClick = {
                            onSelectFilter(
                                if (state.statusFilter == IncomeStatusFilter.SETTLED) {
                                    IncomeStatusFilter.ALL
                                } else {
                                    IncomeStatusFilter.SETTLED
                                }
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // 可结算提醒与快捷发起按钮
                AnimatedVisibility(
                    visible = settleable != null,
                    enter = fadeIn(tween(220)) + expandVertically(tween(220)),
                    exit = fadeOut(tween(180)) + shrinkVertically(tween(180))
                ) {
                    if (settleable != null) {
                        SettleActionBanner(
                            item = settleable,
                            isLoadingDetail = state.isLoadingApplyDetail,
                            onStartSettle = { onStartSettle(settleable.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClickableSummaryTile(
    label: String,
    amount: String,
    countText: String,
    tone: FinanceBadgeTone,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val toneColors = tone.resolveColors()
    val interaction = remember { MutableInteractionSource() }
    val isHovered by interaction.collectIsHoveredAsState()

    val containerColor by animateColorAsState(
        targetValue = when {
            isSelected -> toneColors.container
            isHovered -> colors.surfaceContainer
            else -> colors.surfaceContainerHighest
        },
        label = "summary_tile_bg"
    )
    val titleColor = if (isSelected) toneColors.content else colors.onSurface
    val subColor = if (isSelected) toneColors.content.copy(alpha = 0.78f) else colors.onSurfaceVariant

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .hoverable(interaction)
            .clickable(
                interactionSource = interaction,
                indication = ripple(),
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) toneColors.content else toneColors.accent)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = subColor,
                maxLines = 1
            )
        }
        Text(
            text = amount,
            style = MaterialTheme.typography.titleSmall.tabularNumbers(),
            fontWeight = FontWeight.Bold,
            color = titleColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = countText,
            style = MaterialTheme.typography.labelSmall.tabularNumbers(),
            color = subColor,
            maxLines = 1
        )
    }
}

/**
 * 可结算快捷操作条：使用 M3 secondaryContainer 承载，高饱和 primary 按钮作为唯一视觉主 CTA。
 */
@Composable
internal fun SettleActionBanner(
    item: IncomeVO,
    isLoadingDetail: Boolean,
    onStartSettle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.secondaryContainer)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Payments,
                contentDescription = null,
                tint = colors.onPrimaryContainer,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "${formatDisplayMonth(item.dataMonth)} 可申请结算",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "累计可提取 ${item.availableIncome.formatCurrencyString()}",
                style = MaterialTheme.typography.bodySmall.tabularNumbers(),
                color = colors.onSecondaryContainer.copy(alpha = 0.82f),
                fontWeight = FontWeight.Medium
            )
        }
        Button(
            onClick = onStartSettle,
            enabled = !isLoadingDetail,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            )
        ) {
            if (isLoadingDetail) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = colors.onPrimary
                )
            } else {
                Text(
                    text = "开始结算",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * 平台分段切换 + 状态筛选栏。
 */
@Composable
internal fun IncomeControlBar(
    selectedPlatform: String,
    peCount: Int,
    pcCount: Int,
    statusFilter: IncomeStatusFilter,
    hasAttentionItems: Boolean,
    onSelectPlatform: (String) -> Unit,
    onSelectFilter: (IncomeStatusFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PlatformSegmentedSwitch(
            selectedPlatform = selectedPlatform,
            peCount = peCount,
            pcCount = pcCount,
            onSelect = onSelectPlatform
        )

        StatusFilterRow(
            selectedFilter = statusFilter,
            hasAttentionItems = hasAttentionItems,
            onSelect = onSelectFilter
        )
    }
}

/**
 * PE / PC 分段控制器：遵循 M3 SegmentedButton 配色（选中项使用 secondaryContainer / onSecondaryContainer）。
 */
@Composable
internal fun PlatformSegmentedSwitch(
    selectedPlatform: String,
    peCount: Int,
    pcCount: Int,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceContainerHigh)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        SegmentedOption(
            label = "PE 手机版",
            count = peCount,
            selected = selectedPlatform == "pe",
            onClick = { onSelect("pe") },
            modifier = Modifier.weight(1f)
        )
        SegmentedOption(
            label = "PC 电脑版",
            count = pcCount,
            selected = selectedPlatform == "pc",
            onClick = { onSelect("pc") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SegmentedOption(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val interaction = remember { MutableInteractionSource() }
    val isHovered by interaction.collectIsHoveredAsState()
    val containerColor by animateColorAsState(
        targetValue = when {
            selected -> colors.secondaryContainer
            isHovered -> colors.surfaceContainerHighest
            else -> Color.Transparent
        },
        label = "segment_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) colors.onSecondaryContainer else colors.onSurfaceVariant,
        label = "segment_fg"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(containerColor)
            .hoverable(interaction)
            .clickable(
                interactionSource = interaction,
                indication = ripple(),
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(5.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = contentColor,
            maxLines = 1
        )
        if (count > 0) {
            Spacer(Modifier.width(6.dp))
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall.tabularNumbers(),
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (selected) colors.onSecondaryContainer.copy(alpha = 0.14f)
                        else colors.surfaceContainerHighest
                    )
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            )
        }
    }
}

/**
 * M3 风格状态筛选 Chips：选中为实色 secondaryContainer + onSecondaryContainer，未选中为轮廓描边。
 */
@Composable
private fun StatusFilterRow(
    selectedFilter: IncomeStatusFilter,
    hasAttentionItems: Boolean,
    onSelect: (IncomeStatusFilter) -> Unit
) {
    val colors = LocalAppColors.current
    val options = remember(hasAttentionItems) {
        if (hasAttentionItems) IncomeStatusFilter.entries
        else IncomeStatusFilter.entries.filter { it != IncomeStatusFilter.ATTENTION }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val selected = selectedFilter == option
            val isAttention = option == IncomeStatusFilter.ATTENTION
            val interaction = remember { MutableInteractionSource() }
            val isHovered by interaction.collectIsHoveredAsState()

            val bg by animateColorAsState(
                targetValue = when {
                    selected && isAttention -> colors.scheme.errorContainer
                    selected -> colors.secondaryContainer
                    isHovered -> colors.surfaceContainerHighest
                    else -> Color.Transparent
                },
                label = "filter_chip_bg"
            )
            val fg = when {
                selected && isAttention -> colors.scheme.onErrorContainer
                selected -> colors.onSecondaryContainer
                isAttention -> colors.error
                else -> colors.onSurfaceVariant
            }
            val borderColor = when {
                selected -> Color.Transparent
                isAttention -> colors.error.copy(alpha = 0.5f)
                else -> colors.outlineVariant
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(bg)
                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                    .hoverable(interaction)
                    .clickable(
                        interactionSource = interaction,
                        indication = ripple(),
                        onClick = { onSelect(option) }
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = fg,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = fg
                )
            }
        }
    }
}

/**
 * 窄屏 / 中屏使用的可展开月度结算单卡片：摘要层一眼可见，点击平滑展开收支明细。
 */
@Composable
internal fun ExpandableIncomeRecordCard(
    data: IncomeVO,
    ecoFeeRate: Double?,
    expanded: Boolean,
    isLoadingDetail: Boolean,
    onToggleExpand: () -> Unit,
    onStartSettle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val statusTone = statusBadgeTone(data.status)
    val interaction = remember { MutableInteractionSource() }
    val isHovered by interaction.collectIsHoveredAsState()
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(220),
        label = "expand_arrow"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = when {
                    expanded -> colors.outline
                    isHovered -> colors.outlineVariant
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 可点击头部摘要区
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .hoverable(interaction)
                    .clickable(
                        interactionSource = interaction,
                        indication = ripple(),
                        onClick = onToggleExpand
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formatDisplayMonth(data.dataMonth),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface
                    )
                    if (ecoFeeRate != null) {
                        FinanceStatusBadge(
                            text = "生态费 ${(ecoFeeRate * 100).formatDecimal(2)}%",
                            tone = FinanceBadgeTone.NEUTRAL
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    FinanceStatusBadge(
                        text = data.status,
                        tone = statusTone,
                        showDot = true
                    )
                    Icon(
                        imageVector = Icons.Filled.ExpandMore,
                        contentDescription = if (expanded) "收起明细" else "展开明细",
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(20.dp).rotate(arrowRotation)
                    )
                }

                // 主金额对比行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FinanceMetricTile(
                        label = "当月税前收益",
                        value = data.income.formatCurrencyString(),
                        valueColor = if (data.status == "未结算") colors.primary else colors.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    FinanceMetricTile(
                        label = "实际到手净额",
                        value = data.monthlyNetIncome.formatCurrencyString(),
                        valueColor = colors.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 轻量次级信息行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.ic_diamond),
                            contentDescription = "消耗钻石",
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "消耗 ${data.totalDiamond.formatGrouped()} 钻石",
                            style = MaterialTheme.typography.bodySmall.tabularNumbers(),
                            color = colors.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "累计可提取 ${data.availableIncome.formatCurrencyString()}",
                        style = MaterialTheme.typography.bodySmall.tabularNumbers(),
                        color = colors.onSurfaceVariant
                    )
                }
            }

            // 展开后的完整收支拆解
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(200)) + expandVertically(tween(220)),
                exit = fadeOut(tween(150)) + shrinkVertically(tween(200))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.6f))
                    IncomeBreakdownBlock(
                        data = data,
                        ecoFeeRate = ecoFeeRate
                    )
                    if (data.isSettleable()) {
                        Button(
                            onClick = onStartSettle,
                            enabled = !isLoadingDetail,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = colors.onPrimary
                            )
                        ) {
                            if (isLoadingDetail) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = colors.onPrimary
                                )
                            } else {
                                Text("申请结算本月及可合并月份", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 宽屏 Master-Detail 左侧精炼列表项：选中采用 M3 secondaryContainer，配合主色左指示条。
 */
@Composable
internal fun IncomeMasterListItem(
    data: IncomeVO,
    ecoFeeRate: Double?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val statusTone = statusBadgeTone(data.status)
    val statusColors = statusTone.resolveColors()
    val interaction = remember { MutableInteractionSource() }
    val isHovered by interaction.collectIsHoveredAsState()

    val containerColor by animateColorAsState(
        targetValue = when {
            selected -> colors.secondaryContainer
            isHovered -> colors.surfaceContainerHighest
            else -> colors.surfaceContainerHigh
        },
        label = "master_item_bg"
    )
    val primaryTextColor = if (selected) colors.onSecondaryContainer else colors.onSurface
    val secondaryTextColor = if (selected) colors.onSecondaryContainer.copy(alpha = 0.78f) else colors.onSurfaceVariant

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(containerColor)
            .hoverable(interaction)
            .clickable(
                interactionSource = interaction,
                indication = ripple(),
                onClick = onClick
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 选中状态左侧指示条
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (selected) colors.primary else statusColors.accent.copy(alpha = 0.45f))
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = formatDisplayMonth(data.dataMonth),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryTextColor
                )
                if (ecoFeeRate != null) {
                    Text(
                        text = "${(ecoFeeRate * 100).formatDecimal(1)}%",
                        style = MaterialTheme.typography.labelSmall.tabularNumbers(),
                        color = if (selected) colors.onSecondaryContainer else colors.onSurfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (selected) colors.onSecondaryContainer.copy(alpha = 0.14f)
                                else colors.surfaceContainerHighest
                            )
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
            Text(
                text = "净额 ${data.monthlyNetIncome.formatCurrencyString()} · ${data.totalDiamond.formatGrouped()} 钻",
                style = MaterialTheme.typography.bodySmall.tabularNumbers(),
                color = secondaryTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = data.income.formatCurrencyString(),
                style = MaterialTheme.typography.titleSmall.tabularNumbers(),
                fontWeight = FontWeight.Bold,
                color = if (data.status == "未结算" && !selected) colors.primary else primaryTextColor
            )
            val useContrastOverlay = selected && statusTone == FinanceBadgeTone.SECONDARY
            FinanceStatusBadge(
                text = data.status,
                containerColor = if (useContrastOverlay) {
                    colors.onSecondaryContainer.copy(alpha = 0.14f)
                } else {
                    statusColors.container
                },
                contentColor = if (useContrastOverlay) {
                    colors.onSecondaryContainer
                } else {
                    statusColors.content
                }
            )
        }
    }
}

/**
 * 宽屏 Master-Detail 右侧详情面板：完整展示选中月份的结算拆解与平台汇总。
 */
@Composable
internal fun IncomeDetailPane(
    selectedIncome: IncomeVO?,
    state: IncomeState,
    onStartSettle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val summary = remember(state.currentList) {
        computePlatformSummary(state.currentList)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (selectedIncome != null) {
            val ecoFeeRate = normalizeMonthKey(selectedIncome.dataMonth)?.let { state.ecoFeeRates[it] }
            val statusTone = statusBadgeTone(selectedIncome.status)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${formatDisplayMonth(selectedIncome.dataMonth)} 结算单",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onSurface
                                )
                                FinanceStatusBadge(
                                    text = selectedIncome.platform.uppercase(),
                                    tone = FinanceBadgeTone.NEUTRAL
                                )
                            }
                            if (selectedIncome.opTime.isNotBlank()) {
                                Text(
                                    text = "更新时间：${selectedIncome.opTime}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }

                        FinanceStatusBadge(
                            text = selectedIncome.status,
                            tone = statusTone,
                            showDot = true
                        )
                    }

                    // 三列核心数字卡片：首列采用纯色 primaryContainer + onPrimaryContainer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FinanceMetricTile(
                            label = "当月税前收益",
                            value = selectedIncome.income.formatCurrencyString(),
                            labelColor = colors.onPrimaryContainer.copy(alpha = 0.8f),
                            valueColor = colors.onPrimaryContainer,
                            containerColor = colors.primaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        FinanceMetricTile(
                            label = "实际到手净额",
                            value = selectedIncome.monthlyNetIncome.formatCurrencyString(),
                            modifier = Modifier.weight(1f)
                        )
                        FinanceMetricTile(
                            label = "累计可提取",
                            value = selectedIncome.availableIncome.formatCurrencyString(),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 完整收支拆解
                    IncomeBreakdownBlock(
                        data = selectedIncome,
                        ecoFeeRate = ecoFeeRate
                    )

                    if (selectedIncome.isSettleable()) {
                        SettleActionBanner(
                            item = selectedIncome,
                            isLoadingDetail = state.isLoadingApplyDetail,
                            onStartSettle = { onStartSettle(selectedIncome.id) }
                        )
                    }
                }
            }
        } else {
            EmptyIncomeStateCard(
                message = if (state.isLoading) "正在加载结算数据…" else "从左侧选择月度账单查看明细"
            )
        }

        // 平台结算总览卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${selectedPlatformLabel(state.selectedPlatform)} 累计结算总览",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FinanceMetricTile(
                        label = "未结算 (${summary.unsettledCount}笔)",
                        value = summary.unsettledTotal.formatCurrency(),
                        valueColor = colors.primary,
                        modifier = Modifier.weight(1f)
                    )
                    FinanceMetricTile(
                        label = "结算中 (${summary.processingCount}笔)",
                        value = summary.processingTotal.formatCurrency(),
                        valueColor = colors.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                    FinanceMetricTile(
                        label = "已打款实收 (${summary.settledCount}笔)",
                        value = summary.settledNetTotal.formatCurrency(),
                        valueColor = colors.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

private fun selectedPlatformLabel(platform: String): String =
    if (platform == "pe") "PE 手机版" else "PC 电脑版"

/**
 * 结算单明细拆解块（展开卡片与宽屏右栏共用，保证两处字段完全一致）。
 */
@Composable
internal fun IncomeBreakdownBlock(
    data: IncomeVO,
    ecoFeeRate: Double?,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val taxVal = data.tax.toDoubleOrNull() ?: 0.0
    val hasDeductions = taxVal > 0.0 || data.techServiceFee > 0.0 || data.totalUsagePrice > 0.0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceContainerHighest)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "收入明细",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.primary
        )
        if (ecoFeeRate != null) {
            FinanceKeyValueRow(
                label = "实际生态费率",
                value = "${(ecoFeeRate * 100).formatDecimal(2)}%",
                valueColor = colors.primary,
                bold = true
            )
        }
        FinanceKeyValueRow(
            label = "当前月收益",
            value = data.income.formatCurrencyString(),
            valueColor = if (data.status == "未结算") colors.primary else colors.onSurface,
            bold = true
        )
        FinanceKeyValueRow(
            label = "消耗钻石",
            value = "${data.totalDiamond.formatGrouped()} 钻石"
        )
        FinanceKeyValueRow(
            label = "激励金额",
            value = data.incentiveIncome.formatCurrencyString()
        )
        if (data.platform == "pe") {
            FinanceKeyValueRow(
                label = "畅玩计划收益",
                value = data.playPlanIncome.formatCurrencyString()
            )
        }

        HorizontalDivider(
            color = colors.outlineVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(vertical = 2.dp)
        )

        Text(
            text = "税费与结算",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurfaceVariant
        )
        FinanceKeyValueRow(
            label = "税费",
            value = if (taxVal > 0.0) "-${taxVal.formatCurrency()}" else data.tax.formatCurrencyString(),
            valueColor = if (taxVal > 0.0) colors.tertiary else colors.onSurfaceVariant
        )
        if (data.techServiceFee > 0.0) {
            FinanceKeyValueRow(
                label = "技术服务费",
                value = "-${data.techServiceFee.formatCurrency()}",
                valueColor = colors.tertiary
            )
        }
        if (data.totalUsagePrice > 0.0) {
            FinanceKeyValueRow(
                label = "网络服成本",
                value = "-${data.totalUsagePrice.formatCurrency()}",
                valueColor = colors.tertiary
            )
        }
        if (!hasDeductions) {
            FinanceKeyValueRow(
                label = "其他扣费",
                value = "¥0.00",
                valueColor = colors.onSurfaceVariant
            )
        }

        HorizontalDivider(
            color = colors.outlineVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(vertical = 2.dp)
        )

        FinanceKeyValueRow(
            label = "实际到手净额",
            value = data.monthlyNetIncome.formatCurrencyString(),
            valueColor = colors.primary,
            bold = true
        )
        FinanceKeyValueRow(
            label = "累计可提取收益",
            value = data.availableIncome.formatCurrencyString(),
            valueColor = colors.onSurface,
            bold = true
        )
    }
}

/**
 * 空态卡片。
 */
@Composable
internal fun EmptyIncomeStateCard(
    message: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        }
    }
}

/**
 * 申请结算确认弹窗：采用 Material3 Dialog，原生支持返回键/ESC 关闭与自适应宽度。
 */
@Composable
internal fun ApplyIncomeDetailDialog(
    detailList: List<ApplyIncomeDetailVO>,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onApply: (List<String>) -> Unit
) {
    val colors = LocalAppColors.current
    val first = detailList.firstOrNull() ?: return
    val ids = remember(detailList) { detailList.map { it.id } }
    val totalTaxIncome = remember(detailList) {
        detailList.sumOf { it.taxIncome.toDoubleOrNull() ?: 0.0 }
    }

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .widthIn(min = 300.dp, max = 500.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = colors.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "确认结算申请",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                        Text(
                            text = "共包含 ${detailList.size} 个可结算月份",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "关闭",
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // 双列金额汇总块：税后实得采用纯色 primaryContainer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FinanceMetricTile(
                        label = "总可结算收益",
                        value = first.availableIncome.formatCurrencyString(),
                        modifier = Modifier.weight(1f)
                    )
                    FinanceMetricTile(
                        label = "总税后收益",
                        value = totalTaxIncome.formatCurrency(),
                        labelColor = colors.onPrimaryContainer.copy(alpha = 0.8f),
                        valueColor = colors.onPrimaryContainer,
                        containerColor = colors.primaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 每月明细可滚动列表
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "每月结算明细",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurfaceVariant
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 260.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceContainerHighest)
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        detailList.forEachIndexed { index, detail ->
                            ApplyDetailMonthlyItem(detail = detail)
                            if (index < detailList.lastIndex) {
                                HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.6f))
                            }
                        }
                    }
                }

                // 收款账户信息区块
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceContainerHighest)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountBalance,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "收款账户核对",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSurface
                        )
                    }
                    DialogAccountRow("发票提交方式", first.type, colors)
                    DialogAccountRow("供应商名称", first.providerName.ifBlank { "—" }, colors)
                    DialogAccountRow("收款银行", first.bank.ifBlank { "—" }, colors)
                    DialogAccountRow("银行卡号", first.cardNo.ifBlank { "—" }, colors)
                }

                // 底部操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("取消")
                    }
                    Button(
                        onClick = { onApply(ids) },
                        enabled = !isSubmitting,
                        modifier = Modifier.weight(1.4f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary
                        )
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = colors.onPrimary
                            )
                        } else {
                            Text("确认申请结算", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogAccountRow(label: String, value: String, colors: AppColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.tabularNumbers(),
            fontWeight = FontWeight.Medium,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ApplyDetailMonthlyItem(detail: ApplyIncomeDetailVO) {
    val colors = LocalAppColors.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDisplayMonth(detail.dataMonth),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )
            Text(
                text = "税后 ${detail.taxIncome.formatCurrencyString()}",
                style = MaterialTheme.typography.labelLarge.tabularNumbers(),
                fontWeight = FontWeight.Bold,
                color = colors.primary
            )
        }

        FinanceKeyValueRow("消耗钻石数", "${detail.totalDiamond.formatGrouped()} 钻石")
        if (detail.extraInfo.advIncome > 0.0) {
            FinanceKeyValueRow("广告计划收益", detail.extraInfo.advIncome.formatCurrency())
        }
        FinanceKeyValueRow("分成后收益", detail.income.formatCurrencyString(), bold = true)
        if ((detail.incentiveIncome.toDoubleOrNull() ?: 0.0) > 0.0) {
            FinanceKeyValueRow("激励收益", detail.incentiveIncome.formatCurrencyString())
        }
        if ((detail.playPlanIncome.toDoubleOrNull() ?: 0.0) > 0.0) {
            FinanceKeyValueRow("畅玩计划收益", detail.playPlanIncome.formatCurrencyString())
        }

        val taxVal = detail.tax.toDoubleOrNull() ?: 0.0
        val adjustVal = detail.adjustMoney.toDoubleOrNull() ?: 0.0
        FinanceKeyValueRow(
            label = "税费",
            value = if (taxVal > 0.0) "-${taxVal.formatCurrency()}" else detail.tax.formatCurrencyString(),
            valueColor = if (taxVal > 0.0) colors.tertiary else colors.onSurfaceVariant
        )
        if (adjustVal != 0.0) {
            FinanceKeyValueRow(
                label = "扣款",
                value = detail.adjustMoney.formatCurrencyString(),
                valueColor = colors.tertiary
            )
        }
        if (detail.techServiceFee > 0.0) {
            FinanceKeyValueRow(
                label = "技术服务费",
                value = "-${detail.techServiceFee.formatCurrency()}",
                valueColor = colors.tertiary
            )
        }
        if (detail.totalUsagePrice > 0.0) {
            FinanceKeyValueRow(
                label = "网络服成本",
                value = "-${detail.totalUsagePrice.formatCurrency()}",
                valueColor = colors.tertiary
            )
        }
    }
}

private data class PlatformIncomeSummary(
    val unsettledCount: Int,
    val unsettledTotal: Double,
    val processingCount: Int,
    val processingTotal: Double,
    val settledCount: Int,
    val settledNetTotal: Double
)

private fun computePlatformSummary(items: List<IncomeVO>): PlatformIncomeSummary {
    var unsettledCount = 0
    var unsettledTotal = 0.0
    var processingCount = 0
    var processingTotal = 0.0
    var settledCount = 0
    var settledNetTotal = 0.0

    for (item in items) {
        when (item.status) {
            "未结算" -> {
                unsettledCount++
                unsettledTotal += item.income.toDoubleOrNull() ?: 0.0
            }

            "结算中" -> {
                processingCount++
                processingTotal += item.income.toDoubleOrNull() ?: 0.0
            }

            "已打款" -> {
                settledCount++
                settledNetTotal += item.monthlyNetIncome.toDoubleOrNull() ?: 0.0
            }
        }
    }

    return PlatformIncomeSummary(
        unsettledCount = unsettledCount,
        unsettledTotal = unsettledTotal,
        processingCount = processingCount,
        processingTotal = processingTotal,
        settledCount = settledCount,
        settledNetTotal = settledNetTotal
    )
}
