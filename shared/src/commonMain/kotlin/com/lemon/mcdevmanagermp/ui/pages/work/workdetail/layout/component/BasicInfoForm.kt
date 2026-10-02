package com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.consts.enums.PE_PREREQUISITE_PRI_TYPE
import com.lemon.mcdevmanagermp.platform.copyTextToClipboard
import com.lemon.mcdevmanagermp.ui.components.BinarySelector
import com.lemon.mcdevmanagermp.ui.components.FieldLabel
import com.lemon.mcdevmanagermp.ui.components.FormSection
import com.lemon.mcdevmanagermp.ui.components.ModSearchSelectField
import com.lemon.mcdevmanagermp.ui.components.ModSelectOption
import com.lemon.mcdevmanagermp.ui.components.ReadOnlyField
import com.lemon.mcdevmanagermp.ui.components.TagInputField
import com.lemon.mcdevmanagermp.ui.components.YesNoSelector
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailAction
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailState
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import kotlinx.coroutines.delay

/**
 * 基本信息区块表单（图1）。三档布局共用，按 [columns] 自适应排列。
 *
 * - [columns] <= 1：所有短字段单列
 * - [columns] >= 2：只读元数据 / 是·否选项用 FlowRow 多列网格，长字段（名称/前置/标签/活动说明）整行
 * - [showMetaRow]：是否在表单内渲染只读元数据组（expanded 改用 [MetaInfoBar] 顶栏展示）
 *
 * [WorkDetailState.readOnly]=true 时全部字段改为只读呈现：文本用 [ReadOnlyField]，
 * 选择器置灰保留选中态，标签/授权图仅展示不可增删。
 */
