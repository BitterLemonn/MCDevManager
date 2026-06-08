package com.lemon.mcdevmanagermp.ui.pages.analyze.realtimeProfit

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.dto.netease.income.OneResRealtimeIncomeVO
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_diamond
import mcdevmanagermpr.shared.generated.resources.ic_emerald
import mcdevmanagermpr.shared.generated.resources.ic_mod
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun RealtimeProfitItemCard(
    name: String,
    iid: String,
    data: OneResRealtimeIncomeVO,
    isHovered: Boolean = false,
    isSelected: Boolean = false
) {
    val colors = LocalAppColors.current

    val showDiamond = data.totalDiamonds > 0 || (data.totalDiamonds == 0 && data.totalPoints == 0)
    val showPoint = data.totalPoints > 0 || (data.totalDiamonds == 0 && data.totalPoints == 0)

    val cardBgColor = when {
        isSelected -> colors.primary.copy(alpha = 0.08f)
        isHovered -> colors.primary.copy(alpha = 0.06f)
        else -> colors.surfaceContainerHigh
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_mod),
                    contentDescription = "mod",
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = colors.textColor
                    )
                    Text(
                        text = iid,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showDiamond) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "${data.totalDiamonds}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textColor
                            )
                            Spacer(Modifier.width(4.dp))
                            Image(
                                painter = painterResource(Res.drawable.ic_diamond),
                                contentDescription = "diamond",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (showPoint) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "${data.totalPoints}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textColor
                            )
                            Spacer(Modifier.width(4.dp))
                            Image(
                                painter = painterResource(Res.drawable.ic_emerald),
                                contentDescription = "emerald",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 如果有订单详情，展示订单数量
            if (data.count > 0) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "共 ${data.count} 笔订单",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}
