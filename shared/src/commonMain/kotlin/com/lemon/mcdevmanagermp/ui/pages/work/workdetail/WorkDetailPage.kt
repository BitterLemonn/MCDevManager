package com.lemon.mcdevmanagermp.ui.pages.work.workdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.lemon.mcdevmanagermp.ui.components.LocalWindowWidthSizeClass
import com.lemon.mcdevmanagermp.ui.components.collectUiEffect
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.WorkDetailCompactLayout
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.WorkDetailExpandedLayout
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.WorkDetailMediumLayout
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

@Composable
fun WorkDetailPage(
    itemId: String,
    onBack: () -> Unit,
    onNeedReLogin: () -> Unit = {}
) {
    val viewModel = remember { WorkDetailViewModel() }
    val state by viewModel.state.collectAsState()
    val colors = LocalAppColors.current

    viewModel.effect.collectUiEffect { effect ->
        when (effect) {
            is WorkDetailEffect.ShowToast -> showToast(effect.message)
            WorkDetailEffect.NeedReLogin -> onNeedReLogin()
        }
    }

    LaunchedEffect(itemId) {
        viewModel.dispatch(WorkDetailAction.LoadDetail(itemId))
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        val widthSizeClass = LocalWindowWidthSizeClass.current
        when (widthSizeClass) {
            WindowWidthSizeClass.Expanded -> WorkDetailExpandedLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack
            )

            WindowWidthSizeClass.Medium -> WorkDetailMediumLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack
            )

            else -> WorkDetailCompactLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack
            )
        }
    }
}
