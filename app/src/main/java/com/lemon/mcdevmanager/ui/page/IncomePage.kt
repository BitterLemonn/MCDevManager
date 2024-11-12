package com.lemon.mcdevmanager.ui.page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.data.global.AppContext
import com.lemon.mcdevmanager.data.netease.income.ApplyIncomeDetailBean
import com.lemon.mcdevmanager.data.netease.income.ExtraInfo
import com.lemon.mcdevmanager.data.netease.income.IncomeBean
import com.lemon.mcdevmanager.ui.base.BasePage
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.MCDevManagerTheme
import com.lemon.mcdevmanager.ui.theme.TextWhite
import com.lemon.mcdevmanager.ui.widget.AppLoadingWidget
import com.lemon.mcdevmanager.ui.widget.DividedLine
import com.lemon.mcdevmanager.ui.widget.HeaderWidget
import com.lemon.mcdevmanager.ui.widget.SelectTextCard
import com.lemon.mcdevmanager.utils.getNoScaleTextSize
import com.lemon.mcdevmanager.utils.pxToDp
import com.lemon.mcdevmanager.viewModel.IncomeDetailActions
import com.lemon.mcdevmanager.viewModel.IncomeDetailEvents
import com.lemon.mcdevmanager.viewModel.IncomeDetailViewModel
import java.util.Locale

@Composable
fun IncomePage(
    navController: NavController = rememberNavController(),
    showToast: (String, String) -> Unit = { _, _ -> },
    viewModel: IncomeDetailViewModel = viewModel()
) {
    val context = LocalContext.current
    var nowPlatform by remember { mutableStateOf("pe") }

    val states by viewModel.viewStates.collectAsState()
    val dataList by remember {
        derivedStateOf {
            if (nowPlatform == "pe") states.peDetailList else states.pcDetailList
        }
    }
    val detailList by remember {
        derivedStateOf { states.applyIncomeDetail }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatch(IncomeDetailActions.LoadIncomeDetail)
    }

    BasePage(viewEvent = viewModel.viewEvents, onEvent = {
        when (it) {
            is IncomeDetailEvents.ShowToast -> showToast(it.message, it.type)
        }
    }) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 标题
            HeaderWidget(
                title = "收益详情",
                leftAction = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .clickable(
                                indication = rememberRipple(),
                                interactionSource = remember { MutableInteractionSource() }
                            ) { navController.navigateUp() }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "back"
                        )
                    }
                }
            )
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AppTheme.colors.card
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "未提取收益",
                            color = AppTheme.colors.hintColor,
                            fontSize = getNoScaleTextSize(context, 14f).sp
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = AppContext.curUserInfo?.unExtractIncome ?: "0.00",
                            color = AppTheme.colors.textColor,
                            fontSize = getNoScaleTextSize(context, 16f).sp,
                            fontFamily = Font(R.font.minecraft_ae).toFontFamily()
                        )
                        Image(
                            painter = painterResource(R.drawable.ic_money),
                            contentDescription = "money",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                SelectTextCard(modifier = Modifier
                    .width(120.dp)
                    .padding(vertical = 0.dp),
                    initSelectLeft = nowPlatform == "pe",
                    leftName = "PE",
                    rightName = "PC",
                    nowSelectLeft = {
                        nowPlatform = if (it) "pe" else "pc"
                    })
            }
            DividedLine()
            if (dataList.isEmpty() && !states.isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无数据",
                        color = AppTheme.colors.hintColor,
                        fontSize = getNoScaleTextSize(context, 16f).sp
                    )
                }
            } else {
                AnimatedVisibility(
                    visible = dataList.any { it.status == "未结算" && it.availableIncome != "0.00" },
                    enter = slideInHorizontally(initialOffsetX = { -it }),
                    exit = slideOutHorizontally(targetOffsetX = { -it })
                ) {
                    NotifyCard {
                        viewModel.dispatch(
                            IncomeDetailActions.GetApplyDetail(
                                incomeId = dataList.first { it.status == "未结算" && it.availableIncome != "0.00" }.id,
                                platform = nowPlatform
                            )
                        )
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {
                    items(dataList) { data ->
                        IncomeInfoCard(data)
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = !states.isLoading && detailList.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        indication = null,
                        interactionSource = null
                    ) { viewModel.dispatch(IncomeDetailActions.DismissApplyDetail) },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = null
                        ) {}
                ) {
                    if (detailList.isNotEmpty()) {
                        ApplyIncomeDetailCard(detailList) {
                            viewModel.dispatch(
                                IncomeDetailActions.ApplyIncome(listOf(it))
                            )
                        }
                    }
                }
            }
        }
        AnimatedVisibility(visible = states.isLoading,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null, interactionSource = null
                ) {}) {
            AppLoadingWidget(showBackground = true)
        }
    }
}

