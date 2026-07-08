package com.lemon.mcdevmanagermp.ui.pages.work.promotion

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
import com.lemon.mcdevmanagermp.ui.pages.work.promotion.layout.PromotionCompactLayout
import com.lemon.mcdevmanagermp.ui.pages.work.promotion.layout.PromotionExpandedLayout
import com.lemon.mcdevmanagermp.ui.pages.work.promotion.layout.PromotionMediumLayout
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

@Composable
fun PromotionPage(
    onBack: () -> Unit
) {
    val viewModel = remember { PromotionViewModel() }
    val state by viewModel.state.collectAsState()
    val colors = LocalAppColors.current

    viewModel.effect.collectUiEffect { effect ->
        when (effect) {
            is PromotionEffect.ShowToast -> showToast(effect.message)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatch(PromotionAction.LoadData)
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        val widthSizeClass = LocalWindowWidthSizeClass.current
        when (widthSizeClass) {
            WindowWidthSizeClass.Compact -> PromotionCompactLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack
            )

            WindowWidthSizeClass.Medium -> PromotionMediumLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack
            )

            else -> PromotionExpandedLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack
            )
        }
    }
}
