package com.lemon.mcdevmanagermp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min

// ============================================================
// Seed Color — 项目主色调，所有 Material3 颜色由此派生
// ============================================================
val DefaultSeedColor = Color(0xFF4F378B)

// ============================================================
// ExtendedColors — 不属于 Material3 标准色板的业务语义颜色
// ============================================================
data class ExtendedColors(
    val textColor: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val info: Color,
    val vip: Color,
    val gold: Color,
    val online: Color,
    val offline: Color,
    val rarity: Color,
    val gameQuality: Color,
    val divider: Color,
    val disabled: Color,
    val shimmer: Color,
)

// ============================================================
// AppColors — 统一颜色入口，桥接 Material3 ColorScheme + 业务色
// ============================================================
class AppColors(
    val scheme: ColorScheme,
    val extended: ExtendedColors,
) {
    // Material3 基础色
    val primary: Color get() = scheme.primary
    val onPrimary: Color get() = scheme.onPrimary
    val primaryContainer: Color get() = scheme.primaryContainer
    val onPrimaryContainer: Color get() = scheme.onPrimaryContainer
    val secondary: Color get() = scheme.secondary
    val onSecondary: Color get() = scheme.onSecondary
    val secondaryContainer: Color get() = scheme.secondaryContainer
    val onSecondaryContainer: Color get() = scheme.onSecondaryContainer
    val tertiary: Color get() = scheme.tertiary
    val onTertiary: Color get() = scheme.onTertiary
    val background: Color get() = scheme.background
    val onBackground: Color get() = scheme.onBackground
    val surface: Color get() = scheme.surface
    val onSurface: Color get() = scheme.onSurface
    val surfaceVariant: Color get() = scheme.surfaceVariant
    val onSurfaceVariant: Color get() = scheme.onSurfaceVariant
    val surfaceTint: Color get() = scheme.surfaceTint
    val inverseSurface: Color get() = scheme.inverseSurface
    val inverseOnSurface: Color get() = scheme.inverseOnSurface
    val surfaceContainer: Color get() = scheme.surfaceContainer
    val surfaceContainerHigh: Color get() = scheme.surfaceContainerHigh
    val surfaceContainerHighest: Color get() = scheme.surfaceContainerHighest
    val surfaceContainerLow: Color get() = scheme.surfaceContainerLow
    val surfaceContainerLowest: Color get() = scheme.surfaceContainerLowest
    val error: Color get() = scheme.error
    val onError: Color get() = scheme.onError
    val outline: Color get() = scheme.outline
    val outlineVariant: Color get() = scheme.outlineVariant
    val scrim: Color get() = scheme.scrim

    // 业务语义色
    val textColor: Color get() = extended.textColor
    val success: Color get() = extended.success
    val warning: Color get() = extended.warning
    val danger: Color get() = extended.danger
    val info: Color get() = extended.info
    val vip: Color get() = extended.vip
    val gold: Color get() = extended.gold
    val online: Color get() = extended.online
    val offline: Color get() = extended.offline
    val rarity: Color get() = extended.rarity
    val gameQuality: Color get() = extended.gameQuality
    val divider: Color get() = extended.divider
    val disabled: Color get() = extended.disabled
    val shimmer: Color get() = extended.shimmer
}

val LocalAppColors = compositionLocalOf<AppColors> {
    error("No AppColors provided — 请确保 AppTheme 包裹了根 Composable")
}

// ============================================================
// HSL Color Utilities — 从 seed 派生 Material3 tonal palette
// ============================================================

private data class Hsl(val h: Float, val s: Float, val l: Float)

private fun Color.toHsl(): Hsl {
    val r = red
    val g = green
    val b = blue
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val l = (max + min) / 2f

    if (max == min) return Hsl(0f, 0f, l)

    val d = max - min
    val s = if (l > 0.5f) d / (2f - max - min) else d / (max + min)
    val h = when (max) {
        r -> ((g - b) / d + if (g < b) 6f else 0f) / 6f
        g -> ((b - r) / d + 2f) / 6f
        else -> ((r - g) / d + 4f) / 6f
    }
    return Hsl(h * 360f, s.coerceIn(0f, 1f), l.coerceIn(0f, 1f))
}

