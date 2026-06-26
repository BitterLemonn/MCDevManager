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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_close
import org.jetbrains.compose.resources.painterResource

/**
 * 标签输入：聚焦弹出默认标签列表（可直接选择 / 输入筛选），回车 / 逗号仍可添加自定义标签；
 * 已选标签以可删除 Chip 展示。
 *
 * - [suggestions] 为默认标签源；输入为空时展示全部（排除已选），输入时按包含筛选。
 * - 列表为 popover，**强制在输入框下方展开**（自定义 [Popup] 位置，不向上翻转，
 *   避免遮挡输入框上方的已选标签 Chip）；超长可下滑；点击项即选用并保持展开，便于连续选择多个。
 */
@Composable
fun TagInputField(
    label: String,
    tags: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (Int) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "输入标签后回车 / 逗号添加",
    required: Boolean = false,
    suggestions: List<String> = emptyList(),
    // false：仅可从 suggestions 选用，输入仅用于筛选列表，回车/逗号不添加自定义标签
    allowCustom: Boolean = true
) {
    val colors = LocalAppColors.current
    val density = LocalDensity.current
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var input by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var fieldWidthPx by remember { mutableIntStateOf(0) }

    // 输入为空 → 全部默认标签（可直接选择）；非空 → 按包含筛选；均排除已选
    val displayList = remember(input, suggestions, tags) {
        val query = input.trim().lowercase()
        val base = if (query.isEmpty()) suggestions
        else suggestions.filter { it.lowercase().contains(query) }
        base.filter { it !in tags }.distinct()
    }

    fun commit() {
        if (allowCustom) {
            val token = input.trim().trimEnd(',', '，', '\n').trim()
            if (token.isNotEmpty()) onAdd(token)
        }
        input = ""
    }

    val menuOpen = expanded && displayList.isNotEmpty()

    Column(modifier = modifier.fillMaxWidth()) {
        FieldLabel(text = label, required = required)
        if (tags.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                tags.forEachIndexed { index, tag ->
                    InputChip(
                        selected = false,
                        onClick = {},
                        label = { Text(tag) },
                        trailingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_close),
                                contentDescription = "删除标签",
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable(onClick = { onRemove(index) })
                            )
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Box(modifier = Modifier.onSizeChanged { fieldWidthPx = it.width }) {
            OutlinedTextField(
                value = input,
                onValueChange = { raw ->
                    if (raw.endsWith(',') || raw.endsWith('，') || raw.endsWith('\n')) {
                        commit()
                    } else {
                        input = raw
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { expanded = it.isFocused },
                singleLine = true,
                placeholder = { Text(placeholder) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    commit()
                    keyboard?.hide()
                })
            )
            // 默认标签弹出列表：强制在输入框下方展开（不向上翻转，避免遮挡上方已选标签）
            if (menuOpen) {
                val menuWidth = with(density) { fieldWidthPx.toDp() }
                Popup(
                    popupPositionProvider = BelowAnchorPositionProvider,
                    // 通过清除输入框焦点间接触发 onFocusChanged(false) → expanded=false 关闭菜单。
                    // 这样关闭时一定伴随失焦，再次点击输入框能重新聚焦展开（避免“关闭后无法重新展开”）。
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
                            .heightIn(max = 220.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = colors.scheme.surface,
                        tonalElevation = 3.dp,
                        shadowElevation = 6.dp
                    ) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            displayList.forEach { tag ->
                                DropdownMenuItem(
                                    text = { Text(tag) },
                                    onClick = {
                                        onAdd(tag)
                                        input = ""
                                        focusRequester.requestFocus()
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

// BelowAnchorPositionProvider 已提取至同包 PopupPosition.kt，供向下展开的下拉共用。
