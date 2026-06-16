package com.lemon.mcdevmanagermp.ui.pages.work.activity.detail.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ComposableImageOptions
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ReviewActivityItemVO
import com.lemon.mcdevmanagermp.ui.components.CollapsingTopBar
import com.lemon.mcdevmanagermp.ui.components.RichHtmlText
import com.lemon.mcdevmanagermp.ui.pages.work.activity.detail.ActivityDetailState
import com.lemon.mcdevmanagermp.ui.pages.work.activity.layout.ActivityStatusTag
import com.lemon.mcdevmanagermp.ui.pages.work.activity.layout.formatTimeRange
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_add
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun ActivityDetailMediumLayout(
    state: ActivityDetailState,
    onBack: () -> Unit,
    showTopBar: Boolean = true,
    onParticipate: (ReviewActivityItemVO) -> Unit = {}
) {
    val colors = LocalAppColors.current
    val activity = state.activity ?: return
    val scrollState = rememberScrollState()
    val scrollAlpha = remember {
        derivedStateOf { (scrollState.value / 100f).coerceIn(0f, 1f) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (showTopBar) {
            CollapsingTopBar(
                title = activity.name,
                collapseFraction = scrollAlpha.value,
                onBack = onBack
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 上方：Banner + 信息卡片 双列
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 左列：Banner
                if (activity.banner.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        AsyncImage(
                            uri = activity.banner,
                            state = rememberAsyncImageState(ComposableImageOptions {
                                crossfade()
                            }),
                            contentDescription = activity.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // 渐变遮罩
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.5f)
                                        )
                                    )
                                )
                        )

                        // 状态标签
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                        ) {
                            ActivityStatusTag(status = activity.statusTag)
                        }
                    }
                }

                // 右列：信息卡片
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 无 Banner 时显示标题
                    if (activity.banner.isEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activity.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = colors.textColor,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            ActivityStatusTag(status = activity.statusTag)
                        }
                    }

                    // 信息面板
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.surfaceContainerHigh)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (activity.banner.isNotEmpty()) {
                            Text(
                                text = activity.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textColor,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        val timeRange = formatTimeRange(activity.beginAt, activity.endAt)
                        if (timeRange.isNotEmpty()) {
                            InfoChipRow(label = "活动时间", value = timeRange)
                        }

                        if (activity.modules.isNotEmpty()) {
                            InfoChipRow(
                                label = "活动赛道",
                                value = "共 ${activity.modules.size} 个赛道"
                            )
                        }
                    }

                    // 参与按钮
                    FilledTonalButton(
                        onClick = { onParticipate(activity) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_add),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "参与活动",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // 下方：描述 + 规则
            if (activity.desc.isNotEmpty()) {
                SectionCard(
                    title = "活动描述",
                    content = {
                        RichHtmlText(
                            html = activity.desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant
                        )
                    },
                    colors = colors
                )
            }

            if (activity.instruction.isNotEmpty()) {
                SectionCard(
                    title = "活动规则",
                    content = {
                        RichHtmlText(
                            html = activity.instruction,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant
                        )
                    },
                    colors = colors
                )
            }

            // 赛道摘要
            if (activity.modules.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceContainerHigh)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "赛道信息",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textColor
                    )
                    activity.modules.forEach { module ->
                        Column {
                            Text(
                                text = module.moduleName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = colors.textColor
                            )
                            if (module.moduleDescription.isNotEmpty()) {
                                RichHtmlText(
                                    html = module.moduleDescription,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 我的作品（审核中/已通过模组）
            ModItemsSection(
                activity = activity,
                reviewingItems = state.reviewingItems,
                approvedItems = state.approvedItems,
                rejectedItems = state.rejectedItems,
                isLoading = state.isLoadingModules
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InfoChipRow(label: String, value: String) {
    val colors = LocalAppColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(colors.primary.copy(alpha = 0.08f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = colors.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit,
    colors: com.lemon.mcdevmanagermp.ui.theme.AppColors
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceContainerHigh)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = colors.textColor
        )
        Spacer(Modifier.height(8.dp))
        content()
    }
}
