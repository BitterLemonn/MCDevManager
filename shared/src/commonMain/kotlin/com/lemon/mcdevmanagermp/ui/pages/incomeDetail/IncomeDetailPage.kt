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
import com.lemon.mcdevmanagermp.utils.ModuleIncomeDetail
import com.lemon.mcdevmanagermp.utils.ProfitData
import com.lemon.mcdevmanagermp.utils.extension.formatDecimal
import com.lemon.mcdevmanagermp.utils.getTaxMoney
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
    val viewModel = remember(initialMonth) { IncomeDetailViewModel(initialMonth, initialData) }
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
                        SummaryCard(profitData, modules)
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

@Composable
private fun SummaryCard(profitData: ProfitData, modules: List<ModuleIncomeDetail>) {
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
