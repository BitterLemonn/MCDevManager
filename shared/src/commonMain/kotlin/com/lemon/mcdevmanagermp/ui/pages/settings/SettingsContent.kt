package com.lemon.mcdevmanagermp.ui.pages.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lemon.mcdevmanagermp.platform.AppUpdateManager
import com.lemon.mcdevmanagermp.platform.BackHandler
import com.lemon.mcdevmanagermp.platform.supportsDynamicColor
import com.lemon.mcdevmanagermp.ui.components.CollapsingTopBar
import com.lemon.mcdevmanagermp.ui.pages.settings.account.AccountManagementPage
import com.lemon.mcdevmanagermp.ui.pages.settings.layout.CompactThemeLayout
import com.lemon.mcdevmanagermp.ui.pages.settings.layout.ExpandedThemeLayout
import com.lemon.mcdevmanagermp.ui.pages.settings.layout.MediumThemeLayout
import com.lemon.mcdevmanagermp.ui.pages.update.UpdateAction
import com.lemon.mcdevmanagermp.ui.pages.update.UpdateDialog
import com.lemon.mcdevmanagermp.ui.pages.update.UpdateEffect
import com.lemon.mcdevmanagermp.ui.pages.update.UpdateViewModel
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import com.lemon.mcdevmanagermp.ui.theme.LocalThemeViewModel
import com.lemon.mcdevmanagermp.ui.theme.PredefinedSeedColors
import com.lemon.mcdevmanagermp.ui.theme.ThemeMode
import com.lemon.mcdevmanagermp.ui.theme.seedDarkColorScheme
import com.lemon.mcdevmanagermp.ui.theme.seedLightColorScheme
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_correct
import mcdevmanagermpr.shared.generated.resources.ic_download
import mcdevmanagermpr.shared.generated.resources.ic_setting
import mcdevmanagermpr.shared.generated.resources.ic_user
import org.jetbrains.compose.resources.painterResource

private enum class SettingsSubPage { List, Theme, Account }

@Composable
fun SettingsContent(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToAddAccount: () -> Unit = {},
    onAccountSwitched: () -> Unit = {},
    showAccountManagement: Boolean = true,
    onBack: (() -> Unit)? = null
) {
    var currentSubPage by remember { mutableStateOf(SettingsSubPage.List) }
    val updateViewModel = remember { UpdateViewModel() }
    val updateState by updateViewModel.state.collectAsState()
    val currentVersion = remember { AppUpdateManager().getCurrentVersion() }

    LaunchedEffect(Unit) {
        updateViewModel.effect.collect { effect ->
            when (effect) {
                is UpdateEffect.ShowToast -> {
                    // Toast will be handled by parent via callback
                }

                is UpdateEffect.OpenUrl -> {
                    // Open URL via platform mechanism
                }
            }
        }
    }

    BackHandler(enabled = currentSubPage != SettingsSubPage.List) {
        currentSubPage = SettingsSubPage.List
    }

    AnimatedContent(
        targetState = currentSubPage,
        transitionSpec = {
            if (targetState == SettingsSubPage.List) {
                (slideInHorizontally(tween(300)) { -it } + fadeIn(tween(300)))
                    .togetherWith(slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300)))
            } else {
                (slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)))
                    .togetherWith(slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(300)))
            }
        },
        label = "settings_subpage"
    ) { page ->
        when (page) {
            SettingsSubPage.List -> SettingsListPage(
                currentVersion = currentVersion,
                onCheckUpdate = { updateViewModel.dispatch(UpdateAction.CheckUpdate) },
                onNavigateToTheme = { currentSubPage = SettingsSubPage.Theme },
                onNavigateToAccount = { currentSubPage = SettingsSubPage.Account },
                showAccountManagement = showAccountManagement,
                onBack = onBack
            )

            SettingsSubPage.Theme -> ThemeSettingsPage(
                onBack = { currentSubPage = SettingsSubPage.List }
            )

            SettingsSubPage.Account -> AccountManagementPage(
                onBack = { currentSubPage = SettingsSubPage.List },
                onNavigateToLogin = onNavigateToLogin,
                onNavigateToAddAccount = onNavigateToAddAccount,
                onAccountSwitched = onAccountSwitched
            )
        }
    }

    if (updateState.showDialog) {
        UpdateDialog(
            state = updateState,
            onAction = updateViewModel::dispatch
        )
    }
}

// ============================================================
// Settings List Page
// ============================================================

