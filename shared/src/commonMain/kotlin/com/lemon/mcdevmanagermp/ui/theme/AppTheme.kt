package com.lemon.mcdevmanagermp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.lemon.mcdevmanagermp.platform.ConfigureSystemBars
import com.lemon.mcdevmanagermp.platform.appColorScheme
import com.lemon.mcdevmanagermp.utils.extension.applyDefaultFont
import mcdevmanagermpr.shared.generated.resources.MiSans_Regular
import mcdevmanagermpr.shared.generated.resources.Res
import org.jetbrains.compose.resources.Font

private val miSansFontFamily
    @Composable get() = FontFamily(Font(Res.font.MiSans_Regular))

private val miSansTypography: Typography
    @Composable get() = Typography().applyDefaultFont(miSansFontFamily)

@Composable
fun AppTheme(
    viewModel: ThemeViewModel,
    content: @Composable () -> Unit,
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val seedColor by viewModel.seedColor.collectAsState()
    val useDynamicColor by viewModel.useDynamicColor.collectAsState()
    AppTheme(themeMode = themeMode, seedColor = seedColor, useDynamicColor = useDynamicColor, content = content)
}

@Composable
fun AppTheme(
    themeMode: ThemeMode,
    seedColor: Color = DefaultSeedColor,
    useDynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    // 根据主题模式设置系统状态栏图标颜色
    ConfigureSystemBars(isDark = isDark)

    val colorScheme = appColorScheme(seedColor = seedColor, isDark = isDark, useDynamicColor = useDynamicColor)
    val extendedColors = if (isDark) darkExtendedColors() else lightExtendedColors()
    val appColors = AppColors(scheme = colorScheme, extended = extendedColors)

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = miSansTypography,
            content = content
        )
    }
}
