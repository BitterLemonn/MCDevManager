package com.lemon.mcdevmanagermp.ui.pages.analyze.realtimeProfit.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.dto.netease.income.OneResRealtimeIncomeOrder
import com.lemon.mcdevmanagermp.data.dto.netease.income.OneResRealtimeIncomeVO
import com.lemon.mcdevmanagermp.ui.components.CollapsingTopBar
import com.lemon.mcdevmanagermp.ui.pages.analyze.realtimeProfit.RealtimeProfitAction
import com.lemon.mcdevmanagermp.ui.pages.analyze.realtimeProfit.RealtimeProfitItemCard
import com.lemon.mcdevmanagermp.ui.pages.analyze.realtimeProfit.RealtimeProfitState
import com.lemon.mcdevmanagermp.ui.pages.analyze.realtimeProfit.components.RealtimeProfitDatePicker
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_calendar
import mcdevmanagermpr.shared.generated.resources.ic_diamond
import mcdevmanagermpr.shared.generated.resources.ic_emerald
import mcdevmanagermpr.shared.generated.resources.ic_refresh
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RealtimeProfitExpandedLayout(
    state: RealtimeProfitState,
    onAction: (RealtimeProfitAction) -> Unit,
    onBack: () -> Unit,
    navBarBottom: androidx.compose.ui.unit.Dp,
    showDatePicker: Boolean,
    onDatePickerDismiss: () -> Unit,
    onDateSelected: (String) -> Unit,
) {
    val colors = LocalAppColors.current
    val todayStr = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    }

    // 选中项管理：优先保持用户选择，否则自动选中第一个
    var selectedIid by remember { mutableStateOf<String?>(null) }
    val effectiveSelectedIid = selectedIid?.takeIf { it in state.profitMap }
        ?: state.profitMap.keys.firstOrNull()
    if (selectedIid != effectiveSelectedIid) {
        selectedIid = effectiveSelectedIid
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = navBarBottom)
    ) {
        CollapsingTopBar(
            title = "实时收益",
            collapseFraction = 0f,
            onBack = onBack,
            actions = {
                IconButton(onClick = { onAction(RealtimeProfitAction.ToggleDateSelector) }) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_calendar),
                        contentDescription = "选择日期",
                        modifier = Modifier.size(20.dp),
                        tint = colors.textColor
                    )
                }
                IconButton(onClick = { onAction(RealtimeProfitAction.RefreshData) }) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_refresh),
                        contentDescription = "刷新",
                        modifier = Modifier.size(20.dp),
                        tint = colors.textColor
                    )
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // 左侧：收益列表
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(420.dp)
            ) {
                Spacer(Modifier.height(8.dp))
                SummaryCard(
                    totalDiamond = state.totalDiamond,
                    totalPoints = state.totalPoints
                )
                Spacer(Modifier.height(8.dp))
                TimeInfoRow(
                    checkDay = state.checkDay,
                    isToday = state.checkDay == todayStr,
                    lastRequestTime = state.lastRequestTime,
                    onRefresh = { onAction(RealtimeProfitAction.RefreshData) }
                )
                Spacer(Modifier.height(8.dp))

                if (state.profitMap.isEmpty() && !state.isLoading) {
                    // 空数据状态
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (state.resList.isEmpty()) "暂无资源数据" else "暂无收益数据",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = state.profitMap.keys.toList(),
                            key = { it }
                        ) { iid ->
                            val data = state.profitMap[iid] ?: return@items
                            val name =
                                state.resList.find { it.itemId == iid }?.itemName ?: "未知资源"
                            val isSelected = iid == effectiveSelectedIid
                            val itemInteractionSource = remember { MutableInteractionSource() }
                            val isItemHovered by itemInteractionSource.collectIsHoveredAsState()
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .hoverable(itemInteractionSource)
                                    .clickable(
                                        interactionSource = itemInteractionSource,
                                        indication = null,
                                        onClick = { selectedIid = iid }
                                    )
                            ) {
                                RealtimeProfitItemCard(
                                    name = name,
                                    iid = iid,
                                    data = data,
                                    isHovered = isItemHovered,
                                    isSelected = isSelected
                                )
                            }
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }
            }

            // 分隔线
            HorizontalDivider(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .padding(vertical = 8.dp),
                color = colors.outlineVariant,
                thickness = 1.dp
            )

            // 右侧：订单详情
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (effectiveSelectedIid != null) {
                    val data = state.profitMap[effectiveSelectedIid]
                    val name = state.resList.find { it.itemId == effectiveSelectedIid }?.itemName
                        ?: "未知资源"
                    if (data != null) {
                        ExpandedOrderDetail(
                            name = name,
                            iid = effectiveSelectedIid,
                            data = data
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (state.isLoading) "加载中..." else "暂无订单详情",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // 日期选择器弹窗
    AnimatedVisibility(visible = showDatePicker) {
        RealtimeProfitDatePicker(
            currentDay = state.checkDay,
            onDateSelected = onDateSelected,
            onDismiss = onDatePickerDismiss
        )
    }
}

/**
 * 右侧面板：选中资源的订单详情
 * 使用 LazyColumn 支持大量订单的高效滚动
 */
@Composable
private fun ExpandedOrderDetail(
    name: String,
    iid: String,
    data: OneResRealtimeIncomeVO
) {
    val colors = LocalAppColors.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 资源信息卡片
        item(key = "header") {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = colors.surfaceContainerHigh),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textColor
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "IID: $iid",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${data.totalDiamonds}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                            Spacer(Modifier.width(4.dp))
                            Image(
                                painter = painterResource(Res.drawable.ic_diamond),
                                contentDescription = "钻石",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${data.totalPoints}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                            Spacer(Modifier.width(4.dp))
                            Image(
                                painter = painterResource(Res.drawable.ic_emerald),
                                contentDescription = "绿宝石",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // 订单列表标题
        if (data.orders.isNotEmpty()) {
            item(key = "order_header") {
                Text(
                    text = "订单明细（${data.count} 笔）",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textColor
                )
            }

            // 使用 items + key 支持差异化更新
            items(
                items = data.orders,
                key = { it.appOrderId }
            ) { order ->
                OrderItemCard(order)
            }
        }

        // 底部留白
        item(key = "footer") { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun OrderItemCard(order: OneResRealtimeIncomeOrder) {
    val colors = LocalAppColors.current

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = colors.surfaceContainerHigh),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = order.productName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colors.textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatOrderPrice(order.point, order.pointType),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
                Text(
                    text = formatShipTime(order.shipTime),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "玩家: ${order.appUid}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant
            )
        }
    }
}

/**
 * 格式化订单价格显示
 */
private fun formatOrderPrice(point: Int, type: String): String {
    val parts = mutableListOf<String>()
    when (type) {
        "付费钻石" -> parts.add("$point 钻石")
        "免费积分" -> parts.add("$point 绿宝石")
    }
    return parts.takeIf { it.isNotEmpty() }?.joinToString(" + ") ?: "免费"
}

/**
 * 将 ISO 格式时间字符串（如 2024-01-15T10:30:00）转为可读格式（如 2024-01-15 10:30）
 */
private fun formatShipTime(isoTime: String): String {
    return try {
        val cleaned = isoTime.substringBefore('.')
        val (date, time) = cleaned.split('T')
        val timeParts = time.split(':')
        val formattedTime =
            if (timeParts.size >= 2) "${timeParts[0]}:${timeParts[1]}" else timeParts[0]
        "$date $formattedTime"
    } catch (_: Exception) {
        isoTime
    }
}
