package com.lemon.mcdevmanagermp.ui.pages.work.activity.detail.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.consts.enums.ActivityStatusEnum
import com.lemon.mcdevmanagermp.data.consts.enums.priceTypeLabel
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ActivityItemVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ActivityReviewItemVO
import com.lemon.mcdevmanagermp.ui.theme.AppColors
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

/**
 * 模组状态标签颜色
 */
private val ActivityItemVO.statusLabel: String
    get() = ActivityStatusEnum.fromValue(status)?.label ?: status

/**
 * 模组列表展示区域 - 在活动详情页中显示审核中/已通过/已拒绝的模组
 */
@Composable
internal fun ModItemsSection(
    activity: ActivityReviewItemVO,
    reviewingItems: Map<Int, List<ActivityItemVO>>,
    approvedItems: Map<Int, List<ActivityItemVO>>,
    rejectedItems: Map<Int, List<ActivityItemVO>>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    // 没有数据且不在加载中时不显示
    if (!isLoading && reviewingItems.isEmpty() && approvedItems.isEmpty() && rejectedItems.isEmpty()) {
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceContainerHigh)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "我的作品",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = colors.textColor
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = colors.primary,
                    strokeWidth = 2.dp
                )
            }
        } else {
            // 按赛道展示
            activity.modules.forEach { module ->
                val moduleReviewing = reviewingItems[module.moduleId].orEmpty()
                val moduleApproved = approvedItems[module.moduleId].orEmpty()
                val moduleRejected = rejectedItems[module.moduleId].orEmpty()
                val allModuleItems = moduleReviewing + moduleApproved + moduleRejected

                if (allModuleItems.isEmpty()) return@forEach

                // 赛道标题（多赛道时显示）
                if (activity.modules.size > 1) {
                    Text(
                        text = module.moduleName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = colors.primary
                    )
                }

                // 审核中的模组
                if (moduleReviewing.isNotEmpty()) {
                    ModStatusGroup(
                        label = "审核中",
                        labelColor = colors.tertiary,
                        items = moduleReviewing,
                        colors = colors
                    )
                }

                // 已通过的模组
                if (moduleApproved.isNotEmpty()) {
                    ModStatusGroup(
                        label = "已通过",
                        labelColor = colors.primary,
                        items = moduleApproved,
                        colors = colors
                    )
                }

                // 已拒绝的模组
                if (moduleRejected.isNotEmpty()) {
                    ModStatusGroup(
                        label = "已拒绝",
                        labelColor = colors.error,
                        items = moduleRejected,
                        colors = colors
                    )
                }
            }
        }
    }
}

/**
 * 同一状态的模组分组
 */
@Composable
private fun ModStatusGroup(
    label: String,
    labelColor: androidx.compose.ui.graphics.Color,
    items: List<ActivityItemVO>,
    colors: AppColors
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // 状态标签
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(labelColor)
            )
            Text(
                text = "$label (${items.size})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = labelColor
            )
        }

        // 模组卡片列表
        items.forEach { item ->
            ModItemCard(
                item = item,
                colors = colors
            )
        }
    }
}

/**
 * 单个模组卡片
 */
@Composable
private fun ModItemCard(
    item: ActivityItemVO,
    colors: AppColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surfaceContainerLow)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 模组名称
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.itemName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colors.textColor,
                maxLines = 1
            )
            // 价格信息
            if (item.price > 0) {
                Text(
                    text = "${item.price} ${item.priceTypeName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        // 状态标签
        val statusColor = when (ActivityStatusEnum.fromValue(item.status)) {
            ActivityStatusEnum.REVIEWING -> colors.tertiary
            ActivityStatusEnum.APPROVED -> colors.primary
            ActivityStatusEnum.REJECTED -> colors.error
            null -> colors.onSurfaceVariant
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(statusColor.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = item.statusLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = statusColor
            )
        }
    }
}

private val ActivityItemVO.priceTypeName: String
    get() = priceTypeLabel(priceType)
