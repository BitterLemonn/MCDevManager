package com.lemon.mcdevmanager.ui.widget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.ui.theme.AppTheme

@Composable
fun ProfitWidget(
    curMonthProfit: Int,
    curMonthDl: Int,
    lastMonthProfit: Int,
    lastMonthDl: Int,
    yesterdayProfit: Int,
    halfAvgProfit: Int,
    yesterdayDl: Int,
    halfAvgDl: Int,
    isLoading: Boolean = true
) {
    var size by remember { mutableStateOf(IntSize.Zero) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.card
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        if (isLoading){
            ShimmerAnimation {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp))
            }
        }else{
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .onGloballyPositioned {
                        size = it.size
                    }
            ) {
                Row(modifier = Modifier.fillMaxWidth(1f)) {
                    Box(modifier = Modifier.fillMaxWidth(0.5f)) {
                        ProfitSmallWidget(
                            icon = R.drawable.ic_diamond,
                            mainText = "本月钻石收益",
                            mainNum = curMonthProfit,
                            subText = "上月钻石收益",
                            subNum = lastMonthProfit,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        ProfitSmallWidget(
                            icon = R.drawable.ic_diamond,
                            mainText = "昨日钻石收益",
                            mainNum = yesterdayProfit,
                            subText = "14日均钻石收益",
                            subNum = halfAvgProfit,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(1f)) {
                    Box(modifier = Modifier.fillMaxWidth(0.5f)) {
                        ProfitSmallWidget(
                            icon = R.drawable.ic_download,
                            mainText = "本月下载量",
                            mainNum = curMonthDl,
                            subText = "上月下载量",
                            subNum = lastMonthDl,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        ProfitSmallWidget(
                            icon = R.drawable.ic_download,
                            mainText = "昨日下载量",
                            mainNum = yesterdayDl,
                            subText = "14日均下载量",
                            subNum = halfAvgDl,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ProfitWidgetPreview() {
    ProfitWidget(
        curMonthProfit = 30000000,
        curMonthDl = 10,
        yesterdayProfit = 300,
        yesterdayDl = 1,
        lastMonthProfit = 150000,
        lastMonthDl = 5,
        halfAvgProfit = 2000,
        halfAvgDl = 8
    )
}