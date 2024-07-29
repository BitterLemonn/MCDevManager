package com.lemon.mcdevmanager.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.data.netease.resource.ResMonthDetailBean
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.MCDevManagerTheme
import com.lemon.mcdevmanager.utils.getNoScaleTextSize
import com.lt.compose_views.other.HorizontalSpace
import com.lt.compose_views.other.VerticalSpace

@Composable
fun MonthlyAnalyzeInfoCard(
    infoData: ResMonthDetailBean
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = infoData.resName,
            fontSize = getNoScaleTextSize(context, 16f).sp,
            color = AppTheme.colors.textColor,
            fontFamily = FontFamily(Font(R.font.minecraft_ae)),
            letterSpacing = getNoScaleTextSize(context, 2f).sp
        )
        Text(
            text = infoData.iid,
            fontSize = getNoScaleTextSize(context, 12f).sp,
            color = AppTheme.colors.hintColor,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Row {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Image(
                    painter = painterResource(id = R.drawable.ic_sale),
                    contentDescription = "sale count",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(AppTheme.colors.primaryColor)
                )
                HorizontalSpace(dp = 4.dp)
                Text(
                    text = "销售总量",
                    fontSize = getNoScaleTextSize(context, 14f).sp,
                    color = AppTheme.colors.hintColor
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = infoData.downloadNum.toString(),
                    fontSize = getNoScaleTextSize(context, 16f).sp,
                    color = AppTheme.colors.textColor,
                    fontFamily = FontFamily(Font(R.font.minecraft_ae))
                )
            }
            HorizontalSpace(dp = 8.dp)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Image(
                    painter = painterResource(id = R.drawable.ic_buy),
                    contentDescription = "buy count",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(AppTheme.colors.primaryColor)
                )
                HorizontalSpace(dp = 4.dp)
                Text(
                    text = "日均购买",
                    fontSize = getNoScaleTextSize(context, 14f).sp,
                    color = AppTheme.colors.hintColor
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = infoData.avgDayBuy.toString(),
                    fontSize = getNoScaleTextSize(context, 16f).sp,
                    color = AppTheme.colors.textColor,
                    fontFamily = FontFamily(Font(R.font.minecraft_ae))
                )
            }
        }
        VerticalSpace(dp = 8.dp)
        Row {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Image(
                    painter = painterResource(id = R.drawable.ic_dau),
                    contentDescription = "dau count",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(AppTheme.colors.primaryColor)
                )
                HorizontalSpace(dp = 4.dp)
                Text(
                    text = "日均活跃",
                    fontSize = getNoScaleTextSize(context, 14f).sp,
                    color = AppTheme.colors.hintColor
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = infoData.avgDau.toString(),
                    fontSize = getNoScaleTextSize(context, 16f).sp,
                    color = AppTheme.colors.textColor,
                    fontFamily = FontFamily(Font(R.font.minecraft_ae))
                )
            }
            HorizontalSpace(dp = 8.dp)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Image(
                    painter = painterResource(id = R.drawable.ic_dau),
                    contentDescription = "mau count",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(AppTheme.colors.primaryColor)
                )
                HorizontalSpace(dp = 4.dp)
                Text(
                    text = "月均活跃",
                    fontSize = getNoScaleTextSize(context, 14f).sp,
                    color = AppTheme.colors.hintColor
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = infoData.mau.toString(),
                    fontSize = getNoScaleTextSize(context, 16f).sp,
                    color = AppTheme.colors.textColor,
                    fontFamily = FontFamily(Font(R.font.minecraft_ae))
                )
            }
        }
        VerticalSpace(dp = 8.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_diamond_line),
                contentDescription = "diamond count",
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(AppTheme.colors.primaryColor)
            )
            HorizontalSpace(dp = 4.dp)
            Text(
                text = "钻石收益",
                fontSize = getNoScaleTextSize(context, 14f).sp,
                color = AppTheme.colors.hintColor
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = infoData.totalDiamond.toString(),
                fontSize = getNoScaleTextSize(context, 16f).sp,
                color = AppTheme.colors.textColor,
                fontFamily = FontFamily(Font(R.font.minecraft_ae))
            )
        }
        VerticalSpace(dp = 8.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_point),
                contentDescription = "points count",
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(AppTheme.colors.primaryColor)
            )
            HorizontalSpace(dp = 4.dp)
            Text(
                text = "绿宝石收益",
                fontSize = getNoScaleTextSize(context, 14f).sp,
                color = AppTheme.colors.hintColor
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = infoData.totalPoints.toString(),
                fontSize = getNoScaleTextSize(context, 16f).sp,
                color = AppTheme.colors.textColor,
                fontFamily = FontFamily(Font(R.font.minecraft_ae))
            )
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun MonthlyAnalyzeInfoCardPreview() {
    MCDevManagerTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            MonthlyAnalyzeInfoCard(
                ResMonthDetailBean(
                    avgDau = 1000,
                    avgDayBuy = 100,
                    downloadNum = 2000000,
                    iid = "12312312313131231321",
                    mau = 2000,
                    monthId = "2021-01",
                    resName = "test",
                    uploadTime = "2021-01-01",
                    platform = "PE",
                    totalDiamond = 1000,
                    totalPoints = 1000
                )
            )
        }
    }
}