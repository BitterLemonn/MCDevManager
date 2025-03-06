package com.lemon.mcdevmanager.ui.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.data.common.scoreImage
import com.lemon.mcdevmanager.ui.base.BasePage
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.MCDevManagerTheme
import com.lemon.mcdevmanager.ui.theme.TextWhite
import com.lemon.mcdevmanager.ui.widget.AppLoadingWidget
import com.lemon.mcdevmanager.ui.widget.FlowTabWidget
import com.lemon.mcdevmanager.ui.widget.HeaderWidget
import com.lemon.mcdevmanager.ui.widget.SNACK_ERROR
import com.lemon.mcdevmanager.ui.widget.SelectCard
import com.lemon.mcdevmanager.utils.getAvgItems
import com.lemon.mcdevmanager.utils.getNoScaleTextSize
import com.lemon.mcdevmanager.utils.pxToDp
import com.lemon.mcdevmanager.viewModel.NewAnalyseAction
import com.lemon.mcdevmanager.viewModel.NewAnalyseEvent
import com.lemon.mcdevmanager.viewModel.NewAnalyzeViewModel
import com.orhanobut.logger.Logger
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.extensions.format
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.IndicatorCount
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import java.util.Base64
import kotlin.math.max

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewAnalyzePage(
    navController: NavController = rememberNavController(),
    viewModel: NewAnalyzeViewModel = viewModel(),
    showToast: (String, String) -> Unit = { _, _ -> },
    platform: String = "pe",
    iid: String = ""
) {
    val viewStates by viewModel.viewStates.collectAsState()

    var isShowLoading by remember { mutableStateOf(false) }
    var chartType by remember { mutableStateOf("line") }

    var analyzeWidth by remember { mutableIntStateOf(0) }
    var analyzeHeight by remember { mutableIntStateOf(0) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.dispatch(NewAnalyseAction.GetAnalyseList(platform, iid))
        if (iid.isEmpty()) {
            showToast("组件iid无法获取,请检查", SNACK_ERROR)
            navController.navigateUp()
        }
    }

    BasePage(viewEvent = viewModel.viewEvents, onEvent = { event ->
        when (event) {
            is NewAnalyseEvent.ShowToast -> showToast(event.message, event.flag)
            is NewAnalyseEvent.ShowLoading -> isShowLoading = true
            is NewAnalyseEvent.DismissLoading -> isShowLoading = false
        }
    }) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            HeaderWidget(title = "组件数据", leftAction = {
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
            })
            Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                TitleCard(
                    modName = viewStates.modName, iid = viewStates.iid, score = viewStates.modScore
                )
                Spacer(modifier = Modifier.height(8.dp))
                QuadCard(
                    newPurchaseCount = viewStates.newPurchaseCount,
                    newPurchasePercent = (viewStates.newPurchasePercent * 100.0).format(2)
                        .toDouble(),
                    dau = viewStates.dau,
                    dauPercent = (viewStates.dauPercent * 100.0).format(2).toDouble(),
                    newFollowCount = viewStates.newFollowCount,
                    newFollowPercent = (viewStates.newFollowPercent * 100.0).format(2).toDouble(),
                    avgPlayTime = viewStates.avgPlayTime.format(2).toDouble(),
                    avgPlayTimePercent = (viewStates.avgPlayTimePercent * 100.0).format(2)
                        .toDouble()
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            // 选择类型
            FlowRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                FlowTabWidget(text = "新增购买",
                    isSelected = viewStates.filterType == 0,
                    onClick = {
                        viewModel.dispatch(NewAnalyseAction.UpdateFilterType(0))
                    })
                FlowTabWidget(text = "日活", isSelected = viewStates.filterType == 1, onClick = {
                    viewModel.dispatch(NewAnalyseAction.UpdateFilterType(1))
                })
                FlowTabWidget(text = "新增粉丝",
                    isSelected = viewStates.filterType == 2,
                    onClick = {
                        viewModel.dispatch(NewAnalyseAction.UpdateFilterType(2))
                    })
                FlowTabWidget(text = "人均游玩时间",
                    isSelected = viewStates.filterType == 3,
                    onClick = {
                        viewModel.dispatch(NewAnalyseAction.UpdateFilterType(3))
                    })
            }
            // 选择图表类型
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    color = AppTheme.colors.dividerColor,
                    thickness = 0.5.dp
                )
                SelectCard(modifier = Modifier
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
                    })
            }
            // 图表
            if (viewStates.lineParams.isEmpty() && viewStates.barParams.isEmpty() && !isShowLoading) {
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
            } else if (!isShowLoading) {
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
                            data = viewStates.lineParams,
                            curvedEdges = false,
                            animationMode = AnimationMode.Together(),
                            animationDelay = 0,
                            labelProperties = LabelProperties(
                                labels = viewStates.chartDateList,
                                enabled = true,
                                textStyle = TextStyle(
                                    fontSize = 12.sp, color = AppTheme.colors.textColor
                                ),
                                rotationDegreeOnSizeConflict = -45f,
                                forceRotation = true
                            ),
                            gridProperties = GridProperties(
                                yAxisProperties = GridProperties.AxisProperties(
                                    enabled = true,
                                    color = SolidColor(AppTheme.colors.hintColor.copy(alpha = 0.5f)),
                                    lineCount = viewStates.chartDateList.size
                                ), xAxisProperties = GridProperties.AxisProperties(
                                    enabled = true,
                                    color = SolidColor(AppTheme.colors.hintColor.copy(alpha = 0.5f)),
                                    lineCount = analyzeHeight / 40
                                )
                            ),
                            labelHelperProperties = LabelHelperProperties(
                                textStyle = TextStyle(
                                    fontSize = 12.sp, color = AppTheme.colors.textColor
                                )
                            ),
                            indicatorProperties = HorizontalIndicatorProperties(
                                count = IndicatorCount.CountBased(analyzeHeight / 40),
                                textStyle = TextStyle(
                                    fontSize = 12.sp, color = AppTheme.colors.textColor
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
                                })
                        )
                    } else {
                        // 柱状图
                        val analyzeResNameList =
                            LinkedHashSet(viewStates.analyseList.map { it.resName }).toList()
                        val decreaseSize =
                            (viewStates.chartDateList.size * analyzeResNameList.size) / 5
                        if (viewStates.barParams.isNotEmpty())
                            ColumnChart(
                                modifier = Modifier.padding(
                                    bottom = 32.dp
                                ),
                                data = viewStates.barParams,
                                barProperties = BarProperties(
                                    thickness = max(2, 15 - decreaseSize).dp,
                                    spacing = (max(2, 15 - decreaseSize) / 2.0).dp
                                ),
                                animationMode = AnimationMode.Together(),
                                labelProperties = LabelProperties(
                                    labels = analyzeResNameList,
                                    enabled = true,
                                    textStyle = TextStyle(
                                        fontSize = 12.sp, color = AppTheme.colors.textColor
                                    )
                                ),
                                gridProperties = GridProperties(
                                    yAxisProperties = GridProperties.AxisProperties(
                                        enabled = true,
                                        color = SolidColor(AppTheme.colors.hintColor.copy(alpha = 0.5f)),
                                        lineCount = viewStates.chartDateList.size
                                    ), xAxisProperties = GridProperties.AxisProperties(
                                        enabled = true,
                                        color = SolidColor(AppTheme.colors.hintColor.copy(alpha = 0.5f)),
                                        lineCount = analyzeHeight / 40
                                    )
                                ),
                                labelHelperProperties = LabelHelperProperties(
                                    textStyle = TextStyle(
                                        fontSize = 12.sp, color = AppTheme.colors.textColor
                                    )
                                ),
                                indicatorProperties = HorizontalIndicatorProperties(count = IndicatorCount.CountBased(
                                    analyzeHeight / 40
                                ),
                                    textStyle = TextStyle(
                                        fontSize = 12.sp, color = AppTheme.colors.textColor
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
                                    })
                            )
                        else Text(
                            text = "暂无数据",
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            color = AppTheme.colors.hintColor,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
            Box(modifier = Modifier.padding(WindowInsets.navigationBars.asPaddingValues()))
        }
        if (isShowLoading) {
            AppLoadingWidget()
        }
    }
}

