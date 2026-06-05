package com.lemon.mcdevmanagermp.ui.pages.settings.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.pages.settings.SeedColorSection
import com.lemon.mcdevmanagermp.ui.pages.settings.ThemeModeSection
import com.lemon.mcdevmanagermp.ui.theme.ThemeMode

@Composable
internal fun ExpandedThemeLayout(
    themeMode: ThemeMode,
    seedColor: Color,
    useDynamicColor: Boolean,
    onThemeModeChange: (ThemeMode) -> Unit,
    onSeedColorChange: (Color) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            ThemeModeSection(themeMode, onThemeModeChange)
        }
        Box(modifier = Modifier.weight(1f)) {
            SeedColorSection(seedColor, useDynamicColor, onSeedColorChange, onDynamicColorChange)
        }
    }
}
