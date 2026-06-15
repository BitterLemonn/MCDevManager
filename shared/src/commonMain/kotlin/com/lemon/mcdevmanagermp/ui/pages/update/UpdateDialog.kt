package com.lemon.mcdevmanagermp.ui.pages.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.domain.update.CheckUpdateResult
import com.lemon.mcdevmanagermp.platform.UpdateStrategy
import com.lemon.mcdevmanagermp.ui.components.RichMarkdownText
import com.lemon.mcdevmanagermp.utils.extension.formatDecimal
import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.PermissionState
import com.mohamedrejeb.calf.permissions.isGranted

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun UpdateDialog(
    state: UpdateState,
    onAction: (UpdateAction) -> Unit,
    notificationPermissionState: PermissionState? = null
) {
    AlertDialog(
        onDismissRequest = {
            if (!state.isDownloading && !state.isPatching) {
                onAction(UpdateAction.DismissDialog)
            }
        },
        title = {
            when {
                state.isChecking -> Text("检查更新")
                state.isPatching -> Text("安装更新")
                state.patchComplete -> Text("更新完成")
                state.isDownloading -> Text("下载中")
                state.checkResult is CheckUpdateResult.UpdateAvailable -> Text("发现新版本!")
                else -> Text("检查更新")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when {
                    // 检查中
                    state.isChecking -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                        }
                        Text(
                            text = "正在检查更新...",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Patch 中
                    state.isPatching -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                        }
                        Text(
                            text = "正在安装更新，请稍候...",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Patch 完成
                    state.patchComplete -> {
                        Text(
                            text = "更新已安装完成，请重启应用以使更新生效。",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // 下载中
                    state.isDownloading -> {
                        val fileSize =
                            (state.checkResult as? CheckUpdateResult.UpdateAvailable)?.fileSize
                                ?: 0L
                        val downloadedBytes = (fileSize.toFloat() * state.downloadProgress).toLong()

                        LinearProgressIndicator(
                            progress = { state.downloadProgress },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${(state.downloadProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (fileSize > 0) {
                                Text(
                                    text = "${formatFileSize(downloadedBytes)} / ${
                                        formatFileSize(
                                            fileSize
                                        )
                                    }",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (state.downloadSpeedBps > 0) {
                            Text(
                                text = formatDownloadSpeed(state.downloadSpeedBps),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 有更新可用
                    state.checkResult is CheckUpdateResult.UpdateAvailable -> {
                        val result = state.checkResult
                        // 版本信息
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "当前版本",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = result.currentVersion,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "→",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "最新版本",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = result.latestVersion,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // 更新日志
                        if (result.releaseNotes.isNotEmpty()) {
                            Column {
                                Text(
                                    text = "更新日志",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(4.dp))
                                RichMarkdownText(
                                    markdown = result.releaseNotes,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .verticalScroll(rememberScrollState()),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // iOS: 提示
                        if (result.strategy == UpdateStrategy.OPEN_BROWSER) {
                            Text(
                                text = "iOS 不支持应用内更新，请前往 GitHub 下载最新版本。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            when {
                state.isChecking || state.isPatching -> {}
                state.patchComplete -> {
                    Button(onClick = { onAction(UpdateAction.RestartApp) }) {
                        Text("重启应用")
                    }
                }

                state.isDownloading -> {
                    TextButton(onClick = { onAction(UpdateAction.DismissDialog) }) {
                        Text("取消下载")
                    }
                }

                state.checkResult is CheckUpdateResult.UpdateAvailable -> {
                    val result = state.checkResult
                    when (result.strategy) {
                        UpdateStrategy.OPEN_BROWSER -> {
                            Button(onClick = { onAction(UpdateAction.OpenInBrowser) }) {
                                Text("前往 GitHub 下载")
                            }
                        }

                        else -> {
                            Button(onClick = {
                                if (notificationPermissionState != null &&
                                    !notificationPermissionState.status.isGranted
                                ) {
                                    notificationPermissionState.launchPermissionRequest()
                                }
                                onAction(UpdateAction.StartDownload)
                            }) {
                                Text("立即更新")
                            }
                        }
                    }
                }
            }
        },
        dismissButton = {
            when {
                state.isDownloading || state.isPatching -> {}
                state.patchComplete -> {}
                state.checkResult is CheckUpdateResult.UpdateAvailable && !state.isDownloading -> {
                    TextButton(onClick = { onAction(UpdateAction.IgnoreVersion) }) {
                        Text("忽略此版本")
                    }
                    TextButton(onClick = { onAction(UpdateAction.DismissDialog) }) {
                        Text("取消")
                    }
                }
            }
        }
    )
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1_000_000 -> "${(bytes / 1_000_000.0).formatDecimal(2)} MB"
        bytes >= 1_000 -> "${(bytes / 1_000.0).formatDecimal(0)} KB"
        else -> "$bytes B"
    }
}

private fun formatDownloadSpeed(bytesPerSecond: Long): String {
    return when {
        bytesPerSecond >= 1_000_000 -> "${(bytesPerSecond / 1_000_000.0).formatDecimal(2)} MB/s"
        bytesPerSecond >= 1_000 -> "${(bytesPerSecond / 1_000.0).formatDecimal(0)} KB/s"
        else -> "$bytesPerSecond B/s"
    }
}
