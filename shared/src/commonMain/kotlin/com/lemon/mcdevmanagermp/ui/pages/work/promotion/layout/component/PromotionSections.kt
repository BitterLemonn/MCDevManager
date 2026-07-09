package com.lemon.mcdevmanagermp.ui.pages.work.promotion.layout.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ComposableImageOptions
import com.lemon.mcdevmanagermp.data.consts.enums.PromotionPositionEnum
import com.lemon.mcdevmanagermp.data.consts.enums.PromotionStatusEnum
import com.lemon.mcdevmanagermp.data.vo.netease.promotion.UserApplyItemVO
import com.lemon.mcdevmanagermp.domain.promotion.PromotionTemplate
import com.lemon.mcdevmanagermp.platform.cropImageToRect
import com.lemon.mcdevmanagermp.platform.imageSize
import com.lemon.mcdevmanagermp.platform.validatePromoImage
import com.lemon.mcdevmanagermp.ui.components.FieldLabel
import com.lemon.mcdevmanagermp.ui.components.FormSection
import com.lemon.mcdevmanagermp.ui.components.ImageCropDialog
import com.lemon.mcdevmanagermp.ui.components.LocalSnackbarHostState
import com.lemon.mcdevmanagermp.ui.components.ModSearchSelectField
import com.lemon.mcdevmanagermp.ui.components.RichDetailForm
import com.lemon.mcdevmanagermp.ui.pages.work.promotion.PromotionAction
import com.lemon.mcdevmanagermp.ui.pages.work.promotion.PromotionState
import com.lemon.mcdevmanagermp.ui.pages.work.promotion.PromotionTab
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import com.lemon.mcdevmanagermp.utils.extension.toDateString
import com.lemon.mcdevmanagermp.utils.extension.toDateTimeString
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_add
import mcdevmanagermpr.shared.generated.resources.ic_close
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Instant

/**
 * PE 轮播图申请页面共享组件集合（三布局统一调用）。
 */

@Composable
internal fun PromotionLoadingBox(modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp), color = colors.primary, strokeWidth = 3.dp
        )
    }
}

