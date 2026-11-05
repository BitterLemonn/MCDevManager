package com.lemon.mcdevmanagermp.ui.pages.incomeDetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.domain.main.ProfitMonth
import com.lemon.mcdevmanagermp.domain.main.profitMonthWindow
import com.lemon.mcdevmanagermp.ui.components.AppScaffold
import com.lemon.mcdevmanagermp.ui.components.CollapsingTopBar
import com.lemon.mcdevmanagermp.ui.components.LocalSnackbarHostState
import com.lemon.mcdevmanagermp.ui.components.LocalToastScope
import com.lemon.mcdevmanagermp.ui.components.LocalWindowWidthSizeClass
import com.lemon.mcdevmanagermp.ui.pages.main.MainViewModel
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import com.lemon.mcdevmanagermp.utils.ECO_FEE_RATE
import com.lemon.mcdevmanagermp.utils.ModuleIncomeDetail
import com.lemon.mcdevmanagermp.utils.ProfitData
import com.lemon.mcdevmanagermp.utils.extension.formatDecimal
import com.lemon.mcdevmanagermp.utils.getTaxMoney
import com.lemon.mcdevmanagermp.utils.reverseEcoFeeRate
import com.lemon.mcdevmanagermp.utils.toModuleIncomeDetails
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_money
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Clock

@Composable
fun IncomeDetailPage(initialMonthOffset: Int = 0, onBack: () -> Unit) {
    val colors = LocalAppColors.current
    val today = remember {
        val now = Clock.System.now().toLocalDateTime(TimeZone.of("Asia/Shanghai"))
        LocalDate(now.year, now.month.number, now.day)
    }
    val monthWindow = remember(today) { profitMonthWindow(today) }
    val initialMonth = remember(initialMonthOffset, monthWindow) {
        monthWindow.current.shift(initialMonthOffset)
    }
    val initialData = remember(initialMonthOffset) {
        when (initialMonthOffset) {
            -1 -> MainViewModel.cachedLastMonthProfitData
            1 -> MainViewModel.cachedNextMonthProfitData
            else -> MainViewModel.cachedProfitData
        }
    }
    val viewModel = remember(initialMonth) {
        IncomeDetailViewModel(initialMonth, initialData, MainViewModel.cachedUserInfo)
    }
    val state by viewModel.state.collectAsState()
    val profitData = state.profitData
    val monthLabel = "${state.selectedMonth.year}年${state.selectedMonth.month}月"
    val modules = profitData.toModuleIncomeDetails()
    val maxSelectableMonth = if (monthWindow.showNextMonth) {
        monthWindow.current.shift(1)
    } else {
        monthWindow.current
    }
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    var showMonthPicker by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val topBarAlpha by remember {
        derivedStateOf {
            (scrollState.value.toFloat() / 100f).coerceIn(0f, 1f)
        }
    }

    val snackbarHostState = LocalSnackbarHostState.current
    val toastScope = LocalToastScope.current
    LaunchedEffect(state.selectedMonth) {
        if (state.selectedMonth < ProfitMonth(2026, 1)) {
            toastScope.launch {
                snackbarHostState.showSnackbar("2026.1以前的收益算法与现在不一致，数据可能产生偏差")
            }
        }
    }

    AppScaffold {
        Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                Spacer(Modifier.height(statusBarTop))
                Spacer(Modifier.height(56.dp))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = navBarBottom + 16.dp)
                ) {
                    val widthSizeClass = LocalWindowWidthSizeClass.current

                    val columns = when (widthSizeClass) {
                        WindowWidthSizeClass.Compact -> 1
                        WindowWidthSizeClass.Medium -> 2
                        else -> 3
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        state.errorMessage?.let { message ->
                            LoadErrorCard(message = message, onRetry = viewModel::retry)
                        }
                        SummaryCard(profitData, modules, showTaxEstimate = !state.isEnterprise)
                        EcoFeeReverseCard(
                            profitData = profitData,
                            month = state.selectedMonth,
                            dataReady = !state.isLoading && state.errorMessage == null,
                            onRetry = viewModel::retry
                        )
                        ModuleList(modules, columns)
                    }
                }
            }

            CollapsingTopBar(
                title = "收益详情 - $monthLabel",
                collapseFraction = topBarAlpha,
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showMonthPicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "选择月份"
                        )
                    }
                }
            )

            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center).size(36.dp),
                    color = colors.primary,
                    strokeWidth = 3.dp
                )
            }
        }
    }

    if (showMonthPicker) {
        ProfitMonthPickerDialog(
            selectedMonth = state.selectedMonth,
            maxMonth = maxSelectableMonth,
            onSelect = {
                viewModel.selectMonth(it)
                showMonthPicker = false
            },
            onDismiss = { showMonthPicker = false }
        )
    }
}

