package com.lemon.mcdevmanager.ui.widget


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.MCDevManagerTheme
import com.lemon.mcdevmanager.ui.theme.TextWhite
import com.lt.compose_views.value_selector.date_selector.DateSelector
import com.lt.compose_views.value_selector.date_selector.DateSelectorState
import java.time.ZoneId
import java.time.ZonedDateTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FromToDatePickerWidget(
    modifier: Modifier = Modifier,
    startTime: ZonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Shanghai")).minusDays(7),
    endTime: ZonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Shanghai")),
    onChangeFromDate: (String) -> Unit = {},
    onChangeToDate: (String) -> Unit = {},
    onChanging: (Boolean) -> Unit = {}
) {
    var fromDate by remember { mutableStateOf("${startTime.year}-${startTime.monthValue}-${startTime.dayOfMonth}") }
    var toDate by remember { mutableStateOf("${endTime.year}-${endTime.monthValue}-${endTime.dayOfMonth}") }
    val fromDateSelectorState = remember {
        DateSelectorState(
            defaultYear = startTime.year,
            defaultMonth = startTime.monthValue,
            defaultDay = startTime.dayOfMonth
        )
    }
    val toDateSelectorState = remember {
        DateSelectorState(
            defaultYear = endTime.year,
            defaultMonth = endTime.monthValue,
            defaultDay = endTime.dayOfMonth
        )
    }

    var isSelectFromDate by remember { mutableStateOf(false) }
    var isSelectToDate by remember { mutableStateOf(false) }

    val animateFromWidth by animateFloatAsState(
        targetValue = if (!isSelectToDate) 1f else 0.1f, animationSpec = tween(200)
    )
    val animateToWidth by animateFloatAsState(
        targetValue = if (!isSelectFromDate) 1f else 0.1f, animationSpec = tween(200)
    )

    val hintColor = AppTheme.colors.hintColor
    val dateSelectorColorList = arrayListOf(hintColor)
    val dateSelectorFontSize = arrayListOf(14.sp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .then(modifier)
    ) {
        AnimatedVisibility(
            modifier = Modifier.weight(animateFromWidth),
            visible = !isSelectToDate,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 45.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 1.dp,
                        color = if (isSelectFromDate) Color.Transparent else AppTheme.colors.primaryColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .animateContentSize()
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isSelectFromDate,
                    enter = fadeIn(tween(200, delayMillis = 200)),
                    exit = fadeOut(tween(200))
                ) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple()
                            ) {
                                isSelectFromDate = true
                                onChanging(true)
                            }
                    ) {
                        Text(
                            text = fromDate,
                            fontSize = 14.sp,
                            letterSpacing = 2.sp,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(8.dp),
                            color = AppTheme.colors.textColor
                        )
                    }
                }
                androidx.compose.animation.AnimatedVisibility(
                    visible = isSelectFromDate,
                    enter = expandVertically(animationSpec = tween(200, delayMillis = 200))
                            + fadeIn(tween(200)),
                    exit = shrinkVertically(animationSpec = tween(200))
                            + fadeOut(tween(200))
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = AppTheme.colors.card
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        DateSelector(
                            state = fromDateSelectorState,
                            isLoop = true,
                            cacheSize = 1,
                            textColors = remember { dateSelectorColorList.toMutableStateList() },
                            selectedTextSize = 14.sp,
                            selectedTextColor = AppTheme.colors.textColor,
                            textSizes = remember { dateSelectorFontSize.toMutableStateList() }
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppTheme.colors.primaryColor)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple()
                                ) {
                                    isSelectFromDate = false
                                    val year = fromDateSelectorState
                                        .getYear()
                                        .toInt()
                                    val month = fromDateSelectorState
                                        .getMonth()
                                        .toInt()
                                    val day = fromDateSelectorState
                                        .getDay()
                                        .toInt()
                                    fromDate = "$year-$month-$day"
                                    onChangeFromDate(
                                        ZonedDateTime
                                            .of(
                                                year,
                                                month,
                                                day,
                                                0,
                                                0,
                                                0,
                                                0,
                                                ZoneId.of("Asia/Shanghai")
                                            )
                                            .toString()
                                    )
                                    onChanging(false)
                                }

                        ) {
                            Text(
                                text = "确定",
                                color = TextWhite,
                                fontSize = 14.sp,
                                letterSpacing = 2.sp,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
        AnimatedVisibility(
            modifier = Modifier.align(Alignment.CenterVertically),
            visible = (!isSelectFromDate && !isSelectToDate),
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200))
        ) {
            Text(
                text = "至",
                fontSize = 14.sp,
                color = AppTheme.colors.textColor,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        AnimatedVisibility(
            modifier = Modifier.weight(animateToWidth),
            visible = !isSelectFromDate,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 45.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 1.dp,
                        color = if (isSelectToDate) Color.Transparent else AppTheme.colors.primaryColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .animateContentSize()
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isSelectToDate,
                    enter = fadeIn(tween(200, delayMillis = 200)),
                    exit = fadeOut(tween(200))
                ) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple()
                            ) {
                                isSelectToDate = true
                                onChanging(true)
                            }
                    ) {
                        Text(
                            text = toDate,
                            fontSize = 14.sp,
                            letterSpacing = 2.sp,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(8.dp),
                            color = AppTheme.colors.textColor
                        )
                    }
                }
                androidx.compose.animation.AnimatedVisibility(
                    visible = isSelectToDate,
                    enter = expandVertically(animationSpec = tween(200, delayMillis = 200))
                            + fadeIn(tween(200)),
                    exit = shrinkVertically(animationSpec = tween(200))
                            + fadeOut(tween(200))
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = AppTheme.colors.card
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        DateSelector(
                            state = toDateSelectorState,
                            cacheSize = 1,
                            isLoop = true,
                            textColors = remember { dateSelectorColorList.toMutableStateList() },
                            selectedTextSize = 14.sp,
                            selectedTextColor = AppTheme.colors.textColor,
                            textSizes = remember { dateSelectorFontSize.toMutableStateList() }
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppTheme.colors.primaryColor)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple()
                                ) {
                                    isSelectToDate = false
                                    val year = toDateSelectorState.getYear()
                                    val month = toDateSelectorState.getMonth()
                                    val day = toDateSelectorState.getDay()
                                    toDate = "$year-$month-$day"
                                    onChangeToDate(
                                        ZonedDateTime.of(
                                                year.toInt(),
                                                month.toInt(),
                                                day.toInt(),
                                                0,
                                                0,
                                                0,
                                                0,
                                                ZoneId.of("Asia/Shanghai")
                                            )
                                            .toString()
                                    )
                                    onChanging(false)
                                }
                        )
                        {
                            Text(
                                text = "确定",
                                color = TextWhite,
                                fontSize = 14.sp,
                                letterSpacing = 2.sp,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun FromToDatePickerWidgetPreview() {
    MCDevManagerTheme {
        FromToDatePickerWidget()
    }
}