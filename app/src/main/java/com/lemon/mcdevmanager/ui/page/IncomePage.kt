package com.lemon.mcdevmanager.ui.page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.lemon.mcdevmanager.data.netease.income.IncomeBean
import com.lemon.mcdevmanager.ui.base.BasePage
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.MCDevManagerTheme
import com.lemon.mcdevmanager.ui.widget.AppLoadingWidget
import com.lemon.mcdevmanager.ui.widget.DividedLine
import com.lemon.mcdevmanager.ui.widget.HeaderWidget
import com.lemon.mcdevmanager.ui.widget.SelectTextCard
import com.lemon.mcdevmanager.utils.getNoScaleTextSize
import com.lemon.mcdevmanager.utils.pxToDp
import com.lemon.mcdevmanager.viewModel.IncomeDetailActions
import com.lemon.mcdevmanager.viewModel.IncomeDetailEvents
import com.lemon.mcdevmanager.viewModel.IncomeDetailViewModel

@Composable
fun IncomePage(
    navController: NavController = rememberNavController(),
    showToast: (String, String) -> Unit = { _, _ -> },
    viewModel: IncomeDetailViewModel = viewModel()
) {
    val context = LocalContext.current
    var nowPlatform by remember { mutableStateOf("pe") }

    val states by viewModel.viewStates.collectAsState()
    val dataList by rememberUpdatedState(
        if (nowPlatform == "pe") states.peDetailList else states.pcDetailList
    )

    LaunchedEffect(Unit) {
        viewModel.dispatch(IncomeDetailActions.LoadIncomeDetail)
    }

    BasePage(
        viewEvent = viewModel.viewEvents,
        onEvent = {
            when (it) {
                is IncomeDetailEvents.ShowToast -> showToast(it.message, it.type)
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 标题
            HeaderWidget(title = "收益详情", leftAction = {
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
            })
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
                        .fillMaxWidth(), contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无数据",
                        color = AppTheme.colors.hintColor,
                        fontSize = getNoScaleTextSize(context, 16f).sp
                    )
                }
            } else {
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
            visible = states.isLoading,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = null
                ) {}
        ) {
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
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
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
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
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
private fun HeaderCardPreview() {
    MCDevManagerTheme {
        IncomeInfoCard(
            IncomeBean(
                _status = "init",
                platform = "pe",
                dataMonth = "2024-10"
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