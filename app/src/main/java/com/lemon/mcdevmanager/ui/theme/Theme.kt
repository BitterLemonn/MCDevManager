package com.lemon.mcdevmanager.ui.theme

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


private val DarkColorPalette = AppColors(
    textColor = TextNight,
    hintColor = Hint,
    card = CardDark,
    background = BackgroundDark,
    primaryColor = Purple40,
    primarySubColor = PurpleGrey40,
    secondaryColor = Pink40,
    info = InfoNight,
    warn = WarnNight,
    success = SuccessNight,
    error = ErrorNight
)

private val LightColorPalette = AppColors(
    textColor = TextDay,
    hintColor = Hint,
    card = CardLight,
    background = BackgroundLight,
    primaryColor = Purple80,
    primarySubColor = PurpleGrey80,
    secondaryColor = Pink80,
    info = InfoLight,
    warn = WarnLight,
    success = SuccessLight,
    error = ErrorLight
)

var LocalAppColors = compositionLocalOf {
    LightColorPalette
    DarkColorPalette
}

@Stable
object AppTheme {
    val colors: AppColors
        @Composable
        get() = LocalAppColors.current

    enum class Theme {
        Light, Dark
    }
}

@Composable
fun MCDevManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val theme = if (isSystemInDarkTheme()) AppTheme.Theme.Dark
    else AppTheme.Theme.Light

    val targetColors = when (theme) {
        AppTheme.Theme.Light -> LightColorPalette
        AppTheme.Theme.Dark -> DarkColorPalette
    }

    val textColor = animateColorAsState(targetColors.textColor, TweenSpec(600))
    val hintColor = animateColorAsState(targetColors.hintColor, TweenSpec(600))
    val card = animateColorAsState(targetColors.card, TweenSpec(600))
    val background = animateColorAsState(targetColors.background, TweenSpec(600))
    val primaryColor = animateColorAsState(targetColors.primaryColor, TweenSpec(600))
    val primarySubColor = animateColorAsState(targetColors.primarySubColor, TweenSpec(600))
    val secondaryColor = animateColorAsState(targetColors.secondaryColor, TweenSpec(600))
    val info = animateColorAsState(targetColors.info, TweenSpec(600))
    val warn = animateColorAsState(targetColors.warn, TweenSpec(600))
    val success = animateColorAsState(targetColors.success, TweenSpec(600))
    val error = animateColorAsState(targetValue = targetColors.error, TweenSpec(600))

    val appColors = AppColors(
        textColor = textColor.value,
        hintColor = hintColor.value,
        card = card.value,
        background = background.value,
        primaryColor = primaryColor.value,
        primarySubColor = primarySubColor.value,
        secondaryColor = secondaryColor.value,
        info = info.value,
        warn = warn.value,
        success = success.value,
        error = error.value
    )

//    val view = LocalView.current
//    if (!view.isInEditMode) {
//        SideEffect {
//            val window = (view.context as Activity).window
//            window.statusBarColor = appColors.primaryColor.toArgb()
//            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
//            WindowCompat.setDecorFitsSystemWindows(window, false)
//        }
//    }

    CompositionLocalProvider(LocalAppColors provides appColors, content = content)
}


class AppColors(
    textColor: Color,
    hintColor: Color,
    card: Color,
    background: Color,
    primaryColor: Color,
    primarySubColor: Color,
    secondaryColor: Color,
    info: Color,
    warn: Color,
    success: Color,
    error: Color,
) {
    var textColor: Color by mutableStateOf(textColor)
        internal set
    var hintColor: Color by mutableStateOf(hintColor)
        internal set
    var card: Color by mutableStateOf(card)
        internal set
    var background: Color by mutableStateOf(background)
        private set
    var primaryColor: Color by mutableStateOf(primaryColor)
        private set
    var primarySubColor: Color by mutableStateOf(primarySubColor)
        private set
    var secondaryColor: Color by mutableStateOf(secondaryColor)
        private set
    var info: Color by mutableStateOf(info)
        private set
    var warn: Color by mutableStateOf(warn)
        private set
    var success: Color by mutableStateOf(success)
        private set
    var error: Color by mutableStateOf(error)
        private set
}
