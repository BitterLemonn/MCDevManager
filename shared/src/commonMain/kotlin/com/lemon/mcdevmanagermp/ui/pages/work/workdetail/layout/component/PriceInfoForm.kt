package com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.consts.enums.PriceRankEnum
import com.lemon.mcdevmanagermp.data.consts.enums.PriceTypeEnum
import com.lemon.mcdevmanagermp.ui.components.AppDatePickerDialog
import com.lemon.mcdevmanagermp.ui.components.FieldLabel
import com.lemon.mcdevmanagermp.ui.components.FormSection
import com.lemon.mcdevmanagermp.ui.components.OptionChips
import com.lemon.mcdevmanagermp.ui.components.ReadOnlyField
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.DiscountConfig
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailAction
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailState
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/** 钻石档位胶囊选项：(显示文案, 档位枚举)。文案用纯钻石数，紧凑排列。 */
private val DIAMOND_RANK_OPTIONS = listOf(
    "300" to PriceRankEnum.DIAMOND_TIER_ONE,
    "600" to PriceRankEnum.DIAMOND_TIER_TWO,
    "1000" to PriceRankEnum.DIAMOND_TIER_THREE,
    "2000" to PriceRankEnum.DIAMOND_TIER_FOUR,
    "5000" to PriceRankEnum.DIAMOND_TIER_FIVE,
    "10000" to PriceRankEnum.DIAMOND_TIER_SIX,
    "20000" to PriceRankEnum.DIAMOND_TIER_SEVEN
)

/**
 * 定价区块表单。三档布局共用，内部胶囊组([OptionChips])自带 FlowRow 自适应换行，
 * 天然适配手机/平板/桌面不同宽度。
 *
 * 联动：定价类型 → 是否显示档位 / 价格输入方式 / 折扣区可见性，均由 [WorkDetailState] 驱动。
 */
