package com.lemon.mcdevmanagermp.ui.pages.income

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.lemon.mcdevmanagermp.platform.BackHandler
import com.lemon.mcdevmanagermp.ui.components.AppScaffold
import com.lemon.mcdevmanagermp.ui.components.LocalWindowWidthSizeClass
import com.lemon.mcdevmanagermp.ui.components.collectUiEffect
import com.lemon.mcdevmanagermp.ui.pages.income.components.ApplyIncomeDetailDialog
import com.lemon.mcdevmanagermp.ui.pages.income.layout.CompactIncomeLayout
import com.lemon.mcdevmanagermp.ui.pages.income.layout.ExpandedIncomeLayout
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

@Composable
fun IncomePage(onBack: () -> Unit) {
    val viewModel = remember { IncomeViewModel() }
    val state by viewModel.state.collectAsState()
    val colors = LocalAppColors.current
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    viewModel.effect.collectUiEffect { effect ->
        when (effect) {
            is IncomeEffect.ShowToast -> showToast(effect.message)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatch(IncomeAction.LoadData)
    }

    val showApplyDialog = !state.isLoadingApplyDetail && state.applyDetailList.isNotEmpty()
    BackHandler(enabled = showApplyDialog) {
        viewModel.dispatch(IncomeAction.DismissApplyDetail)
    }

    AppScaffold {
        Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
            when (LocalWindowWidthSizeClass.current) {
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
        }
    }

    if (showApplyDialog) {
        ApplyIncomeDetailDialog(
            detailList = state.applyDetailList,
            isSubmitting = state.isApplyingIncome,
            onDismiss = { viewModel.dispatch(IncomeAction.DismissApplyDetail) },
            onApply = { ids -> viewModel.dispatch(IncomeAction.ApplyIncome(ids)) }
        )
    }
}
