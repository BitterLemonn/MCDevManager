package com.lemon.mcdevmanagermp.ui.pages.settings.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.pages.settings.SeedColorSection
import com.lemon.mcdevmanagermp.ui.pages.settings.ThemeModeSection
import com.lemon.mcdevmanagermp.ui.theme.ThemeMode

@Composable
internal fun CompactThemeLayout(
    themeMode: ThemeMode,
    seedColor: Color,
    useDynamicColor: Boolean,
    onThemeModeChange: (ThemeMode) -> Unit,
    onSeedColorChange: (Color) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ThemeModeSection(themeMode, onThemeModeChange)
        SeedColorSection(seedColor, useDynamicColor, onSeedColorChange, onDynamicColorChange)
    }
}
