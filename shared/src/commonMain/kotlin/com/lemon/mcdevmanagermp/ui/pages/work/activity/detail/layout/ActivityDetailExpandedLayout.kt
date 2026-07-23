package com.lemon.mcdevmanagermp.ui.pages.work.activity.detail.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ActivityReviewItemVO
import com.lemon.mcdevmanagermp.ui.components.CollapsingTopBar
import com.lemon.mcdevmanagermp.ui.components.RichHtmlText
import com.lemon.mcdevmanagermp.ui.pages.work.activity.detail.ActivityDetailState
import com.lemon.mcdevmanagermp.ui.pages.work.activity.layout.ActivityStatusTag
import com.lemon.mcdevmanagermp.ui.pages.work.activity.layout.formatTimeRange
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import com.lemon.mcdevmanagermp.utils.extension.toDateString

@Composable
internal fun ActivityDetailExpandedLayout(
    state: ActivityDetailState,
    onBack: () -> Unit,
    showTopBar: Boolean = true,
    onParticipate: (ActivityReviewItemVO) -> Unit = {}
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

        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 左列（约 40%）
            Column(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Banner
                if (activity.banner.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        AsyncImage(
                            uri = activity.banner,
                            state = rememberAsyncImageState(ComposableImageOptions {
                                crossfade()
                                sizeMultiplier(2f)
                            }),
                            contentDescription = activity.name,
                            modifier = Modifier.fillMaxWidth()
                                .height(320.dp),
                            contentScale = ContentScale.Crop
                        )

                        // 渐变遮罩
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.6f)
                                        )
                                    )
                                )
                        )

                        // 状态标签
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                        ) {
                            ActivityStatusTag(status = activity.statusTag)
                        }

                        // 底部活动名称浮层
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = activity.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // 活动描述
                if (activity.desc.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.surfaceContainerHigh)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "活动描述",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textColor
                        )
                        Spacer(Modifier.height(8.dp))
                        RichHtmlText(
                            html = activity.desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant
                        )
                    }
                }

            }

            // 右列：信息面板（约 60%）
            Column(
                modifier = Modifier
                    .weight(3f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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

                // 时间信息
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceContainerHigh)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val timeRange = formatTimeRange(activity.beginAt, activity.endAt)
                    if (timeRange.isNotEmpty()) {
                        InfoChipRow(label = "活动时间", value = timeRange)
                    }

                    if (activity.applyEndAt > 0) {
                        InfoChipRow(
                            label = "报名截止",
                            value = activity.applyEndAt.toLong().toDateString()
                        )
                    }

                    if (activity.modules.isNotEmpty()) {
                        InfoChipRow(
                            label = "活动分区",
                            value = "共 ${activity.modules.size} 个分区"
                        )
                    }
                }

                // 活动规则
                if (activity.instruction.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.surfaceContainerHigh)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "活动规则",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textColor
                        )
                        Spacer(Modifier.height(8.dp))
                        RichHtmlText(
                            html = activity.instruction,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant
                        )
                    }
                }

                // 分区信息
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
                            text = "分区信息",
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

                // 参与活动按钮
                FilledTonalButton(
                    onClick = { onParticipate(activity) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "参与活动",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(8.dp))
            }
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

