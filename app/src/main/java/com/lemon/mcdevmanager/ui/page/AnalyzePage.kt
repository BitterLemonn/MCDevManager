package com.lemon.mcdevmanager.ui.page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.aay.compose.baseComponents.model.GridOrientation
import com.aay.compose.lineChart.LineChart
import com.aay.compose.lineChart.model.LineParameters
import com.aay.compose.lineChart.model.LineType
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.data.common.LOGIN_PAGE
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.HeaderHeight
import com.lemon.mcdevmanager.ui.theme.MCDevManagerTheme
import com.lemon.mcdevmanager.ui.theme.TextWhite
import com.lemon.mcdevmanager.ui.widget.DividedLine
import com.lemon.mcdevmanager.ui.widget.FlowTabWidget
import com.lemon.mcdevmanager.ui.widget.FromToDatePickerWidget
import com.lemon.mcdevmanager.ui.widget.HeaderWidget
import com.lemon.mcdevmanager.ui.widget.SNACK_ERROR
import com.lemon.mcdevmanager.ui.widget.SNACK_INFO
import com.lemon.mcdevmanager.ui.widget.SelectCard
import com.lemon.mcdevmanager.ui.widget.SelectableItem
import com.lemon.mcdevmanager.utils.pxToDp
import com.lemon.mcdevmanager.viewModel.AnalyzeAction
import com.lemon.mcdevmanager.viewModel.AnalyzeEvent
import com.lemon.mcdevmanager.viewModel.AnalyzeViewModel
import com.lt.compose_views.other.HorizontalSpace
import com.zj.mvi.core.observeEvent
import java.time.ZonedDateTime

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalyzePage(
    navController: NavController,
    showToast: (String, String) -> Unit,
    viewModel: AnalyzeViewModel = viewModel()
) {
    val states by viewModel.viewStates.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    var filterHeight by remember { mutableStateOf(0) }

    var isOnChangingDate by remember { mutableStateOf(false) }
    var isShowFilter by remember { mutableStateOf(false) }
    var isShowResPicker by remember { mutableStateOf(false) }

    val analyzeResNameList = remember{ mutableStateListOf<String>() }
    val analyzeYValueList = remember{ mutableStateListOf<List<Double>>() }
    val analyzeXValueList = remember{ mutableStateListOf<List<String>>() }

    LaunchedEffect(key1 = Unit) {
        viewModel.dispatch(AnalyzeAction.GetAllResourceList)
        viewModel.viewEvents.observeEvent(lifecycleOwner) { event ->
            when (event) {
                is AnalyzeEvent.ShowToast ->
                    showToast(event.msg, if (event.isError) SNACK_ERROR else SNACK_INFO)

                is AnalyzeEvent.NeedReLogin -> navController.navigate(LOGIN_PAGE) {
                    launchSingleTop = true
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        }
    }

    LaunchedEffect(key1 = states.chartData) {
        analyzeYValueList.clear()
        analyzeXValueList.clear()
        analyzeResNameList.clear()
        for (item in states.chartData) {
            analyzeResNameList.add(item.key)
            val yValue = mutableListOf<Float>()
            val xValue = mutableListOf<String>()
            for (data in item.value) {
                yValue.add(data.second)
                xValue.add(data.first)
            }
            analyzeYValueList.add(yValue.map { it.toDouble() })
            analyzeXValueList.add(xValue)
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth()) {
            HeaderWidget(
                title = "数据分析",
                leftAction = {
                    Box(modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .clickable(indication = rememberRipple(),
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
                    .padding(end = 8.dp)
                    .onGloballyPositioned { filterHeight = it.size.height }
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SelectCard(leftName = "PE", rightName = "PC") {
                        if (it)
                            viewModel.dispatch(AnalyzeAction.UpdatePlatform("pe"))
                        else
                            viewModel.dispatch(AnalyzeAction.UpdatePlatform("pc"))
                    }
                }
                Box(modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterVertically)
                    .clip(CircleShape)
                    .background(AppTheme.colors.primaryColor)
                    .clickable(indication = rememberRipple(),
                        interactionSource = remember { MutableInteractionSource() }) {
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
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp)
            ) {
                FlowTabWidget(text = "新增购买", isSelected = states.filterType == 0, onClick = {
                    viewModel.dispatch(AnalyzeAction.UpdateFilterType(0))
                })
                FlowTabWidget(text = "销售总量", isSelected = states.filterType == 1, onClick = {
                    viewModel.dispatch(AnalyzeAction.UpdateFilterType(1))
                })
                FlowTabWidget(text = "收益", isSelected = states.filterType == 2, onClick = {
                    viewModel.dispatch(AnalyzeAction.UpdateFilterType(2))
                })
                FlowTabWidget(text = "绿宝石", isSelected = states.filterType == 3, onClick = {
                    viewModel.dispatch(AnalyzeAction.UpdateFilterType(3))
                })
                FlowTabWidget(text = "日活", isSelected = states.filterType == 4, onClick = {
                    viewModel.dispatch(AnalyzeAction.UpdateFilterType(4))
                })
                FlowTabWidget(text = "退款率", isSelected = states.filterType == 5, onClick = {
                    viewModel.dispatch(AnalyzeAction.UpdateFilterType(5))
                })
            }
            DividedLine(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 8.dp)
                    .background(AppTheme.colors.hintColor.copy(alpha = 0.5f))
            )
            if (analyzeResNameList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(
                        text = "暂无数据\n请检查筛选条件",
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        color = AppTheme.colors.hintColor,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } else {
                val lineParams = mutableListOf<LineParameters>()
                for (i in analyzeResNameList.indices){
                    lineParams.add(
                        LineParameters(
                            label = analyzeResNameList[i],
                            data = analyzeYValueList[i],
                            lineColor = AppTheme.colors.lineChartColors[i % AppTheme.colors.lineChartColors.size],
                            lineType = LineType.DEFAULT_LINE,
                            lineShadow = false
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                ){
                    LineChart(
                        modifier = Modifier.fillMaxWidth(),
                        linesParameters = lineParams,
                        animateChart = true,
                        showGridWithSpacer = true,
                        xAxisData = analyzeXValueList[0],
                        oneLineChart = false,
                        gridOrientation = GridOrientation.HORIZONTAL
                    )
                }
            }
        }

        // 过滤
        AnimatedVisibility(
            visible = isShowFilter,
            enter = expandVertically(animationSpec = tween(400)),
            exit = shrinkVertically(animationSpec = tween(400)),
            modifier = Modifier.padding(
                top = HeaderHeight + pxToDp(context, filterHeight.toFloat()).dp
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .wrapContentHeight(),
                colors = CardDefaults.cardColors(
                    containerColor = AppTheme.colors.card
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    FromToDatePickerWidget(
                        modifier = Modifier.fillMaxWidth(),
                        startTime = ZonedDateTime.parse(states.startDate),
                        endTime = ZonedDateTime.parse(states.endDate),
                        onChanging = { isOnChangingDate = it },
                        onChangeFromDate = { viewModel.dispatch(AnalyzeAction.UpdateStartDate(it)) },
                        onChangeToDate = { viewModel.dispatch(AnalyzeAction.UpdateEndDate(it)) },
                    )
                    if (!isOnChangingDate) {
                        Box(Modifier.fillMaxWidth()) {
                            if (states.filterResourceList.isNotEmpty()) {
                                Text(
                                    text = "组件对比",
                                    fontSize = 16.sp,
                                    color = AppTheme.colors.hintColor,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 8.dp)
                            ) {
                                Box(modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        1.dp,
                                        AppTheme.colors.primaryColor,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable(
                                        indication = rememberRipple(),
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) { isShowResPicker = true }
                                    .padding(8.dp)) {
                                    Text(
                                        text = "添加组件对比",
                                        fontSize = 14.sp,
                                        color = AppTheme.colors.primaryColor
                                    )
                                }
                            }
                        }
                        FlowRow(modifier = Modifier.fillMaxWidth()) {
                            for (item in states.filterResourceList) {
                                FlowTabWidget(
                                    text = states.allResourceList.find { it.itemId == item }?.itemName
                                        ?: "",
                                    isSelected = true,
                                    isShowDelete = true,
                                    onDeleteClick = {
                                        viewModel.dispatch(
                                            AnalyzeAction.ChangeResourceList(item, true)
                                        )
                                    })
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppTheme.colors.primaryColor)
                                .clickable(
                                    indication = rememberRipple(),
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    viewModel.dispatch(AnalyzeAction.LoadAnalyze)
                                    isShowFilter = false
                                }
                        ) {
                            Text(
                                text = "确定",
                                fontSize = 16.sp,
                                color = TextWhite,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }

            }
        }

        // 选择对比组件
        AnimatedVisibility(
            visible = isShowResPicker,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        isShowResPicker = false
                    }
            )
        }
        AnimatedVisibility(
            visible = isShowResPicker,
            enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400)),
            exit = slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(400)),
        ) {
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = AppTheme.colors.card
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "组件列表",
                            fontSize = 14.sp,
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.CenterStart),
                            color = AppTheme.colors.hintColor
                        )
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .padding(8.dp)
                                .clip(CircleShape)
                                .clickable(
                                    indication = rememberRipple(),
                                    interactionSource = remember { MutableInteractionSource() })
                                { isShowResPicker = false }
                                .align(Alignment.CenterEnd)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_close),
                                contentDescription = "close",
                                colorFilter = ColorFilter.tint(AppTheme.colors.textColor)
                            )
                        }
                    }
                    LazyColumn {
                        items(states.allResourceList) { item ->
                            SelectableItem(
                                isSelected = item.itemId in states.filterResourceList,
                                containItem = {
                                    Row(modifier = Modifier.align(Alignment.CenterVertically)) {
                                        Text(
                                            text = item.itemName,
                                            fontSize = 14.sp,
                                            color = AppTheme.colors.textColor,
                                            modifier = Modifier.align(Alignment.Bottom)
                                        )
                                        HorizontalSpace(dp = 8.dp)
                                        Text(
                                            text = item.itemId,
                                            fontSize = 12.sp,
                                            color = AppTheme.colors.hintColor,
                                            modifier = Modifier.align(Alignment.Bottom)
                                        )
                                    }
                                }
                            ) {
                                if (it)
                                    viewModel.dispatch(
                                        AnalyzeAction.ChangeResourceList(item.itemId, true)
                                    )
                                else if (states.filterResourceList.size < 5)
                                    viewModel.dispatch(
                                        AnalyzeAction.ChangeResourceList(item.itemId, false)
                                    )
                                else showToast("最多选择5个组件", SNACK_INFO)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun AnalyzePagePreview() {
    MCDevManagerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
        ) {
            AnalyzePage(navController = rememberNavController(), showToast = { _, _ -> })
        }
    }
}