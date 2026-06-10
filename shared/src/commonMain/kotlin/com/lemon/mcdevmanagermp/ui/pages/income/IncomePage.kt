package com.lemon.mcdevmanagermp.ui.pages.income

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.components.CollapsingTopBar
import com.lemon.mcdevmanagermp.ui.pages.income.layout.CompactIncomeLayout
import com.lemon.mcdevmanagermp.ui.pages.income.layout.ExpandedIncomeLayout
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import com.lemon.mcdevmanagermp.utils.extension.formatDecimal
import kotlinx.coroutines.launch
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_refresh
import org.jetbrains.compose.resources.painterResource

@Composable
fun IncomePage(onBack: () -> Unit) {
    val viewModel = remember { IncomeViewModel() }
    val state by viewModel.state.collectAsState()
    val colors = LocalAppColors.current
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is IncomeEffect.ShowToast -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatch(IncomeAction.LoadData)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(colors.background)) {
        val widthSizeClass = when {
            maxWidth < 600.dp -> WindowWidthSizeClass.Compact
            maxWidth < 840.dp -> WindowWidthSizeClass.Medium
            else -> WindowWidthSizeClass.Expanded
        }

        when (widthSizeClass) {
            WindowWidthSizeClass.Compact, WindowWidthSizeClass.Medium -> CompactIncomeLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack,
                statusBarTop = statusBarTop,
                navBarBottom = navBarBottom
            )

            else -> ExpandedIncomeLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack,
                statusBarTop = statusBarTop,
                navBarBottom = navBarBottom
            )
        }

        // Apply income detail dialog
        ApplyDetailOverlay(
            state = state,
            onAction = viewModel::dispatch
        )

        // Loading overlay
        if (state.isLoading && state.peList.isEmpty() && state.pcList.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center).size(36.dp),
                color = colors.primary,
                strokeWidth = 3.dp
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = navBarBottom + 8.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                shape = RoundedCornerShape(8.dp),
                containerColor = colors.surface,
                contentColor = colors.textColor
            )
        }
    }
}

@Composable
internal fun ApplyDetailOverlay(
    state: IncomeState,
    onAction: (IncomeAction) -> Unit
) {
    val colors = LocalAppColors.current

    AnimatedVisibility(
        visible = !state.isLoading && state.applyDetailList.isNotEmpty(),
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onAction(IncomeAction.DismissApplyDetail) }
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )) {
                ApplyIncomeDetailDialog(
                    detailList = state.applyDetailList,
                    onApply = { ids -> onAction(IncomeAction.ApplyIncome(ids)) }
                )
            }
        }
    }
}

// ============================================================
// Top Bar
// ============================================================

@Composable
internal fun IncomeTopBar(onBack: () -> Unit, onRefresh: () -> Unit) {
    val colors = LocalAppColors.current
    CollapsingTopBar(
        title = "收益详情",
        collapseFraction = 0f,
        onBack = onBack,
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(
                    painter = painterResource(Res.drawable.ic_refresh),
                    contentDescription = "刷新",
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    )
}

// ============================================================
// Platform Toggle (PE / PC)
// ============================================================

@Composable
internal fun PlatformToggle(
    selectedPlatform: String,
    onSelect: (String) -> Unit
) {
    val colors = LocalAppColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        ElevatedCard(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = colors.surfaceContainerHigh)
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PlatformChip(
                    label = "PE",
                    isSelected = selectedPlatform == "pe",
                    onClick = { onSelect("pe") }
                )
                PlatformChip(
                    label = "PC",
                    isSelected = selectedPlatform == "pc",
                    onClick = { onSelect("pc") }
                )
            }
        }
    }
}

@Composable
private fun PlatformChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) colors.primary else colors.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 28.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) colors.onPrimary else colors.onSurfaceVariant
        )
    }
}

// ============================================================
// Un-Extracted Income Card
// ============================================================