@Composable
private fun SettingsListPage(
    currentVersion: String = "",
    onCheckUpdate: () -> Unit = {},
    onNavigateToTheme: () -> Unit,
    onNavigateToAccount: () -> Unit = {},
    showAccountManagement: Boolean = true,
    onBack: (() -> Unit)? = null
) {
    val colors = LocalAppColors.current
    val viewModel = LocalThemeViewModel.current
    val themeMode by viewModel.themeMode.collectAsState()
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = if (onBack != null) 0.dp else statusBarTop)
            .padding(bottom = navBarBottom)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (onBack != null) {
            CollapsingTopBar(
                title = "设置",
                collapseFraction = 0f,
                onBack = onBack
            )
            Spacer(Modifier.height(4.dp))
        } else {
            Text(
                text = "设置",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.textColor
            )
            Spacer(Modifier.height(4.dp))
        }

        SettingsGroupCard {
            if (showAccountManagement) {
                SettingsItem(
                    icon = Res.drawable.ic_user,
                    title = "账号管理",
                    subtitle = "切换、添加或退出账号",
                    onClick = onNavigateToAccount
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = colors.outlineVariant,
                    thickness = 0.5.dp
                )
            }

            SettingsItem(
                icon = Res.drawable.ic_setting,
                title = "主题与色彩",
                subtitle = when (themeMode) {
                    ThemeMode.LIGHT -> "浅色"
                    ThemeMode.DARK -> "深色"
                    ThemeMode.SYSTEM -> "跟随系统"
                },
                onClick = onNavigateToTheme
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = colors.outlineVariant,
                thickness = 0.5.dp
            )

            SettingsItem(
                icon = Res.drawable.ic_download,
                title = "检查更新",
                subtitle = if (currentVersion.isNotEmpty()) "当前版本: $currentVersion" else "",
                onClick = onCheckUpdate
            )
        }
    }
}

@Composable
private fun SettingsGroupCard(
    content: @Composable () -> Unit
) {
    val colors = LocalAppColors.current
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = colors.surfaceContainerHigh),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        content()
    }
}

@Composable
private fun SettingsItem(
    icon: org.jetbrains.compose.resources.DrawableResource,
    title: String,
    subtitle: String,
    showArrow: Boolean = true,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val bgColor = if (isHovered) colors.primary.copy(alpha = 0.06f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = title,
                tint = colors.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = colors.textColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }

        if (showArrow) {
            Text(
                text = "›",
                fontSize = 20.sp,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

// ============================================================
// Theme Settings Sub-Page
// ============================================================

@Composable
private fun ThemeSettingsPage(
    onBack: () -> Unit
) {
    val viewModel = LocalThemeViewModel.current
    val themeMode by viewModel.themeMode.collectAsState()
    val seedColor by viewModel.seedColor.collectAsState()
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val scrollState = rememberScrollState()
    val topBarAlpha by remember {
        derivedStateOf {
            (scrollState.value.toFloat() / 100f).coerceIn(0f, 1f)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Spacer(Modifier.height(statusBarTop))
            Spacer(Modifier.height(56.dp))

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(bottom = navBarBottom)
            ) {
                val widthSizeClass = when {
                    maxWidth < 600.dp -> WindowWidthSizeClass.Compact
                    maxWidth < 840.dp -> WindowWidthSizeClass.Medium
                    else -> WindowWidthSizeClass.Expanded
                }

                when (widthSizeClass) {
                    WindowWidthSizeClass.Compact -> CompactThemeLayout(
                        themeMode = themeMode,
                        seedColor = seedColor,
                        useDynamicColor = viewModel.useDynamicColor.collectAsState().value,
                        onThemeModeChange = viewModel::setThemeMode,
                        onSeedColorChange = viewModel::setSeedColor,
                        onDynamicColorChange = viewModel::setUseDynamicColor
                    )

                    WindowWidthSizeClass.Medium -> MediumThemeLayout(
                        themeMode = themeMode,
                        seedColor = seedColor,
                        useDynamicColor = viewModel.useDynamicColor.collectAsState().value,
                        onThemeModeChange = viewModel::setThemeMode,
                        onSeedColorChange = viewModel::setSeedColor,
                        onDynamicColorChange = viewModel::setUseDynamicColor
                    )

                    else -> ExpandedThemeLayout(
                        themeMode = themeMode,
                        seedColor = seedColor,
                        useDynamicColor = viewModel.useDynamicColor.collectAsState().value,
                        onThemeModeChange = viewModel::setThemeMode,
                        onSeedColorChange = viewModel::setSeedColor,
                        onDynamicColorChange = viewModel::setUseDynamicColor
                    )
                }
            }
        }

        CollapsingTopBar(
            title = "主题与色彩",
            collapseFraction = topBarAlpha,
            onBack = onBack
        )
    }
}

// ============================================================
// Section: Theme Mode
// ============================================================

@Composable
internal fun ThemeModeSection(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    val colors = LocalAppColors.current

    SettingsSectionCard(title = "主题模式") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ThemeModeCard(
                label = "浅色",
                modifier = Modifier.weight(1f),
                isSelected = selectedMode == ThemeMode.LIGHT,
                isDark = false,
                seedColor = colors.scheme.primary,
                onClick = { onModeSelected(ThemeMode.LIGHT) }
            )
            ThemeModeCard(
                label = "深色",
                modifier = Modifier.weight(1f),
                isSelected = selectedMode == ThemeMode.DARK,
                isDark = true,
                seedColor = colors.scheme.primary,
                onClick = { onModeSelected(ThemeMode.DARK) }
            )
            ThemeModeCard(
                label = "跟随系统",
                modifier = Modifier.weight(1f),
                isSelected = selectedMode == ThemeMode.SYSTEM,
                isAuto = true,
                seedColor = colors.scheme.primary,
                onClick = { onModeSelected(ThemeMode.SYSTEM) }
            )
        }
    }
}