@Composable
private fun IncomeInfoCard(data: IncomeBean) {
    val context = LocalContext.current

    var leftPanelHeight by remember { mutableFloatStateOf(0f) }

    if (data.status == "未结算") {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppTheme.colors.card
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = data.dataMonth,
                        color = AppTheme.colors.hintColor,
                        fontSize = getNoScaleTextSize(context, 14f).sp
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = data.status,
                        color = AppTheme.colors.hintColor,
                        fontSize = getNoScaleTextSize(context, 14f).sp
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row {
                    Column(modifier = Modifier
                        .weight(1f)
                        .onGloballyPositioned { leftPanelHeight = it.size.height.toFloat() }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "当前月收益",
                                color = AppTheme.colors.hintColor,
                                fontSize = getNoScaleTextSize(context, 14f).sp
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = data.income,
                                color = if (data.status == "未结算") AppTheme.colors.primaryColor
                                else AppTheme.colors.textColor,
                                fontSize = getNoScaleTextSize(context, 16f).sp,
                                fontFamily = Font(R.font.minecraft_ae).toFontFamily()
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "消耗钻石",
                                color = AppTheme.colors.hintColor,
                                fontSize = getNoScaleTextSize(context, 14f).sp
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = data.totalDiamond.toString(),
                                color = AppTheme.colors.textColor,
                                fontSize = getNoScaleTextSize(context, 16f).sp,
                                fontFamily = Font(R.font.minecraft_ae).toFontFamily()
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "激励金额",
                                color = AppTheme.colors.hintColor,
                                fontSize = getNoScaleTextSize(context, 14f).sp
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = data.incentiveIncome,
                                color = AppTheme.colors.textColor,
                                fontSize = getNoScaleTextSize(context, 16f).sp,
                                fontFamily = Font(R.font.minecraft_ae).toFontFamily()
                            )
                        }
                        if (data.platform == "pe") {
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "畅玩计划收益",
                                    color = AppTheme.colors.hintColor,
                                    fontSize = getNoScaleTextSize(context, 14f).sp
                                )
                                Spacer(Modifier.weight(1f))
                                Text(
                                    text = data.playPlanIncome,
                                    color = AppTheme.colors.textColor,
                                    fontSize = getNoScaleTextSize(context, 16f).sp,
                                    fontFamily = Font(R.font.minecraft_ae).toFontFamily()
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(
                        modifier = Modifier
                            .height(pxToDp(context, leftPanelHeight).dp)
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (data.status == "未结算") {
                            Text(
                                text = "累计可提取收益",
                                color = AppTheme.colors.textColor,
                                fontSize = getNoScaleTextSize(context, 14f).sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Box(
                                modifier = Modifier.weight(1f), contentAlignment = Alignment.Center
                            ) {
                                val textSize = (18 - (data.availableIncome.length / 6)).toFloat()
                                Text(
                                    text = data.availableIncome,
                                    color = AppTheme.colors.secondaryColor,
                                    fontSize = getNoScaleTextSize(context, textSize).sp,
                                    fontFamily = Font(R.font.minecraft_ae).toFontFamily()
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "税费",
                                color = AppTheme.colors.hintColor,
                                fontSize = getNoScaleTextSize(context, 14f).sp
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = data.tax,
                                color = AppTheme.colors.textColor,
                                fontSize = getNoScaleTextSize(context, 16f).sp,
                                fontFamily = Font(R.font.minecraft_ae).toFontFamily()
                            )
                        }
                        if (data.techServiceFee > 0.0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val testSize =
                                    (16 - (data.techServiceFee.toString().length / 6)).toFloat()
                                Text(
                                    text = "技术服务费",
                                    color = AppTheme.colors.hintColor,
                                    fontSize = getNoScaleTextSize(context, 14f).sp
                                )
                                Spacer(Modifier.weight(1f))
                                Text(
                                    text = data.techServiceFee.toString(),
                                    color = AppTheme.colors.textColor,
                                    fontSize = getNoScaleTextSize(context, testSize).sp,
                                    fontFamily = Font(R.font.minecraft_ae).toFontFamily()
                                )
                            }
                        }
                        if (data.totalUsagePrice > 0.0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val testSize =
                                    (16 - (data.totalUsagePrice.toString().length / 6)).toFloat()
                                Text(
                                    text = "网络服成本",
                                    color = AppTheme.colors.hintColor,
                                    fontSize = getNoScaleTextSize(context, 14f).sp
                                )
                                Spacer(Modifier.weight(1f))
                                Text(
                                    text = data.totalUsagePrice.toString(),
                                    color = AppTheme.colors.textColor,
                                    fontSize = getNoScaleTextSize(context, testSize).sp,
                                    fontFamily = Font(R.font.minecraft_ae).toFontFamily()
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = data.dataMonth,
                    color = AppTheme.colors.hintColor,
                    fontSize = getNoScaleTextSize(context, 14f).sp
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = data.status,
                    color = AppTheme.colors.hintColor,
                    fontSize = getNoScaleTextSize(context, 14f).sp
                )
            }
            Spacer(Modifier.height(8.dp))
            Row {
                Column(modifier = Modifier
                    .weight(1f)
                    .onGloballyPositioned { leftPanelHeight = it.size.height.toFloat() }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "当前月收益",
                            color = AppTheme.colors.hintColor,
                            fontSize = getNoScaleTextSize(context, 14f).sp
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = data.income,
                            color = if (data.status == "未结算") AppTheme.colors.primaryColor
                            else AppTheme.colors.textColor,
                            fontSize = getNoScaleTextSize(context, 16f).sp,
                            fontFamily = Font(R.font.minecraft_ae).toFontFamily()
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "消耗钻石",
                            color = AppTheme.colors.hintColor,
                            fontSize = getNoScaleTextSize(context, 14f).sp
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = data.totalDiamond.toString(),
                            color = AppTheme.colors.textColor,
                            fontSize = getNoScaleTextSize(context, 16f).sp,
                            fontFamily = Font(R.font.minecraft_ae).toFontFamily()
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "激励金额",
                            color = AppTheme.colors.hintColor,
                            fontSize = getNoScaleTextSize(context, 14f).sp
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = data.incentiveIncome,
                            color = AppTheme.colors.textColor,
                            fontSize = getNoScaleTextSize(context, 16f).sp,
                            fontFamily = Font(R.font.minecraft_ae).toFontFamily()
                        )
                    }
                    if (data.platform == "pe") {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "畅玩计划收益",
                                color = AppTheme.colors.hintColor,
                                fontSize = getNoScaleTextSize(context, 14f).sp
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = data.playPlanIncome,
                                color = AppTheme.colors.textColor,
                                fontSize = getNoScaleTextSize(context, 16f).sp,
                                fontFamily = Font(R.font.minecraft_ae).toFontFamily()
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    modifier = Modifier
                        .height(pxToDp(context, leftPanelHeight).dp)
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (data.status == "未结算") {
                        Text(
                            text = "累计可提取收益",
                            color = AppTheme.colors.textColor,
                            fontSize = getNoScaleTextSize(context, 14f).sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier.weight(1f), contentAlignment = Alignment.Center
                        ) {
                            val textSize = (18 - (data.availableIncome.length / 6)).toFloat()
                            Text(
                                text = data.availableIncome,
                                color = AppTheme.colors.secondaryColor,
                                fontSize = getNoScaleTextSize(context, textSize).sp,
                                fontFamily = Font(R.font.minecraft_ae).toFontFamily()
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "税费",
                            color = AppTheme.colors.hintColor,
                            fontSize = getNoScaleTextSize(context, 14f).sp
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = data.tax,
                            color = AppTheme.colors.textColor,
                            fontSize = getNoScaleTextSize(context, 16f).sp,
                            fontFamily = Font(R.font.minecraft_ae).toFontFamily()
                        )
                    }
                    if (data.techServiceFee > 0.0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val testSize =
                                (16 - (data.techServiceFee.toString().length / 6)).toFloat()
                            Text(
                                text = "技术服务费",
                                color = AppTheme.colors.hintColor,
                                fontSize = getNoScaleTextSize(context, 14f).sp
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = data.techServiceFee.toString(),
                                color = AppTheme.colors.textColor,
                                fontSize = getNoScaleTextSize(context, testSize).sp,
                                fontFamily = Font(R.font.minecraft_ae).toFontFamily()
                            )
                        }
                    }
                    if (data.totalUsagePrice > 0.0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val testSize =
                                (16 - (data.totalUsagePrice.toString().length / 6)).toFloat()
                            Text(
                                text = "网络服成本",
                                color = AppTheme.colors.hintColor,
                                fontSize = getNoScaleTextSize(context, 14f).sp
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = data.totalUsagePrice.toString(),
                                color = AppTheme.colors.textColor,
                                fontSize = getNoScaleTextSize(context, testSize).sp,
                                fontFamily = Font(R.font.minecraft_ae).toFontFamily()
                            )
                        }
                    }
                }
            }
        }
        DividedLine()
    }

}

