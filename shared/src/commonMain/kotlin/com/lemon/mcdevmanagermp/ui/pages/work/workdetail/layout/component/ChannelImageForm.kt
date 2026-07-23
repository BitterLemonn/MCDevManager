package com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ComposableImageOptions
import com.lemon.mcdevmanagermp.platform.cropImageToRect
import com.lemon.mcdevmanagermp.platform.imageSize
import com.lemon.mcdevmanagermp.platform.validatePromoImage
import com.lemon.mcdevmanagermp.ui.components.FieldLabel
import com.lemon.mcdevmanagermp.ui.components.FormSection
import com.lemon.mcdevmanagermp.ui.components.ImageCropDialog
import com.lemon.mcdevmanagermp.ui.components.ImagePreviewOverlay
import com.lemon.mcdevmanagermp.ui.components.LocalSnackbarHostState
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.ChannelImageSlot
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.launch

/**
 * PE/PC 宣传图编辑区块（通用）。每个 [ChannelImageSlot] 对应一个固定尺寸的图片位：
 * 标题 + 要求尺寸 + 缩略图（回显远端 URL）/ 上传中 / 空占位。
 *
 * 选图流程：FileKit 选图 → [validatePromoImage] 校验 → [imageSize] 读尺寸 → [ImageCropDialog]
 * 按该位 W:H 裁剪 → [cropImageToRect] 缩放到精确尺寸 → [onSelect] 回传（ViewModel 立即上传）。
 * 模式参照 `promotion/layout/component/PromotionSections.kt` 的 PromoImagePicker。
 *
 * @param onSelect 回传 channelId + 裁剪后文件 + mimeType
 * @param onRemove 清空对应 channel 的 URL（本地状态，submit 接入后由服务端落库）
 */
@Composable
internal fun ChannelImageForm(
    title: String,
    slots: List<ChannelImageSlot>,
    onSelect: (channelId: Int, file: PlatformFile, mimeType: String) -> Unit,
    onRemove: (channelId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    FormSection(title = title, modifier = modifier) {
        if (slots.isEmpty()) {
            Text(
                text = "暂无图片位",
                style = MaterialTheme.typography.bodySmall,
                color = LocalAppColors.current.onSurfaceVariant
            )
        } else {
            slots.forEach { slot ->
                ChannelImagePicker(slot = slot, onSelect = onSelect, onRemove = onRemove)
            }
        }
    }
}

@Composable
private fun ChannelImagePicker(
    slot: ChannelImageSlot,
    onSelect: (channelId: Int, file: PlatformFile, mimeType: String) -> Unit,
    onRemove: (channelId: Int) -> Unit
) {
    val colors = LocalAppColors.current
    val scope = rememberCoroutineScope()
    val snackbar = LocalSnackbarHostState.current
    // 待裁剪的原图 + 像素尺寸（null 表示不显示裁剪框）
    var pendingCrop by remember { mutableStateOf<Pair<PlatformFile, IntSize>?>(null) }
    var showViewer by remember { mutableStateOf(false) }

    // 裁剪比例 / 目标尺寸取自 mc_consts.channel 定义；缺失则不可裁剪
    val aspectRatio = if (slot.height > 0) slot.width.toFloat() / slot.height else 1f
    val canCrop = slot.width > 0 && slot.height > 0

    val launcher = rememberFilePickerLauncher(type = FileKitType.Image) { file ->
        if (file == null) return@rememberFilePickerLauncher
        if (!canCrop) {
            scope.launch { snackbar.showSnackbar("该图片位尺寸定义缺失，无法裁剪") }
            return@rememberFilePickerLauncher
        }
        scope.launch {
            val err = validatePromoImage(file)
            if (err != null) {
                snackbar.showSnackbar(err)
                return@launch
            }
            val size = imageSize(file)
            if (size == null) {
                snackbar.showSnackbar("无法读取图片尺寸，请重试")
                return@launch
            }
            pendingCrop = file to size
        }
    }

    val hasImage = slot.channelUrl.isNotEmpty()
    Column(modifier = Modifier.fillMaxWidth()) {
        FieldLabel(text = slot.title.ifEmpty { "图片 ${slot.channelId}" })
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .width(200.dp)
                .aspectRatio(aspectRatio)
                .clip(RoundedCornerShape(10.dp))
                .background(if (hasImage) colors.surfaceContainerHigh else colors.surfaceContainerLow)
                .border(
                    1.dp,
                    colors.outlineVariant.copy(alpha = if (hasImage) 0.3f else 0.5f),
                    RoundedCornerShape(10.dp)
                )
                .then(
                    if (hasImage || slot.isUploading) Modifier
                    else Modifier.clickable { launcher.launch() }
                )
        ) {
            when {
                slot.isUploading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = colors.primary,
                        strokeWidth = 2.dp
                    )
                }

                hasImage -> {
                    AsyncImage(
                        uri = slot.channelUrl,
                        state = rememberAsyncImageState(ComposableImageOptions { crossfade() }),
                        contentDescription = slot.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showViewer = true }
                    )
                    // 删除角标
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(colors.error.copy(alpha = 0.85f))
                            .clickable { onRemove(slot.channelId) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "删除",
                            modifier = Modifier.size(14.dp),
                            tint = colors.onError
                        )
                    }
                }

                else -> Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "上传图片",
                        modifier = Modifier.size(24.dp),
                        tint = colors.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "上传图片",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "${slot.width}×${slot.height}，仅 PNG / JPG / JPEG，≤ 10MB",
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant
        )
    }

    pendingCrop?.let { (file, size) ->
        ImageCropDialog(
            imageUri = file.path,
            origSize = size,
            aspectRatio = aspectRatio,
            onConfirm = { rect ->
                scope.launch {
                    val result = cropImageToRect(file, rect, slot.width, slot.height)
                    pendingCrop = null
                    if (result == null) {
                        snackbar.showSnackbar("裁剪失败，请重试")
                    } else {
                        onSelect(slot.channelId, result.file, result.mimeType)
                    }
                }
            },
            onDismiss = { pendingCrop = null }
        )
    }

    // 大图查看（缩放）
    if (showViewer) {
        Dialog(
            onDismissRequest = { showViewer = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            ImagePreviewOverlay(
                imageUrl = slot.channelUrl,
                onDismiss = { showViewer = false }
            )
        }
    }
}