@Composable
internal fun PromotionEmptyState(
    onRefresh: () -> Unit, modifier: Modifier = Modifier, message: String = "暂无可申请日期"
) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message, style = MaterialTheme.typography.titleMedium, color = colors.textColor)
        Spacer(Modifier.height(12.dp))
        FilledTonalButton(onClick = onRefresh) {
            Text(
                "刷新",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * 可申请周选择：每个 permit key 视为一周申请（周一→周日），整行 7 天横排展示，点选整周。
 */
@Composable
internal fun PromotionDatesSection(
    state: PromotionState, onAction: (PromotionAction) -> Unit, modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val zone = remember { TimeZone.of("Asia/Shanghai") }
    // 每个 permit timestamp → 所在周的周一；按周一去重（一个 permit key = 一周），按时间升序
    val weeks = remember(state.availableDates) {
        state.availableDates.map { ts ->
            val d = Instant.fromEpochSeconds(ts).toLocalDateTime(zone).date
            d.minus(d.dayOfWeek.ordinal, DateTimeUnit.DAY) to ts // Monday to timestamp
        }.distinctBy { it.first }.sortedBy { it.second }
    }
    val selectedMonday = state.selectedStartTime?.let {
        val d = Instant.fromEpochSeconds(it).toLocalDateTime(zone).date
        d.minus(d.dayOfWeek.ordinal, DateTimeUnit.DAY)
    }

    FormSection(title = "可申请周", modifier = modifier) {
        if (weeks.isEmpty()) {
            Text(
                "暂无可申请周",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
            return@FormSection
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            weeks.forEach { (monday, ts) ->
                PromotionWeekRow(
                    monday = monday,
                    selected = monday == selectedMonday,
                    onClick = { onAction(PromotionAction.SelectDate(ts)) })
            }
        }
    }
}

/**
 * 一周条带：周一→周日 7 天横排（星期 + 月/日），整行点击选中。
 */
@Composable
private fun PromotionWeekRow(
    monday: LocalDate, selected: Boolean, onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    val weekdays = listOf("一", "二", "三", "四", "五", "六", "日")
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(if (selected) colors.primaryContainer.copy(alpha = 0.5f) else colors.surfaceContainerHigh)
            .then(
                if (selected) Modifier.border(
                    1.dp, colors.primary, RoundedCornerShape(12.dp)
                ) else Modifier
            ).clickable(onClick = onClick).padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until 7) {
            val d = monday.plus(i, DateTimeUnit.DAY)
            val weekend = i >= 5
            Column(
                modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    weekdays[i],
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant
                )
                Text(
                    "${d.month.number}/${d.day}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) colors.onPrimaryContainer else colors.textColor,
                    fontWeight = if (weekend) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

/**
 * 申请表单：作品选择 + 宣传图上传 + 4 个描述 + 提交。
 */
@Composable
internal fun PromotionApplyForm(
    state: PromotionState, onAction: (PromotionAction) -> Unit, modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val busy = state.isUploading || state.isSubmitting
    FormSection(title = "申请信息", modifier = modifier) {
        if (state.editingApplicationId != null) {
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .background(colors.warning.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "修改模式：保存后将覆盖原申请",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.warning,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { onAction(PromotionAction.CancelModify) }) { Text("取消") }
            }
            Spacer(Modifier.height(12.dp))
        }
        // 参与作品
        ModSearchSelectField(
            label = "参与作品",
            results = state.searchResults,
            isLoading = false,
            selected = listOfNotNull(state.selectedItem),
            onSearch = { onAction(PromotionAction.SearchItems(it)) },
            onSelect = { onAction(PromotionAction.SelectItem(it)) },
            onRemove = { onAction(PromotionAction.RemoveItem) },
            placeholder = "搜索 PE 作品名称",
            required = true,
            multiSelect = false
        )

        Spacer(Modifier.height(16.dp))

        // 宣传图
        FieldLabel(text = "宣传图", required = true)
        PromoImagePicker(
            image = state.promoImage,
            imageUrl = state.promoImageUrl,
            enabled = !busy,
            onSelect = { onAction(PromotionAction.SelectPromoImage(it)) },
            onRemove = { onAction(PromotionAction.RemovePromoImage) })

        // 描述信息（HTML 富文本）+ 一键同步所选作品 PE 详细信息到以下全部文本框
        Spacer(Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "描述信息",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textColor
                )
                Text(
                    text = "可一键同步所选作品的 PE 详细信息到以下所有文本框",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                FilledTonalButton(
                    onClick = { onAction(PromotionAction.SyncFromPe) },
                    enabled = !busy && !state.isSyncing && state.selectedItem != null,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (state.isSyncing) {
                        CircularProgressIndicator(
                            Modifier.size(16.dp), colors.primary, strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Filled.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(if (state.isSyncing) "同步中" else "同步 PE 详情")
                }
                FilledTonalButton(
                    onClick = { onAction(PromotionAction.ShowTemplatePicker) },
                    enabled = !busy,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        Icons.Filled.Bookmark,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("模板")
                }
            }
        }
        RichDetailForm(
            title = "活动信息",
            html = state.activity,
            echoKey = state.syncEchoKey,
            onHtmlChange = { onAction(PromotionAction.UpdateActivity(it)) },
            showPreviewButton = false
        )
        RichDetailForm(
            title = "特性",
            html = state.feature,
            echoKey = state.syncEchoKey,
            onHtmlChange = { onAction(PromotionAction.UpdateFeature(it)) },
            showPreviewButton = false
        )
        RichDetailForm(
            title = "更新内容",
            html = state.update,
            echoKey = state.syncEchoKey,
            onHtmlChange = { onAction(PromotionAction.UpdateUpdate(it)) },
            showPreviewButton = false
        )

        Spacer(Modifier.height(8.dp))

        // 提交
        FilledTonalButton(
            onClick = { onAction(PromotionAction.Submit) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !busy
        ) {
            if (busy) {
                CircularProgressIndicator(Modifier.size(18.dp), colors.primary, strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = when {
                    state.isUploading -> "上传宣传图..."
                    state.isSubmitting -> "提交中..."
                    state.editingApplicationId != null -> "保存修改"
                    else -> "提交申请"
                }, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold
            )
        }

        if (state.showSaveTemplateDialog) {
            SaveTemplateDialog(
                defaultName = state.saveTemplateName,
                onConfirm = { onAction(PromotionAction.SaveTemplate(it)) },
                onDismiss = { onAction(PromotionAction.DismissSaveTemplateDialog) })
        }
        if (state.showTemplatePicker) {
            TemplatePickerDialog(
                templates = state.templates,
                onApply = { onAction(PromotionAction.ApplyTemplate(it)) },
                onDelete = { onAction(PromotionAction.DeleteTemplate(it)) },
                onDismiss = { onAction(PromotionAction.DismissTemplatePicker) })
        }
    }
}

/**
 * 单张宣传图选择器：选图 / 预览 / 删除。
 */
@Composable
private fun PromoImagePicker(
    image: PlatformFile?,
    imageUrl: String,
    enabled: Boolean,
    onSelect: (PlatformFile) -> Unit,
    onRemove: () -> Unit
) {
    val colors = LocalAppColors.current
    val scope = rememberCoroutineScope()
    val snackbar = LocalSnackbarHostState.current
    // 待裁剪的原图 + 像素尺寸（null 表示不显示裁剪框）
    var pendingCrop by remember { mutableStateOf<Pair<PlatformFile, IntSize>?>(null) }

    val launcher = rememberFilePickerLauncher(type = FileKitType.Image) { file ->
        if (file == null) return@rememberFilePickerLauncher
        scope.launch {
            val err = validatePromoImage(file)
            if (err != null) {
                snackbar.showSnackbar(err)
                return@launch
            }
            val size = imageSize(file)
            if (size == null) {
                // 无法解码尺寸（iOS 降级），直接用原图
                onSelect(file)
            } else {
                pendingCrop = file to size
            }
        }
    }

    // 优先本地图；否则用模板回填的 URL（套用模板时如同已上传宣传图）
    val displayUri = image?.path ?: imageUrl.ifBlank { null }
    Column {
        Box(
            modifier = Modifier.width(220.dp).aspectRatio(940f / 450f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (displayUri != null) colors.surfaceContainerHigh else colors.surfaceContainerLow)
                .border(
                    1.dp,
                    colors.outlineVariant.copy(alpha = if (displayUri != null) 0.3f else 0.5f),
                    RoundedCornerShape(10.dp)
                )
                .then(if (displayUri == null && enabled) Modifier.clickable { launcher.launch() } else Modifier)) {
            if (displayUri != null) {
                AsyncImage(
                    uri = displayUri,
                    state = rememberAsyncImageState(ComposableImageOptions { crossfade() }),
                    contentDescription = "宣传图预览",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp))
                )
                if (enabled) {
                    Box(
                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(22.dp)
                            .clip(CircleShape).background(colors.error.copy(alpha = 0.85f))
                            .clickable(onClick = onRemove), contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_close),
                            contentDescription = "删除",
                            modifier = Modifier.size(14.dp),
                            tint = colors.onError
                        )
                    }
                }
            } else if (enabled) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_add),
                        contentDescription = "上传宣传图",
                        modifier = Modifier.size(24.dp),
                        tint = colors.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "上传图片",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "940×450，仅 PNG / JPG / JPEG，≤ 10MB",
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant
        )
    }

    pendingCrop?.let { (file, size) ->
        ImageCropDialog(
            imageUri = file.path,
            origSize = size,
            aspectRatio = 940f / 450f,
            onConfirm = { rect ->
                scope.launch {
                    val result = cropImageToRect(file, rect, 940, 450)
                    pendingCrop = null
                    if (result == null) {
                        snackbar.showSnackbar("裁剪失败，请重试")
                    } else {
                        onSelect(result.file)
                    }
                }
            },
            onDismiss = { pendingCrop = null })
    }
}

/**
 * 提交成功后的保存模板询问框：输入模板名，确认即保存当前 4 个描述字段。
 */
@Composable
private fun SaveTemplateDialog(
    defaultName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    var name by remember { mutableStateOf(defaultName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("保存为模板") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "是否将本次提交的描述信息保存为模板，供下次一键应用？",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("模板名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(name.trim().ifBlank { defaultName })
                }
            ) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("不保存") } }
    )
}

