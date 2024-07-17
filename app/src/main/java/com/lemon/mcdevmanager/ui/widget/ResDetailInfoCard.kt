package com.lemon.mcdevmanager.ui.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.data.netease.resource.ResDetailBean
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.utils.pxToDp

@Composable
fun ResDetailInfoCard(
    modifier: Modifier = Modifier,
    containerColor: Color = AppTheme.colors.card,
    resBeans: List<ResDetailBean>,
    filterType: Int = 0
) {
    var fillWidth by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { fillWidth = pxToDp(context, it.size.width.toFloat()) }
            .then(modifier),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            resBeans.forEach {
                ResDetailInfoItem(
                    resName = it.resName,
                    iid = it.iid,
                    value = when (filterType) {
                        0 -> it.cntBuy.toDouble()
                        1 -> it.downloadNum.toDouble()
                        2 -> it.diamond.toDouble()
                        3 -> it.points.toDouble()
                        4 -> it.dau.toDouble()
                        5 -> it.refundRate
                        else -> 0.0
                    },
                    fillWidth = fillWidth
                )
            }
        }
    }
}

@Composable
private fun ResDetailInfoItem(
    resName: String,
    iid: String,
    value: Double,
    fillWidth: Int
) {
    Row(Modifier.fillMaxWidth()) {
        Text(
            text = resName,
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.Bottom),
            color = AppTheme.colors.textColor,
            fontSize = 14.sp
        )
        if (fillWidth > 320)
            Text(
                text = iid,
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.Bottom),
                color = AppTheme.colors.hintColor,
                fontSize = 12.sp
            )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "${if (value == value.toInt().toDouble()) value.toInt() else value}",
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.Bottom),
            color = AppTheme.colors.textColor,
            fontSize = 14.sp,
            fontFamily = Font(R.font.minecraft_ae).toFontFamily()
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun PreviewResDetailInfoCard() {
    ResDetailInfoCard(
        resBeans = listOf(
            ResDetailBean(
                dau = 100,
                cntBuy = 10,
                dateId = "2021-10-01",
                diamond = 4000,
                downloadNum = 1000,
                iid = "4671862965461320892",
                platform = "pe",
                points = 200,
                refundRate = 0.1,
                resName = "神话之森",
                uploadTime = "2021-10-01"
            ),
            ResDetailBean(
                dau = 100,
                cntBuy = 10,
                dateId = "2021-10-01",
                diamond = 2000,
                downloadNum = 1000,
                iid = "4671862965412312312",
                platform = "pe",
                points = 300,
                refundRate = 0.1,
                resName = "苦柠",
                uploadTime = "2021-10-01"
            )
        )
    )
}