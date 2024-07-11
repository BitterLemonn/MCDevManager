package com.lemon.mcdevmanager.ui.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.TextWhite

@Composable
fun SelectCard(
    leftName: String,
    rightName: String,
    nowSelectLeft: (Boolean) -> Unit = {}
) {
    var isSelectLeft by remember { mutableStateOf(true) }

    val animatedLeft by animateFloatAsState(targetValue = if (isSelectLeft) 0f else 0.5f)
    val animatedRight by animateFloatAsState(targetValue = if (isSelectLeft) 0.5f else 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.card
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box {
            Row(modifier = Modifier.fillMaxWidth()) {
                AnimatedVisibility(visible = !isSelectLeft) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedLeft)
                            .clip(RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        Text(text = "", modifier = Modifier.padding(8.dp))
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedRight)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppTheme.colors.primaryColor)
                        .padding(4.dp)
                ) {
                    Text(text = "", modifier = Modifier.padding(8.dp))
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = !isSelectLeft
                        ) {
                            isSelectLeft = true
                            nowSelectLeft(true)
                        }
                ) {
                    Text(
                        text = leftName,
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.Center),
                        color = if (isSelectLeft) TextWhite else AppTheme.colors.textColor
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = isSelectLeft
                        ) {
                            isSelectLeft = false
                            nowSelectLeft(false)
                        }
                ) {
                    Text(
                        text = rightName,
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.Center),
                        color = if (isSelectLeft) AppTheme.colors.textColor else TextWhite
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun SelectCardPreview() {
    SelectCard(
        leftName = "PE",
        rightName = "PC"
    )
}