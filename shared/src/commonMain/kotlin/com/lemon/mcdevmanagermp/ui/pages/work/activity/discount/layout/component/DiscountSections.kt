package com.lemon.mcdevmanagermp.ui.pages.work.activity.discount.layout.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.consts.enums.ActivityStatusEnum
import com.lemon.mcdevmanagermp.data.consts.enums.priceTypeLabel
import com.lemon.mcdevmanagermp.data.vo.netease.activity.DiscountActivityVO
import com.lemon.mcdevmanagermp.ui.components.RichHtmlText
import com.lemon.mcdevmanagermp.ui.pages.work.activity.discount.DiscountActivityAction
import com.lemon.mcdevmanagermp.ui.pages.work.activity.discount.DiscountActivityState
import com.lemon.mcdevmanagermp.ui.pages.work.activity.layout.ActivityStatusTag
import com.lemon.mcdevmanagermp.ui.theme.AppColors
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import com.lemon.mcdevmanagermp.utils.extension.toDateString
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_add
import org.jetbrains.compose.resources.painterResource

/**
 * 折扣特卖页面共享组件集合。
 * 三个宽度布局（Compact/Medium/Expanded）统一调用这些组件，差异仅在排列（列数与 weight），
 * 以保证视觉一致并避免重复实现。
 */

// ==================== 加载态 ====================

@Composable
internal fun DiscountLoadingBox(modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = colors.primary,
            strokeWidth = 3.dp
        )
    }
}

/**
 * 无活动数据时的占位态。
 */
@Composable
internal fun DiscountEmptyState(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    message: String = "暂无进行中的折扣特卖"
) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textColor
        )
        Spacer(Modifier.height(12.dp))
        FilledTonalButton(onClick = onRefresh) {
            Text(
                text = "刷新",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ==================== 活动信息头 ====================

/**
 * 活动信息头：名称 + 状态 + 时间/赛道摘要（无横幅，纯信息卡）。
 */
@Composable
internal fun DiscountInfoHeader(
    activity: DiscountActivityVO,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceContainerHigh)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 标题行：名称 + 状态标签
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = activity.name.ifEmpty { "折扣特卖" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.textColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (activity.status.isNotEmpty()) {
                Spacer(Modifier.width(8.dp))
                ActivityStatusTag(status = activity.status)
            }
        }

        // 时间/赛道摘要
        InfoChipRow(label = "活动时间", value = formatTimeRange(activity.beginAt, activity.endAt))
        if (activity.applyEndAt > 0) {
            InfoChipRow(label = "报名截止", value = activity.applyEndAt.toLong().toDateString())
        }
        if (activity.modules.isNotEmpty()) {
            InfoChipRow(label = "活动赛道", value = "共 ${activity.modules.size} 个赛道")
        }
    }
}

/**
 * 活动描述 + 活动说明。
 */
@Composable
internal fun DiscountDescCard(
    activity: DiscountActivityVO,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val hasDesc = activity.activityDescription.isNotEmpty()
    val hasInstr = activity.activityInstruction.isNotEmpty()
    if (!hasDesc && !hasInstr) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceContainerHigh)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (hasDesc) {
            SectionHtmlText(
                title = "活动描述",
                html = activity.activityDescription,
                colors = colors
            )
        }
        if (hasInstr) {
            SectionHtmlText(
                title = "活动说明",
                html = activity.activityInstruction,
                colors = colors
            )
        }
    }
}

/**
 * 分区规则卡。
 */