@Composable
@Preview
private fun IncomeInfoCardPreview() {
    MCDevManagerTheme {
        IncomeInfoCard(
            IncomeBean(
                _status = "init", platform = "pe", dataMonth = "2024-10"
            )
        )
    }
}

@Composable
private fun NotifyCard(onClickCompute: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.card
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_no_reply),
                contentDescription = "有可结算收益",
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(AppTheme.colors.primaryColor)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "有可结算收益", color = AppTheme.colors.hintColor, fontSize = 16.sp
            )
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
                        indication = rememberRipple(),
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onClickCompute
                    ),
            ) {
                Text(
                    text = "开始结算",
                    color = AppTheme.colors.info,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
@Preview
private fun NotifyCardPreview() {
    MCDevManagerTheme {
        NotifyCard()
    }
}

@Composable
private fun ApplyIncomeDetailCard(
    details: List<ApplyIncomeDetailBean>,
    onApplyIncome: (String) -> Unit = {}
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppTheme.colors.card
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val availableIncome = details.first().availableIncome
                    val textSize = (18 - (availableIncome.length / 6)).toFloat()
                    Text(
                        text = "总可结算收益",
                        color = AppTheme.colors.textColor,
                        fontSize = getNoScaleTextSize(context, 16f).sp
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = availableIncome,
                        color = AppTheme.colors.primaryColor,
                        fontSize = getNoScaleTextSize(context, textSize).sp,
                        fontFamily = Font(R.font.minecraft_ae).toFontFamily()
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 保留两位小数
                    val taxIncome = String.format(
                        Locale.CHINA,
                        "%.2f",
                        details.sumOf { it.taxIncome.toDouble() }
                    )
                    val taxIncomeTextSize = (18 - (taxIncome.length / 6)).toFloat()
                    Text(
                        text = "总税后收益",
                        color = AppTheme.colors.textColor,
                        fontSize = getNoScaleTextSize(context, 16f).sp
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = taxIncome,
                        color = AppTheme.colors.secondaryColor,
                        fontSize = getNoScaleTextSize(context, taxIncomeTextSize).sp,
                        fontFamily = Font(R.font.minecraft_ae).toFontFamily()
                    )
                }
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.background)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .heightIn(max = 400.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    items(details) { data ->
                        ApplyIncomeDetailMonthlyItem(data)
                        if (data != details.last()) {
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "发票提交方式",
                        color = AppTheme.colors.textColor,
                        fontSize = getNoScaleTextSize(context, 12f).sp
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = details.first().type,
                        color = AppTheme.colors.primaryColor,
                        fontSize = getNoScaleTextSize(context, 12f).sp
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "供应商名称",
                        color = AppTheme.colors.textColor,
                        fontSize = getNoScaleTextSize(context, 12f).sp
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = details.first().providerName,
                        color = AppTheme.colors.primaryColor,
                        fontSize = getNoScaleTextSize(context, 12f).sp
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "收款银行",
                        color = AppTheme.colors.textColor,
                        fontSize = getNoScaleTextSize(context, 12f).sp
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = details.first().bank,
                        color = AppTheme.colors.primaryColor,
                        fontSize = getNoScaleTextSize(context, 12f).sp
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "银行卡号",
                        color = AppTheme.colors.textColor,
                        fontSize = getNoScaleTextSize(context, 12f).sp
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = details.first().cardNo,
                        color = AppTheme.colors.primaryColor,
                        fontSize = getNoScaleTextSize(context, 12f).sp
                    )
                }
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppTheme.colors.primaryColor)
                        .clickable(
                            indication = rememberRipple(),
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { onApplyIncome(details.first().id) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "开始结算",
                        color = TextWhite,
                        fontSize = getNoScaleTextSize(context, 16f).sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ApplyIncomeDetailMonthlyItem(data: ApplyIncomeDetailBean) {
    val context = LocalContext.current
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = data.dataMonth,
                color = AppTheme.colors.hintColor,
                fontSize = getNoScaleTextSize(context, 14f).sp
            )
            Spacer(Modifier.width(8.dp))
            DividedLine(
                modifier = Modifier
                    .weight(1f)
                    .background(AppTheme.colors.hintColor)
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            val diamond = data.totalDiamond.toString()
            val diamondTextSize = (16 - (diamond.length / 6)).toFloat()
            Text(
                text = "消耗钻石数",
                color = AppTheme.colors.textColor,
                fontSize = getNoScaleTextSize(context, 14f).sp
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = diamond,
                color = AppTheme.colors.textColor,
                fontSize = getNoScaleTextSize(context, diamondTextSize).sp,
                fontFamily = Font(R.font.minecraft_ae).toFontFamily()
            )
        }
        if (data.extraInfo.advIncome != 0.0) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                val advIncome = data.extraInfo.advIncome.toString()
                val advIncomeTextSize = (16 - (advIncome.length / 6)).toFloat()
                Text(
                    text = "广告计划收益",
                    color = AppTheme.colors.textColor,
                    fontSize = getNoScaleTextSize(context, 14f).sp
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = advIncome,
                    color = AppTheme.colors.textColor,
                    fontSize = getNoScaleTextSize(context, advIncomeTextSize).sp,
                    fontFamily = Font(R.font.minecraft_ae).toFontFamily()
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            val income = data.income
            val incomeTextSize = (16 - (income.length / 6)).toFloat()
            Text(
                text = "分成后收益",
                color = AppTheme.colors.textColor,
                fontSize = getNoScaleTextSize(context, 14f).sp
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = income,
                color = AppTheme.colors.textColor,
                fontSize = getNoScaleTextSize(context, incomeTextSize).sp,
                fontFamily = Font(R.font.minecraft_ae).toFontFamily()
            )
        }
        if (data.incentiveIncome != "0.00") {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                val incentiveIncome = data.incentiveIncome
                val incentiveIncomeTextSize = (16 - (incentiveIncome.length / 6)).toFloat()
                Text(
                    text = "激励收益",
                    color = AppTheme.colors.textColor,
                    fontSize = getNoScaleTextSize(context, 14f).sp
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = incentiveIncome,
                    color = AppTheme.colors.textColor,
                    fontSize = getNoScaleTextSize(context, incentiveIncomeTextSize).sp,
                    fontFamily = Font(R.font.minecraft_ae).toFontFamily()
                )
            }
        }
        if (data.playPlanIncome != "0.00") {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                val playPlanIncome = data.playPlanIncome
                val playPlanIncomeTextSize = (16 - (playPlanIncome.length / 6)).toFloat()
                Text(
                    text = "畅玩计划收益",
                    color = AppTheme.colors.textColor,
                    fontSize = getNoScaleTextSize(context, 14f).sp
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = playPlanIncome,
                    color = AppTheme.colors.textColor,
                    fontSize = getNoScaleTextSize(context, playPlanIncomeTextSize).sp,
                    fontFamily = Font(R.font.minecraft_ae).toFontFamily()
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        DividedLine()
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            val tax = data.tax
            val taxTextSize = (16 - (tax.length / 6)).toFloat()
            Text(
                text = "税费",
                color = AppTheme.colors.hintColor,
                fontSize = getNoScaleTextSize(context, 14f).sp
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = tax,
                color = AppTheme.colors.hintColor,
                fontSize = getNoScaleTextSize(context, taxTextSize).sp,
                fontFamily = Font(R.font.minecraft_ae).toFontFamily()
            )
        }
        if (data.adjustMoney != "0.00") {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                val adjustMoney = data.adjustMoney
                val adjustMoneyTextSize = (16 - (adjustMoney.length / 6)).toFloat()
                Text(
                    text = "扣款",
                    color = AppTheme.colors.hintColor,
                    fontSize = getNoScaleTextSize(context, 14f).sp
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = adjustMoney,
                    color = AppTheme.colors.hintColor,
                    fontSize = getNoScaleTextSize(context, adjustMoneyTextSize).sp,
                    fontFamily = Font(R.font.minecraft_ae).toFontFamily()
                )
            }
        }
        if (data.techServiceFee != 0.0) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                val techServiceFee = data.techServiceFee.toString()
                val techServiceFeeTextSize = (16 - (techServiceFee.length / 6)).toFloat()
                Text(
                    text = "技术服务费",
                    color = AppTheme.colors.hintColor,
                    fontSize = getNoScaleTextSize(context, 14f).sp
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = techServiceFee,
                    color = AppTheme.colors.hintColor,
                    fontSize = getNoScaleTextSize(context, techServiceFeeTextSize).sp,
                    fontFamily = Font(R.font.minecraft_ae).toFontFamily()
                )
            }
        }
        if (data.totalUsagePrice != 0.0) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                val totalUsagePrice = data.totalUsagePrice.toString()
                val totalUsagePriceTextSize = (16 - (totalUsagePrice.length / 6)).toFloat()
                Text(
                    text = "网络服成本",
                    color = AppTheme.colors.hintColor,
                    fontSize = getNoScaleTextSize(context, 14f).sp
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = totalUsagePrice,
                    color = AppTheme.colors.hintColor,
                    fontSize = getNoScaleTextSize(context, totalUsagePriceTextSize).sp,
                    fontFamily = Font(R.font.minecraft_ae).toFontFamily()
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        DividedLine()
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            val taxIncome = data.taxIncome
            val taxIncomeTextSize = (16 - (taxIncome.length / 6)).toFloat()
            Text(
                text = "税后收益",
                color = AppTheme.colors.textColor,
                fontSize = getNoScaleTextSize(context, 16f).sp
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = taxIncome,
                color = AppTheme.colors.secondaryColor,
                fontSize = getNoScaleTextSize(context, taxIncomeTextSize).sp,
                fontFamily = Font(R.font.minecraft_ae).toFontFamily()
            )
        }
    }
}