/**
 * 模板选择框：列出已保存模板，支持应用 / 删除。
 */
@Composable
private fun TemplatePickerDialog(
    templates: List<PromotionTemplate>,
    onApply: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    AlertDialog(onDismissRequest = onDismiss, title = { Text("应用模板") }, text = {
        if (templates.isEmpty()) {
            Text(
                "暂无模板",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        } else {
            Column(
                modifier = Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                templates.forEach { t ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceContainerHigh)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = t.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textColor,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        TextButton(onClick = { onApply(t.id) }) { Text("应用") }
                        IconButton(
                            onClick = { onDelete(t.id) }, modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_close),
                                contentDescription = "删除",
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }, confirmButton = {}, dismissButton = { TextButton(onClick = onDismiss) { Text("关闭") } })
}

/**
 * 我的申请记录：列出已提交的轮播图申请及审核状态。
 */
@Composable
internal fun PromotionApplicationsSection(
    state: PromotionState, onAction: (PromotionAction) -> Unit, modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    FormSection(title = "我的申请", modifier = modifier) {
        if (state.userApplies.isEmpty()) {
            Text(
                "暂无申请记录",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
            return@FormSection
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.userApplies.forEach { item ->
                UserApplyCard(item = item) {
                    onAction(PromotionAction.StartModifyApply(item))
                }
            }
            if (state.userApplies.size < state.userApplyCount) {
                FilledTonalButton(
                    onClick = { onAction(PromotionAction.LoadMoreUserApply) },
                    enabled = !state.isLoadingMoreUserApply,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isLoadingMoreUserApply) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp), strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("加载中…")
                    } else {
                        Text("加载更多（${state.userApplies.size}/${state.userApplyCount}）")
                    }
                }
            }
        }
    }
}

@Composable
private fun UserApplyCard(item: UserApplyItemVO, onModify: () -> Unit) {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceContainerHigh).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.itemName.ifBlank { item.itemId },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            ApplyStatusChip(status = PromotionStatusEnum.fromValue(item.status, item.startTime))
        }
        Row {
            Column {
                if (item.positionName.isNotBlank()) {
                    Text(
                        text = "广告位: ${PromotionPositionEnum.fromValue(item.positionName).label}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant
                    )
                }
                Text(
                    text = "展示: ${item.startTime.toLong().toDateString()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant
                )
                Text(
                    text = "申请: ${item.applyTime.toLong().toDateTimeString()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant
                )
            }
            if (isEditableStatus(item.status)) {
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onModify) { Text("修改") }
            }
        }
    }
}