@Composable
internal fun DiscountPartitionRulesCard(
    activity: DiscountActivityVO,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    if (activity.partition.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceContainerHigh)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "分区规则",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = colors.textColor
        )
        activity.partition.forEach { p ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = p.partitionName.ifEmpty { "分区" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = colors.primary
                )
                if (p.partitionRule.isNotEmpty()) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = p.partitionRule,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ==================== 赛道选择 ====================

/**
 * 多赛道显示 Tab，单赛道显示标题。
 */
@Composable
internal fun DiscountModuleSelector(
    state: DiscountActivityState,
    onAction: (DiscountActivityAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val modules = state.activity?.modules ?: return
    if (modules.size > 1) {
        DiscountModuleTabRow(
            modules = modules.map { it.moduleName to it.moduleId },
            selectedModuleId = state.selectedModuleId,
            onModuleSelected = { onAction(DiscountActivityAction.SelectModule(it)) },
            modifier = modifier
        )
    } else {
        val colors = LocalAppColors.current
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceContainerHigh)
                .padding(14.dp)
        ) {
            Text(
                text = "赛道: ${modules.first().moduleName}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colors.textColor
            )
        }
    }
}

/**
 * 多赛道切换 Tab 行。
 */
@Composable
private fun DiscountModuleTabRow(
    modules: List<Pair<String, String>>,
    selectedModuleId: String?,
    onModuleSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val selectedIndex = modules.indexOfFirst { it.second == selectedModuleId }.coerceAtLeast(0)

    SecondaryTabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier.clip(RoundedCornerShape(12.dp)),
        containerColor = colors.surfaceContainerHigh,
        contentColor = colors.textColor,
        indicator = {
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(selectedIndex),
                color = colors.primary,
                height = 3.dp
            )
        }
    ) {
        modules.forEachIndexed { index, (name, id) ->
            Tab(
                selected = index == selectedIndex,
                onClick = { onModuleSelected(id) },
                text = {
                    Text(
                        text = name,
                        fontWeight = if (index == selectedIndex) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (index == selectedIndex) colors.primary else colors.onSurfaceVariant
                    )
                }
            )
        }
    }
}

// ==================== 已参加项目 ====================

/**
 * 当前赛道已参加的折扣项目列表（可取消）。
 */
@Composable
internal fun DiscountJoinedSection(
    state: DiscountActivityState,
    onAction: (DiscountActivityAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val items = state.joinedItems
    if (items.isEmpty()) return
    var pendingCancel by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceContainerHigh)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "已参加 (${items.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = colors.textColor
        )
        items.forEach { item ->
            // 仅审核中（或未知状态）可撤销；已通过/已拒绝不可撤销
            val statusEnum = ActivityStatusEnum.fromValue(item.discountActivityStatus)
            val cancellable = !state.isSubmitting &&
                    statusEnum != ActivityStatusEnum.APPROVED &&
                    statusEnum != ActivityStatusEnum.REJECTED
            JoinedItemRow(
                name = item.itemName,
                price = item.price,
                priceUnit = priceTypeLabel(item.priceType),
                discount = item.discountActivityDiscount,
                status = item.discountActivityStatus,
                canCancel = cancellable,
                onCancel = { pendingCancel = item.itemId },
                colors = colors
            )
        }
    }

    // 撤销确认对话框
    pendingCancel?.let { itemId ->
        val target = items.find { it.itemId == itemId }
        AlertDialog(
            onDismissRequest = { pendingCancel = null },
            title = { Text("撤销参与") },
            text = {
                Text(
                    text = "确定撤销《${target?.itemName ?: "该作品"}》的折扣特卖参与申请？撤销后可重新提交。",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(onClick = {
                    onAction(DiscountActivityAction.CancelJoin(itemId))
                    pendingCancel = null
                }) { Text("撤销") }
            },
            dismissButton = {
                TextButton(onClick = { pendingCancel = null }) { Text("取消") }
            }
        )
    }
}

// ==================== 候选项目 + 参与表单 ====================

/**
 * 候选项目多选列表 + 参与表单（折扣/分区/简介/提交）。
 * 三种宽度布局统一调用，保证参与流程一致。
 */
