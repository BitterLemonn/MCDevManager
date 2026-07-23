package com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

/** 应用时区（与 AppDatePickerDialog 一致：Asia/Shanghai）。 */
private val appTimeZone = TimeZone.of("Asia/Shanghai")

/**
 * 定时上架弹窗：日期（Material3 DatePicker）+ 时分输入，组装接口要求的 "YYYY-MM-DD HH:mm:ss"。
 * 最早可选今天；默认选中次日 00:00。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppointOnlineDialog(
    item: ResourceData,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current

    val today = remember { nowLocalDate() }
    val tomorrow = remember { today.plus(1, DateTimeUnit.DAY) }
    var hourInput by remember { mutableStateOf("0") }
    var minuteInput by remember { mutableStateOf("0") }

    val minMillis = remember(today) { today.atStartOfDayIn(appTimeZone).toEpochMilliseconds() }
    val initialMillis =
        remember(tomorrow) { tomorrow.atStartOfDayIn(appTimeZone).toEpochMilliseconds() }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= minMillis
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        confirmButton = {
            Button(
                onClick = {
                    val millis = datePickerState.selectedDateMillis ?: return@Button
                    val date =
                        Instant.fromEpochMilliseconds(millis).toLocalDateTime(appTimeZone).date
                    val h = hourInput.toIntOrNull()?.coerceIn(0, 23) ?: 0
                    val m = minuteInput.toIntOrNull()?.coerceIn(0, 59) ?: 0
                    onConfirm("${date} ${twoDigit(h)}:${twoDigit(m)}:00")
                },
                enabled = datePickerState.selectedDateMillis != null,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                Text("确定", color = colors.onPrimary, fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("取消", color = colors.onSurfaceVariant, fontWeight = FontWeight.Medium)
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = colors.surfaceContainerHigh,
            titleContentColor = colors.textColor,
            headlineContentColor = colors.textColor,
            weekdayContentColor = colors.primary,
            subheadContentColor = colors.onSurfaceVariant,
            yearContentColor = colors.onSurfaceVariant,
            currentYearContentColor = colors.primary,
            selectedYearContentColor = colors.onPrimary,
            selectedYearContainerColor = colors.primary,
            selectedDayContentColor = colors.onPrimary
        )
    ) {
        Column {
            DatePicker(state = datePickerState)
            Spacer(Modifier.height(8.dp))
            // 时分输入（默认 00:00）
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "上架时间",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textColor
                )
                OutlinedTextField(
                    value = hourInput,
                    onValueChange = { s -> hourInput = s.filter { it.isDigit() }.take(2) },
                    label = { Text("时") },
                    singleLine = true,
                    modifier = Modifier.width(72.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text(":", style = MaterialTheme.typography.titleMedium, color = colors.textColor)
                OutlinedTextField(
                    value = minuteInput,
                    onValueChange = { s -> minuteInput = s.filter { it.isDigit() }.take(2) },
                    label = { Text("分") },
                    singleLine = true,
                    modifier = Modifier.width(72.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

private fun twoDigit(n: Int): String = n.coerceIn(0, 99).toString().padStart(2, '0')

private fun nowLocalDate(): LocalDate {
    val nowMillis = Clock.System.now().toEpochMilliseconds()
    return Instant.fromEpochMilliseconds(nowMillis).toLocalDateTime(appTimeZone).date
}
