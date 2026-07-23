package com.lemon.mcdevmanagermp.ui.pages.work.activity.participate.layout.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ComposableImageOptions
import com.lemon.mcdevmanagermp.platform.validateVideoFile
import com.lemon.mcdevmanagermp.ui.pages.work.activity.participate.SelectedImage
import com.lemon.mcdevmanagermp.ui.pages.work.activity.participate.SelectedVideo
import com.lemon.mcdevmanagermp.ui.theme.AppColors
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

/**
 * 图片选择行组件
 * 支持最多 maxImages 张图片选择，显示缩略图和添加按钮
 */
@Composable
fun ImageSelectorRow(
    selectedImages: List<SelectedImage>,
    onAddImages: (List<SelectedImage>) -> Unit,
    onRemoveImage: (Int) -> Unit,
    maxImages: Int = 3,
    enabled: Boolean = true,
    colors: AppColors = LocalAppColors.current
) {
    val scope = rememberCoroutineScope()

    val imageLauncher = rememberFilePickerLauncher(
        type = FileKitType.Image,
        mode = FileKitMode.Multiple(maxItems = maxImages)
    ) { files: List<PlatformFile>? ->
        if (files != null) {
            val images = files.mapNotNull { file ->
                try {
                    SelectedImage(
                        name = file.name,
                        file = file,
                        mimeType = file.mimeType()?.toString() ?: "image/jpeg"
                    )
                } catch (_: Exception) {
                    null
                }
            }
            if (images.isNotEmpty()) {
                onAddImages(images)
            }
        }
    }

    Column {
        // 标题
        Text(
            text = "图片（最多 $maxImages 张，可选）",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = colors.textColor
        )

        Spacer(Modifier.height(8.dp))

        // 图片列表 + 添加按钮
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            selectedImages.forEachIndexed { index, image ->
                ImageThumbnailCard(
                    imageName = image.name,
                    imageUri = image.uri,
                    onRemove = { onRemoveImage(index) },
                    enabled = enabled,
                    colors = colors
                )
            }

            // 添加按钮（未达到上限时显示）
            if (selectedImages.size < maxImages && enabled) {
                AddFileButton(
                    onClick = { imageLauncher.launch() },
                    colors = colors
                )
            }
        }
    }
}

/**
 * 视频选择区域组件
 * 支持单个视频选择，要求 16:9、1:30 以内、50MB 以内、H264 编码
 */
@Composable
fun VideoSelectorBox(
    selectedVideo: SelectedVideo?,
    onAddVideo: (SelectedVideo) -> Unit,
    onRemoveVideo: () -> Unit,
    onValidationError: (String) -> Unit = {},
    enabled: Boolean = true,
    colors: AppColors = LocalAppColors.current
) {
    val scope = rememberCoroutineScope()

    val videoLauncher = rememberFilePickerLauncher(
        type = FileKitType.Video
    ) { file: PlatformFile? ->
        if (file != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    val size = file.size()

                    // 先进行视频元数据校验
                    val validation = validateVideoFile(file, size)
                    if (!validation.isValid) {
                        onValidationError(validation.errorMessage ?: "视频文件不符合要求")
                        return@launch
                    }

                    // 校验通过，保存文件引用（不立即读取内容）
                    onAddVideo(
                        SelectedVideo(
                            name = file.name,
                            file = file,
                            size = size,
                            mimeType = file.mimeType()?.toString() ?: "video/mp4"
                        )
                    )
                } catch (_: Exception) {
                    // 文件访问失败
                }
            }
        }
    }

    Column {
        // 标题 + 要求说明
        Text(
            text = "视频（可选）",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = colors.textColor
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "要求: 16:9 比例，时长 1:30 以内，50MB 以内，H264 编码",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant.copy(alpha = 0.6f)
        )

        Spacer(Modifier.height(8.dp))

        if (selectedVideo != null) {
            // 已选择视频 - 显示文件信息和删除按钮
            VideoInfoCard(
                videoName = selectedVideo.name,
                videoSize = selectedVideo.size,
                onRemove = onRemoveVideo,
                enabled = enabled,
                colors = colors
            )
        } else if (enabled) {
            // 未选择 - 显示添加按钮
            AddVideoButton(
                onClick = { videoLauncher.launch() },
                colors = colors
            )
        }
    }
}

/**
 * 图片缩略图卡片（使用 Sketch 加载本地文件预览）
 */
@Composable
private fun ImageThumbnailCard(
    imageName: String,
    imageUri: String?,
    onRemove: () -> Unit,
    enabled: Boolean,
    colors: AppColors
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surfaceContainerHigh)
            .border(
                width = 1.dp,
                color = colors.outlineVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(10.dp)
            )
    ) {
        if (!imageUri.isNullOrEmpty()) {
            // 使用 Sketch 加载本地文件图片
            AsyncImage(
                uri = imageUri,
                state = rememberAsyncImageState(ComposableImageOptions {
                    crossfade()
                }),
                contentDescription = imageName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(10.dp))
            )
        } else {
            // URI 为空时显示文件名占位
            Text(
                text = imageName.take(10),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 4.dp)
            )
        }

        // 删除按钮
        if (enabled) {
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
                    contentDescription = "删除",
                    modifier = Modifier.size(12.dp),
                    tint = colors.onError
                )
            }
        }
    }
}

/**
 * 视频信息卡片
 */
@Composable
private fun VideoInfoCard(
    videoName: String,
    videoSize: Long,
    onRemove: () -> Unit,
    enabled: Boolean,
    colors: AppColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceContainerHigh)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 视频文件名和大小
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = videoName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colors.textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatFileSize(videoSize),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }

        // 删除按钮
        if (enabled) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(colors.error.copy(alpha = 0.1f))
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "删除视频",
                    modifier = Modifier.size(14.dp),
                    tint = colors.error
                )
            }
        }
    }
}

/**
 * 添加文件按钮
 */
@Composable
private fun AddFileButton(
    onClick: () -> Unit,
    colors: AppColors
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = 1.dp,
                color = colors.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp)
            )
            .background(colors.surfaceContainerLow)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "添加图片",
            modifier = Modifier.size(24.dp),
            tint = colors.onSurfaceVariant
        )
    }
}

/**
 * 添加视频按钮
 */
@Composable
private fun AddVideoButton(
    onClick: () -> Unit,
    colors: AppColors
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = colors.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .background(colors.surfaceContainerLow)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = colors.onSurfaceVariant
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "选择视频",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        }
    }
}

/**
 * 格式化文件大小（Kotlin 多平台兼容）
 */
private fun formatFileSize(size: Long): String {
    if (size < 1024) return "$size B"
    val kb = size / 1024.0
    if (kb < 1024.0) {
        val kbStr = (kb * 10).toLong()
        return "${kbStr / 10}.${kbStr % 10} KB"
    }
    val mb = kb / 1024.0
    val mbStr = (mb * 10).toLong()
    return "${mbStr / 10}.${mbStr % 10} MB"
}
