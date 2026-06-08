package com.lemon.mcdevmanagermp.ui.pages.analyze.realtimeProfit.layout

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.components.CollapsingTopBar
import com.lemon.mcdevmanagermp.ui.pages.analyze.realtimeProfit.RealtimeProfitAction
import com.lemon.mcdevmanagermp.ui.pages.analyze.realtimeProfit.RealtimeProfitItemCard
import com.lemon.mcdevmanagermp.ui.pages.analyze.realtimeProfit.RealtimeProfitState
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import androidx.compose.material3.HorizontalDivider
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_calendar
import mcdevmanagermpr.shared.generated.resources.ic_refresh
import org.jetbrains.compose.resources.painterResource

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = navBarBottom)
    ) {
        CollapsingTopBar(
            title = "实时收益",
            alpha = 0f,
            onBack = onBack,
            actions = {
                IconButton(onClick = { onAction(RealtimeProfitAction.ToggleDateSelector) }) {
                    Image(
                        painter = painterResource(Res.drawable.ic_calendar),
                        contentDescription = "选择日期",
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = { onAction(RealtimeProfitAction.RefreshData) }) {
                    Image(
                        painter = painterResource(Res.drawable.ic_refresh),
                        contentDescription = "刷新",
                        modifier = Modifier.size(20.dp)
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

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.profitMap.forEach { (iid, data) ->
                        item(key = iid) {
                            val name = state.resList.find { it.itemId == iid }?.itemName ?: "未知资源"
                            RealtimeProfitItemCard(
                                name = name,
                                iid = iid,
                                data = data
                            )
                        }
                    }
                    item { Spacer(Modifier.height(8.dp)) }
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

            // 右侧：订单详情区域
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                val selectedEntry = state.profitMap.entries.firstOrNull()
                if (selectedEntry != null) {
                    val iid = selectedEntry.key
                    val data = selectedEntry.value
                    val name = state.resList.find { it.itemId == iid }?.itemName ?: "未知资源"
                    ExpandedOrderDetail(
                        name = name,
                        iid = iid,
                        data = data
                    )
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
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = onDatePickerDismiss,
            confirmButton = {
                Button(
                    onClick = onDatePickerDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text("取消", color = colors.onPrimary)
                }
            }
        ) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = try {
                    Instant.parse(state.checkDay + "T00:00:00Z").toEpochMilliseconds()
                } catch (_: Exception) { null }
            )
            DatePicker(state = datePickerState)
            Button(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.fromEpochMilliseconds(millis)
                            .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                        onDateSelected(selectedDate)
                    }
                    onDatePickerDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                Text("确定", color = colors.onPrimary)
            }
        }
    }
}

@Composable
private fun ExpandedOrderDetail(
    name: String,
    iid: String,
    data: com.lemon.mcdevmanagermp.data.dto.netease.income.OneResRealtimeIncomeVO
) {
    val colors = LocalAppColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 资源信息
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
                        Text("钻石", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${data.totalPoints}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("绿宝石", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                }
            }
        }

        // 订单列表
        if (data.orders.isNotEmpty()) {
            Text(
                text = "订单明细（${data.count} 笔）",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.textColor
            )
            data.orders.forEach { order ->
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
                            color = colors.textColor
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${order.price} 钻石",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                            Text(
                                text = order.shipTime,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
