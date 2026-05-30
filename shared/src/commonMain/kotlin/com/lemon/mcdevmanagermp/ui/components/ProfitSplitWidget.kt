package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_diamond
import mcdevmanagermpr.shared.generated.resources.ic_download
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun ProfitSplitWidget(
    overview: com.lemon.mcdevmanagermp.data.common.NetworkState<com.lemon.mcdevmanagermp.data.vo.netease.user.OverviewVO>?,
    isLoading: Boolean,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val data = (overview as? com.lemon.mcdevmanagermp.data.common.NetworkState.Success)?.data

    Column(
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            ProfitSingleWidget(
                icon = Res.drawable.ic_diamond,
                mainText = "本月收益",
                mainNum = data?.thisMonthDiamond,
                subText = "上月收益",
                subNum = data?.lastMonthDiamond,
                isLoading = isLoading,
                onClick = onClick,
                modifier = Modifier.weight(1f)
            )
            ProfitSingleWidget(
                icon = Res.drawable.ic_download,
                mainText = "本月下载",
                mainNum = data?.thisMonthDownload,
                subText = "上月下载",
                subNum = data?.lastMonthDownload,
                isLoading = isLoading,
                onClick = onClick,
                modifier = Modifier.weight(1f)
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            ProfitSingleWidget(
                icon = Res.drawable.ic_diamond,
                mainText = "昨日收益",
                mainNum = data?.yesterdayDiamond,
                subText = "14日均收益",
                subNum = data?.days14AverageDiamond,
                isLoading = isLoading,
                onClick = onClick,
                modifier = Modifier.weight(1f)
            )
            ProfitSingleWidget(
                icon = Res.drawable.ic_download,
                mainText = "昨日下载",
                mainNum = data?.yesterdayDownload,
                subText = "14日均下载",
                subNum = data?.days14AverageDownload,
                isLoading = isLoading,
                onClick = onClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ProfitSingleWidget(
    icon: DrawableResource,
    mainText: String,
    mainNum: Int?,
    subText: String,
    subNum: Int?,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    Card(
        modifier = modifier
            .height(130.dp)
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                enabled = !isLoading,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        if (!isLoading) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = mainText,
                    fontSize = 14.sp,
                    color = colors.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(icon),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = mainNum?.toString() ?: "--",
                        fontSize = 24.sp,
                        color = colors.onSurface,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = subText,
                        fontSize = 13.sp,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = subNum?.toString() ?: "--",
                        fontSize = 13.sp,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.outlineVariant.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text("...", color = colors.onSurfaceVariant)
            }
        }
    }
}
