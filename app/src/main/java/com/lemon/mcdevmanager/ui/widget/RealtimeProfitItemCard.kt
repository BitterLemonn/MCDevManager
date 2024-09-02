package com.lemon.mcdevmanager.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.data.netease.income.OneResRealtimeIncomeBean
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.MCDevManagerTheme
import com.lemon.mcdevmanager.utils.getNoScaleTextSize
import com.lt.compose_views.other.HorizontalSpace
import com.lt.compose_views.other.VerticalSpace

@Composable
fun RealtimeProfitItemCard(
    iid: String = "",
    name: String = "",
    data: OneResRealtimeIncomeBean = OneResRealtimeIncomeBean()
) {
    val context = LocalContext.current

    val showDiamond = data.totalDiamonds > 0 || (data.totalDiamonds == 0 && data.totalPoints == 0)
    val showPoint = data.totalPoints > 0 || (data.totalDiamonds == 0 && data.totalPoints == 0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.card),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_mod),
                    contentDescription = "mod",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(AppTheme.colors.primaryColor)
                )
                HorizontalSpace(dp = 4)
                Column {
                    Text(
                        text = name,
                        fontSize = getNoScaleTextSize(context, 14f).sp,
                        color = AppTheme.colors.hintColor
                    )
                    Text(
                        text = iid,
                        fontSize = getNoScaleTextSize(context, 10f).sp,
                        color = AppTheme.colors.hintColor
                    )
                }
                HorizontalSpace(dp = 4)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showDiamond)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = data.totalDiamonds.toString(),
                                fontSize = getNoScaleTextSize(context, 14f).sp,
                                color = AppTheme.colors.hintColor
                            )
                            HorizontalSpace(dp = 8)
                            Image(
                                painter = painterResource(id = R.drawable.ic_diamond),
                                contentDescription = "diamond",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    if (showDiamond && showPoint)
                        VerticalSpace(dp = 8)
                    if (showPoint)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = data.totalPoints.toString(),
                                fontSize = getNoScaleTextSize(context, 14f).sp,
                                color = AppTheme.colors.hintColor
                            )
                            HorizontalSpace(dp = 8)
                            Image(
                                painter = painterResource(id = R.drawable.ic_emerald),
                                contentDescription = "point",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun RealtimeProfitItemCardPreview() {
    MCDevManagerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
        ) {
            RealtimeProfitItemCard(
                name = "abcderf",
                iid = "123131312312312123"
            )
        }
    }
}