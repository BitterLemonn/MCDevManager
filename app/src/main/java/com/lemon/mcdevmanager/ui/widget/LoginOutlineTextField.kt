package com.lemon.mcdevmanager.ui.widget

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanager.ui.theme.AppTheme

@Composable
fun LoginOutlineTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .then(modifier),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppTheme.colors.primaryColor,
            focusedTextColor = AppTheme.colors.textColor,
            focusedLabelColor = AppTheme.colors.primaryColor,
            focusedContainerColor = AppTheme.colors.card,
            unfocusedLabelColor = AppTheme.colors.secondaryColor,
            unfocusedBorderColor = AppTheme.colors.secondaryColor,
            unfocusedTextColor = AppTheme.colors.textColor,
            unfocusedContainerColor = AppTheme.colors.card
        ),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = keyboardType, imeAction = imeAction
        ),
        singleLine = singleLine,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation
    )
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun LoginOutlineTextFieldPreview() {
    LoginOutlineTextField(
        value = "",
        onValueChange = {},
        label = {
            Text("Username")
        }
    )
}