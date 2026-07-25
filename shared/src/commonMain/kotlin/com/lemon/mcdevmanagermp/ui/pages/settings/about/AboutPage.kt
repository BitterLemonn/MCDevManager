package com.lemon.mcdevmanagermp.ui.pages.settings.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.window.Dialog
import com.lemon.mcdevmanagermp.platform.AppUpdateManager
import com.lemon.mcdevmanagermp.platform.FeaturePreferences
import com.lemon.mcdevmanagermp.ui.components.CollapsingTopBar
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import kotlinx.coroutines.delay
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_icon
import org.jetbrains.compose.resources.painterResource

// ============================================================
// 隐藏功能解锁
// ============================================================

/** 关于页应用图标连点次数达到此阈值，解锁「作品管理 - PE 轮播图申请」入口。 */
private const val UNLOCK_THRESHOLD = 5

// ============================================================
// 开源许可数据
// ============================================================

private data class OpenSourceLib(
    val name: String,
    val author: String,
    val license: String,
    val description: String,
)

private val OPEN_SOURCE_LIBS = listOf(
    OpenSourceLib("Kotlin", "JetBrains", "Apache 2.0", "Kotlin 编程语言"),
    OpenSourceLib("Kotlinx Coroutines", "JetBrains", "Apache 2.0", "Kotlin 协程库"),
    OpenSourceLib("Compose Multiplatform", "JetBrains", "Apache 2.0", "跨平台 UI 框架"),
    OpenSourceLib("Material 3", "Google", "Apache 2.0", "Material Design 3 组件库"),
    OpenSourceLib("Ktor", "JetBrains", "Apache 2.0", "HTTP 客户端框架"),
    OpenSourceLib("Ktorfit", "Jens Klingenberg", "Apache 2.0", "Ktor 声明式 API 封装"),
    OpenSourceLib("kotlinx-serialization", "JetBrains", "Apache 2.0", "Kotlin 序列化框架"),
    OpenSourceLib("Room", "Google", "Apache 2.0", "SQLite ORM 数据库"),
    OpenSourceLib("Navigation Compose", "Google", "Apache 2.0", "Compose 导航框架"),
    OpenSourceLib("Sketch", "Panpf", "Apache 2.0", "跨平台图片加载库"),
    OpenSourceLib("Vico", "Patryk & Patrick", "Apache 2.0", "Compose 图表库"),
    OpenSourceLib("OkIO", "Square", "Apache 2.0", "跨平台 I/O 库"),
    OpenSourceLib("kotlinx-datetime", "JetBrains", "Apache 2.0", "跨平台日期时间库"),
    OpenSourceLib("bignum", "Ionspin", "Apache 2.0", "Kotlin 大数运算库"),
    OpenSourceLib("logback", "QOS.ch", "EPL 1.0 / LGPL 2.1", "Java 日志框架"),
    OpenSourceLib("SLF4J", "QOS.ch", "MIT", "日志门面框架"),
    OpenSourceLib("kotlin-logging", "oshai", "MIT", "Kotlin 日志封装库"),
    OpenSourceLib("Calf", "Mohamed Rejeb", "Apache 2.0", "跨平台权限适配库"),
    OpenSourceLib("Cryptography", "whyoleg", "Apache 2.0", "Kotlin 跨平台加密库"),
    OpenSourceLib("FileKit", "Vincent", "MIT", "跨平台文件选择库"),
    OpenSourceLib("Rich Editor", "Mohamed Rejeb", "Apache 2.0", "Compose 富文本编辑器"),
)

// ============================================================
// 许可证弹窗内容
// ============================================================

private data class LicenseInfo(
    val name: String,
    val summary: String,
)

private val LICENSE_MAP = mapOf(
    "BNCL-1.0" to LicenseInfo(
        name = "BitterLemon Noncommercial Copyleft License 1.0（源码可见许可证）",
        summary = "源码可见许可证。允许查看、下载、学习、修改、编译、运行和分发（含修改版本），但分发时必须提供完整对应源代码与构建脚本，修改版本须以相同许可证分发。禁止任何商业使用，商业使用需另行取得版权所有者授权。",
    ),
    "Apache 2.0" to LicenseInfo(
        name = "Apache License 2.0",
        summary = "允许商业使用、修改、分发、专利授权。要求保留版权声明和许可声明，修改后的文件需注明变更。",
    ),
    "EPL 1.0 / LGPL 2.1" to LicenseInfo(
        name = "Eclipse Public License 1.0 / GNU LGPL 2.1",
        summary = "EPL 是弱 copyleft 许可证，允许与专有软件链接。LGPL 允许以库的形式被专有软件使用，修改库本身需开源。",
    ),
    "MIT" to LicenseInfo(
        name = "MIT License",
        summary = "极其宽松的许可证，仅要求保留版权声明和许可声明。允许商业使用、修改、分发等。",
    ),
)

// ============================================================
// About Page
// ============================================================

