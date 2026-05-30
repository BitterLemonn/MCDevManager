package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import com.lemon.mcdevmanagermp.utils.ProfitData
import com.lemon.mcdevmanagermp.utils.extension.formatDecimal
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_money
import org.jetbrains.compose.resources.painterResource

@Composable
fun ProfitCard(
    title: String,
    profitData: ProfitData,
    isLoading: Boolean = true,
    expanded: Boolean = false,
    onToggleExpand: () -> Unit = {}
) {
    val colors = LocalAppColors.current

    if (!isLoading) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggleExpand
                )
                .animateContentSize(),
            colors = CardDefaults.elevatedCardColors(containerColor = colors.surfaceContainerHigh),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(Res.drawable.ic_money),
                        contentDescription = "money",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textColor
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = profitData.totalProfit.formatDecimal(2),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                        Text(
                            text = "含扣税 ${(profitData.totalProfit - getTaxMoney(profitData)).formatDecimal(2)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .padding(bottom = 12.dp)
                            .heightIn(max = 280.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailSection(
                            color = colors.surfaceContainerHighest,
                            rows = listOf(
                                "月总流水" to "${profitData.sumProfit.toInt()}",
                                "开发者分成" to "${profitData.developerProfit.toInt()}"
                            )
                        )

                        if (profitData.subsidyProfit.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colors.surfaceContainerHighest, RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "模组激励",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                profitData.subsidyProfit.entries.forEach { (name, value) ->
                                    DetailRow(
                                        label = name,
                                        value = "${value.toInt()}",
                                        labelColor = colors.onSurfaceVariant,
                                        valueColor = colors.textColor
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DetailSection(
                                modifier = Modifier.weight(1f),
                                color = colors.surfaceContainerHighest,
                                rows = listOf(
                                    "流水激励" to "${profitData.profitSubsidy.toInt()}"
                                )
                            )
                            DetailSection(
                                modifier = Modifier.weight(1f),
                                color = colors.surfaceContainerHighest,
                                rows = listOf(
                                    "分成返还" to "${(profitData.subsidyPercent * 100).toInt()}%"
                                )
                            )
                        }
                    }
                }
            }
        }
    } else {
        ShimmerProfitCard()
    }
}

@Composable
private fun ShimmerProfitCard() {
    val colors = LocalAppColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )
    val shimmerColors = listOf(
        colors.shimmer,
        colors.shimmer.copy(alpha = 0.3f),
        colors.shimmer
    )

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = colors.surfaceContainerHigh),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Brush.linearGradient(shimmerColors, start = androidx.compose.ui.geometry.Offset(shimmerOffset, 0f), end = androidx.compose.ui.geometry.Offset(shimmerOffset + 300f, 0f)))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Brush.linearGradient(shimmerColors, start = androidx.compose.ui.geometry.Offset(shimmerOffset, 0f), end = androidx.compose.ui.geometry.Offset(shimmerOffset + 300f, 0f)))
            )
            Spacer(modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Brush.linearGradient(shimmerColors, start = androidx.compose.ui.geometry.Offset(shimmerOffset, 0f), end = androidx.compose.ui.geometry.Offset(shimmerOffset + 300f, 0f)))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Brush.linearGradient(shimmerColors, start = androidx.compose.ui.geometry.Offset(shimmerOffset, 0f), end = androidx.compose.ui.geometry.Offset(shimmerOffset + 300f, 0f)))
                )
            }
        }
    }
}

@Composable
private fun DetailSection(
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color,
    rows: List<Pair<String, String>>
) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier
            .background(color, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        rows.forEach { (label, value) ->
            DetailRow(
                label = label,
                value = value,
                labelColor = colors.onSurfaceVariant,
                valueColor = colors.textColor
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    labelColor: androidx.compose.ui.graphics.Color,
    valueColor: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = labelColor
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

private fun getTaxMoney(profitData: ProfitData): Double {
    val realMoney = profitData.totalProfit
    return when {
        realMoney < 800 -> 0.0
        realMoney < 4000 -> (realMoney - 800) * 0.2
        else -> (realMoney * 0.8) * 0.2
    }
}