@Composable
internal fun UnExtractedIncomeCard(unExtractedIncome: String) {
    val colors = LocalAppColors.current

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = colors.surfaceContainerHigh),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "未提取收益",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = unExtractedIncome,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textColor
            )
        }
    }
}

// ============================================================
// Settleable Notification Banner
// ============================================================

@Composable
internal fun SettleableBanner(
    incomeName: String,
    onGetDetail: () -> Unit
) {
    val colors = LocalAppColors.current

    AnimatedVisibility(
        visible = true,
        enter = slideInHorizontally(tween(300)) { -it } + fadeIn(tween(300)),
        exit = slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(300))
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colors.primary.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "可结算收益: $incomeName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = onGetDetail,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text("开始结算", color = colors.onPrimary)
                }
            }
        }
    }
}

// ============================================================
// Income List
// ============================================================

@Composable
internal fun IncomeList(
    incomeList: List<com.lemon.mcdevmanagermp.data.vo.netease.income.IncomeVO>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    if (!isLoading && incomeList.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
            Text(
                text = "暂无收益数据",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(incomeList) { _, income ->
            IncomeItemCard(income)
        }
    }
}

// ============================================================
// Income Item Card
// ============================================================

@Composable
internal fun IncomeItemCard(data: com.lemon.mcdevmanagermp.data.vo.netease.income.IncomeVO) {
    val colors = LocalAppColors.current
    val isUnsettled = data.status == "未结算"

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = colors.surfaceContainerHigh),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            IncomeItemHeader(data, isUnsettled, colors)
            Spacer(Modifier.height(10.dp))
            IncomeItemDetails(data, isUnsettled, colors)
        }
    }
}

@Composable
private fun IncomeItemHeader(
    data: com.lemon.mcdevmanagermp.data.vo.netease.income.IncomeVO,
    isUnsettled: Boolean,
    colors: com.lemon.mcdevmanagermp.ui.theme.AppColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = data.dataMonth,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = colors.textColor,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = data.status,
            style = MaterialTheme.typography.labelSmall,
            color = if (isUnsettled) colors.primary else colors.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun IncomeItemDetails(
    data: com.lemon.mcdevmanagermp.data.vo.netease.income.IncomeVO,
    isUnsettled: Boolean,
    colors: com.lemon.mcdevmanagermp.ui.theme.AppColors
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerHighest, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        DetailRow("当前月收益", data.income, if (isUnsettled) colors.primary else colors.textColor)
        DetailRow("消耗钻石", "${data.totalDiamond}", colors.textColor)
        DetailRow("激励金额", data.incentiveIncome, colors.textColor)
        if (data.platform == "pe") {
            DetailRow("畅玩计划收益", data.playPlanIncome, colors.textColor)
        }

        HorizontalDivider(color = colors.outlineVariant, thickness = 1.dp, modifier = Modifier.padding(vertical = 2.dp))

        DetailRow("累计可提取收益", data.availableIncome, colors.textColor, bold = true)
        DetailRow("税费", data.tax, colors.textColor)
        if (data.techServiceFee > 0.0) {
            DetailRow("技术服务费", "${data.techServiceFee}", colors.textColor)
        }
        if (data.totalUsagePrice > 0.0) {
            DetailRow("网络服成本", "${data.totalUsagePrice}", colors.textColor)
        }
    }
}

@Composable
internal fun DetailRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = LocalAppColors.current.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            color = valueColor
        )
    }
}

// ============================================================
// Apply Income Detail Dialog
// ============================================================

