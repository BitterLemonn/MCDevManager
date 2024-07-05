package com.lemon.mcdevmanager.ui.widget


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.MCDevManagerTheme
import com.lemon.mcdevmanager.ui.theme.TextWhite
import com.lt.compose_views.value_selector.date_selector.DateSelector
import com.lt.compose_views.value_selector.date_selector.DateSelectorState
import com.orhanobut.logger.Logger
import java.time.ZoneId
import java.time.ZonedDateTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FromToDatePickerWidget(
    modifier: Modifier = Modifier,
    onChangeFromDate: (String) -> Unit = {},
    onChangeToDate: (String) -> Unit = {},
    onChanging: (Boolean) -> Unit = {}
) {
    val nowDate = remember { ZonedDateTime.now(ZoneId.of("Asia/Shanghai")) }
    var fromDate by remember { mutableStateOf("${nowDate.year}-${nowDate.monthValue}-${nowDate.dayOfMonth}") }
    var toDate by remember { mutableStateOf("${nowDate.year}-${nowDate.monthValue}-${nowDate.dayOfMonth}") }
    val fromDateSelectorState = remember {
        DateSelectorState(
            defaultYear = nowDate.year,
            defaultMonth = nowDate.monthValue,
            defaultDay = nowDate.dayOfMonth
        )
    }
    val toDateSelectorState = remember {
        DateSelectorState(
            defaultYear = nowDate.year,
            defaultMonth = nowDate.monthValue,
            defaultDay = nowDate.dayOfMonth
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
                                indication = rememberRipple()
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
                    enter = expandIn(
                        animationSpec = tween(durationMillis = 200, delayMillis = 1000),
                        expandFrom = Alignment.TopCenter,
                        initialSize = { IntSize(it.width, 0) }
                    ) + fadeIn(tween(200)),
                    exit = shrinkOut(
                        tween(200),
                        shrinkTowards = Alignment.TopCenter,
                        targetSize = { IntSize(it.width, 0) }
                    ) + fadeOut(tween(200))
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = AppTheme.colors.card
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        DateSelector(
                            state = fromDateSelectorState,
                            cacheSize = 1,
                            textColors = arrayListOf(AppTheme.colors.hintColor),
                            selectedTextSize = 14.sp,
                            selectedTextColor = AppTheme.colors.textColor,
                            textSizes = arrayListOf(14.sp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppTheme.colors.primaryColor)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = rememberRipple()
                                ) {
                                    isSelectFromDate = false
                                    val year = fromDateSelectorState.getYear()
                                    val month = fromDateSelectorState.getMonth()
                                    val day = fromDateSelectorState.getDay()
                                    fromDate = "$year-$month-$day"
                                    onChangeFromDate(fromDate)
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
                                indication = rememberRipple()
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
                    enter = expandIn(
                        animationSpec = tween(durationMillis = 200, delayMillis = 1000),
                        expandFrom = Alignment.TopCenter,
                        initialSize = { IntSize(it.width, 0) }
                    ) + fadeIn(tween(200)),
                    exit = shrinkOut(
                        tween(200),
                        shrinkTowards = Alignment.TopCenter,
                        targetSize = { IntSize(it.width, 0) }
                    ) + fadeOut(tween(200))
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
                            textColors = arrayListOf(AppTheme.colors.hintColor),
                            selectedTextSize = 14.sp,
                            selectedTextColor = AppTheme.colors.textColor,
                            textSizes = arrayListOf(14.sp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppTheme.colors.primaryColor)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = rememberRipple()
                                ) {
                                    isSelectToDate = false
                                    val year = toDateSelectorState.getYear()
                                    val month = toDateSelectorState.getMonth()
                                    val day = toDateSelectorState.getDay()
                                    toDate = "$year-$month-$day"
                                    onChangeToDate(toDate)
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