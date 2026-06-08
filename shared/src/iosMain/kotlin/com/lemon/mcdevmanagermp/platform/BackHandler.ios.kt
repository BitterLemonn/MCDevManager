package com.lemon.mcdevmanagermp.platform

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS: no hardware back button
}
