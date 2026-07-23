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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lemon.mcdevmanagermp.platform.AppUpdateManager
import com.lemon.mcdevmanagermp.platform.BackHandler
import com.lemon.mcdevmanagermp.platform.openUrl
import com.lemon.mcdevmanagermp.platform.supportsDynamicColor
import com.lemon.mcdevmanagermp.ui.components.CollapsingTopBar
import com.lemon.mcdevmanagermp.ui.components.LocalWindowWidthSizeClass
import com.lemon.mcdevmanagermp.ui.iconpack.Feedback
import com.lemon.mcdevmanagermp.ui.iconpack.IconPack
import com.lemon.mcdevmanagermp.ui.iconpack.License
import com.lemon.mcdevmanagermp.ui.iconpack.Setting
import com.lemon.mcdevmanagermp.ui.iconpack.Star
import com.lemon.mcdevmanagermp.ui.iconpack.User
import com.lemon.mcdevmanagermp.ui.pages.settings.about.AboutPage
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
import com.lemon.mcdevmanagermp.utils.LogFileInfo
import com.lemon.mcdevmanagermp.utils.Logger
import com.lemon.mcdevmanagermp.utils.extension.formatDecimal
import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.Notification
import com.mohamedrejeb.calf.permissions.Permission
import com.mohamedrejeb.calf.permissions.rememberPermissionState
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.writeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_download
import org.jetbrains.compose.resources.painterResource

private enum class SettingsSubPage { List, Theme, Account, Log, About }

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingsContent(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToAddAccount: () -> Unit = {},
    onAccountSwitched: () -> Unit = {},
    showAccountManagement: Boolean = true,
    startAtAccount: Boolean = false,
    onBack: (() -> Unit)? = null,
    onCheckUpdate: (() -> Unit)? = null
) {
    var currentSubPage by remember {
        mutableStateOf(if (startAtAccount) SettingsSubPage.Account else SettingsSubPage.List)
    }
    val currentVersion = remember { AppUpdateManager().getCurrentVersion() }

    // 独立使用时（如 Route.Settings），自建 UpdateViewModel 处理手动检查
    val localUpdateViewModel = if (onCheckUpdate == null) remember { UpdateViewModel() } else null
    val localUpdateState by localUpdateViewModel?.state?.collectAsState()
        ?: remember { mutableStateOf(null) }
    val localNotificationPermissionState =
        if (onCheckUpdate == null) rememberPermissionState(Permission.Notification) else null
    val effectiveOnCheckUpdate: () -> Unit =
        onCheckUpdate ?: { localUpdateViewModel?.dispatch(UpdateAction.CheckUpdate(true)) }

    localUpdateViewModel?.let { vm ->
        LaunchedEffect(Unit) {
            vm.effect.collect { effect ->
                when (effect) {
                    is UpdateEffect.OpenUrl -> openUrl(effect.url)
                    is UpdateEffect.ShowToast -> { /* 由 MainPage 统一处理 */
                    }
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
                onCheckUpdate = effectiveOnCheckUpdate,
                onNavigateToTheme = { currentSubPage = SettingsSubPage.Theme },
                onNavigateToAccount = { currentSubPage = SettingsSubPage.Account },
                onNavigateToAbout = { currentSubPage = SettingsSubPage.About },
                onNavigateToLog = { currentSubPage = SettingsSubPage.Log },
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

            SettingsSubPage.About -> AboutPage(
                onBack = { currentSubPage = SettingsSubPage.List }
            )

            SettingsSubPage.Log -> LogViewerPage(
                onBack = { currentSubPage = SettingsSubPage.List }
            )
        }
    }

    // 独立使用时显示本地 UpdateDialog
    if (localUpdateState?.showDialog == true && localUpdateViewModel != null && localNotificationPermissionState != null) {
        UpdateDialog(
            state = localUpdateState!!,
            onAction = localUpdateViewModel::dispatch,
            notificationPermissionState = localNotificationPermissionState
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
    onNavigateToAbout: () -> Unit = {},
    onNavigateToLog: () -> Unit = {},
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
                    iconVector = IconPack.User,
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
                iconVector = IconPack.Setting,
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

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = colors.outlineVariant,
                thickness = 0.5.dp
            )

            SettingsItem(
                iconVector = IconPack.Feedback,
                title = "反馈",
                subtitle = "提交 Bug 或功能建议",
                onClick = { openUrl("https://github.com/BitterLemonn/McDevManagerMP/issues") }
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = colors.outlineVariant,
                thickness = 0.5.dp
            )

            SettingsItem(
                iconVector = IconPack.Star,
                title = "给个星星",
                subtitle = "在 GitHub 上为项目点个 Star",
                onClick = { openUrl("https://github.com/BitterLemonn/McDevManager") }
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = colors.outlineVariant,
                thickness = 0.5.dp
            )

            SettingsItem(
                iconVector = Icons.Default.Description,
                title = "日志查看",
                subtitle = "查看运行日志",
                onClick = onNavigateToLog
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = colors.outlineVariant,
                thickness = 0.5.dp
            )

            SettingsItem(
                iconVector = IconPack.License,
                title = "关于",
                subtitle = "版本信息与开源协议",
                onClick = onNavigateToAbout
            )
        }
    }
}

@Composable
private fun SettingsGroupCard(
    content: @Composable () -> Unit
) {
    val colors = LocalAppColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh),
        shape = RoundedCornerShape(16.dp),

        ) {
        content()
    }
}

