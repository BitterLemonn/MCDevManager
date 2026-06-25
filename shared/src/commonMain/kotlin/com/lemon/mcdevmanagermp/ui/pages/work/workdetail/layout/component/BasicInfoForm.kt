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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.components.BinarySelector
import com.lemon.mcdevmanagermp.ui.components.FieldLabel
import com.lemon.mcdevmanagermp.ui.components.FormSection
import com.lemon.mcdevmanagermp.ui.components.ReadOnlyField
import com.lemon.mcdevmanagermp.ui.components.TagInputField
import com.lemon.mcdevmanagermp.ui.components.YesNoSelector
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailAction
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailState
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

/**
 * 基本信息区块表单（图1）。三档布局共用，按 [columns] 自适应排列。
 *
 * - [columns] <= 1：所有短字段单列
 * - [columns] >= 2：只读元数据 / 是·否选项用 FlowRow 多列网格，长字段（名称/前置/标签/活动说明）整行
 * - [showMetaRow]：是否在表单内渲染只读元数据组（expanded 改用 [MetaInfoBar] 顶栏展示）
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
    // columns<=1 时给一个超过任何屏宽的 min，强制单列；否则以 260dp 为每列最小宽度
    val minFieldWidth = if (columns <= 1) 1000.dp else 260.dp

    FormSection(title = "基本信息", modifier = modifier) {
        // 资源名称（必填，整行）
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

        // 只读元数据组（可关闭：expanded 用顶部信息条替代）
        if (showMetaRow) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReadOnlyField(
                    label = "资源ID",
                    value = state.itemId,
                    modifier = Modifier.weight(1f).widthIn(min = minFieldWidth)
                )
                ReadOnlyField(
                    label = "模组码",
                    value = state.normalNumber,
                    modifier = Modifier.weight(1f).widthIn(min = minFieldWidth)
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
            YesNoSelector(
                label = "是否加入到「我的山头」专区",
                value = state.joinShantou,
                onValueChange = { onAction(WorkDetailAction.ToggleJoinShantou(it)) },
                modifier = Modifier.weight(1f).widthIn(min = minFieldWidth),
                required = true
            )
            YesNoSelector(
                label = "是否原创作品",
                value = state.isOriginal,
                onValueChange = { onAction(WorkDetailAction.ToggleOriginal(it)) },
                modifier = Modifier.weight(1f).widthIn(min = minFieldWidth),
                required = true
            )
            YesNoSelector(
                label = "是否为关联模组",
                value = state.isRelatedMod,
                onValueChange = { onAction(WorkDetailAction.ToggleRelatedMod(it)) },
                modifier = Modifier.weight(1f).widthIn(min = minFieldWidth)
            )
            YesNoSelector(
                label = "是否同步生成 PC 模组",
                value = state.syncPc,
                onValueChange = { onAction(WorkDetailAction.ToggleSyncPc(it)) },
                modifier = Modifier.weight(1f).widthIn(min = minFieldWidth)
            )
        }

        // 授权信息图片（非原创必填）
        if (!state.isOriginal) {
            CorpProofImageUploader(
                imageUrl = state.corpProofImage,
                localFile = state.corpProofFile,
                onSelect = { onAction(WorkDetailAction.SelectCorpProof(it)) },
                onRemove = { onAction(WorkDetailAction.RemoveCorpProof) },
                modifier = Modifier.fillMaxWidth()
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

        // 前置模组（整行）
        OutlinedTextField(
            value = state.prerequisite,
            onValueChange = { onAction(WorkDetailAction.UpdatePrerequisite(it)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("前置模组") },
            placeholder = { Text("搜索前置模组名称") },
            supportingText = {
                Text(
                    text = "搜索并选择已上传的私有前置模组，仅能关联一个前置模组",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
        )

        // 模组标签（整行）
        TagInputField(
            label = "模组标签",
            tags = state.tags,
            onAdd = { onAction(WorkDetailAction.AddTag(it)) },
            onRemove = { onAction(WorkDetailAction.RemoveTag(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = "搜索标签 / 输入自定义标签",
            suggestions = state.availableTags
        )

        // 活动参与说明（整行，多行）
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
        MetaItem(label = "资源ID", value = state.itemId, modifier = Modifier.weight(1f))
        MetaItem(label = "模组码", value = state.normalNumber, modifier = Modifier.weight(1f))
        MetaItem(label = "资源版本", value = state.itemVersion, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun MetaItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Column(modifier = modifier) {
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
}

/**
 * 关联模组补充字段（「是否为关联模组」选「是」时展开）：模组类型、搜索模组、当前关联模组（只读）。
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
            modifier = Modifier.fillMaxWidth()
        )

        // 搜索模组
        OutlinedTextField(
            value = state.relatedSearchKey,
            onValueChange = { onAction(WorkDetailAction.UpdateRelatedSearch(it)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("搜索模组") },
            placeholder = { Text("搜索模组名称") }
        )

        // 当前关联模组（只读）
        FieldLabel(text = "当前关联模组")
        ReadOnlyField(label = "主包", value = state.detail?.dlcInfo?.master ?: "")
        ReadOnlyField(label = "副包", value = state.detail?.dlcInfo?.slaveList ?: "")
    }
}
