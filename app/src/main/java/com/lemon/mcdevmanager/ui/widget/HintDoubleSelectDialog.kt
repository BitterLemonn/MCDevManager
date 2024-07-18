package com.lemon.mcdevmanager.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.utils.pxToDp

@Composable
fun HintDoubleSelectDialog(
    hint: String = "",
    highlightCertain: Boolean? = true,
    canTouchOutside: Boolean = false,
    onCanceled: () -> Unit = {},
    onConfirmed: () -> Unit = {}
) {

    var buttonHeight by remember { mutableStateOf(0) }
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(0.65f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                enabled = canTouchOutside
            ) { onCanceled() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .align(Alignment.Center),
            colors = CardDefaults.cardColors(
                containerColor = AppTheme.colors.card,
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .heightIn(min = 80.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = hint,
                        fontSize = 16.sp,
                        color = AppTheme.colors.textColor,
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = TextAlign.Center
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = AppTheme.colors.dividerColor
                )
                Row(Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                onClick = onCanceled,
                                indication = rememberRipple(),
                                interactionSource = remember { MutableInteractionSource() }
                            )
                            .onGloballyPositioned {
                                buttonHeight = pxToDp(context, it.size.height.toFloat())
                            }
                    ) {
                        Text(
                            text = "取消",
                            fontSize = 16.sp,
                            color = if (highlightCertain == false) AppTheme.colors.error
                            else AppTheme.colors.textColor,
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.Center)
                        )
                    }
                    VerticalDivider(
                        modifier = Modifier
                            .height((buttonHeight * 0.8).dp)
                            .align(Alignment.CenterVertically),
                        thickness = 1.dp,
                        color = AppTheme.colors.dividerColor
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                onClick = onConfirmed,
                                indication = rememberRipple(),
                                interactionSource = remember { MutableInteractionSource() }
                            )
                    ) {
                        Text(
                            text = "确定",
                            fontSize = 16.sp,
                            color = if (highlightCertain == true) AppTheme.colors.primaryColor
                            else AppTheme.colors.textColor,
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun HintDoubleSelectDialogPreview() {
    HintDoubleSelectDialog(
        "123123"
    )
}