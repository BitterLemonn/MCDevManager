package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import kotlinx.coroutines.delay

/**
 * 模组搜索选择项：统一各类模组（ResourceData / RequirementItemData）为 id + name。
 */
data class ModSelectOption(
    val id: String,
    val name: String
)

/**
 * 模组搜索选择字段：输入名称（350ms 防抖）触发 [onSearch]，结果以**向下** popover 展示，
 * 选中项以可删除 Chip 显示在输入框上方。
 *
 * - [multiSelect]=false（默认，单选）：选中后关闭 popover，[onSelect] 替换、[onRemove] 清空。
 * - [multiSelect]=true（多选）：选中后保持 popover 继续选（已选项自动从列表排除），
 *   [onSelect] 追加、[onRemove] 移除指定项。
 *
 * 交互与 [TagInputField] 一致：聚焦展开、点外部关闭、再次聚焦可重新展开。强制向下展开。
 */
@Composable
fun ModSearchSelectField(
    label: String,
    results: List<ModSelectOption>,
    isLoading: Boolean,
    selected: List<ModSelectOption>,
    onSearch: (String) -> Unit,
    onSelect: (ModSelectOption) -> Unit,
    onRemove: (ModSelectOption) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜索模组名称",
    required: Boolean = false,
    multiSelect: Boolean = false
) {
    val colors = LocalAppColors.current
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var query by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var fieldWidthPx by remember { mutableIntStateOf(0) }

    // 输入防抖：query 变化后 350ms 触发搜索（取消上一次等待）
    LaunchedEffect(query) {
        delay(350)
        onSearch(query.trim())
    }

    // 多选时从结果中排除已选；单选时展示全部
    val displayResults = if (multiSelect) {
        val selectedIds = selected.map { it.id }.toSet()
        results.filter { it.id !in selectedIds }
    } else {
        results
    }

    val menuOpen = expanded && (isLoading || displayResults.isNotEmpty())

    Column(modifier = modifier.fillMaxWidth()) {
        FieldLabel(text = label, required = required)
        if (selected.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                selected.forEach { opt ->
                    InputChip(
                        selected = false,
                        onClick = {},
                        label = {
                            Text(
                                text = opt.name.ifEmpty { opt.id },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "移除",
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable(onClick = { onRemove(opt) })
                            )
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Box(modifier = Modifier.onSizeChanged { fieldWidthPx = it.width }) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { expanded = it.isFocused },
                singleLine = true,
                placeholder = { Text(placeholder) }
            )
            if (menuOpen) {
                val menuWidth = with(density) { fieldWidthPx.toDp() }
                Popup(
                    popupPositionProvider = BelowAnchorPositionProvider,
                    onDismissRequest = { focusManager.clearFocus() },
                    properties = PopupProperties(
                        focusable = false,
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true
                    )
                ) {
                    Surface(
                        modifier = Modifier
                            .width(menuWidth)
                            .heightIn(max = 240.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = colors.scheme.surface,
                        tonalElevation = 3.dp,
                        shadowElevation = 6.dp
                    ) {
                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = colors.primary
                                )
                            }
                        } else {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                displayResults.forEach { res ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = res.name,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = res.id,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = colors.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            onSelect(res)
                                            if (!multiSelect) {
                                                // 单选：清空筛选并关闭，便于看到已选 Chip
                                                query = ""
                                                focusManager.clearFocus()
                                            }
                                            // 多选：保持展开继续选（displayResults 已排除新选项）
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
