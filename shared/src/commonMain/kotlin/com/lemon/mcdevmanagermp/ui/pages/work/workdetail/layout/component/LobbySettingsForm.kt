package com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.consts.enums.PePriTypeEnum
import com.lemon.mcdevmanagermp.ui.components.FieldLabel
import com.lemon.mcdevmanagermp.ui.components.FormSection
import com.lemon.mcdevmanagermp.ui.components.OptionChip
import com.lemon.mcdevmanagermp.ui.components.OptionChips
import com.lemon.mcdevmanagermp.ui.components.YesNoSelector
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailAction
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailState

@Composable
internal fun LobbySettingsForm(
    state: WorkDetailState,
    onAction: (WorkDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.peResourceType != PePriTypeEnum.LOBBY.value.toInt()) return

    FormSection(title = "联机大厅设置", modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LobbyNumberField(
                label = "建议最少人数",
                value = state.lobbyMinNum,
                supportingText = "0 或 2–${state.lobbyForceMaxNum}",
                onValueChange = { onAction(WorkDetailAction.UpdateLobbyMinNum(it)) },
                modifier = Modifier.weight(1f)
            )
            LobbyNumberField(
                label = "建议最多人数",
                value = state.lobbyMaxNum,
                supportingText = "0 或 2–${state.lobbyForceMaxNum}",
                onValueChange = { onAction(WorkDetailAction.UpdateLobbyMaxNum(it)) },
                modifier = Modifier.weight(1f)
            )
        }
        LobbyNumberField(
            label = "房间限制人数",
            value = state.lobbyForceMaxNum,
            supportingText = "范围 2–10",
            onValueChange = { onAction(WorkDetailAction.UpdateLobbyForceMaxNum(it)) }
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FieldLabel(text = "联机大厅专区分类", required = true)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.lobbyTagOptions.forEach { tag ->
                    val selected = tag.id in state.lobbyTags
                    val atLimit = state.lobbyTagLimit > 0 &&
                            state.lobbyTags.size >= state.lobbyTagLimit
                    OptionChip(
                        text = tag.title,
                        selected = selected,
                        enabled = selected || !state.isLobbyCompetitive && !atLimit,
                        onClick = { onAction(WorkDetailAction.ToggleLobbyTag(tag.id)) }
                    )
                }
            }
        }

        if (state.isLobbyCompetitive) {
            OptionChips(
                label = "是否为非对称对抗",
                options = listOf("是" to true, "否" to false),
                selected = state.lobbyIsAsymmetric,
                enabled = state.detail == null,
                onSelect = { onAction(WorkDetailAction.ToggleLobbyAsymmetric(it)) }
            )
            if (state.lobbyIsAsymmetric) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FieldLabel(text = "阵营分类", required = true)
                    state.lobbyCamps.forEachIndexed { index, camp ->
                        OutlinedTextField(
                            value = camp,
                            onValueChange = {
                                onAction(WorkDetailAction.UpdateLobbyCamp(index, it))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = state.detail == null,
                            singleLine = true,
                            label = { Text("阵营 ${index + 1}") },
                            placeholder = { Text("请输入阵营名称") }
                        )
                    }
                }
            }
            LobbyNumberField(
                label = "游戏开始人数",
                value = state.lobbyPlayerNum,
                supportingText = "范围 1–15",
                onValueChange = { onAction(WorkDetailAction.UpdateLobbyPlayerNum(it)) }
            )
            YesNoSelector(
                label = "在普通模式中显示",
                value = state.lobbyNormalMode,
                onValueChange = { onAction(WorkDetailAction.ToggleLobbyNormalMode(it)) }
            )
            LobbyNumberField(
                label = "逃跑时间（分钟）",
                value = state.lobbyReconnectTime,
                supportingText = "范围 1–99",
                onValueChange = { onAction(WorkDetailAction.UpdateLobbyReconnectTime(it)) }
            )
        }
        // ponytail: 商业化与资源中心同步会改变 sub_type 提交语义，等产品明确开关流程后再开放。
    }
}

@Composable
private fun LobbyNumberField(
    label: String,
    value: Int,
    supportingText: String,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var text by remember { mutableStateOf(value.toString()) }
    var focused by remember { mutableStateOf(false) }
    LaunchedEffect(value, focused) {
        if (!focused) text = value.toString()
    }
    OutlinedTextField(
        value = text,
        onValueChange = { raw ->
            text = raw.filter(Char::isDigit).take(2)
            onValueChange(text.toIntOrNull() ?: 0)
        },
        modifier = modifier.onFocusChanged {
            focused = it.isFocused
        },
        singleLine = true,
        label = { Text(label) },
        supportingText = { Text(supportingText) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
    )
}