@Composable
internal fun BasicInfoForm(
    state: WorkDetailState,
    onAction: (WorkDetailAction) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 1,
    showMetaRow: Boolean = true
) {
    val colors = LocalAppColors.current
    val readOnly = state.readOnly
    // columns<=1 时给一个超过任何屏宽的 min，强制单列；否则以 260dp 为每列最小宽度
    val minFieldWidth = if (columns <= 1) 1000.dp else 260.dp

    FormSection(title = "基本信息", modifier = modifier) {
        // 资源名称（必填，整行）
        if (readOnly) {
            ReadOnlyField(
                label = "资源名称",
                value = state.itemName,
                required = true,
                singleLine = false,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            OutlinedTextField(
                value = state.itemName,
                onValueChange = { onAction(WorkDetailAction.UpdateItemName(it)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = {
                    Text(buildAnnotatedString {
                        withStyle(SpanStyle(color = colors.error)) { append("* ") }
                        append("资源名称")
                    })
                }
            )
        }

        // 前置模组（pri_type=9）基本信息只需填写名称，其余字段整块隐藏
        if (state.isPrerequisiteType) return@FormSection

        // 只读元数据组（可关闭：expanded 用顶部信息条替代）；前置模组不展示
        if (showMetaRow && state.showListingMeta) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReadOnlyField(
                    label = "资源ID",
                    value = state.itemId,
                    modifier = Modifier.weight(1f).widthIn(min = minFieldWidth),
                    trailing = { CopyButton(state.itemId) }
                )
                ReadOnlyField(
                    label = "模组码",
                    value = state.normalNumber,
                    modifier = Modifier.weight(1f).widthIn(min = minFieldWidth),
                    trailing = { CopyButton(state.normalNumber) }
                )
                ReadOnlyField(
                    label = "资源版本",
                    value = state.itemVersion,
                    modifier = Modifier.weight(1f).widthIn(min = minFieldWidth)
                )
            }
        }

        // 是 / 否 选项组
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 前置模组不可加入「我的山头」专区，也不可设为非原创（恒为原创，无需授权图）
            if (!state.isPrerequisiteType) {
                YesNoSelector(
                    label = "是否加入到「我的山头」专区",
                    value = state.joinShantou,
                    onValueChange = { onAction(WorkDetailAction.ToggleJoinShantou(it)) },
                    modifier = Modifier.weight(1f).widthIn(min = minFieldWidth),
                    required = true,
                    enabled = !readOnly
                )
            }
            YesNoSelector(
                label = "是否原创作品",
                value = state.isOriginal,
                onValueChange = { onAction(WorkDetailAction.ToggleOriginal(it)) },
                modifier = Modifier.weight(1f).widthIn(min = minFieldWidth),
                required = true,
                enabled = !readOnly && !state.isPrerequisiteType
            )
            YesNoSelector(
                label = "是否为关联模组",
                value = state.isRelatedMod,
                onValueChange = { onAction(WorkDetailAction.ToggleRelatedMod(it)) },
                modifier = Modifier.weight(1f).widthIn(min = minFieldWidth),
                required = true,
                enabled = !readOnly
            )
            // 前置模组不同步生成 PC 侧内容，不展示该开关
            if (!state.isPrerequisiteType) {
                YesNoSelector(
                    label = "是否同步生成 PC 模组",
                    value = state.syncPc,
                    onValueChange = { onAction(WorkDetailAction.ToggleSyncPc(it)) },
                    modifier = Modifier.weight(1f).widthIn(min = minFieldWidth),
                    required = true,
                    enabled = !readOnly
                )
            }
        }

        // 授权信息图片（非原创必填）
        if (!state.isOriginal) {
            CorpProofImageUploader(
                imageUrl = state.corpProofImage,
                localFile = state.corpProofFile,
                onSelect = { onAction(WorkDetailAction.SelectCorpProof(it)) },
                onRemove = { onAction(WorkDetailAction.RemoveCorpProof) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = readOnly
            )
        }

        // 关联模组（选「是」时展开补充字段）
        if (state.isRelatedMod) {
            RelatedModFields(
                state = state,
                onAction = onAction,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // PE 前置模组（单选，pe 类别里 pri_type=9 的前置池）。
        // 账号需开通前置模组功能（users/me.prerequisite_switch）；前置模组自身不可再挂前置。
        // 已存在前置关系时无条件展示，避免无开关账号编辑旧数据时看不到而误清空。
        if (state.peResourceType != PE_PREREQUISITE_PRI_TYPE &&
            (state.hasPrerequisiteSwitch || state.prerequisiteItemId.isNotEmpty())
        ) {
            if (readOnly) {
                ReadOnlyField(
                    label = "前置模组",
                    value = state.prerequisiteItemName.ifEmpty { state.prerequisiteItemId },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                ModSearchSelectField(
                    label = "前置模组",
                    results = state.prereqSearchResults,
                    isLoading = state.isSearchingPrereq,
                    selected = if (state.prerequisiteItemId.isNotEmpty()) {
                        listOf(
                            ModSelectOption(state.prerequisiteItemId, state.prerequisiteItemName)
                        )
                    } else {
                        emptyList()
                    },
                    onSearch = { onAction(WorkDetailAction.SearchPrereqMods(it)) },
                    onSelect = { onAction(WorkDetailAction.SelectPrereqMod(it)) },
                    onRemove = { onAction(WorkDetailAction.ClearPrereqMod) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 模组标签（整行）；前置模组不要求标签
        if (!state.isPrerequisiteType) {
            TagInputField(
                label = "模组标签",
                tags = state.tags,
                onAdd = { onAction(WorkDetailAction.AddTag(it)) },
                onRemove = { onAction(WorkDetailAction.RemoveTag(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = "搜索标签 / 输入自定义标签",
                required = true,
                suggestions = state.availableTags,
                readOnly = readOnly
            )
        }

        // 活动参与说明
        if (readOnly) {
            ReadOnlyField(
                label = "活动参与说明",
                value = state.activityDesc,
                singleLine = false,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            OutlinedTextField(
                value = state.activityDesc,
                onValueChange = { onAction(WorkDetailAction.UpdateActivityDesc(it)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                label = { Text("活动参与说明") },
                placeholder = { Text("用于填写参与官方活动需上传介绍与说明，此处内容不会在游戏端出现") }
            )
        }
    }
}

/**
 * 作品信息条（expanded 顶栏）：把只读元数据以横排仪表盘式展示，充分利用宽屏。
 */
@Composable
internal fun MetaInfoBar(
    state: WorkDetailState,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceContainerHigh)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MetaItem(
            label = "资源ID",
            value = state.itemId,
            modifier = Modifier.weight(1f),
            copyable = true
        )
        MetaItem(
            label = "模组码",
            value = state.normalNumber,
            modifier = Modifier.weight(1f),
            copyable = true
        )
        MetaItem(label = "资源版本", value = state.itemVersion, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun MetaItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    copyable: Boolean = false
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value.ifEmpty { "—" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (copyable) CopyButton(value)
    }
}

/**
 * 关联模组补充字段（「是否为关联模组」选「是」时展开）：模组类型、搜索模组、当前关联模组（只读）。
 *
 * [WorkDetailState.readOnly]=true 时不渲染搜索选择框，改为只读展示所选关联模组。
 */
@Composable
internal fun RelatedModFields(
    state: WorkDetailState,
    onAction: (WorkDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.primary.copy(alpha = 0.05f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 当前模组类型（主包 / 副包）
        BinarySelector(
            label = "当前模组类型",
            optionTrue = "主包",
            optionFalse = "副包",
            value = state.relatedIsMaster,
            onValueChange = { onAction(WorkDetailAction.ToggleRelatedPackType(it)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.readOnly
        )

        if (state.readOnly) {
            // 只读：不提供搜索/清空入口，仅展示当前关联的模组
            ReadOnlyField(
                label = "关联模组",
                value = state.relatedItemName.ifEmpty { state.relatedItemId },
                singleLine = false,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // 搜索模组（pe，mcStatus=1）
            ModSearchSelectField(
                label = "搜索模组",
                results = state.relatedSearchResults,
                isLoading = state.isSearchingRelated,
                selected = if (state.relatedItemId.isNotEmpty())
                    listOf(ModSelectOption(state.relatedItemId, state.relatedItemName))
                else emptyList(),
                onSearch = { onAction(WorkDetailAction.SearchRelatedMods(it)) },
                onSelect = { onAction(WorkDetailAction.SelectRelatedMod(it)) },
                onRemove = { onAction(WorkDetailAction.ClearRelatedMod) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 当前关联模组（只读）
        FieldLabel(text = "当前关联模组")
        ReadOnlyField(
            label = "主包",
            value = state.detail?.dlcInfo?.master.orEmpty(),
            singleLine = false
        )
        ReadOnlyField(
            label = "副包",
            value = state.detail?.dlcInfo?.slaveList.orEmpty(),
            singleLine = false
        )
    }
}

/**
 * 复制按钮：点击将 [value] 写入剪贴板，复制成功后图标短暂变为勾选反馈。
 */
@Composable
private fun CopyButton(value: String, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(1000)
            copied = false
        }
    }
    IconButton(
        onClick = {
            if (value.isNotEmpty()) {
                copyTextToClipboard(value)
                copied = true
            }
        },
        modifier = modifier.size(32.dp)
    ) {
        Icon(
            imageVector = if (copied) Icons.Filled.Check else Icons.Filled.ContentCopy,
            contentDescription = "复制",
            tint = if (copied) colors.success else colors.onSurfaceVariant
        )
    }
}
