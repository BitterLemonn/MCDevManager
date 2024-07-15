package com.lemon.mcdevmanager.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.TextWhite
import com.lt.compose_views.other.HorizontalSpace

@Composable
fun FlowTabWidget(
    modifier: Modifier = Modifier,
    text: String = "",
    isSelected: Boolean = false,
    isShowDelete: Boolean = false,
    onDeleteClick: () -> Unit = {},
    onClick: (Boolean) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .padding(start = 8.dp, top = 8.dp)
            .then(modifier)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(color = if (isSelected) AppTheme.colors.primaryColor else AppTheme.colors.card)
                .border(
                    width = 1.dp,
                    color = AppTheme.colors.primaryColor,
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple()
                ) {
                    onClick(isSelected)
                }
                .padding(8.dp)
                .align(Alignment.Center)
        ) {
            Row {
                Text(
                    text = text,
                    color = if (isSelected) TextWhite else AppTheme.colors.textColor,
                    fontSize = 14.sp,
                    modifier = Modifier.align(
                        Alignment.CenterVertically
                    )
                )
                if (isShowDelete) {
                    HorizontalSpace(dp = 8.dp)
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.primaryColor)
                            .clickable {
                                onDeleteClick()
                            }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "delete",
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview
private fun PreviewFlowTabWidget() {
    FlowTabWidget(
        text = "FlowTabWidget",
        isSelected = false,
        isShowDelete = true
    ) {}
}