private fun hslColor(h: Float, s: Float, l: Float, alpha: Float = 1f): Color {
    val hh = (h % 360f).let { if (it < 0) it + 360f else it } / 360f
    val ss = s.coerceIn(0f, 1f)
    val ll = l.coerceIn(0f, 1f)

    if (ss == 0f) return Color(ll, ll, ll, alpha)

    fun hue2rgb(p: Float, q: Float, t: Float): Float {
        var tt = t
        if (tt < 0f) tt += 1f
        if (tt > 1f) tt -= 1f
        return when {
            tt < 1f / 6f -> p + (q - p) * 6f * tt
            tt < 1f / 2f -> q
            tt < 2f / 3f -> p + (q - p) * (2f / 3f - tt) * 6f
            else -> p
        }
    }

    val q = if (ll < 0.5f) ll * (1f + ss) else ll + ss - ll * ss
    val p = 2f * ll - q
    val r = hue2rgb(p, q, hh + 1f / 3f)
    val g = hue2rgb(p, q, hh)
    val b = hue2rgb(p, q, hh - 1f / 3f)
    return Color(r, g, b, alpha)
}

// ============================================================
// Seed-based ColorScheme Generators
// ============================================================

fun seedLightColorScheme(seed: Color = DefaultSeedColor): ColorScheme {
    val (h, s, _) = seed.toHsl()
    val neutralHue = h

    return lightColorScheme(
        primary = hslColor(h, s, 0.40f),
        onPrimary = Color.White,
        primaryContainer = hslColor(h, min(s, 0.70f), 0.90f),
        onPrimaryContainer = hslColor(h, s, 0.10f),
        inversePrimary = hslColor(h, min(s, 0.70f), 0.75f),

        secondary = hslColor(h, min(s, 0.24f), 0.40f),
        onSecondary = Color.White,
        secondaryContainer = hslColor(h, min(s, 0.24f), 0.90f),
        onSecondaryContainer = hslColor(h, min(s, 0.24f), 0.10f),

        tertiary = hslColor((h + 60f) % 360f, min(s, 0.40f), 0.40f),
        onTertiary = Color.White,
        tertiaryContainer = hslColor((h + 60f) % 360f, min(s, 0.40f), 0.90f),
        onTertiaryContainer = hslColor((h + 60f) % 360f, min(s, 0.40f), 0.10f),

        background = hslColor(neutralHue, min(s, 0.04f), 0.98f),
        onBackground = hslColor(neutralHue, min(s, 0.04f), 0.10f),
        surface = hslColor(neutralHue, min(s, 0.04f), 0.99f),
        onSurface = hslColor(neutralHue, min(s, 0.04f), 0.10f),
        surfaceVariant = hslColor(neutralHue, min(s, 0.12f), 0.92f),
        onSurfaceVariant = hslColor(neutralHue, min(s, 0.12f), 0.25f),
        surfaceTint = hslColor(h, s, 0.40f),

        inverseSurface = hslColor(neutralHue, min(s, 0.12f), 0.15f),
        inverseOnSurface = hslColor(neutralHue, min(s, 0.04f), 0.95f),

        surfaceBright = hslColor(neutralHue, min(s, 0.04f), 0.98f),
        surfaceDim = hslColor(neutralHue, min(s, 0.04f), 0.88f),
        surfaceContainer = hslColor(neutralHue, min(s, 0.06f), 0.93f),
        surfaceContainerHigh = hslColor(neutralHue, min(s, 0.06f), 0.90f),
        surfaceContainerHighest = hslColor(neutralHue, min(s, 0.06f), 0.88f),
        surfaceContainerLow = hslColor(neutralHue, min(s, 0.06f), 0.96f),
        surfaceContainerLowest = Color.White,

        error = Color(0xFFBA1A1A),
        onError = Color.White,
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),

        outline = hslColor(neutralHue, min(s, 0.08f), 0.45f),
        outlineVariant = hslColor(neutralHue, min(s, 0.08f), 0.75f),
        scrim = Color.Black,
    )
}

