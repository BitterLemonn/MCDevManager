package com.lemon.mcdevmanager.ui.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.TextBlack

@Composable
fun BottomHintDialog(
    hint: String,
    items: List<BottomButtonItem>,
    isShow: Boolean,
    onCancel: () -> Unit
) {
    if (isShow)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = TextBlack.copy(alpha = 0.4f))
                .clickable(
                    indication = null,
                    interactionSource = MutableInteractionSource()
                ) { onCancel.invoke() }
        )
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isShow,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideIn { fullSize -> IntOffset(0, fullSize.height) },
            exit = slideOut { fullSize -> IntOffset(0, fullSize.height) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                    .background(color = AppTheme.colors.hintColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        .background(AppTheme.colors.card)
                        .heightIn(min = 120.dp)
                        .padding(bottom = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = hint,
                        color = AppTheme.colors.textColor,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 25.dp),
                        textAlign = TextAlign.Center
                    )
                }
                Divider(
                    color = AppTheme.colors.hintColor,
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEach {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    indication = rememberRipple(),
                                    interactionSource = MutableInteractionSource(),
                                    onClick = it.func
                                )
                                .background(AppTheme.colors.card)
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = it.text,
                                fontSize = 14.sp,
                                color = AppTheme.colors.textColor
                            )
                        }
                        if (it != items.last()) {
                            Box(
                                modifier = Modifier
                                    .background(AppTheme.colors.hintColor)
                                    .height(12.dp)
                                    .width(1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showSystemUi = true)
private fun BottomDialogPreview() {
    val list = ArrayList<BottomButtonItem>()
    list.add(BottomButtonItem("确认") {})
    list.add(BottomButtonItem("取消") {})
    var isShow by remember { mutableStateOf(true) }
    BottomHintDialog(
        hint = "应用向你请求权限",
        items = list,
        isShow = isShow,
        onCancel = {isShow = false}
    )
}