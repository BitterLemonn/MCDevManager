package com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.consts.enums.PePriTypeEnum
import com.lemon.mcdevmanagermp.platform.platformFileFromPath
import com.lemon.mcdevmanagermp.platform.readFilePaths
import com.lemon.mcdevmanagermp.ui.components.FieldLabel
import com.lemon.mcdevmanagermp.ui.components.FormSection
import com.lemon.mcdevmanagermp.ui.components.YesNoSelector
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailAction
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailState
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.inferFileType
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher

/**
 * PE 资源管理区块：资源类别(单选下拉) / 推荐标签(玩法+主题双下拉，共享 item_tag_limit 上限) / 模组畅玩计划 / 坐骑召唤 / 提升版本 / zip 上传(占位框)。
 *
 * 资源文件改为单文件占位框：未上传时显示可点击/可拖入的虚线占位区，已上传时显示文件名 + 删除。
 */
@Composable
internal fun PeResourceManageForm(
    state: WorkDetailState,
    onAction: (WorkDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    // zip 文件选择 → 触发上传
    val zipPicker = rememberFilePickerLauncher(type = FileKitType.File()) { file: PlatformFile? ->
        if (file != null) onAction(WorkDetailAction.UploadPeZip(file))
    }

    FormSection(title = "上传 PE 资源管理", modifier = modifier) {
        // 推荐标签合计上限
        val tagLimit = state.peRecommendTagLimit
        val tagAtLimit = tagLimit > 0 && state.peRecommendTags.size >= tagLimit

        // 资源类别
        var typeExpanded by remember { mutableStateOf(false) }
        val uploadedFileType = state.peResource?.name?.let { inferFileType(it) }
        val personalizeId = PePriTypeEnum.PERSONALIZE.value.toInt()
        val lobbyId = PePriTypeEnum.LOBBY.value.toInt()
        val isCreateMode = state.detail == null
        val typeOptions = state.peResourceTypeOptions.filter { opt ->
            val notPersonalize = opt.id != personalizeId
            val lobbyOk = opt.id != lobbyId || isCreateMode || state.detail.priType == lobbyId
            val fileTypes = state.pePriTypeFileTypes[opt.id]
            val fileTypeOk =
                uploadedFileType == null || fileTypes.isNullOrEmpty() || uploadedFileType in fileTypes
            notPersonalize && lobbyOk && fileTypeOk
        }
        val selectedTypeTitle =
            state.peResourceTypeOptions.firstOrNull { it.id == state.peResourceType }?.title
        Box {
            DropdownField(
                label = "资源类别",
                valueText = selectedTypeTitle ?: "选择资源类别",
                expanded = typeExpanded,
                onClick = { typeExpanded = !typeExpanded },
                required = true
            )
            DropdownMenu(
                expanded = typeExpanded,
                onDismissRequest = { typeExpanded = false }
            ) {
                typeOptions.forEach { opt ->
                    TagCheckItem(
                        title = opt.title,
                        selected = opt.id == state.peResourceType,
                        onClick = {
                            onAction(WorkDetailAction.UpdatePeResourceType(opt.id))
                            typeExpanded = false   // 单选：选完即关
                        }
                    )
                }
            }
        }

        // 具体类别
        val subTypeOptions = state.peResourceSubTypeOptions[state.peResourceType].orEmpty()
        if (subTypeOptions.isNotEmpty()) {
            var subTypeExpanded by remember { mutableStateOf(false) }
            val selectedSubTypeTitle =
                subTypeOptions.firstOrNull { it.id == state.peResourceSubType }?.title
            Box {
                DropdownField(
                    label = "具体类别",
                    valueText = selectedSubTypeTitle ?: "选择具体类别",
                    expanded = subTypeExpanded,
                    onClick = { subTypeExpanded = !subTypeExpanded },
                    required = true
                )
                DropdownMenu(
                    expanded = subTypeExpanded,
                    onDismissRequest = { subTypeExpanded = false }
                ) {
                    subTypeOptions.forEach { opt ->
                        TagCheckItem(
                            title = opt.title,
                            selected = opt.id == state.peResourceSubType,
                            onClick = {
                                onAction(WorkDetailAction.UpdatePeResourceSubType(opt.id))
                                subTypeExpanded = false   // 单选：选完即关
                            }
                        )
                    }
                }
            }
        }

        // 次级分类（单选下拉，必填；仅资源类别=玩法组件 add_ons 且 consts 有 mod_second_type 时显示）
        if (state.peResourceType == PePriTypeEnum.ADD_ONS.value.toInt() &&
            state.peModSecondTypeOptions.isNotEmpty()
        ) {
            val modSecondOptions = state.peModSecondTypeOptions
            var modSecondExpanded by remember { mutableStateOf(false) }
            val selectedModSecondTitle =
                modSecondOptions.firstOrNull { it.id == state.peResourceModSecondType }?.title
            Box {
                DropdownField(
                    label = "次级分类",
                    valueText = selectedModSecondTitle ?: "选择次级分类",
                    expanded = modSecondExpanded,
                    onClick = { modSecondExpanded = !modSecondExpanded },
                    required = true
                )
                DropdownMenu(
                    expanded = modSecondExpanded,
                    onDismissRequest = { modSecondExpanded = false }
                ) {
                    modSecondOptions.forEach { opt ->
                        TagCheckItem(
                            title = opt.title,
                            selected = opt.id == state.peResourceModSecondType,
                            onClick = {
                                onAction(WorkDetailAction.UpdatePeResourceModSecondType(opt.id))
                                modSecondExpanded = false   // 单选：选完即关
                            }
                        )
                    }
                }
            }
        }

        // 推荐标签 · 玩法（下拉多选，必选≥1；与主题共享合计上限）
        var gameplayExpanded by remember { mutableStateOf(false) }
        val selectedGameplayNames = state.peRecommendTagOptions.gameplayTag
            .filter { state.peRecommendTags.contains(it.id) }
            .joinToString("、") { it.title }
        Box {
            DropdownField(
                label = "推荐标签 · 玩法",
                valueText = selectedGameplayNames.ifEmpty { "至少选 1 个" },
                expanded = gameplayExpanded,
                onClick = { gameplayExpanded = !gameplayExpanded },
                required = true
            )
            DropdownMenu(
                expanded = gameplayExpanded,
                onDismissRequest = { gameplayExpanded = false }
            ) {
                state.peRecommendTagOptions.gameplayTag.forEach { tag ->
                    TagCheckItem(
                        title = tag.title,
                        selected = state.peRecommendTags.contains(tag.id),
                        enabled = state.peRecommendTags.contains(tag.id) || !tagAtLimit,
                        onClick = { onAction(WorkDetailAction.TogglePeRecommendTag(tag.id)) }
                    )
                }
            }
        }

        // 推荐标签 · 主题（下拉多选，必选≥1；与玩法共享合计上限）
        var themeExpanded by remember { mutableStateOf(false) }
        val selectedThemeNames = state.peRecommendTagOptions.themeTag
            .filter { state.peRecommendTags.contains(it.id) }
            .joinToString("、") { it.title }
        Box {
            DropdownField(
                label = "推荐标签 · 主题",
                valueText = selectedThemeNames.ifEmpty { "至少选 1 个" },
                expanded = themeExpanded,
                onClick = { themeExpanded = !themeExpanded },
                required = true
            )
            DropdownMenu(
                expanded = themeExpanded,
                onDismissRequest = { themeExpanded = false }
            ) {
                state.peRecommendTagOptions.themeTag.forEach { tag ->
                    TagCheckItem(
                        title = tag.title,
                        selected = state.peRecommendTags.contains(tag.id),
                        enabled = state.peRecommendTags.contains(tag.id) || !tagAtLimit,
                        onClick = { onAction(WorkDetailAction.TogglePeRecommendTag(tag.id)) }
                    )
                }
            }
        }

        // 是否加入模组畅玩计划
        YesNoSelector(
            label = "加入模组畅玩计划",
            value = state.peAddPlayPlan,
            onValueChange = { onAction(WorkDetailAction.TogglePePlayPlan(it)) },
            modifier = Modifier.fillMaxWidth()
        )

        // 是否启用坐骑召唤功能
        YesNoSelector(
            label = "启用坐骑召唤功能",
            value = state.peMountCallEnabled,
            onValueChange = { onAction(WorkDetailAction.TogglePeMountCall(it)) },
            modifier = Modifier.fillMaxWidth()
        )

        // 本次上传是否提升版本
        YesNoSelector(
            label = "提升版本",
            value = state.peAddVersion,
            onValueChange = { onAction(WorkDetailAction.TogglePeAddVersion(it)) },
            modifier = Modifier.fillMaxWidth()
        )

        // 资源文件（单文件占位框：未上传点击/拖入，已上传显示名称 + 删除）
        FieldLabel(text = "资源文件")
        val res = state.peResource
        if (res != null) {
            ResourceFileRow(
                name = res.name,
                meta = listOf(formatSize(res.size), res.mcVersion.joinToString(", "))
                    .filter { it.isNotEmpty() }
                    .joinToString(" · "),
                isUploading = state.isUploadingPeZip,
                onRemove = { onAction(WorkDetailAction.RemovePeResource) }
            )
        } else {
            ResourceDropZone(
                isUploading = state.isUploadingPeZip,
                onPickFile = { zipPicker.launch() },
                onDropFile = { file -> onAction(WorkDetailAction.UploadPeZip(file)) }
            )
        }
    }
}

/**
 * 资源占位框：点击触发文件选择；Desktop 端支持拖入文件（onExternalDrag，移动端 no-op）。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ResourceDropZone(
    isUploading: Boolean,
    onPickFile: () -> Unit,
    onDropFile: (PlatformFile) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    // Desktop 拖放接收：仅取首个文件路径转 PlatformFile 回调；移动端 onDrop 不会触发
    val dropTarget = remember {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {
                val paths = event.readFilePaths()
                paths.firstOrNull()?.let { path ->
                    platformFileFromPath(path)?.let(onDropFile)
                }
                return paths.isNotEmpty()
            }
        }
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(12.dp))
            .clickable(onClick = onPickFile)
            .dragAndDropTarget(shouldStartDragAndDrop = { true }, target = dropTarget),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = colors.primary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Upload,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                text = if (isUploading) "上传中..." else "点击或拖入 zip 文件",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        }
    }
}

/** 单文件展示行：图标 + 名称/大小/版本 + 删除按钮。 */
@Composable
private fun ResourceFileRow(
    name: String,
    meta: String,
    isUploading: Boolean,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceContainerHigh)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.FolderZip,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textColor
            )
            if (meta.isNotEmpty()) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
        }
        if (isUploading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = colors.primary,
                strokeWidth = 2.dp
            )
        } else {
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "移除资源",
                    tint = colors.onSurfaceVariant
                )
            }
        }
    }
}

/** 下拉锚点字段：标签 + 当前值 + 展开箭头，点击切换展开（视觉与 DateField 一致）。 */
@Composable
private fun DropdownField(
    label: String,
    valueText: String,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    required: Boolean = false
) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        FieldLabel(text = label, required = required)
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textColor,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = colors.onSurfaceVariant
            )
        }
    }
}

/** 推荐标签多选项：勾选图标 + 标题，点击 toggle（自定义行，不关闭菜单）；enabled=false 时置灰不可选。 */
@Composable
private fun TagCheckItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(20.dp),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = if (enabled) colors.primary else colors.disabled,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(Modifier.size(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                selected -> colors.textColor
                !enabled -> colors.disabled
                else -> colors.onSurfaceVariant
            }
        )
    }
}

/** 字节数格式化（整数 KB/MB，KMP 无 String.format）。 */
private fun formatSize(bytes: Long): String {
    if (bytes <= 0) return ""
    val kb = bytes / 1024
    return if (kb >= 1024) "${kb / 1024} MB" else "$kb KB"
}
