package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_close
import org.jetbrains.compose.resources.painterResource

/**
 * 标签输入：回车 / 逗号添加，已选标签以可删除 Chip 展示。
 */
@Composable
fun TagInputField(
    label: String,
    tags: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (Int) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "输入标签后回车 / 逗号添加",
    required: Boolean = false
) {
    val colors = LocalAppColors.current
    val keyboard = LocalSoftwareKeyboardController.current
    var input by remember { mutableStateOf("") }

    fun commit() {
        val token = input.trim().trimEnd(',', '，', '\n').trim()
        if (token.isNotEmpty()) onAdd(token)
        input = ""
    }

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
        OutlinedTextField(
            value = input,
            onValueChange = { raw ->
                if (raw.endsWith(',') || raw.endsWith('，') || raw.endsWith('\n')) {
                    commit()
                } else {
                    input = raw
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text(placeholder) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                commit()
                keyboard?.hide()
            })
        )
    }
}