@Composable
private fun ThemeModeCard(
    label: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isDark: Boolean = false,
    isAuto: Boolean = false,
    seedColor: Color = Color(0xFF4F378B),
    onClick: () -> Unit = {}
) {
    val colors = LocalAppColors.current
    val borderColor = if (isSelected) colors.primary else colors.outlineVariant

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val previewLightColor = seedLightColorScheme(seedColor)
        val previewDarkColor = seedDarkColorScheme(seedColor)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = if (isSelected) 2.5.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (isAuto) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .background(previewLightColor.surface)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .background(previewDarkColor.surface)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(previewLightColor.primary, CircleShape)
                )
            } else {
                val scheme = if (isDark) previewDarkColor else previewLightColor
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(scheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(scheme.primary, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(4.dp)
                                .background(scheme.surfaceVariant, RoundedCornerShape(2.dp))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.4f)
                                .height(4.dp)
                                .background(scheme.surfaceVariant, RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .size(20.dp)
                .border(
                    width = 2.dp,
                    color = if (isSelected) colors.primary else colors.outline,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(colors.primary, CircleShape)
                )
            }
        }

        Spacer(Modifier.height(2.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) colors.primary else colors.onSurfaceVariant
        )
    }
}

// ============================================================
// Section: Seed Color
// ============================================================

@Composable
internal fun SeedColorSection(
    selectedColor: Color,
    useDynamicColor: Boolean,
    onColorSelected: (Color) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit
) {
    val colors = LocalAppColors.current
    val showDynamicToggle = supportsDynamicColor()

    SettingsSectionCard(title = "主题颜色") {
        Text(
            text = "选择一个主题色，应用将基于此颜色生成完整的色彩方案",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )

        if (showDynamicToggle) {
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "动态取色",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = colors.textColor
                    )
                    Text(
                        text = "使用系统壁纸颜色自动生成主题",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
                Switch(
                    checked = useDynamicColor,
                    onCheckedChange = onDynamicColorChange,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = colors.primary,
                        checkedBorderColor = colors.primary,
                    )
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        ColorPaletteGrid(
            colorOptions = PredefinedSeedColors,
            selectedColor = selectedColor,
            onColorSelected = onColorSelected,
            enabled = !useDynamicColor
        )
    }
}

@Composable
private fun ColorPaletteGrid(
    colorOptions: List<com.lemon.mcdevmanagermp.ui.theme.SeedColorOption>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    enabled: Boolean = true
) {
    val columns = 6

    colorOptions.chunked(columns).forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            row.forEach { option ->
                ColorSwatch(
                    color = option.color,
                    isSelected = option.color == selectedColor,
                    enabled = enabled,
                    onClick = { onColorSelected(option.color) }
                )
            }
            repeat(columns - row.size) {
                Spacer(modifier = Modifier.size(40.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    isSelected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val appColors = LocalAppColors.current
    val derivedColors = remember(color) { seedLightColorScheme(color) }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .then(
                if (isSelected) {
                    Modifier.border(3.dp, appColors.primary, CircleShape)
                } else {
                    Modifier.border(1.dp, appColors.outlineVariant, CircleShape)
                }
            )
            .then(
                if (enabled) Modifier.clickable(onClick = onClick) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(40.dp)) {
            val alpha = if (enabled) 1f else 0.4f
            drawArc(
                color = derivedColors.primary,
                startAngle = 90f,
                sweepAngle = 180f,
                useCenter = true,
                alpha = alpha
            )
            drawArc(
                color = derivedColors.primaryContainer,
                startAngle = 270f,
                sweepAngle = 90f,
                useCenter = true,
                alpha = alpha
            )
            drawArc(
                color = derivedColors.secondary,
                startAngle = 0f,
                sweepAngle = 90f,
                useCenter = true,
                alpha = alpha
            )
        }
        if (isSelected) {
            Image(
                painter = painterResource(Res.drawable.ic_correct),
                contentDescription = "Selected",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ============================================================
// Shared: Section Card
// ============================================================

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    val colors = LocalAppColors.current

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = colors.surfaceContainerHigh),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.textColor
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}
