package com.lemon.mcdevmanagermp.ui.pages.settings.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.pages.settings.SeedColorSection
import com.lemon.mcdevmanagermp.ui.pages.settings.ThemeModeSection
import com.lemon.mcdevmanagermp.ui.theme.ThemeMode

@Composable
internal fun MediumThemeLayout(
    themeMode: ThemeMode,
    seedColor: Color,
    useDynamicColor: Boolean,
    onThemeModeChange: (ThemeMode) -> Unit,
    onSeedColorChange: (Color) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.width(560.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ThemeModeSection(themeMode, onThemeModeChange)
            SeedColorSection(seedColor, useDynamicColor, onSeedColorChange, onDynamicColorChange)
        }
    }
}
