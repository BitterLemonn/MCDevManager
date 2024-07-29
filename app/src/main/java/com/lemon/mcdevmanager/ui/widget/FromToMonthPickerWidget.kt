package com.lemon.mcdevmanager.ui.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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
import com.lt.compose_views.other.VerticalSpace
import com.sd.lib.compose.wheel_picker.FVerticalWheelPicker
import com.sd.lib.compose.wheel_picker.rememberFWheelPickerState
import java.time.ZoneId
import java.time.ZonedDateTime

@Composable
fun FromToMonthPickerWidget(
    modifier: Modifier = Modifier,
    fromMonthStr: String,
    toMonthStr: String,
    onFromMonthChange: (String) -> Unit = {},
    onToMonthChange: (String) -> Unit = {},
    onChanging: (Boolean) -> Unit = {},
    showToast: (String, String) -> Unit = { _, _ -> }
) {
    val nowDate = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"))

    val fromMonth by rememberUpdatedState(newValue = fromMonthStr)
    val toMonth by rememberUpdatedState(newValue = toMonthStr)

    val fromMonthMonth = fromMonth.split("-")[1]
    val toMonthMonth = toMonth.split("-")[1]
    val fromMonthYear = fromMonth.split("-")[0]
    val toMonthYear = toMonth.split("-")[0]

    var isChangingFromMonth by remember { mutableStateOf(false) }
    var isChangingToMonth by remember { mutableStateOf(false) }

    val yearSelectList = (2010..nowDate.year).map { it.toString() } as ArrayList<String>
    val monthSelectList = (1..12).map { it.toString().padStart(2, '0') } as ArrayList<String>

    val fromYearState =
        rememberFWheelPickerState(initialIndex = yearSelectList.indexOf(fromMonthYear))
    val fromMonthState =
        rememberFWheelPickerState(initialIndex = monthSelectList.indexOf(fromMonthMonth))
    val toYearState = rememberFWheelPickerState(initialIndex = yearSelectList.indexOf(toMonthYear))
    val toMonthState =
        rememberFWheelPickerState(initialIndex = monthSelectList.indexOf(toMonthMonth))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier
            .weight(1f)
            .border(1.dp, AppTheme.colors.primaryColor, RoundedCornerShape(8.dp))
            .clickable(indication = null,
                interactionSource = remember { MutableInteractionSource() }) {
                isChangingFromMonth = true
                onChanging(true)
            }, contentAlignment = Alignment.Center
        ) {
            Text(
                text = fromMonth,
                color = AppTheme.colors.textColor,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
        Box(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .width(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "至",
                modifier = Modifier.padding(vertical = 8.dp),
                color = AppTheme.colors.textColor
            )
        }
        Box(modifier = Modifier
            .weight(1f)
            .border(1.dp, AppTheme.colors.primaryColor, RoundedCornerShape(8.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }) {
                isChangingToMonth = true
                onChanging(true)
            }, contentAlignment = Alignment.Center
        ) {
            Text(
                text = toMonth,
                color = AppTheme.colors.textColor,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }

    ModalBackgroundWidget(
        visibility = isChangingFromMonth || isChangingToMonth
    )

    AnimatedVisibility(
        visible = isChangingFromMonth,
        enter = slideInHorizontally(
            initialOffsetX = { -it }, animationSpec = spring(stiffness = 100f)
        ) + fadeIn(),
        exit = slideOutHorizontally(
            targetOffsetX = { -it }, animationSpec = spring(stiffness = 100f)
        ) + fadeOut()
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .border(1.dp, AppTheme.colors.primaryColor, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(AppTheme.colors.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FVerticalWheelPicker(modifier = Modifier.weight(1f),
                    state = fromYearState,
                    count = yearSelectList.size,
                    focus = {},
                    itemHeight = 40.dp,
                    content = {},
                    display = { index ->
                        if (state.currentIndexSnapshot == index) {
                            Text(
                                text = yearSelectList[index],
                                color = AppTheme.colors.textColor,
                                fontSize = 16.sp
                            )
                        } else {
                            Text(
                                text = yearSelectList[index],
                                color = AppTheme.colors.hintColor,
                                fontSize = 16.sp
                            )
                        }
                    })
                Text(
                    text = "年",
                    color = AppTheme.colors.hintColor,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                FVerticalWheelPicker(modifier = Modifier.weight(1f),
                    state = fromMonthState,
                    count = monthSelectList.size,
                    focus = {},
                    itemHeight = 40.dp,
                    content = {},
                    display = { index ->
                        if (state.currentIndexSnapshot == index) {
                            Text(
                                text = monthSelectList[index],
                                color = AppTheme.colors.textColor,
                                fontSize = 16.sp
                            )
                        } else {
                            Text(
                                text = monthSelectList[index],
                                color = AppTheme.colors.hintColor,
                                fontSize = 16.sp
                            )
                        }
                    })
                Text(
                    text = "月",
                    color = AppTheme.colors.hintColor,
                    modifier = Modifier.weight(0.5f)
                )
            }
            VerticalSpace(dp = 16.dp)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple()
                    ) {
                        val selectYearIndex = fromYearState.currentIndexSnapshot
                        val selectMonthIndex = fromMonthState.currentIndexSnapshot
                        val year = yearSelectList[selectYearIndex]
                        val month = monthSelectList[selectMonthIndex]
                        onFromMonthChange("$year-$month")
                        isChangingFromMonth = false
                        onChanging(false)
                    },
                colors = CardDefaults.cardColors(
                    containerColor = AppTheme.colors.primaryColor
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "确定",
                        color = TextWhite,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
            VerticalSpace(dp = 8.dp)
        }
    }
    AnimatedVisibility(
        visible = isChangingToMonth,
        enter = slideInHorizontally(
            initialOffsetX = { it }, animationSpec = spring(stiffness = 100f)
        ) + fadeIn(),
        exit = slideOutHorizontally(
            targetOffsetX = { it }, animationSpec = spring(stiffness = 100f)
        ) + fadeOut()
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .border(1.dp, AppTheme.colors.primaryColor, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(AppTheme.colors.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FVerticalWheelPicker(modifier = Modifier.weight(1f),
                    state = toYearState,
                    count = yearSelectList.size,
                    focus = {},
                    itemHeight = 40.dp,
                    content = {},
                    display = { index ->
                        if (state.currentIndexSnapshot == index) {
                            Text(
                                text = yearSelectList[index],
                                color = AppTheme.colors.textColor,
                                fontSize = 16.sp
                            )
                        } else {
                            Text(
                                text = yearSelectList[index],
                                color = AppTheme.colors.hintColor,
                                fontSize = 16.sp
                            )
                        }
                    })
                Text(
                    text = "年",
                    color = AppTheme.colors.hintColor,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                FVerticalWheelPicker(modifier = Modifier.weight(1f),
                    state = toMonthState,
                    count = monthSelectList.size,
                    focus = {},
                    itemHeight = 40.dp,
                    content = {},
                    display = { index ->
                        if (state.currentIndexSnapshot == index) {
                            Text(
                                text = monthSelectList[index],
                                color = AppTheme.colors.textColor,
                                fontSize = 16.sp
                            )
                        } else {
                            Text(
                                text = monthSelectList[index],
                                color = AppTheme.colors.hintColor,
                                fontSize = 16.sp
                            )
                        }
                    })
                Text(
                    text = "月",
                    color = AppTheme.colors.hintColor,
                    modifier = Modifier.weight(0.5f)
                )
            }
            VerticalSpace(dp = 16.dp)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple()
                    ) {
                        val selectYearIndex = toYearState.currentIndexSnapshot
                        val selectMonthIndex = toMonthState.currentIndexSnapshot
                        val year = yearSelectList[selectYearIndex]
                        val month = monthSelectList[selectMonthIndex]
                        isChangingToMonth = false
                        onChanging(false)

                        if (year < fromMonthYear || (year == fromMonthYear && month < fromMonthMonth)) {
                            showToast("结束月份不能小于开始月份", SNACK_ERROR)
                            return@clickable
                        }
                        onToMonthChange("$year-$month")
                    },
                colors = CardDefaults.cardColors(
                    containerColor = AppTheme.colors.primaryColor
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "确定",
                        color = TextWhite,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
            VerticalSpace(dp = 8.dp)
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun FromToMonthPickerWidgetPreview() {
    var fromMonth by remember { mutableStateOf("2024-01") }
    var toMonth by remember { mutableStateOf("2024-12") }
    MCDevManagerTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
        ) {
            FromToMonthPickerWidget(fromMonthStr = fromMonth,
                toMonthStr = toMonth,
                onFromMonthChange = { fromMonth = it },
                onToMonthChange = { toMonth = it })
        }
    }
}