@Composable
private fun SettingsItem(
    icon: org.jetbrains.compose.resources.DrawableResource? = null,
    iconVector: ImageVector? = null,
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
            when {
                iconVector != null -> Icon(
                    imageVector = iconVector,
                    contentDescription = title,
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp)
                )

                icon != null -> Icon(
                    painter = painterResource(icon),
                    contentDescription = title,
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(bottom = navBarBottom)
            ) {
                val widthSizeClass = LocalWindowWidthSizeClass.current

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
                imageVector = Icons.Filled.Check,
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh),
        shape = RoundedCornerShape(12.dp),

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

// ============================================================
// Log Viewer Sub-Page
// ============================================================

private enum class LogLevelFilter(val label: String, val token: String?) {
    ALL("全部", null),
    ERROR("ERROR", " [ERROR] "),
    WARN("WARN", " [WARN] "),
    INFO("INFO", " [INFO] "),
    DEBUG("DEBUG", " [DEBUG] ")
}

@Composable
private fun LogViewerPage(
    onBack: () -> Unit
) {
    val colors = LocalAppColors.current
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var logFiles by remember { mutableStateOf<List<LogFileInfo>>(emptyList()) }
    var selectedFile by remember { mutableStateOf<LogFileInfo?>(null) }
    val rawLines = remember { mutableStateListOf<String>() }
    val visibleLines = remember { mutableStateListOf<String>() }
    var levelFilter by remember { mutableStateOf(LogLevelFilter.ALL) }
    var loading by remember { mutableStateOf(true) }
    var confirmClear by remember { mutableStateOf(false) }

    val topBarAlpha by remember {
        derivedStateOf { (listState.firstVisibleItemIndex.toFloat() / 8f).coerceIn(0f, 1f) }
    }

    fun filterChunk(chunk: List<String>): List<String> {
        val token = levelFilter.token
        return if (token == null) chunk else chunk.filter { it.contains(token) }
    }

    fun reapplyFilter() {
        visibleLines.clear()
        visibleLines.addAll(filterChunk(rawLines))
    }

    suspend fun reloadFiles(pickFirst: Boolean) {
        val files = withContext(Dispatchers.Default) { Logger.listLogFiles() }
        logFiles = files
        if (pickFirst) selectedFile = files.firstOrNull()
    }

    suspend fun loadContent(path: okio.Path?) {
        rawLines.clear()
        visibleLines.clear()
        if (path == null) return
        Logger.readLogLines(path)
            .flowOn(Dispatchers.IO)
            .collect { chunk ->
                rawLines.addAll(chunk)
                visibleLines.addAll(filterChunk(chunk))
            }
        if (visibleLines.isNotEmpty()) {
            listState.scrollToItem(visibleLines.lastIndex.coerceAtLeast(0))
        }
    }

    LaunchedEffect(Unit) {
        loading = true
        reloadFiles(pickFirst = true)
        loading = false
    }

    LaunchedEffect(selectedFile) {
        loadContent(selectedFile?.path)
    }

    val saverLauncher = rememberFileSaverLauncher(
        dialogSettings = FileKitDialogSettings.createDefault()
    ) { file ->
        file?.let { f ->
            scope.launch { runCatching { f.writeString(rawLines.joinToString("\n")) } }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(statusBarTop))
            Spacer(Modifier.height(56.dp))

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (logFiles.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(logFiles, key = { it.name }) { file ->
                            LogFilterChip(
                                label = "${file.name}  (${formatSize(file.size)})",
                                selected = file.name == selectedFile?.name,
                                onClick = { selectedFile = file }
                            )
                        }
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(LogLevelFilter.entries.toList()) { level ->
                        LogFilterChip(
                            label = level.label,
                            selected = level == levelFilter,
                            onClick = { levelFilter = level; reapplyFilter() }
                        )
                    }
                }
            }

            when {
                loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("加载中…", color = colors.onSurfaceVariant)
                }

                selectedFile == null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无日志文件", color = colors.onSurfaceVariant)
                }

                rawLines.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("读取中…", color = colors.onSurfaceVariant)
                }

                visibleLines.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("没有匹配的日志", color = colors.onSurfaceVariant)
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    state = listState,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                ) {
                    itemsIndexed(visibleLines, key = { i, _ -> i }) { _, line ->
                        Text(
                            text = remember(line, colors) { logLineAnnotated(line, colors) },
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(navBarBottom))
        }

        CollapsingTopBar(
            title = "日志查看",
            collapseFraction = topBarAlpha,
            actions = {
                IconButton(onClick = {
                    scope.launch {
                        val path = selectedFile?.path
                        reloadFiles(pickFirst = false)
                        loadContent(path)
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "刷新",
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = {
                        val f = selectedFile ?: return@IconButton
                        saverLauncher.launch(
                            suggestedName = f.name.removeSuffix(".log"),
                            defaultExtension = "log"
                        )
                    },
                    enabled = selectedFile != null && rawLines.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "导出",
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = { confirmClear = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "清除日志",
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            onBack = onBack
        )
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("清除全部日志") },
            text = { Text("将删除所有日志文件，此操作不可撤销。") },
            confirmButton = {
                TextButton(onClick = {
                    confirmClear = false
                    scope.launch {
                        withContext(Dispatchers.Default) { Logger.deleteAllLogs() }
                        selectedFile = null
                        rawLines.clear()
                        visibleLines.clear()
                        reloadFiles(pickFirst = false)
                    }
                }) { Text("清除", color = colors.danger) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun LogFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label, style = MaterialTheme.typography.labelMedium) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = colors.primary.copy(alpha = 0.12f),
            selectedLabelColor = colors.primary,
            containerColor = colors.surfaceContainerLow,
            labelColor = colors.onSurfaceVariant
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = colors.outlineVariant,
            selectedBorderColor = colors.primary,
            enabled = true,
            selected = selected
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

private fun logLineAnnotated(
    line: String,
    colors: com.lemon.mcdevmanagermp.ui.theme.AppColors
): AnnotatedString =
    buildAnnotatedString {
        val match = Regex("""\[(ERROR|WARN|INFO|DEBUG)]""").find(line)
        if (match == null) {
            append(line)
            return@buildAnnotatedString
        }
        append(line.substring(0, match.range.first))
        val color = when (match.groupValues[1]) {
            "ERROR" -> colors.error
            "WARN" -> colors.warning
            "INFO" -> colors.info
            else -> colors.onSurfaceVariant
        }
        withStyle(SpanStyle(color = color, fontWeight = FontWeight.SemiBold)) {
            append(match.value)
        }
        append(line.substring(match.range.last + 1))
    }

private fun formatSize(bytes: Long): String = when {
    bytes >= 1024 * 1024 -> "${(bytes / (1024.0 * 1024.0)).formatDecimal(1)} MB"
    bytes >= 1024 -> "${(bytes / 1024.0).formatDecimal(1)} KB"
    else -> "$bytes B"
}
