package com.lemon.mcdevmanager.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.zj.mvi.core.observeEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun BasePage(
    viewEvent: SharedFlow<List<*>>,
    onEvent: (Any?) -> Unit,
    content: @Composable () -> Unit
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

    content()
}