@Composable
@Preview
private fun ApplyIncomeDetailCardPreview() {
    MCDevManagerTheme {
        ApplyIncomeDetailCard(
            listOf(
                ApplyIncomeDetailBean(
                    dataMonth = "2024-10",
                    availableIncome = "10000000.00",
                    totalDiamond = 1000000000,
                    income = "1000000.00",
                    incentiveIncome = "100000.00",
                    playPlanIncome = "10000.00",
                    extraInfo = ExtraInfo(advIncome = 100000.0),
                    tax = "1000.00",
                    techServiceFee = 100.0,
                    totalUsagePrice = 1000.0,
                    adjustMoney = "100.00"
                ),
                ApplyIncomeDetailBean(
                    dataMonth = "2024-09",
                    availableIncome = "10000000.00",
                    totalDiamond = 1000000000,
                    income = "1000000.00",
                    incentiveIncome = "100000.00",
                    playPlanIncome = "10000.00",
                    extraInfo = ExtraInfo(advIncome = 100000.0),
                    tax = "1000.00",
                    techServiceFee = 100.0,
                    totalUsagePrice = 1000.0,
                    adjustMoney = "100.00"
                )
            )
        )
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun IncomePagePreview() {
    MCDevManagerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = AppTheme.colors.background)
        ) {
            IncomePage()
        }
    }
}