@Composable
internal fun ApplyIncomeDetailDialog(
    detailList: List<com.lemon.mcdevmanagermp.data.vo.netease.income.ApplyIncomeDetailVO>,
    onApply: (List<String>) -> Unit
) {
    val colors = LocalAppColors.current
    val first = detailList.firstOrNull() ?: return
    val ids = detailList.map { it.id }
    val availableIncome = first.availableIncome

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(0.9f),
        colors = CardDefaults.elevatedCardColors(containerColor = colors.surfaceContainerHigh),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("总可结算收益", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = colors.textColor)
                Text(availableIncome, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colors.primary)
            }

            val totalTaxIncome = detailList.sumOf { it.taxIncome.toDouble() }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("总税后收益", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = colors.textColor)
                Text(totalTaxIncome.formatDecimal(2), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colors.primary)
            }

            Spacer(Modifier.height(12.dp))
            Text("每月明细", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant, fontWeight = FontWeight.Medium, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp)
                    .background(colors.surfaceContainerLow, RoundedCornerShape(8.dp))
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                detailList.forEach { detail -> ApplyDetailMonthlyItem(detail) }
            }

            Spacer(Modifier.height(12.dp))
            DialogInfoRow("发票提交方式", first.type)
            Spacer(Modifier.height(4.dp))
            DialogInfoRow("供应商名称", first.providerName)
            Spacer(Modifier.height(4.dp))
            DialogInfoRow("收款银行", first.bank)
            Spacer(Modifier.height(4.dp))
            DialogInfoRow("银行卡号", first.cardNo)
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { onApply(ids) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                Text("确认结算", style = MaterialTheme.typography.bodyLarge, color = colors.onPrimary)
            }
        }
    }
}

@Composable
private fun DialogInfoRow(label: String, value: String) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, color = colors.primary)
    }
}

@Composable
internal fun ApplyDetailMonthlyItem(detail: com.lemon.mcdevmanagermp.data.vo.netease.income.ApplyIncomeDetailVO) {
    val colors = LocalAppColors.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(detail.dataMonth, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant, fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(8.dp))
            HorizontalDivider(modifier = Modifier.weight(1f), color = colors.outlineVariant)
        }

        Spacer(Modifier.height(4.dp))
        MonthlyDetailRow("消耗钻石数", "${detail.totalDiamond}", colors.textColor)

        if (detail.extraInfo.advIncome > 0.0) {
            Spacer(Modifier.height(2.dp))
            MonthlyDetailRow("广告计划收益", "${detail.extraInfo.advIncome}", colors.textColor)
        }

        Spacer(Modifier.height(2.dp))
        MonthlyDetailRow("分成后收益", detail.income, colors.textColor, bold = true)

        if (detail.incentiveIncome != "0.00") {
            Spacer(Modifier.height(2.dp))
            MonthlyDetailRow("激励收益", detail.incentiveIncome, colors.textColor)
        }
        if (detail.playPlanIncome != "0.00") {
            Spacer(Modifier.height(2.dp))
            MonthlyDetailRow("畅玩计划收益", detail.playPlanIncome, colors.textColor)
        }

        Spacer(Modifier.height(2.dp))
        HorizontalDivider(color = colors.outlineVariant, thickness = 0.5.dp)
        Spacer(Modifier.height(2.dp))

        MonthlyDetailRow("税费", detail.tax, colors.onSurfaceVariant)
        if (detail.adjustMoney != "0.00") {
            Spacer(Modifier.height(2.dp))
            MonthlyDetailRow("扣款", detail.adjustMoney, colors.onSurfaceVariant)
        }
        if (detail.techServiceFee > 0.0) {
            Spacer(Modifier.height(2.dp))
            MonthlyDetailRow("技术服务费", "${detail.techServiceFee}", colors.onSurfaceVariant)
        }
        if (detail.totalUsagePrice > 0.0) {
            Spacer(Modifier.height(2.dp))
            MonthlyDetailRow("网络服成本", "${detail.totalUsagePrice}", colors.onSurfaceVariant)
        }

        Spacer(Modifier.height(2.dp))
        HorizontalDivider(color = colors.outlineVariant, thickness = 0.5.dp)
        Spacer(Modifier.height(2.dp))
        MonthlyDetailRow("税后收益", detail.taxIncome, colors.primary, bold = true)
    }
}

@Composable
private fun MonthlyDetailRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color,
    bold: Boolean = false
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = LocalAppColors.current.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium, color = valueColor)
    }
}