@Composable
internal fun DiscountCandidateSection(
    state: DiscountActivityState,
    onAction: (DiscountActivityAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "选择参与作品",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = colors.textColor
        )

        if (state.isLoadingModule) {
            DiscountLoadingBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )
        } else if (state.candidates.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceContainerHigh)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "暂无可参与的作品",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.candidates.forEach { candidate ->
                    CandidateItemRow(
                        name = candidate.itemName,
                        price = candidate.price,
                        priceUnit = priceTypeLabel(candidate.priceType),
                        discount = state.discount,
                        isSelected = candidate.itemId in state.selectedItemIds,
                        enabled = !state.isSubmitting,
                        onToggle = { onAction(DiscountActivityAction.ToggleItem(candidate.itemId)) },
                        colors = colors
                    )
                }
            }
        }

        // 参与表单（仅在候选加载完成且有候选/已参加时呈现，避免空表单）
        if (!state.isLoadingModule) {
            DiscountJoinForm(state = state, onAction = onAction)
        }
    }
}

/**
 * 参与表单：折扣力度 + 分区选择 + 简介 + 提交按钮。
 * 折扣取值受当前赛道 minDiscount/maxDiscount 约束；分区按赛道类型过滤。
 * ponytail: 折扣值语义（百分比/具体折扣）以后端为准，这里按"百分比"展示，升级路径：拿到准确语义后调整展示文案。
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun DiscountJoinForm(
    state: DiscountActivityState,
    onAction: (DiscountActivityAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val module = state.currentModule
    val partitions = state.availablePartitions
    val canSubmit = state.selectedItemIds.isNotEmpty() && !state.isSubmitting

    var discountText by remember { mutableStateOf(state.discount.toString()) }
    // 赛道数据加载完成（isLoadingModule 由 true→false）时同步输入框为最新 discount
    LaunchedEffect(state.selectedModuleId, state.isLoadingModule) {
        if (!state.isLoadingModule) discountText = state.discount.toString()
    }
    // 输入框失焦时立刻钳制到上下限并同步显示
    val discountFocus = remember { MutableInteractionSource() }
    val discountFocused by discountFocus.collectIsFocusedAsState()
    LaunchedEffect(discountFocused) {
        if (!discountFocused) {
            val m = state.currentModule
            if (m != null) {
                val clamped = state.discount.coerceIn(m.minDiscount, m.maxDiscount)
                if (clamped.toString() != discountText) discountText = clamped.toString()
                if (clamped != state.discount) onAction(
                    DiscountActivityAction.UpdateDiscount(
                        clamped
                    )
                )
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceContainerHigh)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "参与设置",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = colors.textColor
        )

        // 折扣力度（数字输入）
        if (module != null) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "折扣力度",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = colors.textColor,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "范围 ${module.minDiscount}% ~ ${module.maxDiscount}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant
                    )
                }
                OutlinedTextField(
                    value = discountText,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() }.take(3)
                        discountText = filtered
                        onAction(DiscountActivityAction.UpdateDiscount(filtered.toIntOrNull() ?: 0))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    interactionSource = discountFocus,
                    suffix = { Text("%") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.outlineVariant,
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface
                    )
                )
            }
        }

        // 分区选择
        if (partitions.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "选择分区（可选）",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = colors.textColor
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    partitions.forEach { p ->
                        FilterChip(
                            selected = state.selectedPartitionId == p.partitionId,
                            onClick = {
                                onAction(DiscountActivityAction.SelectPartition(p.partitionId))
                            },
                            label = { Text(p.partitionName.ifEmpty { "分区" }) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primary.copy(alpha = 0.12f),
                                selectedLabelColor = colors.primary
                            ),
                            elevation = FilterChipDefaults.filterChipElevation(
                                elevation = 0.dp,
                                pressedElevation = 0.dp,
                                hoveredElevation = 0.dp,
                                focusedElevation = 0.dp
                            )
                        )
                    }
                }
            }
        }

        // 参与简介
        OutlinedTextField(
            value = state.intro,
            onValueChange = { onAction(DiscountActivityAction.UpdateIntro(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            placeholder = {
                Text(
                    text = "参与简介（可选）",
                    color = colors.onSurfaceVariant.copy(alpha = 0.5f)
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.outlineVariant,
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface
            )
        )

        // 提交按钮
        FilledTonalButton(
            onClick = { onAction(DiscountActivityAction.Submit) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = canSubmit
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = colors.primary,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
            } else {
                Icon(
                    painter = painterResource(Res.drawable.ic_add),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = if (state.isSubmitting) "提交中..." else "提交参与",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ==================== 内部小组件 ====================

@Composable
private fun SectionHtmlText(
    title: String,
    html: String,
    colors: AppColors
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = colors.textColor
        )
        Spacer(Modifier.height(4.dp))
        RichHtmlText(
            html = html,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoChipRow(label: String, value: String) {
    val colors = LocalAppColors.current
    if (value.isBlank()) return
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
private fun JoinedItemRow(
    name: String,
    price: Int,
    priceUnit: String,
    discount: Int,
    status: String,
    canCancel: Boolean,
    onCancel: () -> Unit,
    colors: AppColors
) {
    val statusEnum = ActivityStatusEnum.fromValue(status)
    val statusLabel = statusEnum?.label ?: status
    val statusColor = when (statusEnum) {
        ActivityStatusEnum.REVIEWING -> colors.tertiary
        ActivityStatusEnum.APPROVED -> colors.primary
        ActivityStatusEnum.REJECTED -> colors.error
        null -> colors.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surfaceContainerLow)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colors.textColor,
                maxLines = 1
            )
            DiscountPriceRow(
                price = price,
                priceUnit = priceUnit,
                discount = discount,
                colors = colors
            )
        }
        if (statusLabel.isNotEmpty()) {
            StatusPill(text = statusLabel, color = statusColor, colors = colors)
            Spacer(Modifier.width(8.dp))
        }
        // 仅审核中/未知状态显示撤销入口；已通过/已拒绝只展示状态
        if (canCancel) {
            Text(
                text = "撤销",
                style = MaterialTheme.typography.labelMedium,
                color = colors.error,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.error.copy(alpha = 0.1f))
                    .clickable(onClick = onCancel)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

/**
 * 价格行：免费 / 原价 / 原价划线 + 折后价（折后价高亮）。
 * 准确语义以后端为准，若实为"减免百分比"，把公式改为 price*(100-discount)/100。
 */
