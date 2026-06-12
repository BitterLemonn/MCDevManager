package com.lemon.mcdevmanagermp.ui.pages.analyze.realtimeProfit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.components.LocalSnackbarHostState
import com.lemon.mcdevmanagermp.ui.components.LocalWindowWidthSizeClass
import com.lemon.mcdevmanagermp.ui.pages.analyze.realtimeProfit.layout.RealtimeProfitCompactLayout
import com.lemon.mcdevmanagermp.ui.pages.analyze.realtimeProfit.layout.RealtimeProfitExpandedLayout
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import kotlinx.coroutines.launch

@Composable
fun RealtimeProfitPage(onBack: () -> Unit) {
    val viewModel = remember { RealtimeProfitViewModel() }
    val state by viewModel.state.collectAsState()
    val colors = LocalAppColors.current
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()

    var showDatePicker by remember { mutableStateOf(false) }

    // Effect 收集
    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is RealtimeProfitEffect.ShowToast -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                }
                RealtimeProfitEffect.NeedReLogin -> {
                    scope.launch { snackbarHostState.showSnackbar("登录已过期，请重新登录") }
                }
            }
        }
    }

    // 初始化加载
    LaunchedEffect(Unit) {
        viewModel.initLoad()
    }

    // 日期选择器状态同步
    LaunchedEffect(state.isDateSelectorVisible) {
        showDatePicker = state.isDateSelectorVisible
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        val widthSizeClass = LocalWindowWidthSizeClass.current

        when (widthSizeClass) {
            WindowWidthSizeClass.Compact, WindowWidthSizeClass.Medium -> RealtimeProfitCompactLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack,
                navBarBottom = navBarBottom,
                showDatePicker = showDatePicker,
                onDatePickerDismiss = {
                    showDatePicker = false
                    viewModel.dispatch(RealtimeProfitAction.ToggleDateSelector)
                },
                onDateSelected = { day ->
                    viewModel.dispatch(RealtimeProfitAction.UpdateCheckDay(day))
                    viewModel.dispatch(RealtimeProfitAction.LoadData(day))
                }
            )

            else -> RealtimeProfitExpandedLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack,
                navBarBottom = navBarBottom,
                showDatePicker = showDatePicker,
                onDatePickerDismiss = {
                    showDatePicker = false
                    viewModel.dispatch(RealtimeProfitAction.ToggleDateSelector)
                },
                onDateSelected = { day ->
                    viewModel.dispatch(RealtimeProfitAction.UpdateCheckDay(day))
                    viewModel.dispatch(RealtimeProfitAction.LoadData(day))
                }
            )
        }

        // Loading
        if (state.isLoading && state.profitMap.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center).size(36.dp),
                color = colors.primary,
                strokeWidth = 3.dp
            )
        }

    }
}
