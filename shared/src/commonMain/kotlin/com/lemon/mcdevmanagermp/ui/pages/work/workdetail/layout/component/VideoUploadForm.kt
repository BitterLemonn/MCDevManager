package com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ComposableImageOptions
import com.lemon.mcdevmanagermp.data.consts.enums.PriceTypeEnum
import com.lemon.mcdevmanagermp.ui.components.FieldLabel
import com.lemon.mcdevmanagermp.ui.components.FormSection
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.VideoItem
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailAction
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailState
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.mimeType

/**
 * 上传视频区块：回显 video_info_list + 选视频即时上传（16:9 / ≤1:30 / ≤50MB / H264）。
 * 上限 1 个；cover 由用户单独上传封面图得到（封面区点击选图）。
 */
@Composable
internal fun VideoUploadForm(
    state: WorkDetailState,
    onAction: (WorkDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val videoPicker = rememberFilePickerLauncher(type = FileKitType.Video) { file: PlatformFile? ->
        if (file != null) onAction(WorkDetailAction.UploadVideo(file))
    }

    FormSection(
        title = "上传视频",
        modifier = modifier,
        required = state.priceType == PriceTypeEnum.DIAMOND ||
                state.priceType == PriceTypeEnum.EMERALD
    ) {
        Text(
            text = "要求: 16:9 比例，时长 1:30 以内，50MB 以内，H264 编码",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(8.dp))

        if (state.videos.isNotEmpty()) {
            state.videos.forEachIndexed { index, video ->
                VideoCard(
                    video = video,
                    isUploadingVideo = state.isUploadingVideo,
                    onRemove = { onAction(WorkDetailAction.RemoveVideo(index)) }
                )
                FieldLabel(text = "视频封面")
                VideoCoverPicker(
                    video = video,
                    enabled = !state.isUploadingVideo,
                    onUpload = { file, mt ->
                        onAction(WorkDetailAction.UploadVideoCover(index, file, mt))
                    }
                )
            }
        } else if (!state.isUploadingVideo) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        1.dp,
                        colors.outlineVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(12.dp)
                    )
                    .background(colors.surfaceContainerLow)
                    .clickable { videoPicker.launch() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "选择视频",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }
        }
        if (state.isUploadingVideo) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = colors.primary,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "上传中...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}

/** 视频文件卡片：大小 + 删除/上传中指示。 */
@Composable
private fun VideoCard(
    video: VideoItem,
    isUploadingVideo: Boolean,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceContainerHigh)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.VideoFile,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "宣传视频",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatSize(video.size),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
        if (isUploadingVideo) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = colors.primary,
                strokeWidth = 2.dp
            )
        } else {
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
                    modifier = Modifier.size(16.dp),
                    tint = colors.error
                )
            }
        }
    }
}

@Composable
private fun VideoCoverPicker(
    video: VideoItem,
    enabled: Boolean,
    onUpload: (PlatformFile, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val picker = rememberFilePickerLauncher(type = FileKitType.Image) { file: PlatformFile? ->
        if (file != null) {
            val mimeType = runCatching { file.mimeType()?.toString() }.getOrNull() ?: "image/jpeg"
            onUpload(file, mimeType)
        }
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(12.dp))
            .background(colors.surfaceContainerLow)
            .clickable(enabled = enabled && !video.isUploadingCover) { picker.launch() },
        contentAlignment = Alignment.Center
    ) {
        when {
            video.isUploadingCover -> CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = colors.primary,
                strokeWidth = 2.dp
            )

            video.cover.isNotEmpty() -> AsyncImage(
                uri = video.cover,
                state = rememberAsyncImageState(ComposableImageOptions { crossfade() }),
                contentDescription = "视频封面，点击替换",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            else -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.VideoFile,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "点击上传视频封面",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}

/** 字节数格式化（整数 KB/MB，KMP 无 String.format）。 */
private fun formatSize(bytes: Long): String {
    if (bytes <= 0) return ""
    val kb = bytes / 1024
    return if (kb >= 1024) "${kb / 1024} MB" else "$kb KB"
}