@Composable
private fun DiscountPriceRow(
    price: Int,
    priceUnit: String,
    discount: Int,
    colors: AppColors
) {
    when {
        price <= 0 -> Text(
            text = "免费",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )

        discount > 0 -> {
            val finalPrice = price * discount / 100
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$price",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    textDecoration = TextDecoration.LineThrough
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "$finalPrice $priceUnit",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        else -> Text(
            text = "$price $priceUnit",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
    }
}

@Composable
private fun CandidateItemRow(
    name: String,
    price: Int,
    priceUnit: String,
    discount: Int,
    isSelected: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
    colors: AppColors
) {
    val interactionSource = rememberMutableInteractionSource()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) colors.primary.copy(alpha = 0.08f) else colors.surfaceContainerHigh)
            .then(
                if (isSelected) Modifier.border(
                    1.dp,
                    colors.primary.copy(alpha = 0.3f),
                    RoundedCornerShape(12.dp)
                )
                else Modifier
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onToggle
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() },
            enabled = enabled,
            colors = androidx.compose.material3.CheckboxDefaults.colors(
                checkedColor = colors.primary,
                uncheckedColor = colors.onSurfaceVariant
            )
        )
        Spacer(Modifier.width(4.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colors.textColor,
                maxLines = 1
            )
            DiscountPriceRow(
                price = price,
                priceUnit = priceUnit,
                discount = discount,
                colors = colors
            )
        }
    }
}

@Composable
private fun StatusPill(text: String, color: Color, colors: AppColors) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
private fun rememberMutableInteractionSource(): MutableInteractionSource {
    return androidx.compose.runtime.remember { MutableInteractionSource() }
}

// ==================== 工具函数 ====================


private fun formatTimeRange(beginAt: Int, endAt: Int): String {
    if (beginAt == 0 && endAt == 0) return ""
    val begin = if (beginAt > 0) beginAt.toLong().toDateString() else "未知"
    val end = if (endAt > 0) endAt.toLong().toDateString() else "未知"
    return "$begin ~ $end"
}
