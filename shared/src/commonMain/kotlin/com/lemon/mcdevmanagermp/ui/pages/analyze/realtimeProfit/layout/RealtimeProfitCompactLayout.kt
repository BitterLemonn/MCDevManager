package com.lemon.mcdevmanagermp.ui.pages.analyze.realtimeProfit.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_calendar
import mcdevmanagermpr.shared.generated.resources.ic_diamond
import mcdevmanagermpr.shared.generated.resources.ic_emerald
import mcdevmanagermpr.shared.generated.resources.ic_refresh
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RealtimeProfitCompactLayout(
    state: RealtimeProfitState,
    onAction: (RealtimeProfitAction) -> Unit,
    onBack: () -> Unit,
    navBarBottom: androidx.compose.ui.unit.Dp,
    showDatePicker: Boolean,
    onDatePickerDismiss: () -> Unit,
    onDateSelected: (String) -> Unit,
) {
    val colors = LocalAppColors.current
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val todayStr = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    }
    val scrollState = remember { androidx.compose.foundation.ScrollState(0) }
    val alpha = ((scrollState.value / 100f).coerceIn(0f, 1f))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = navBarBottom)
    ) {
        // TopBar
        CollapsingTopBar(
            title = "实时收益",
            alpha = alpha,
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // 汇总卡片
            SummaryCard(
                totalDiamond = state.totalDiamond,
                totalPoints = state.totalPoints
            )

            // 时间信息行
            TimeInfoRow(
                checkDay = state.checkDay,
                isToday = state.checkDay == todayStr,
                lastRequestTime = state.lastRequestTime,
                onRefresh = { onAction(RealtimeProfitAction.RefreshData) }
            )

            // 收益列表
            if (state.profitMap.isEmpty() && !state.isLoading) {
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
                    modifier = Modifier.fillMaxWidth().weight(1f),
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
                    // 底部留白
                    item { Spacer(Modifier.height(8.dp)) }
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
                    onClick = {
                        // DatePicker 确认逻辑在 onDateSelected 中处理
                        onDatePickerDismiss()
                    },
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
            // 手动确认按钮
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
internal fun SummaryCard(
    totalDiamond: Int,
    totalPoints: Int
) {
    val colors = LocalAppColors.current

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = colors.surfaceContainerHigh),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 钻石
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$totalDiamond",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textColor
                )
                Spacer(Modifier.width(6.dp))
                Image(
                    painter = painterResource(Res.drawable.ic_diamond),
                    contentDescription = "diamond",
                    modifier = Modifier.size(22.dp)
                )
            }
            // 分隔
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(colors.outlineVariant)
            )
            // 绿宝石
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$totalPoints",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textColor
                )
                Spacer(Modifier.width(6.dp))
                Image(
                    painter = painterResource(Res.drawable.ic_emerald),
                    contentDescription = "emerald",
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
internal fun TimeInfoRow(
    checkDay: String,
    isToday: Boolean,
    lastRequestTime: Long,
    onRefresh: () -> Unit
) {
    val colors = LocalAppColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isToday) {
                val time = Instant.fromEpochMilliseconds(lastRequestTime)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                "实时收益 ${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}:${time.second.toString().padStart(2, '0')}"
            } else {
                checkDay
            },
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        if (isToday) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onRefresh
                    )
                    .padding(4.dp)
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_refresh),
                    contentDescription = "刷新",
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
