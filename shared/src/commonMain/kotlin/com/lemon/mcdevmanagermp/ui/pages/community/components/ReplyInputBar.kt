package com.lemon.mcdevmanagermp.ui.pages.community.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_correct
import org.jetbrains.compose.resources.painterResource

@Composable
fun ReplyInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    placeholder: String = "输入回复内容...",
    isEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                focusedTextColor = colors.textColor,
                focusedContainerColor = colors.surfaceContainerHigh,
                unfocusedBorderColor = colors.outline,
                unfocusedTextColor = colors.textColor,
                unfocusedContainerColor = colors.surfaceContainerHigh,
                disabledBorderColor = colors.outline.copy(alpha = 0.3f),
                disabledTextColor = colors.onSurfaceVariant,
                disabledContainerColor = colors.surfaceContainerHigh.copy(alpha = 0.5f),
                disabledPlaceholderColor = colors.onSurfaceVariant.copy(alpha = 0.3f)
            ),
            enabled = isEnabled,
            maxLines = 3,
            textStyle = MaterialTheme.typography.bodyMedium
        )

        AnimatedVisibility(
            visible = value.isNotBlank() && isEnabled,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            IconButton(
                onClick = onSubmit,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_correct),
                    contentDescription = "发送",
                    tint = colors.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