@Composable
internal fun AboutPage(
    onBack: () -> Unit,
) {
    val colors = LocalAppColors.current
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val currentVersion = remember { AppUpdateManager().getCurrentVersion() }
    val scrollState = rememberScrollState()
    val topBarAlpha by remember {
        derivedStateOf { (scrollState.value.toFloat() / 100f).coerceIn(0f, 1f) }
    }

    var showAppLicense by remember { mutableStateOf(false) }
    var selectedLib by remember { mutableStateOf<OpenSourceLib?>(null) }
    var iconClickCount by remember { mutableStateOf(0) }
    var unlockHint by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Spacer(Modifier.height(statusBarTop))
            Spacer(Modifier.height(56.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = navBarBottom),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 应用图标（连点 5 次解锁「作品管理 - PE 轮播图申请」隐藏入口）
                Image(
                    painter = painterResource(Res.drawable.ic_icon),
                    contentDescription = "应用图标",
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) {
                            if (FeaturePreferences().isPromotionUnlocked()) {
                                unlockHint = null
                                return@clickable
                            }
                            iconClickCount++
                            unlockHint = when {
                                iconClickCount >= UNLOCK_THRESHOLD -> {
                                    FeaturePreferences().setPromotionUnlocked(true)
                                    "已解锁：作品管理 · PE 轮播图申请"
                                }

                                iconClickCount >= UNLOCK_THRESHOLD - 2 ->
                                    "再点 ${UNLOCK_THRESHOLD - iconClickCount} 次解锁隐藏功能"

                                else -> null
                            }
                        },
                )

                unlockHint?.let { hint ->
                    LaunchedEffect(hint) {
                        delay(1500)
                        unlockHint = null
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = hint,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.primary,
                    )
                }

                Spacer(Modifier.height(16.dp))

                // 应用名称
                Text(
                    text = "MC开发者管理器",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.textColor,
                )

                Spacer(Modifier.height(4.dp))

                // 版本号
                Text(
                    text = if (currentVersion.isNotEmpty()) "v$currentVersion" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )

                Spacer(Modifier.height(12.dp))

                // 本软件开源协议
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showAppLicense = true }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "本软件基于 ",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                    Text(
                        text = "BNCL-1.0（禁止商用）",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.primary,
                    )
                    Text(
                        text = " 开源",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }

                Spacer(Modifier.height(24.dp))

                // 开源协议标题
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "开源许可",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textColor,
                    )
                }

                Spacer(Modifier.height(8.dp))

                // 开源协议列表
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surfaceContainerHigh
                    ),
                    shape = RoundedCornerShape(16.dp),

                ) {
                    OPEN_SOURCE_LIBS.forEachIndexed { index, lib ->
                        LicenseItem(
                            name = lib.name,
                            license = lib.license,
                            description = lib.description,
                            onClick = { selectedLib = lib },
                        )
                        if (index < OPEN_SOURCE_LIBS.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = colors.outlineVariant,
                                thickness = 0.5.dp,
                            )
                        }
                    }
                }
            }
        }

        CollapsingTopBar(
            title = "关于",
            collapseFraction = topBarAlpha,
            onBack = onBack,
        )
    }

    // 本软件许可证弹窗
    if (showAppLicense) {
        AppLicenseDialog(onDismiss = { showAppLicense = false })
    }

    // 第三方许可证详情弹窗
    selectedLib?.let { lib ->
        LicenseDetailDialog(
            lib = lib,
            onDismiss = { selectedLib = null },
        )
    }
}

// ============================================================
// 开源许可条目
// ============================================================

@Composable
private fun LicenseItem(
    name: String,
    license: String,
    description: String,
    onClick: () -> Unit,
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
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = colors.textColor,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }

        Spacer(Modifier.width(8.dp))

        Text(
            text = license,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
            modifier = Modifier
                .background(
                    colors.outlineVariant.copy(alpha = 0.3f),
                    RoundedCornerShape(4.dp),
                )
                .padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

// ============================================================
// 许可证详情弹窗
// ============================================================

@Composable
private fun LicenseDetailDialog(
    lib: OpenSourceLib,
    onDismiss: () -> Unit,
) {
    val colors = LocalAppColors.current
    val licenseInfo = LICENSE_MAP[lib.license]

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors.surfaceContainerHigh
            ),
            shape = RoundedCornerShape(20.dp),

        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            ) {
                // 库名
                Text(
                    text = lib.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textColor,
                )

                // 作者
                Text(
                    text = "作者: ${lib.author}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )

                Spacer(Modifier.height(16.dp))

                // 许可证名称
                Text(
                    text = licenseInfo?.name ?: lib.license,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primary,
                )

                Spacer(Modifier.height(8.dp))

                // 许可证摘要
                Text(
                    text = licenseInfo?.summary ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    lineHeight = 20.sp,
                )

                Spacer(Modifier.height(20.dp))

                // 关闭按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        text = "关闭",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.primary,
                        modifier = Modifier
                            .clickable(onClick = onDismiss)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(8.dp)),
                    )
                }
            }
        }
    }
}

// ============================================================
// 本软件许可证弹窗
// ============================================================

@Composable
private fun AppLicenseDialog(
    onDismiss: () -> Unit,
) {
    val colors = LocalAppColors.current
    val license = LICENSE_MAP["BNCL-1.0"]!!

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors.surfaceContainerHigh
            ),
            shape = RoundedCornerShape(20.dp),

            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            ) {
                Text(
                    text = "MC开发者管理器",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textColor,
                )

                Text(
                    text = "Copyright © 2024-2026 bitterlemon",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = license.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primary,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = license.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    lineHeight = 20.sp,
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        text = "关闭",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.primary,
                        modifier = Modifier
                            .clickable(onClick = onDismiss)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(8.dp)),
                    )
                }
            }
        }
    }
}
