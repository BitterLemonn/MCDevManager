package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

/**
 * 只读字段：上方标签，下方值（值缺省显示 "—"）。
 */
@Composable
fun ReadOnlyField(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Column(modifier = modifier.fillMaxWidth()) {
        FieldLabel(text = label)
        Spacer(Modifier.height(4.dp))
        Text(
            text = value.ifEmpty { "—" },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = colors.textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
