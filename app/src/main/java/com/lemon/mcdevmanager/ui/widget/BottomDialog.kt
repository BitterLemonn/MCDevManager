package com.lemon.mcdevmanager.ui.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
    if (isShow)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = TextBlack.copy(alpha = 0.4f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onCancel.invoke() }
        )
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isShow,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideIn { fullSize -> IntOffset(0, fullSize.height) },
            exit = slideOut { fullSize -> IntOffset(0, fullSize.height) }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                    .background(color = AppTheme.colors.hintColor)
            ) {
                items(items) { item ->
                    BottomDialogButton(onClick = item.func, text = item.text)
                    Divider(
                        modifier = Modifier.fillMaxWidth(),
                        color = AppTheme.colors.hintColor,
                        thickness = (0.5).dp
                    )
                    if (item == items.last())
                        Spacer(Modifier.height(5.dp))
                }
                item {
                    Divider(
                        modifier = Modifier.fillMaxWidth(),
                        color = AppTheme.colors.hintColor,
                        thickness = (0.5).dp
                    )
                    BottomDialogButton(onClick = { onCancel.invoke() }, text = "取消")
                }
            }
        }
    }
}

@Composable
fun BottomDialogButton(text: String, onClick: () -> Unit = {}) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.card),
        contentPadding = PaddingValues(vertical = 20.dp),
        shape = RectangleShape
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = AppTheme.colors.textColor
        )
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

data class BottomButtonItem(
    val text: String,
    val func: () -> Unit
)