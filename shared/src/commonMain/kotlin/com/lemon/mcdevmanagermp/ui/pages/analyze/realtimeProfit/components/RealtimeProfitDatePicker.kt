package com.lemon.mcdevmanagermp.ui.pages.analyze.realtimeProfit.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * 现代风格日期选择器弹窗
 * 使用自定义 Dialog 包裹 Material3 DatePicker，适配应用主题色
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RealtimeProfitDatePicker(
    currentDay: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalAppColors.current

    // 记录当前时间戳，用于禁止选择未来日期
    val nowMillis = remember { Clock.System.now().toEpochMilliseconds() }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = try {
            Instant.parse(currentDay + "T00:00:00Z").toEpochMilliseconds()
        } catch (_: Exception) {
            null
        },
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= nowMillis
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        confirmButton = {
            Button(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // 二次校验：忽略未来日期
                        if (millis <= nowMillis) {
                            val selectedDate = Instant.fromEpochMilliseconds(millis)
                                .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                            onDateSelected(selectedDate)
                        }
                    }
                    onDismiss()
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                Text(
                    text = "确定",
                    color = colors.onPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "取消",
                    color = colors.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
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
            selectedDayContentColor = colors.onPrimary,
        )
    ) {
        DatePicker(state = datePickerState)
    }
}
