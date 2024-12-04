package com.lemon.mcdevmanager.ui.page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.MCDevManagerTheme
import com.lemon.mcdevmanager.ui.widget.HeaderWidget
import com.lemon.mcdevmanager.viewModel.RealtimeProfitViewModel
import com.lt.compose_views.other.HorizontalSpace
import com.lt.compose_views.other.VerticalSpace
import com.lt.compose_views.value_selector.date_selector.DateSelector
import com.lt.compose_views.value_selector.date_selector.DateSelectorState
import java.time.ZonedDateTime
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lemon.mcdevmanager.data.common.LOGIN_PAGE
import com.lemon.mcdevmanager.ui.base.BasePage
import com.lemon.mcdevmanager.ui.widget.AppLoadingWidget
import com.lemon.mcdevmanager.ui.widget.RealtimeProfitItemCard
import com.lemon.mcdevmanager.viewModel.RealtimeProfitAction
import com.lemon.mcdevmanager.viewModel.RealtimeProfitEvent
import com.zj.mvi.core.observeEvent
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RealtimeProfitPage(
    navController: NavController = rememberNavController(),
    showToast: (String, String) -> Unit = { _, _ -> },
    viewModel: RealtimeProfitViewModel = viewModel()
) {
    val states by viewModel.viewStates.collectAsState()

    val nowDate = remember { ZonedDateTime.now() }
    val hintColor = AppTheme.colors.hintColor
    val dateState = remember {
        DateSelectorState(
            defaultYear = nowDate.year,
            defaultMonth = nowDate.monthValue,
            defaultDay = nowDate.dayOfMonth,
            maxYear = nowDate.plusYears(1L).year
        )
    }
    var isShowDateSelector by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        viewModel.dispatch(
            RealtimeProfitAction.UpdateCheckDay(
                nowDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            )
        )
        viewModel.dispatch(RealtimeProfitAction.GetOneDayDetail)
    }

    BasePage(
        viewEvent = viewModel.viewEvents,
        onEvent = { event ->
            when (event) {
                is RealtimeProfitEvent.ShowToast -> showToast(
                    event.message, event.type
                )

                is RealtimeProfitEvent.NeedReLogin -> navController.navigate(LOGIN_PAGE) {
                    launchSingleTop = true
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        }
    ) {
        Column {
            HeaderWidget(
                title = "实时收益", leftAction = {
                    Box(modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .clickable(indication = ripple(),
                            interactionSource = remember { MutableInteractionSource() }) { navController.navigateUp() }) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "back"
                        )
                    }
                }
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = AppTheme.colors.card
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    // 收益
                    if (!isShowDateSelector) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .padding(horizontal = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = states.totalDiamond.toString(),
                                    fontSize = 14.sp,
                                    color = hintColor
                                )
                                HorizontalSpace(dp = 4)
                                Image(
                                    painter = painterResource(id = R.drawable.ic_diamond),
                                    contentDescription = "calendar",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            HorizontalSpace(dp = 8)
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = states.totalPoints.toString(),
                                    fontSize = 14.sp,
                                    color = hintColor
                                )
                                HorizontalSpace(dp = 4)
                                Image(
                                    painter = painterResource(id = R.drawable.ic_emerald),
                                    contentDescription = "calendar",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    AnimatedVisibility(
                        visible = isShowDateSelector,
                        enter = expandVertically { fullSize -> fullSize },
                        exit = shrinkHorizontally(
                            animationSpec = tween(
                                delayMillis = 300,
                                durationMillis = 200
                            )
                        ) { fullSize -> fullSize }
                    ) {
                        Column {
                            DateSelector(
                                state = dateState,
                                modifier = Modifier.fillMaxWidth(),
                                cacheSize = 1,
                                isLoop = true,
                                textSizes = remember { mutableStateListOf(14.sp) },
                                selectedTextSize = 16.sp,
                                textColors = remember { mutableStateListOf(hintColor) },
                                selectedTextColor = AppTheme.colors.primaryColor
                            )
                            VerticalSpace(dp = 8)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        indication = ripple(),
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        isShowDateSelector = false
                                        val month = if (dateState
                                                .getMonth()
                                                .toInt() < 10
                                        )
                                            "0${dateState.getMonth()}" else dateState.getMonth()
                                        val day = if (dateState
                                                .getDay()
                                                .toInt() < 10
                                        )
                                            "0${dateState.getDay()}" else dateState.getDay()
                                        viewModel.dispatch(RealtimeProfitAction.UpdateCheckDay("${dateState.getYear()}-${month}-${day}"))
                                        viewModel.dispatch(RealtimeProfitAction.GetOneDayDetail)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = AppTheme.colors.primaryColor
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "确定",
                                        fontSize = 14.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                AnimatedVisibility(
                    visible = !isShowDateSelector,
                    enter = expandHorizontally { fullSize -> fullSize },
                    exit = shrinkHorizontally(
                        animationSpec = tween(
                            delayMillis = 300,
                            durationMillis = 200
                        )
                    ) { fullSize -> fullSize }
                ) {
                    Row {
                        HorizontalSpace(dp = 8)
                        Card(
                            shape = CircleShape,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable(
                                    indication = ripple(),
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { isShowDateSelector = true },
                            colors = CardDefaults.cardColors(containerColor = AppTheme.colors.card)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_calendar),
                                contentDescription = "calendar",
                                modifier = Modifier
                                    .size(36.dp)
                                    .padding(8.dp),
                                colorFilter = ColorFilter.tint(AppTheme.colors.primaryColor)
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (states.checkDay == nowDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) "实时收益 ${
                        SimpleDateFormat(
                            "HH:mm:ss",
                            Locale.CHINA
                        ).format(Date(states.lastRequestTime))
                    }" else states.checkDay,
                    color = hintColor,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                if (states.checkDay == nowDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) {
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clip(CircleShape)
                            .clickable(
                                indication = ripple(),
                                interactionSource = remember { MutableInteractionSource() }) {
                                viewModel.dispatch(RealtimeProfitAction.GetOneDayDetail)
                            }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_refresh),
                            contentDescription = "refresh",
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(AppTheme.colors.hintColor),
                        )
                    }
                }
            }
            LazyColumn(Modifier.fillMaxWidth()) {
                states.profitMap.forEach { (iid, data) ->
                    item {
                        RealtimeProfitItemCard(
                            iid = iid,
                            name = states.resList.find { it.itemId == iid }?.itemName ?: "未知资源",
                            data = data
                        )
                    }
                }
            }
        }
        AnimatedVisibility(visible = states.isLoading) {
            AppLoadingWidget()
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun RealtimeProfitPagePreview() {
    MCDevManagerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
        ) {
            RealtimeProfitPage()
        }
    }
}