@Composable
private fun TitleCard(
    modName: String = "",
    iid: String = "",
    score: Double = 0.0,
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.card
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_mod),
                        colorFilter = ColorFilter.tint(AppTheme.colors.primaryColor),
                        contentDescription = "mod name",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = modName,
                        color = AppTheme.colors.textColor,
                        fontSize = getNoScaleTextSize(context, 14f).sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = iid,
                    color = AppTheme.colors.hintColor,
                    fontSize = getNoScaleTextSize(context, 12f).sp
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = score.toString(),
                    color = AppTheme.colors.textColor,
                    fontSize = getNoScaleTextSize(context, 18f).sp,
                    fontFamily = Font(R.font.minecraft_ae).toFontFamily()
                )
                Spacer(modifier = Modifier.width(4.dp))
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(Base64.getDecoder().decode(scoreImage)).build(),
                    contentDescription = "score",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun QuadCard(
    newPurchaseCount: Int = 0,
    newPurchasePercent: Double = 0.0,
    dau: Int = 0,
    dauPercent: Double = 0.0,
    newFollowCount: Int = 0,
    newFollowPercent: Double = 0.0,
    avgPlayTime: Double = 0.0,
    avgPlayTimePercent: Double = 0.0
) {
    var upHeight by remember { mutableDoubleStateOf(0.0) }
    var downHeight by remember { mutableDoubleStateOf(0.0) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.card
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                QuadItem(
                    modifier = Modifier.onGloballyPositioned {
                        upHeight = it.size.height.toDouble()
                    },
                    title = "新增购买",
                    value = newPurchaseCount.toString(),
                    percent = newPurchasePercent.toString()
                )
                Spacer(modifier = Modifier.weight(1f))
                QuadItem(
                    title = "日活", value = dau.toString(), percent = dauPercent.toString()
                )
                Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                QuadItem(
                    modifier = Modifier.onGloballyPositioned {
                        downHeight = it.size.height.toDouble()
                    },
                    title = "新增粉丝",
                    value = newFollowCount.toString(),
                    percent = newFollowPercent.toString()
                )
                Spacer(modifier = Modifier.weight(1f))
                QuadItem(
                    title = "人均游玩时间(min)",
                    value = avgPlayTime.toString(),
                    percent = avgPlayTimePercent.toString()
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun QuadItem(
    modifier: Modifier = Modifier, title: String = "", value: String = "", percent: String = ""
) {
    val context = LocalContext.current
    Column(
        modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = AppTheme.colors.hintColor,
            fontSize = getNoScaleTextSize(context, 12f).sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            color = AppTheme.colors.textColor,
            fontSize = getNoScaleTextSize(context, 18f).sp,
            fontFamily = Font(R.font.minecraft_ae).toFontFamily()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "超过同类型组件",
                color = AppTheme.colors.hintColor,
                fontSize = getNoScaleTextSize(context, 12f).sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${percent}%",
                color = AppTheme.colors.textColor,
                fontSize = getNoScaleTextSize(context, 12f).sp,
                fontFamily = Font(R.font.minecraft_ae).toFontFamily()
            )
        }
    }
}


@Composable
@Preview(showBackground = true)
private fun NewAnalyzePagePreview() {
    MCDevManagerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
        ) {
            NewAnalyzePage()
        }
    }
}

@Composable
@Preview
private fun TitleCardPreview() {
    MCDevManagerTheme {
        TitleCard(
            modName = "测试模组", iid = "12312312312312312321", score = 4.2
        )
    }
}

@Composable
@Preview
private fun QuadCardPreview() {
    MCDevManagerTheme {
        QuadCard()
    }
}