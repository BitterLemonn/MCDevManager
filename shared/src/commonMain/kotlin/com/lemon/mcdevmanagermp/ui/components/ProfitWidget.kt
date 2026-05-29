package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.vo.netease.user.OverviewVO
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_arrow_down
import mcdevmanagermpr.shared.generated.resources.ic_arrow_up
import mcdevmanagermpr.shared.generated.resources.ic_diamond
import mcdevmanagermpr.shared.generated.resources.ic_download
import org.jetbrains.compose.resources.painterResource

@Composable
fun ProfitWidget(
    overview: NetworkState<OverviewVO>?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val data = (overview as? NetworkState.Success)?.data

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfitSmallCard(
                title = "本月钻石",
                value = data?.thisMonthDiamond,
                diff = data?.monthDiamondDiff,
                icon = Res.drawable.ic_diamond,
                isLoading = isLoading,
                modifier = Modifier.weight(1f)
            )
            ProfitSmallCard(
                title = "本月下载",
                value = data?.thisMonthDownload,
                diff = data?.monthDownloadDiff,
                icon = Res.drawable.ic_download,
                isLoading = isLoading,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfitSmallCard(
                title = "昨日钻石",
                value = data?.yesterdayDiamond,
                diff = data?.dayDiamondDiff,
                icon = Res.drawable.ic_diamond,
                isLoading = isLoading,
                modifier = Modifier.weight(1f)
            )
            ProfitSmallCard(
                title = "昨日下载",
                value = data?.yesterdayDownload,
                diff = data?.dayDownloadDiff,
                icon = Res.drawable.ic_download,
                isLoading = isLoading,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ProfitSmallCard(
    title: String,
    value: Int?,
    diff: Int?,
    icon: org.jetbrains.compose.resources.DrawableResource,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface,
            contentColor = colors.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = value?.toString() ?: "--",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            if (diff != null && diff != 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(
                            if (diff > 0) Res.drawable.ic_arrow_up else Res.drawable.ic_arrow_down
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${if (diff > 0) "+" else ""}$diff",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (diff > 0) colors.success else colors.danger,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }
        }
    }
}