/**
 * 是否可修改（审核中）。
 * ponytail: 可改状态集合待后端确认，先按"非终态"粗判；后端明确后收紧。
 */
private fun isEditableStatus(status: String): Boolean = status in setOf(
    "reviewing", "reviewing_1", "reviewing_2", "init", "to_modify", "ready"
)

/**
 * 审核状态标签：颜色按 [PromotionStatusEnum] 分组。
 * - success：展示中 / 竞拍成功
 * - warning：审核中 / 待官方一审·二审 / 待支付 / 待上线 / 待更正修改
 * - danger：审核不通过 / 竞拍失败 / 支付超时 / 自动下线 / 强制下线 / 资源位取消
 * - info：展示完成 / 已过期 / 初始状态 / 替补队列 / 未知
 */
@Composable
private fun ApplyStatusChip(status: PromotionStatusEnum) {
    val colors = LocalAppColors.current
    val color = when (status) {
        PromotionStatusEnum.SHOWING, PromotionStatusEnum.AUCTION_SUCCESS -> colors.success
        PromotionStatusEnum.REVIEWING, PromotionStatusEnum.REVIEWING_1, PromotionStatusEnum.REVIEWING_2,
        PromotionStatusEnum.PAYABLE, PromotionStatusEnum.READY, PromotionStatusEnum.TO_MODIFY -> colors.warning

        PromotionStatusEnum.REJECT, PromotionStatusEnum.AUCTION_FAIL, PromotionStatusEnum.PAY_OVERTIME,
        PromotionStatusEnum.AUTO_OFFLINE, PromotionStatusEnum.FORCE_OFFLINE, PromotionStatusEnum.CANCEL -> colors.danger

        else -> colors.info // SHOWING_COMPLETE / EXPIRED / INIT / UNSELECTED / UNKNOWN
    }
    Box(
        modifier = Modifier.background(color.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 申请 / 历史 视图切换栏（Compact/Medium 用）。风格同 ActivityParticipate 的 ModuleTabRow。
 */
@Composable
internal fun PromotionTabBar(
    selectedTab: PromotionTab,
    onSelect: (PromotionTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val tabs = listOf(PromotionTab.APPLY to "申请", PromotionTab.HISTORY to "我的申请")
    val selectedIndex = tabs.indexOfFirst { it.first == selectedTab }.coerceAtLeast(0)
    SecondaryTabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier.clip(RoundedCornerShape(12.dp)),
        containerColor = colors.surfaceContainerHigh,
        contentColor = colors.textColor,
        indicator = {
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(selectedIndex),
                color = colors.primary,
                height = 3.dp
            )
        }
    ) {
        tabs.forEachIndexed { index, (tab, title) ->
            Tab(
                selected = index == selectedIndex,
                onClick = { onSelect(tab) },
                text = {
                    Text(
                        text = title,
                        fontWeight = if (index == selectedIndex) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (index == selectedIndex) colors.primary else colors.onSurfaceVariant
                    )
                }
            )
        }
    }
}

/**
 * Compact/Medium 共用内容：TabBar + 按 selectedTab 分发申请区（含 loading/empty）或历史区。
 * scrollState 由外层传入，用于 CollapsingTopBar 折叠联动。
 */
@Composable
internal fun PromotionTabbedContent(
    state: PromotionState,
    onAction: (PromotionAction) -> Unit,
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        PromotionTabBar(
            selectedTab = state.selectedTab,
            onSelect = { onAction(PromotionAction.SelectTab(it)) },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        when (state.selectedTab) {
            PromotionTab.APPLY -> when {
                state.isLoading && state.permit.isEmpty() ->
                    PromotionLoadingBox(Modifier.fillMaxSize())

                state.permit.isEmpty() && state.reason.isEmpty() ->
                    PromotionEmptyState(
                        onRefresh = { onAction(PromotionAction.Refresh) },
                        modifier = Modifier.fillMaxSize()
                    )

                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PromotionDatesSection(state = state, onAction = onAction)
                    PromotionApplyForm(state = state, onAction = onAction)
                    Spacer(Modifier.height(16.dp))
                }
            }

            PromotionTab.HISTORY -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PromotionApplicationsSection(state = state, onAction = onAction)
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}