@Composable
private fun LoadErrorCard(message: String, onRetry: () -> Unit) {
    val colors = LocalAppColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                color = colors.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onRetry) { Text("重试") }
        }
    }
}

@Composable
private fun ProfitMonthPickerDialog(
    selectedMonth: ProfitMonth,
    maxMonth: ProfitMonth,
    onSelect: (ProfitMonth) -> Unit,
    onDismiss: () -> Unit
) {
    var displayedYear by remember(selectedMonth) { mutableIntStateOf(selectedMonth.year) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { displayedYear-- }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "上一年")
                }
                Text("${displayedYear}年", style = MaterialTheme.typography.titleMedium)
                IconButton(
                    onClick = { displayedYear++ },
                    enabled = displayedYear < maxMonth.year
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "下一年"
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..12).chunked(3).forEach { rowMonths ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowMonths.forEach { month ->
                            val value = ProfitMonth(displayedYear, month)
                            FilterChip(
                                selected = value == selectedMonth,
                                onClick = { onSelect(value) },
                                label = { Text("${month}月") },
                                enabled = value <= maxMonth,
                                modifier = Modifier.weight(1f)
                                    .heightIn(min = 40.dp),
                                elevation = FilterChipDefaults.elevatedFilterChipElevation(
                                    elevation = 0.dp,
                                    hoveredElevation = 0.dp,
                                    pressedElevation = 0.dp
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

// ============================================================
// Summary Card
// ============================================================

/**
 * 收益汇总卡片。
 * @param showTaxEstimate 是否展示按个人劳务报酬预估的税后收益；企业开发者自行开票缴税，传 false 隐藏
 */
@Composable
private fun SummaryCard(
    profitData: ProfitData,
    modules: List<ModuleIncomeDetail>,
    showTaxEstimate: Boolean
) {
    val colors = LocalAppColors.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh),
        shape = RoundedCornerShape(12.dp),

        ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_money),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "收益汇总",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textColor
                )
            }

            Spacer(Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceContainerHighest, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailRow("月总流水(元)", (profitData.sumProfit / 100.0).formatDecimal(2), colors)
                DetailRow(
                    "开发者分成(元)",
                    (profitData.developerProfit / 100.0).formatDecimal(2),
                    colors
                )
                val totalShareReturn = modules.sumOf { it.shareReturn }
                if (totalShareReturn > 0) {
                    DetailRow("分成返还(元)", totalShareReturn.formatDecimal(2), colors)
                }
                DetailRow(
                    "模组激励合计(元)",
                    profitData.subsidyProfit.values.sum().formatDecimal(2),
                    colors
                )
                DetailRow("流水激励(元)", profitData.profitSubsidy.formatDecimal(2), colors)

                HorizontalDivider(color = colors.outlineVariant, thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "总收益(元)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textColor
                    )
                    Text(
                        text = profitData.totalProfit.formatDecimal(2),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                }
                if (showTaxEstimate) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "税后收益(元)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textColor
                        )
                        Text(
                            text = (profitData.totalProfit - getTaxMoney(profitData.totalProfit)).formatDecimal(
                                2
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// Eco Fee Reverse Card — 输入实际收益反推生态费
// ============================================================

/** 金额输入：非负数，最多 9 位整数、2 位小数 */
private val AMOUNT_INPUT_REGEX = Regex("""^\d{0,9}(\.\d{0,2})?$""")

/**
 * 实际收益反推生态费卡片：输入该月实际税前收益，按预估公式反解实际生态费率与金额。
 * 输入内容随月份隔离，切换月份即清空，避免把上个月的实际收益套到新月份的流水上。
 *
 * 流水不完整（[ProfitData.isFlowComplete] 为 false）时不反推：缺失的作品流水会让费率系统性偏低，
 * 改为提示并提供重新加载。
 *
 * @param dataReady 该月收益数据已加载成功；加载中或失败时流水为空，不展示反推结果
 * @param onRetry 强制重新加载当月收益（跳过缓存），用于流水不完整时补齐数据
 */
@Composable
private fun EcoFeeReverseCard(
    profitData: ProfitData,
    month: ProfitMonth,
    dataReady: Boolean,
    onRetry: () -> Unit
) {
    val colors = LocalAppColors.current
    var input by remember(month) { mutableStateOf("") }
    val actualIncome = input.toDoubleOrNull()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "实际收益反推生态费",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.textColor
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = input,
                onValueChange = { s -> if (AMOUNT_INPUT_REGEX.matches(s)) input = s },
                label = { Text("${month.month}月实际收益(税前，元)") },
                supportingText = { Text("口径同总收益：开发者分成 + 模组激励 + 流水激励") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            if (dataReady && !profitData.isFlowComplete) {
                // 不论是否已输入都提示，避免用户先输入后才发现结果不可用
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceContainerHighest, RoundedCornerShape(8.dp))
                        .padding(start = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "部分作品流水获取失败，数据不完整，暂不反推生态费",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.warning,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onRetry) { Text("重试") }
                }
            } else if (dataReady && actualIncome != null) {
                val ecoFeeRate = profitData.reverseEcoFeeRate(actualIncome)
                Spacer(Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceContainerHighest, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when {
                        ecoFeeRate == null -> Text(
                            text = "该月无流水，无法反推生态费",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )

                        // 费率落在 0~100% 之外：实际收益无法由预估公式解释（如激励未发放、口径不一致）
                        ecoFeeRate !in 0.0..1.0 -> Text(
                            text = "实际收益超出预估公式可解释的范围，请检查输入金额或激励是否已计入",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.warning
                        )

                        else -> {
                            DetailRow("预估生态费率", "${(ECO_FEE_RATE * 100).formatDecimal(2)}%", colors)
                            DetailRow("实际生态费率", "${(ecoFeeRate * 100).formatDecimal(2)}%", colors)
                            DetailRow(
                                "实际生态费(元)",
                                (profitData.sumProfit / 100.0 * ecoFeeRate).formatDecimal(2),
                                colors
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// Module List — grid with configurable columns
// ============================================================

@Composable
private fun ModuleList(modules: List<ModuleIncomeDetail>, columns: Int) {
    if (modules.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "暂无模组收益数据",
                style = MaterialTheme.typography.bodyLarge,
                color = LocalAppColors.current.onSurfaceVariant
            )
        }
        return
    }

    if (columns <= 1) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            modules.forEach { module -> ModuleIncomeCard(module) }
        }
    } else {
        modules.chunked(columns).forEach { rowModules ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowModules.forEach { module ->
                    Box(modifier = Modifier.weight(1f)) {
                        ModuleIncomeCard(module)
                    }
                }
                repeat(columns - rowModules.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ============================================================
// Module Income Card
// ============================================================

@Composable
private fun ModuleIncomeCard(module: ModuleIncomeDetail) {
    val colors = LocalAppColors.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh),
        shape = RoundedCornerShape(12.dp),

        ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = module.moduleName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.textColor
            )

            Spacer(Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceContainerHighest, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DetailRow("流水收益(元)", module.flowIncome.formatDecimal(2), colors)
                DetailRow("开发者分成(元)", module.developerShare.formatDecimal(2), colors)
                if (module.shareReturn > 0) {
                    DetailRow("分成返还(元)", module.shareReturn.formatDecimal(2), colors)
                }
                if (module.subsidyAmount > 0) {
                    DetailRow("模组激励(元)", module.subsidyAmount.formatDecimal(2), colors)
                }

                HorizontalDivider(color = colors.outlineVariant, thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "小计(元)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = colors.onSurfaceVariant
                    )
                    Text(
                        text = module.totalIncome.formatDecimal(2),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                }
            }
        }
    }
}

// ============================================================
// Shared Detail Row
// ============================================================

@Composable
private fun DetailRow(
    label: String,
    value: String,
    colors: com.lemon.mcdevmanagermp.ui.theme.AppColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = colors.textColor
        )
    }
}
