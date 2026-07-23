package com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.lemon.mcdevmanagermp.ui.components.FormSection
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailAction
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailState

/**
 * PC 模组信息区块：模组类别 / 适用范围 / 具体类别
 * 一级分类仅新建可选，编辑场景锁定不可改；具体类别与适用范围均可改。
 */
@Composable
internal fun PcResourceManageForm(
    state: WorkDetailState,
    onAction: (WorkDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    // 一级分类（模组类别）仅新建可选；编辑场景作品已有类别，锁定不可改
    val typeEditable = state.detail == null

    FormSection(title = "上传 PC 模组信息", modifier = modifier) {
        // 模组类别（一级分类；编辑场景不可改）
        var typeExpanded by remember { mutableStateOf(false) }
        val selectedTypeTitle =
            state.pcResourceTypeOptions.firstOrNull { it.id == state.pcResourceType }?.title
        Box {
            DropdownField(
                label = "模组类别",
                valueText = selectedTypeTitle ?: "选择模组类别",
                expanded = typeExpanded,
                onClick = { typeExpanded = !typeExpanded },
                required = true,
                enabled = typeEditable
            )
            DropdownMenu(
                expanded = typeExpanded,
                onDismissRequest = { typeExpanded = false }
            ) {
                state.pcResourceTypeOptions.forEach { opt ->
                    TagCheckItem(
                        title = opt.title,
                        selected = opt.id == state.pcResourceType,
                        onClick = {
                            onAction(WorkDetailAction.UpdatePcResourceType(opt.id))
                            typeExpanded = false   // 单选：选完即关
                        }
                    )
                }
            }
        }

        // 适用范围（id 为 String；可改）
        var scopeExpanded by remember { mutableStateOf(false) }
        val selectedScopeTitle =
            state.pcAvailableScopeOptions.firstOrNull { it.id == state.pcAvailableScope }?.title
        Box {
            DropdownField(
                label = "适用范围",
                valueText = selectedScopeTitle ?: "选择适用范围",
                expanded = scopeExpanded,
                onClick = { scopeExpanded = !scopeExpanded },
                required = true
            )
            DropdownMenu(
                expanded = scopeExpanded,
                onDismissRequest = { scopeExpanded = false }
            ) {
                state.pcAvailableScopeOptions.forEach { opt ->
                    TagCheckItem(
                        title = opt.title,
                        selected = opt.id == state.pcAvailableScope,
                        onClick = {
                            onAction(WorkDetailAction.UpdatePcAvailableScope(opt.id))
                            scopeExpanded = false   // 单选：选完即关
                        }
                    )
                }
            }
        }

        // 具体类别（模组类别的 subtype 子选项，随模组类别联动；可改）
        val subTypeOptions = state.pcResourceSubTypeOptions[state.pcResourceType].orEmpty()
        if (subTypeOptions.isNotEmpty()) {
            var subTypeExpanded by remember { mutableStateOf(false) }
            val selectedSubTypeTitle =
                subTypeOptions.firstOrNull { it.id == state.pcResourceSubType }?.title
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
                            selected = opt.id == state.pcResourceSubType,
                            onClick = {
                                onAction(WorkDetailAction.UpdatePcResourceSubType(opt.id))
                                subTypeExpanded = false   // 单选：选完即关
                            }
                        )
                    }
                }
            }
        }
    }
}
