package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS: no hardware back button
}