@Composable
internal fun PriceInfoForm(
    state: WorkDetailState,
    onAction: (WorkDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDiamond = state.priceType == PriceTypeEnum.DIAMOND
    val isEmerald = state.priceType == PriceTypeEnum.EMERALD
    val isFree = state.priceType == PriceTypeEnum.FREE
    // 折扣仅「钻石 + 二档及以上」可编辑（与 ViewModel.canEditDiscount 保持一致）
    val canEditDiscount = isDiamond && state.priceRank.type >= 1

    FormSection(title = "定价", modifier = modifier) {
        // 定价类型（必填）
        OptionChips(
            label = "定价类型",
            options = listOf(
                "钻石" to PriceTypeEnum.DIAMOND,
                "绿宝石" to PriceTypeEnum.EMERALD,
                "免费" to PriceTypeEnum.FREE
            ),
            selected = state.priceType.takeIf { it != PriceTypeEnum.UNKNOWN },
            onSelect = { onAction(WorkDetailAction.ChangePriceType(it)) },
            required = true
        )

        // 定价档位（仅钻石）
        if (isDiamond) {
            OptionChips(
                label = "定价档位",
                options = DIAMOND_RANK_OPTIONS,
                selected = state.priceRank.takeIf { it.type in 0..6 },
                onSelect = { onAction(WorkDetailAction.ChangePriceRank(it)) }
            )
        }

        // 定价：钻石由档位决定（只读），绿宝石可输入，免费只读
        when {
            isDiamond -> ReadOnlyField(
                label = "定价",
                value = "${state.priceRank.diamondPrice} 钻石"
            )

            isEmerald -> OutlinedTextField(
                value = state.emeraldPrice.toString(),
                onValueChange = { raw ->
                    val digits = raw.filter { it.isDigit() }.take(9)
                    onAction(WorkDetailAction.UpdateEmeraldPrice(digits.toIntOrNull() ?: 0))
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("定价") },
                suffix = { Text("绿宝石") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            isFree -> ReadOnlyField(label = "定价", value = "免费")

            else -> ReadOnlyField(label = "定价", value = "—")
        }

        // 折扣（仅钻石二档及以上）
        if (canEditDiscount) {
            Spacer(Modifier.height(4.dp))
            DiscountSection(
                discounts = state.discounts,
                onChangePercent = { i, p ->
                    onAction(
                        WorkDetailAction.UpdateDiscountPercent(
                            i,
                            p
                        )
                    )
                },
                onChangeBegin = { i, d -> onAction(WorkDetailAction.UpdateDiscountBegin(i, d)) },
                onChangeEnd = { i, d -> onAction(WorkDetailAction.UpdateDiscountEnd(i, d)) },
                onAdd = { onAction(WorkDetailAction.AddDiscount) },
                onRemove = { i -> onAction(WorkDetailAction.RemoveDiscount(i)) }
            )
        }
    }
}

/**
 * 折扣子区：折扣说明 + 折扣列表 + 添加按钮。
 */
@Composable
private fun DiscountSection(
    discounts: List<DiscountConfig>,
    onChangePercent: (Int, Int) -> Unit,
    onChangeBegin: (Int, LocalDate) -> Unit,
    onChangeEnd: (Int, LocalDate) -> Unit,
    onAdd: () -> Unit,
    onRemove: (Int) -> Unit
) {
    FieldLabel(text = "折扣")
    if (discounts.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            discounts.forEachIndexed { index, discount ->
                DiscountItem(
                    discount = discount,
                    onChangePercent = { onChangePercent(index, it) },
                    onChangeBegin = { onChangeBegin(index, it) },
                    onChangeEnd = { onChangeEnd(index, it) },
                    onRemove = { onRemove(index) }
                )
            }
        }
    }
    TextButton(onClick = onAdd) {
        Text("+ 添加折扣")
    }
}

/**
 * 单条折扣：百分比输入（本地缓存，失焦提交，保证键入流畅）+ 起止日期 + 删除。
 */
@Composable
private fun DiscountItem(
    discount: DiscountConfig,
    onChangePercent: (Int) -> Unit,
    onChangeBegin: (LocalDate) -> Unit,
    onChangeEnd: (LocalDate) -> Unit,
    onRemove: () -> Unit
) {
    val colors = LocalAppColors.current
    val focusManager = LocalFocusManager.current
    // 起始日期不得早于今天（与下方 picker 的 minDate 约束一致）
    val today = remember {
        Clock.System.now().toLocalDateTime(TimeZone.of("Asia/Shanghai")).date
    }
    var showBeginPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    // 折扣输入本地缓存：键入时仅更新本地，失焦 / 完成时 clamp 到 60..99 再 dispatch
    var percentText by remember(discount) { mutableStateOf(discount.percent.toString()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.primary.copy(alpha = 0.05f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 折扣百分比输入 + 删除
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = percentText,
                onValueChange = { raw ->
                    percentText = raw.filter { it.isDigit() }.take(2)
                },
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            val clamped = (percentText.toIntOrNull() ?: 60).coerceIn(60, 99)
                            percentText = clamped.toString()
                            if (clamped != discount.percent) onChangePercent(clamped)
                        }
                    },
                singleLine = true,
                label = { Text("折扣") },
                suffix = { Text("%") },
                supportingText = { Text("范围 60–99（6–9.9 折）") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "删除折扣",
                    tint = colors.onSurfaceVariant
                )
            }
        }
        // 起止日期
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DateField(
                label = "起始",
                date = discount.beginDate,
                modifier = Modifier.weight(1f),
                onClick = { showBeginPicker = true }
            )
            Text(
                text = "至",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
            DateField(
                label = "结束",
                date = discount.endDate,
                modifier = Modifier.weight(1f),
                onClick = { showEndPicker = true }
            )
        }
    }

    if (showBeginPicker) {
        AppDatePickerDialog(
            initialDate = discount.beginDate,
            minDate = today,
            maxDate = discount.endDate,
            onConfirm = {
                onChangeBegin(it)
                showBeginPicker = false
            },
            onDismiss = { showBeginPicker = false }
        )
    }
    if (showEndPicker) {
        AppDatePickerDialog(
            initialDate = discount.endDate,
            minDate = discount.beginDate,
            onConfirm = {
                onChangeEnd(it)
                showEndPicker = false
            },
            onDismiss = { showEndPicker = false }
        )
    }
}

/**
 * 可点击的日期展示字段：上方标签 + 下方日期（点击触发日期选择弹窗）。
 */
@Composable
private fun DateField(
    label: String,
    date: LocalDate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = date.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textColor,
            fontWeight = FontWeight.Medium
        )
    }
}
