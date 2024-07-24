package com.lemon.mcdevmanager.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.ui.theme.AppTheme

@Composable
fun NavigationItem(
    title: String,
    icon: Int,
    isSelected: Boolean = false,
    colorList: List<Color> = listOf(AppTheme.colors.primaryColor, AppTheme.colors.secondaryColor),
    onClick: () -> Unit
) {
    val isPureEnglish = title.matches(Regex("^[a-zA-Z]*$"))
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxHeight()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(4.dp)
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(
                    if (isSelected) colorList[0] else colorList[1]
                ),
                contentScale = ContentScale.Fit
            )
            Text(
                text = title,
                fontSize = 12.sp,
                letterSpacing = if (isPureEnglish) 0.5.sp else 5.sp,
                color = if (isSelected) colorList[0] else colorList[1]
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(),
                    onClick = onClick
                )
        )
    }

}

@Composable
@Preview
private fun NavigationItemPreview() {
    NavigationItem(
        title = "分析",
        icon = R.drawable.ic_bar_chart,
        isSelected = true
    ) {}
}