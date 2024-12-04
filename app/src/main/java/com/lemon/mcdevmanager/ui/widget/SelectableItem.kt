package com.lemon.mcdevmanager.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lt.compose_views.other.HorizontalSpace

@Composable
fun SelectableItem(
    containItem: @Composable RowScope.() -> Unit = {},
    isSelected: Boolean = false,
    onClick: (Boolean) -> Unit = {}
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 4.dp)
        .clickable(indication = ripple(),
            interactionSource = remember { MutableInteractionSource() }) { onClick(isSelected) }
    ) {
        containItem()
        Spacer(modifier = Modifier.weight(1f))
        if (isSelected) {
            Image(
                painter = painterResource(id = R.drawable.ic_correct),
                contentDescription = "selected",
                modifier = Modifier
                    .size(30.dp)
                    .padding(4.dp)
                    .align(Alignment.CenterVertically),
                colorFilter = ColorFilter.tint(AppTheme.colors.primaryColor)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .padding(4.dp)
                    .align(Alignment.CenterVertically)
            )
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun SelectableItemPreview() {
    var isSelected by remember { mutableStateOf(false) }
    SelectableItem(
        containItem = {
            Text(
                text = "Item",
                fontSize = 16.sp,
                color = AppTheme.colors.textColor,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            HorizontalSpace(dp = 12.dp)
            Text(
                text = "id12312312312",
                color = AppTheme.colors.hintColor,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }, isSelected = isSelected
    ) {
        isSelected = !it
    }
}