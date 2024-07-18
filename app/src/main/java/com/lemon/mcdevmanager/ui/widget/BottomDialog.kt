package com.lemon.mcdevmanager.ui.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.TextBlack

@Composable
fun BottomDialog(
    items: List<BottomButtonItem>,
    isShow: Boolean,
    onCancel: () -> Unit
) {
    AnimatedVisibility(
        visible = isShow,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Black.copy(alpha = 0.45f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onCancel.invoke() }
        )
    }
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isShow,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                    .background(color = AppTheme.colors.background)
            ) {
                items(items) { item ->
                    BottomDialogButton(onClick = item.func, text = item.text)
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = (0.5).dp,
                        color = AppTheme.colors.dividerColor.copy(alpha = 0.45f)
                    )
                    if (item == items.last())
                        Spacer(Modifier.height(5.dp))
                }
                item {
                    BottomDialogButton(onClick = { onCancel.invoke() }, text = "取消")
                }
            }
        }
    }
}

@Composable
private fun BottomDialogButton(text: String, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(),
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.card,
        ),
        shape = RectangleShape
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
        ){
            Text(
                text = text,
                fontSize = 16.sp,
                color = AppTheme.colors.textColor,
                letterSpacing = 0.5.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
@Preview(showSystemUi = true)
private fun BottomDialogPreview() {
    val list = ArrayList<BottomButtonItem>()
    list.add(BottomButtonItem("拍照") {})
    list.add(BottomButtonItem("从相册中选择") {})
    var isShow by remember { mutableStateOf(true) }
    BottomDialog(items = list, isShow = isShow, onCancel = { isShow = false })
}

/**
 * 底部弹窗按钮
 */
data class BottomButtonItem(
    val text: String,
    val func: () -> Unit
)