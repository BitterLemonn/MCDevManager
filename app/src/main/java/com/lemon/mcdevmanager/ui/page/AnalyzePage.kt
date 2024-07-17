package com.lemon.mcdevmanager.ui.page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.data.common.LOGIN_PAGE
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.HeaderHeight
import com.lemon.mcdevmanager.ui.theme.MCDevManagerTheme
import com.lemon.mcdevmanager.ui.theme.TextWhite
import com.lemon.mcdevmanager.ui.widget.AppLoadingWidget
import com.lemon.mcdevmanager.ui.widget.DividedLine
import com.lemon.mcdevmanager.ui.widget.FlowTabWidget
import com.lemon.mcdevmanager.ui.widget.FromToDatePickerWidget
import com.lemon.mcdevmanager.ui.widget.HeaderWidget
import com.lemon.mcdevmanager.ui.widget.ResDetailInfoCard
import com.lemon.mcdevmanager.ui.widget.SNACK_ERROR
import com.lemon.mcdevmanager.ui.widget.SNACK_INFO
import com.lemon.mcdevmanager.ui.widget.SelectCard
import com.lemon.mcdevmanager.ui.widget.SelectTextCard
import com.lemon.mcdevmanager.ui.widget.SelectableItem
import com.lemon.mcdevmanager.utils.getAvgItems
import com.lemon.mcdevmanager.utils.pxToDp
import com.lemon.mcdevmanager.viewModel.AnalyzeAction
import com.lemon.mcdevmanager.viewModel.AnalyzeEvent
import com.lemon.mcdevmanager.viewModel.AnalyzeViewModel
import com.lt.compose_views.other.HorizontalSpace
import com.lt.compose_views.other.VerticalSpace
import com.zj.mvi.core.observeEvent
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.extensions.format
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import java.time.ZonedDateTime
import kotlin.math.max

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

    var filterHeight by remember { mutableIntStateOf(0) }
    var analyzeWidth by remember { mutableIntStateOf(200) }
    var analyzeHeight by remember { mutableIntStateOf(200) }

    var fullHeight by remember { mutableIntStateOf(0) }

    var isOnChangingDate by remember { mutableStateOf(false) }
    var isShowFilter by remember { mutableStateOf(false) }
    var isShowResPicker by remember { mutableStateOf(false) }
    var isShowDetail by remember { mutableStateOf(false) }

    var chartType by remember { mutableStateOf("line") }
    val chartColor = AppTheme.colors.chartColors

    val animationRotate by animateFloatAsState(targetValue = if (!isShowDetail) 90f else -90f)
    val analyzeDetailMap by rememberUpdatedState(newValue = states.analyzeList.groupBy { it.dateId })

    LaunchedEffect(key1 = Unit) {
        viewModel.dispatch(AnalyzeAction.UpdateChartColor(chartColor))
        viewModel.dispatch(AnalyzeAction.GetLastAnalyzeParams)
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
        showToast("柱状图数据过多可能会导致严重卡顿", "我知道了")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { fullHeight = pxToDp(context, it.size.height.toFloat()) }
    ) {
        Column(Modifier.fillMaxWidth()) {
            // 标题
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
            // 选择平台
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp)
                    .onGloballyPositioned { filterHeight = it.size.height }
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SelectTextCard(
                        initSelectLeft = states.platform == "pe",
                        leftName = "PE",
                        rightName = "PC"
                    ) {
                        if (it)
                            viewModel.dispatch(AnalyzeAction.UpdatePlatform("pe"))
                        else
                            viewModel.dispatch(AnalyzeAction.UpdatePlatform("pc"))
                    }
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.CenterVertically)
                        .clip(CircleShape)
                        .background(AppTheme.colors.primaryColor)
                        .clickable(indication = rememberRipple(),
                            interactionSource = remember { MutableInteractionSource() }
                        ) { isShowFilter = !isShowFilter }
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
            // 选择类型
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
            // 选择图表类型
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                DividedLine(
                    modifier = Modifier
                        .weight(1f)
                        .background(AppTheme.colors.hintColor.copy(alpha = 0.5f))
                        .align(Alignment.CenterVertically)
                )
                SelectCard(
                    modifier = Modifier
                        .width(80.dp)
                        .align(Alignment.CenterVertically),
                    initSelectLeft = chartType == "line",
                    leftContain = {
                        Image(
                            painter = painterResource(id = R.drawable.ic_line_chart),
                            contentDescription = "line chart",
                            modifier = Modifier
                                .size(24.dp)
                                .padding(4.dp)
                                .align(Alignment.Center),
                            colorFilter = ColorFilter.tint(
                                if (chartType == "line") TextWhite else AppTheme.colors.primaryColor
                            )
                        )
                    },
                    rightContain = {
                        Image(
                            painter = painterResource(id = R.drawable.ic_bar_chart),
                            contentDescription = "bar chart",
                            modifier = Modifier
                                .size(24.dp)
                                .padding(4.dp)
                                .align(Alignment.Center),
                            colorFilter = ColorFilter.tint(
                                if (chartType == "bar") TextWhite else AppTheme.colors.primaryColor
                            )
                        )
                    },
                    nowSelectLeft = {
                        chartType = if (it) "line" else "bar"
                    }
                )
            }
            // 图表
            if (states.lineParams.isEmpty() && states.barParams.isEmpty() && !states.isShowLoading) {
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
            } else if (!states.isShowLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                        .onGloballyPositioned {
                            analyzeWidth = pxToDp(context, it.size.width.toFloat())
                            analyzeHeight = pxToDp(context, it.size.height.toFloat())
                        }
                ) {
                    if (chartType == "line") {
                        // 折线图
                        LineChart(
                            modifier = Modifier.padding(bottom = 32.dp),
                            data = states.lineParams,
                            curvedEdges = false,
                            animationMode = AnimationMode.Together(),
                            animationDelay = 0,
                            labelProperties = LabelProperties(
                                labels = getAvgItems(states.chartDateList, analyzeWidth / 80),
                                enabled = true,
                                textStyle = TextStyle(
                                    fontSize = 12.sp,
                                    color = AppTheme.colors.textColor
                                )
                            ),
                            gridProperties = GridProperties(
                                yAxisProperties = GridProperties.AxisProperties(
                                    enabled = true,
                                    color = SolidColor(AppTheme.colors.hintColor.copy(alpha = 0.5f)),
                                    lineCount = states.chartDateList.size
                                ),
                                xAxisProperties = GridProperties.AxisProperties(
                                    enabled = true,
                                    color = SolidColor(AppTheme.colors.hintColor.copy(alpha = 0.5f)),
                                    lineCount = analyzeHeight / 40
                                )
                            ),
                            labelHelperProperties = LabelHelperProperties(
                                textStyle = TextStyle(
                                    fontSize = 12.sp,
                                    color = AppTheme.colors.textColor
                                )
                            ),
                            indicatorProperties = HorizontalIndicatorProperties(
                                count = analyzeHeight / 40,
                                textStyle = TextStyle(
                                    fontSize = 12.sp,
                                    color = AppTheme.colors.textColor
                                ),
                                contentBuilder = { value ->
                                    if (value > 1000000) {
                                        "${(value / 1000000).format(2)}m"
                                    } else if (value > 1000) {
                                        "${(value / 1000).format(2)}k"
                                    } else {
                                        if (value == value.toInt().toDouble()) {
                                            value.format(0)
                                        } else value.format(1)
                                    }
                                }
                            )
                        )
                    } else {
                        // 柱状图
                        val analyzeResNameList =
                            LinkedHashSet(states.analyzeList.map { it.resName }).toList()
                        val decreaseSize = (states.chartDateList.size * analyzeResNameList.size) / 5
                        if (states.barParams.isNotEmpty())
                            ColumnChart(
                                modifier = Modifier.padding(bottom = 32.dp),
                                data = states.barParams,
                                barProperties = BarProperties(
                                    thickness = max(2, 15 - decreaseSize).dp,
                                    spacing = (max(2, 15 - decreaseSize) / 2.0).dp
                                ),
                                animationMode = AnimationMode.Together(),
                                labelProperties = LabelProperties(
                                    labels = analyzeResNameList,
                                    enabled = true,
                                    textStyle = TextStyle(
                                        fontSize = 12.sp,
                                        color = AppTheme.colors.textColor
                                    )
                                ),
                                gridProperties = GridProperties(
                                    yAxisProperties = GridProperties.AxisProperties(
                                        enabled = true,
                                        color = SolidColor(AppTheme.colors.hintColor.copy(alpha = 0.5f)),
                                        lineCount = states.chartDateList.size
                                    ),
                                    xAxisProperties = GridProperties.AxisProperties(
                                        enabled = true,
                                        color = SolidColor(AppTheme.colors.hintColor.copy(alpha = 0.5f)),
                                        lineCount = analyzeHeight / 40
                                    )
                                ),
                                labelHelperProperties = LabelHelperProperties(
                                    textStyle = TextStyle(
                                        fontSize = 12.sp,
                                        color = AppTheme.colors.textColor
                                    )
                                ),
                                indicatorProperties = HorizontalIndicatorProperties(
                                    count = analyzeHeight / 40,
                                    textStyle = TextStyle(
                                        fontSize = 12.sp,
                                        color = AppTheme.colors.textColor
                                    ),
                                    contentBuilder = { value ->
                                        if (value > 1000000) {
                                            "${(value / 1000000).format(2)}m"
                                        } else if (value > 1000) {
                                            "${(value / 1000).format(2)}k"
                                        } else {
                                            if (value == value.toInt().toDouble()) {
                                                value.format(0)
                                            } else value.format(1)
                                        }
                                    }
                                )
                            )
                        else
                            Text(
                                text = "暂无数据",
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center,
                                color = AppTheme.colors.hintColor,
                                modifier = Modifier.align(Alignment.Center)
                            )
                    }
                }
            }
        }

        // 详细信息
        if ((states.lineParams.isNotEmpty() || states.barParams.isNotEmpty()) && !states.isShowLoading) {
            AnimatedVisibility(
                visible = isShowDetail,
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
                        ) { isShowDetail = false }
                )
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 36.dp, max = (fullHeight * 0.8).dp)
                    .align(Alignment.BottomCenter),
                colors = CardDefaults.cardColors(
                    containerColor = if (!isShowDetail) AppTheme.colors.card
                    else AppTheme.colors.background
                ),
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .clickable(
                            indication = rememberRipple(),
                            interactionSource = remember { MutableInteractionSource() }
                        ) { isShowDetail = !isShowDetail }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "more",
                        modifier = Modifier
                            .size(32.dp)
                            .rotate(animationRotate)
                            .align(Alignment.Center),
                        colorFilter = ColorFilter.tint(AppTheme.colors.hintColor)
                    )
                }
                AnimatedVisibility(
                    visible = isShowDetail,
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    Column {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, end = 8.dp, top = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = AppTheme.colors.card
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = when (states.filterType) {
                                    0 -> "新增购买"
                                    1 -> "销售总量"
                                    2 -> "收益"
                                    3 -> "绿宝石"
                                    4 -> "日活"
                                    5 -> "退款率"
                                    else -> "未知"
                                } + "　　" + states.platform.uppercase(),
                                fontSize = 14.sp,
                                color = AppTheme.colors.textColor,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        LazyColumn(Modifier.fillMaxWidth()) {
                            analyzeDetailMap.entries.forEach {
                                item {
                                    val year = it.key.substring(0, 4)
                                    val day = it.key.substring(it.key.length - 2)
                                    val month = it.key.substring(4, it.key.length - 2)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        DividedLine(
                                            modifier = Modifier
                                                .width(16.dp)
                                                .background(AppTheme.colors.hintColor.copy(alpha = 0.5f))
                                                .align(Alignment.CenterVertically)
                                        )
                                        Text(
                                            text = "$year-$month-$day",
                                            fontSize = 12.sp,
                                            color = AppTheme.colors.hintColor,
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .align(Alignment.CenterVertically)
                                        )
                                        DividedLine(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(AppTheme.colors.hintColor.copy(alpha = 0.5f))
                                                .align(Alignment.CenterVertically)
                                        )
                                    }
                                }
                                item {
                                    ResDetailInfoCard(
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        resBeans = it.value,
                                        filterType = states.filterType,
                                        containerColor = Color.Transparent
                                    )
                                }
                            }
                            item{
                                VerticalSpace(dp = 8.dp)
                            }
                        }
                    }
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
                    ) { isShowResPicker = false }
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

        // 加载中
        AnimatedVisibility(
            visible = states.isShowLoading,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            AppLoadingWidget()
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