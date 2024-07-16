package com.lemon.mcdevmanager.ui.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.TextWhite
import com.lemon.mcdevmanager.utils.pxToDp

@Composable
fun SelectTextCard(
    initSelectLeft: Boolean = true,
    leftName: String,
    rightName: String,
    nowSelectLeft: (Boolean) -> Unit = {}
) {
    val isSelectLeft by rememberUpdatedState(initSelectLeft)
    SelectCard(
        initSelectLeft = initSelectLeft,
        leftContain = {
            Text(
                text = leftName,
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.Center),
                color = if (isSelectLeft) TextWhite else AppTheme.colors.textColor
            )
        },
        rightContain = {
            Text(
                text = rightName,
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.Center),
                color = if (isSelectLeft) AppTheme.colors.textColor else TextWhite
            )
        },
        nowSelectLeft = nowSelectLeft
    )
}


@Composable
fun SelectCard(
    modifier: Modifier = Modifier,
    initSelectLeft: Boolean = true,
    leftContain: @Composable BoxScope.() -> Unit,
    rightContain: @Composable BoxScope.() -> Unit,
    nowSelectLeft: (Boolean) -> Unit = {}
) {
    val isSelectLeft by rememberUpdatedState(initSelectLeft)

    val context = LocalContext.current
    var selectableItemHeight by remember { mutableIntStateOf(0) }

    val animatedLeft by animateFloatAsState(targetValue = if (isSelectLeft) 0f else 0.5f)
    val animatedRight by animateFloatAsState(targetValue = if (isSelectLeft) 0.5f else 1f)

    Card(
        modifier = Modifier
            .padding(8.dp)
            .then(modifier),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.card
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box {
            Row {
                AnimatedVisibility(visible = !isSelectLeft) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedLeft)
                            .clip(RoundedCornerShape(8.dp))
                            .padding(4.dp)
                            .height(selectableItemHeight.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedRight)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppTheme.colors.primaryColor)
                        .padding(4.dp)
                        .height(selectableItemHeight.dp)
                )
            }
            Row{
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = !isSelectLeft
                        ) {
                            nowSelectLeft(true)
                        }
                        .onGloballyPositioned {
                            selectableItemHeight = pxToDp(context, it.size.height.toFloat())
                        }
                ) { leftContain() }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = isSelectLeft
                        ) {
                            nowSelectLeft(false)
                        }
                ) { rightContain() }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun SelectCardPreview() {
//    SelectTextCard(
//        leftName = "PE",
//        rightName = "PC"
//    )
    SelectCard(
        modifier = Modifier.width(120.dp),
        leftContain = {
            Image(
                painter = painterResource(id = R.drawable.ic_line_chart),
                contentDescription = "line chart",
                modifier = Modifier.size(36.dp).padding(4.dp).align(Alignment.Center)
            )
        },
        rightContain = {
            Image(
                painter = painterResource(id = R.drawable.ic_bar_chart),
                contentDescription = "bar chart",
                modifier = Modifier.size(36.dp).padding(4.dp).align(Alignment.Center)
            )
        }
    )
}