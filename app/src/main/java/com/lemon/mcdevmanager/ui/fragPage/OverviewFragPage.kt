package com.lemon.mcdevmanager.ui.fragPage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.MCDevManagerTheme
import com.lemon.mcdevmanager.ui.theme.TextWhite
import com.lemon.mcdevmanager.ui.widget.DividedLine
import com.lemon.mcdevmanager.ui.widget.FlowTabWidget
import com.lemon.mcdevmanager.ui.widget.FromToMonthPickerWidget
import com.lemon.mcdevmanager.ui.widget.MonthlyAnalyzeInfoCard
import com.lemon.mcdevmanager.ui.widget.SelectTextCard
import com.lemon.mcdevmanager.utils.pxToDp
import com.lemon.mcdevmanager.viewModel.AnalyzeAction
import com.lemon.mcdevmanager.viewModel.AnalyzeViewModel
import com.lt.compose_views.other.VerticalSpace
import com.orhanobut.logger.Logger
import java.time.ZoneId
import java.time.ZonedDateTime

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OverviewFragPage(
    showToast: (String, String) -> Unit = { _, _ -> },
    viewModel: AnalyzeViewModel
) {
    val context = LocalContext.current
    val states by viewModel.viewStates.collectAsState()

    val nowDate by remember { mutableStateOf(ZonedDateTime.now(ZoneId.of("Asia/Shanghai"))) }

    var selectTextHeight by remember { mutableIntStateOf(0) }
    var isShowFilter by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        viewModel.dispatch(AnalyzeAction.UpdateFromMonth(getMonthStr(nowDate)))
        viewModel.dispatch(AnalyzeAction.UpdateToMonth(getMonthStr(nowDate)))
        viewModel.dispatch(AnalyzeAction.GetMonthlyAnalyze)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 选择平台
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp)
                .onGloballyPositioned {
                    selectTextHeight = pxToDp(context, it.size.height.toFloat())
                }
        ) {
            Box(modifier = Modifier.weight(1f)) {
                SelectTextCard(
                    initSelectLeft = states.platform == "pe", leftName = "PE", rightName = "PC"
                ) {
                    if (it) viewModel.dispatch(AnalyzeAction.UpdatePlatform("pe"))
                    else viewModel.dispatch(AnalyzeAction.UpdatePlatform("pc"))
                }
            }
            Box(modifier = Modifier
                .size(40.dp)
                .align(Alignment.CenterVertically)
                .clip(CircleShape)
                .background(AppTheme.colors.primaryColor)
                .clickable(
                    indication = rememberRipple(),
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    isShowFilter = !isShowFilter
                }
                .padding(horizontal = 8.dp)) {
                Image(
                    modifier = Modifier
                        .padding(4.dp)
                        .align(Alignment.Center),
                    painter = painterResource(id = R.drawable.ic_filter),
                    contentDescription = "filter",
                    colorFilter = ColorFilter.tint(TextWhite)
                )
            }
        }
        FlowRow(Modifier.fillMaxWidth()) {
            FlowTabWidget(
                text = "当月",
                onClick = {
                    viewModel.dispatch(AnalyzeAction.UpdateFromMonth(getMonthStr(nowDate)))
                    viewModel.dispatch(AnalyzeAction.UpdateToMonth(getMonthStr(nowDate)))
                    viewModel.dispatch(AnalyzeAction.GetMonthlyAnalyze)
                }
            )
            FlowTabWidget(
                text = "近三月",
                onClick = {
                    val fromMonthStr = getMonthStr(nowDate.minusMonths(3))
                    viewModel.dispatch(AnalyzeAction.UpdateFromMonth(fromMonthStr))
                    viewModel.dispatch(AnalyzeAction.UpdateToMonth(getMonthStr(nowDate)))
                    viewModel.dispatch(AnalyzeAction.GetMonthlyAnalyze)
                }
            )
            FlowTabWidget(
                text = "近半年",
                onClick = {
                    val fromMonthStr = getMonthStr(nowDate.minusMonths(6))
                    viewModel.dispatch(AnalyzeAction.UpdateFromMonth(fromMonthStr))
                    viewModel.dispatch(AnalyzeAction.UpdateToMonth(getMonthStr(nowDate)))
                    viewModel.dispatch(AnalyzeAction.GetMonthlyAnalyze)
                }
            )
            FlowTabWidget(
                text = "近一年",
                onClick = {
                    val fromMonthStr = getMonthStr(nowDate.minusMonths(12))
                    viewModel.dispatch(AnalyzeAction.UpdateFromMonth(fromMonthStr))
                    viewModel.dispatch(AnalyzeAction.UpdateToMonth(getMonthStr(nowDate)))
                    viewModel.dispatch(AnalyzeAction.GetMonthlyAnalyze)
                }
            )
        }
        VerticalSpace(dp = 8.dp)
        if (!states.isShowLoading)
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (states.monthAnalyseList.isEmpty()) {
                    item {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "暂无数据",
                                color = Color.Gray,
                                modifier = Modifier.fillMaxSize(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    val map = states.monthAnalyseList.groupBy { it.monthId }
                    for (item in map.entries) {
                        item {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DividedLine(modifier = Modifier.width(24.dp))
                                Text(
                                    text = item.key,
                                    modifier = Modifier.padding(8.dp),
                                    color = AppTheme.colors.hintColor,
                                    fontSize = 14.sp
                                )
                                DividedLine(modifier = Modifier.weight(1f))
                            }
                        }
                        items(item.value) { bean ->
                            MonthlyAnalyzeInfoCard(bean)
                        }
                    }
                }
            }
    }

    FromToMonthPickerWidget(
        modifier = Modifier.wrapContentSize(),
        fromMonthStr = states.fromMonth,
        toMonthStr = states.toMonth,
        onFromMonthChange = {
            viewModel.dispatch(AnalyzeAction.UpdateFromMonth(it))
        },
        onToMonthChange = {
            viewModel.dispatch(AnalyzeAction.UpdateToMonth(it))
        },
        onConfirm = {
            viewModel.dispatch(AnalyzeAction.GetMonthlyAnalyze)
            isShowFilter = false
        }
    )


    AnimatedVisibility(
        visible = isShowFilter,
        enter = slideInVertically(initialOffsetY = { -it - selectTextHeight }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it - selectTextHeight }) + fadeOut(),
        modifier = Modifier.padding(top = selectTextHeight.dp)
    ) {
        Logger.d("showing filter")
        FromToMonthPickerWidget(
            fromMonthStr = states.fromMonth,
            toMonthStr = states.toMonth,
            onFromMonthChange = {
                viewModel.dispatch(AnalyzeAction.UpdateFromMonth(it))
            },
            onToMonthChange = {
                viewModel.dispatch(AnalyzeAction.UpdateToMonth(it))
            },
            onConfirm = {
                viewModel.dispatch(AnalyzeAction.GetMonthlyAnalyze)
                isShowFilter = false
            }
        )
    }
}

private fun getMonthStr(date: ZonedDateTime): String {
    return "${date.year}-${date.monthValue.toString().padStart(2, '0')}"
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun OverviewFragPagePreview() {
    MCDevManagerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
        ) {
            OverviewFragPage(viewModel = viewModel())
        }
    }
}