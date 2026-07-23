package com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ComposableImageOptions
import com.lemon.mcdevmanagermp.ui.components.FieldLabel
import com.lemon.mcdevmanagermp.ui.components.ImagePreviewOverlay
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path

/**
 * 授权信息图片上传（单张）。非原创模组必填。
 *
 * - 回显：[imageUrl] 为后端已上传的授权图 URL；[localFile] 为用户新选的本地图片，预览优先取本地。
 * - 选图后仅本地预览 + 持有文件引用，提交时再上传（与活动参与页模式一致）。
 * - 点击缩略图可查看大图（缩放）；视觉与活动页 `UploadComponents` 的缩略图 / 添加按钮一致。
 */
@Composable
internal fun CorpProofImageUploader(
    imageUrl: String,
    localFile: PlatformFile?,
    onSelect: (PlatformFile) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    // 本地新选优先预览，否则回显远端 URL；两者皆空视为未上传
    val previewUri = localFile?.path ?: imageUrl
    var showViewer by remember { mutableStateOf(false) }

    val launcher = rememberFilePickerLauncher(
        type = FileKitType.Image
    ) { file: PlatformFile? ->
        file?.let(onSelect)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        FieldLabel(text = "授权信息图片", required = true)
        Spacer(Modifier.height(4.dp))
        Text(
            text = "请如实上传搬运/合作证明截图，如果发生版权纠纷，我们将以此作为判断依据（这条信息不会在客户端展示）中国版售卖的模组，无论是否付费，都将视为商业用途，开发者转载时请准备商业授权许可，许可需要在原贴链接内公开",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        if (previewUri.isNotEmpty()) {
            // 已有图：缩略图（点击查看大图）+ 删除角标
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surfaceContainerHigh)
                    .border(
                        width = 1.dp,
                        color = colors.outlineVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { showViewer = true }
            ) {
                AsyncImage(
                    uri = previewUri,
                    state = rememberAsyncImageState(ComposableImageOptions {
                        crossfade()
                    }),
                    contentDescription = "授权信息图片",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(colors.error.copy(alpha = 0.85f))
                        .clickable(onClick = onRemove),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "删除授权图片",
                        modifier = Modifier.size(12.dp),
                        tint = colors.onError
                    )
                }
            }
        } else {
            // 无图：添加按钮
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(
                        width = 1.dp,
                        color = colors.outlineVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .background(colors.surfaceContainerLow)
                    .clickable(onClick = { launcher.launch() }),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "添加授权图片",
                    modifier = Modifier.size(24.dp),
                    tint = colors.onSurfaceVariant
                )
            }
        }
    }

    // 大图查看（缩放）
    if (showViewer) {
        Dialog(
            onDismissRequest = { showViewer = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            ImagePreviewOverlay(
                imageUrl = previewUri,
                onDismiss = { showViewer = false }
            )
        }
    }
}
