package com.lemon.mcdevmanager.ui.page

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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.data.global.AppContext
import com.lemon.mcdevmanager.data.netease.income.IncentiveBean
import com.lemon.mcdevmanager.ui.base.BasePage
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.MCDevManagerTheme
import com.lemon.mcdevmanager.ui.widget.HeaderWidget
import com.lemon.mcdevmanager.utils.getNoScaleTextSize
import com.lemon.mcdevmanager.viewModel.IncentiveViewActions
import com.lemon.mcdevmanager.viewModel.IncentiveViewEvents
import com.lemon.mcdevmanager.viewModel.IncentiveViewModel
import java.util.Locale

@Composable
fun IncentivePage(
    navController: NavController = rememberNavController(),
    showToast: (String, String) -> Unit = { _, _ -> },
    viewModel: IncentiveViewModel = viewModel()
) {
    val states by viewModel.viewStates.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.dispatch(IncentiveViewActions.LoadData)
    }

    BasePage(viewEvent = viewModel.viewEvents, onEvent = { event ->
        when (event) {
            is IncentiveViewEvents.ShowToast -> showToast(event.message, event.flag)
        }
    }) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            HeaderWidget(title = "激励历史", leftAction = {
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

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.card)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "本月激励金额",
                            color = AppTheme.colors.textColor,
                            fontSize = getNoScaleTextSize(LocalContext.current, 14f).sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val curMonthFund =
                                AppContext.curUserInfo?.curMonthIncentiveFund ?: 0
                            // 保留两位小数 若为整数则不显示小数
                            val curMonthIncentiveFundStr =
                                if (curMonthFund.toInt().toDouble() == curMonthFund)
                                    curMonthFund.toInt().toString()
                                else String.format(Locale.CHINA, "%.2f", curMonthFund)
                            Text(
                                text = curMonthIncentiveFundStr,
                                color = AppTheme.colors.textColor,
                                fontSize = getNoScaleTextSize(LocalContext.current, 18f).sp,
                                fontFamily = Font(R.font.minecraft_ae).toFontFamily()
                            )
                            Image(
                                painterResource(R.drawable.ic_incentive_coin),
                                contentDescription = "coin",
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "累计激励金额",
                            color = AppTheme.colors.textColor,
                            fontSize = getNoScaleTextSize(LocalContext.current, 14f).sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val sumValue = states.incentiveList.sumOf { it.incentiveCount }
                            // 保留两位小数 若为整数则不显示小数
                            val sumValueStr = if (sumValue.toInt().toDouble() == sumValue) {
                                sumValue.toInt().toString()
                            } else String.format(Locale.CHINA, "%.2f", sumValue)
                            Text(
                                text = sumValueStr,
                                color = AppTheme.colors.textColor,
                                fontSize = getNoScaleTextSize(LocalContext.current, 18f).sp,
                                fontFamily = Font(R.font.minecraft_ae).toFontFamily()
                            )
                            Image(
                                painterResource(R.drawable.ic_incentive_coin),
                                contentDescription = "coin",
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(states.incentiveList) { item ->
                    IncentiveInfoCard(item)
                }
            }
        }
    }
}

@Composable
private fun IncentiveInfoCard(data: IncentiveBean) {
    val context = LocalContext.current
    val state by remember {
        mutableStateOf(
            when (data.status) {
                0 -> "等待结算"
                1 -> "结算中"
                2 -> "已结算"
                else -> "未知状态"
            }
        )
    }
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
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = data.month,
                    color = AppTheme.colors.hintColor,
                    fontSize = getNoScaleTextSize(context, 12f).sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = state,
                    color = AppTheme.colors.hintColor,
                    fontSize = getNoScaleTextSize(context, 12f).sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = data.source,
                    color = AppTheme.colors.hintColor,
                    fontSize = getNoScaleTextSize(context, 12f).sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 160.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${
                            if (data.incentiveCount.toInt()
                                    .toDouble() == data.incentiveCount
                            ) data.incentiveCount.toInt() else data.incentiveCount
                        }",
                        color = AppTheme.colors.textColor,
                        fontSize = getNoScaleTextSize(context, 16f).sp,
                        fontFamily = Font(R.font.minecraft_ae).toFontFamily()
                    )
                    Image(
                        painterResource(R.drawable.ic_incentive_coin),
                        contentDescription = "coin",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
@Preview
private fun IncentiveInfoCardPreview() {
    MCDevManagerTheme {
        IncentiveInfoCard(
            IncentiveBean(
                activityId = "1",
                incentiveCount = 100.0,
                month = "2021-01",
                source = "一次激励活动——1231231231231123",
                status = 1,
                updateTime = 1612137600
            )
        )
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun IncentivePagePreview() {
    MCDevManagerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
        ) {
            IncentivePage()
        }
    }
}