fun seedDarkColorScheme(seed: Color = DefaultSeedColor): ColorScheme {
    val (h, s, _) = seed.toHsl()
    val neutralHue = h

    return darkColorScheme(
        primary = hslColor(h, min(s, 0.70f), 0.75f),
        onPrimary = hslColor(h, s, 0.15f),
        primaryContainer = hslColor(h, s, 0.30f),
        onPrimaryContainer = hslColor(h, min(s, 0.70f), 0.90f),
        inversePrimary = hslColor(h, s, 0.40f),

        secondary = hslColor(h, min(s, 0.24f), 0.75f),
        onSecondary = hslColor(h, min(s, 0.24f), 0.15f),
        secondaryContainer = hslColor(h, min(s, 0.24f), 0.30f),
        onSecondaryContainer = hslColor(h, min(s, 0.24f), 0.90f),

        tertiary = hslColor((h + 60f) % 360f, min(s, 0.40f), 0.75f),
        onTertiary = hslColor((h + 60f) % 360f, min(s, 0.40f), 0.15f),
        tertiaryContainer = hslColor((h + 60f) % 360f, min(s, 0.40f), 0.30f),
        onTertiaryContainer = hslColor((h + 60f) % 360f, min(s, 0.40f), 0.90f),

        background = hslColor(neutralHue, min(s, 0.06f), 0.08f),
        onBackground = hslColor(neutralHue, min(s, 0.04f), 0.90f),
        surface = hslColor(neutralHue, min(s, 0.06f), 0.10f),
        onSurface = hslColor(neutralHue, min(s, 0.04f), 0.90f),
        surfaceVariant = hslColor(neutralHue, min(s, 0.10f), 0.18f),
        onSurfaceVariant = hslColor(neutralHue, min(s, 0.10f), 0.75f),
        surfaceTint = hslColor(h, min(s, 0.70f), 0.75f),

        inverseSurface = hslColor(neutralHue, min(s, 0.04f), 0.90f),
        inverseOnSurface = hslColor(neutralHue, min(s, 0.06f), 0.15f),

        surfaceBright = hslColor(neutralHue, min(s, 0.06f), 0.22f),
        surfaceDim = hslColor(neutralHue, min(s, 0.06f), 0.08f),
        surfaceContainer = hslColor(neutralHue, min(s, 0.06f), 0.14f),
        surfaceContainerHigh = hslColor(neutralHue, min(s, 0.06f), 0.18f),
        surfaceContainerHighest = hslColor(neutralHue, min(s, 0.06f), 0.22f),
        surfaceContainerLow = hslColor(neutralHue, min(s, 0.06f), 0.11f),
        surfaceContainerLowest = hslColor(neutralHue, min(s, 0.06f), 0.06f),

        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),

        outline = hslColor(neutralHue, min(s, 0.06f), 0.50f),
        outlineVariant = hslColor(neutralHue, min(s, 0.08f), 0.28f),
        scrim = Color.Black,
    )
}

// ============================================================
// ExtendedColors — 从 ColorScheme 派生的亮/暗业务色
// ============================================================

fun lightExtendedColors() = ExtendedColors(
    textColor = Color(0xFF1C1B1F),
    success = Color(0xFF16A34A),
    warning = Color(0xFFF59E0B),
    danger = Color(0xFFDC2626),
    info = Color(0xFF0EA5E9),
    vip = Color(0xFFD97706),
    gold = Color(0xFFEAB308),
    online = Color(0xFF22C55E),
    offline = Color(0xFF9CA3AF),
    rarity = Color(0xFF8B5CF6),
    gameQuality = Color(0xFFF97316),
    divider = Color(0xFFE5E7EB),
    disabled = Color(0xFFD1D5DB),
    shimmer = Color(0xFFF3F4F6),
)

fun darkExtendedColors() = ExtendedColors(
    textColor = Color(0xFFE6E1E5),
    success = Color(0xFF4ADE80),
    warning = Color(0xFFFBBF24),
    danger = Color(0xFFF87171),
    info = Color(0xFF38BDF8),
    vip = Color(0xFFF59E0B),
    gold = Color(0xFFFACC15),
    online = Color(0xFF4ADE80),
    offline = Color(0xFF6B7280),
    rarity = Color(0xFFA78BFA),
    gameQuality = Color(0xFFFB923C),
    divider = Color(0xFF374151),
    disabled = Color(0xFF4B5563),
    shimmer = Color(0xFF374151),
)
