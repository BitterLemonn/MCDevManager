package com.lemon.mcdevmanagermp.ui.pages.work.activity.discount

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
import com.lemon.mcdevmanagermp.ui.pages.work.activity.discount.layout.DiscountActivityCompactLayout
import com.lemon.mcdevmanagermp.ui.pages.work.activity.discount.layout.DiscountActivityExpandedLayout
import com.lemon.mcdevmanagermp.ui.pages.work.activity.discount.layout.DiscountActivityMediumLayout
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

@Composable
fun DiscountActivityPage(
    onBack: () -> Unit
) {
    val viewModel = remember { DiscountActivityViewModel() }
    val state by viewModel.state.collectAsState()
    val colors = LocalAppColors.current

    viewModel.effect.collectUiEffect { effect ->
        when (effect) {
            is DiscountActivityEffect.ShowToast -> showToast(effect.message)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatch(DiscountActivityAction.LoadData)
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        val widthSizeClass = LocalWindowWidthSizeClass.current
        when (widthSizeClass) {
            WindowWidthSizeClass.Compact -> DiscountActivityCompactLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack
            )

            WindowWidthSizeClass.Medium -> DiscountActivityMediumLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack
            )

            else -> DiscountActivityExpandedLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack
            )
        }
    }
}
