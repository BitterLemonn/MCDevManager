package com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.components.BinarySelector
import com.lemon.mcdevmanagermp.ui.components.FormSection
import com.lemon.mcdevmanagermp.ui.components.ModSearchSelectField
import com.lemon.mcdevmanagermp.ui.components.TagInputField
import com.lemon.mcdevmanagermp.ui.components.YesNoSelector
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailAction
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailState

/**
 * PC 基本信息区块（勾选「同步生成 PC 模组」后展示）。
 *
 * 字段：是否包含地图 / PC 模组标签 / PC 前置模组(包含·不包含，包含时输入 iid → relate_item_id) / PC 模组简介。
 * 复用 [TagInputField]（默认标签搜索 + 自定义）与 [YesNoSelector] / [BinarySelector]，视觉与基本信息一致。
 */
@Composable
internal fun PcBasicInfoForm(
    state: WorkDetailState,
    onAction: (WorkDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    FormSection(title = "PC 基本信息", modifier = modifier) {
        // 是否包含地图
        YesNoSelector(
            label = "是否包含地图",
            value = state.pcIncludeMap,
            onValueChange = { onAction(WorkDetailAction.TogglePcIncludeMap(it)) },
            modifier = Modifier.fillMaxWidth()
        )

        // PC 模组标签（仅可从 mc_consts.tag.comp 预设标签中选用，不可自定义）
        TagInputField(
            label = "PC 模组标签",
            tags = state.pcTags,
            onAdd = { onAction(WorkDetailAction.AddPcTag(it)) },
            onRemove = { onAction(WorkDetailAction.RemovePcTag(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = "选择标签",
            suggestions = state.pcTagOptions.map { it.title },
            allowCustom = false
        )

        // PC 前置模组（包含 / 不包含）
        BinarySelector(
            label = "PC 前置模组",
            optionTrue = "包含",
            optionFalse = "不包含",
            value = state.pcHasPrerequisite,
            onValueChange = { onAction(WorkDetailAction.TogglePcPrerequisite(it)) },
            modifier = Modifier.fillMaxWidth()
        )

        // 包含时搜索选择前置模组（comp，mcStatus=1；提交映射 relate_item_id）
        if (state.pcHasPrerequisite) {
            ModSearchSelectField(
                label = "PC 前置模组",
                results = state.pcPrereqSearchResults,
                isLoading = state.isSearchingPcPrereq,
                selected = state.pcPrerequisites,
                onSearch = { onAction(WorkDetailAction.SearchPcPrereqMods(it)) },
                onSelect = { onAction(WorkDetailAction.SelectPcPrereqMod(it)) },
                onRemove = { onAction(WorkDetailAction.RemovePcPrereqMod(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = "搜索 PC 前置模组名称",
                multiSelect = true
            )
        }

        Spacer(Modifier.height(4.dp))

        // PC 模组简介（多行）
        OutlinedTextField(
            value = state.pcBrief,
            onValueChange = { onAction(WorkDetailAction.UpdatePcIntro(it)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            label = { Text("PC 模组简介") }
        )
    }
}
