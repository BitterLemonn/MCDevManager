package com.lemon.mcdevmanager.ui.base

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.zj.mvi.core.observeEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun BasePage(
    viewEvent: SharedFlow<List<*>>,
    onEvent: (Any?) -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    var eventJob: Job? = null
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        eventJob = viewEvent.observeEvent(lifecycleOwner) { event ->
            onEvent(event)
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            eventJob?.cancel()
        }
    }

    content(Modifier.padding(WindowInsets.navigationBars.asPaddingValues()))
}