package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
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
    snackbarHost: @Composable () -> Unit = {},
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
        snackbarHost = snackbarHost,
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        content(innerPadding)
    }
}
