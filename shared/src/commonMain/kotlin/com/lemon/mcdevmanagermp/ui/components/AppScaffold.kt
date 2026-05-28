package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import com.lemon.mcdevmanagermp.utils.extension.collectEffect
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun <E : IUiEffect> AppScaffold(
    viewEffect: SharedFlow<E>? = null,
    onEffect: ((E) -> Unit)? = null,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    if (viewEffect != null && onEffect != null) {
        viewEffect.collectEffect { event ->
            onEffect(event)
        }
    }

    Scaffold(
        topBar = topBar,
        bottomBar = bottomBar,
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->
        val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
        val layoutDirection = LayoutDirection.Ltr
        content(
            PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + navBarPadding.calculateBottomPadding(),
                start = innerPadding.calculateStartPadding(layoutDirection) + navBarPadding.calculateStartPadding(layoutDirection),
                end = innerPadding.calculateEndPadding(layoutDirection) + navBarPadding.calculateEndPadding(layoutDirection)
            )
        )
    }
}
