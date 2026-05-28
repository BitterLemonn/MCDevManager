package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

@Composable
fun LoginOutlineTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val colors = LocalAppColors.current

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.primary,
            focusedTextColor = colors.textColor,
            focusedLabelColor = colors.primary,
            focusedContainerColor = colors.background,
            unfocusedLabelColor = colors.secondary,
            unfocusedBorderColor = colors.secondary,
            unfocusedTextColor = colors.textColor,
            unfocusedContainerColor = colors.surface,
        ),
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon
    )
}
