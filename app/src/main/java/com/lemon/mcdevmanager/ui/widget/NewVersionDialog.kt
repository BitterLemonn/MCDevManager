package com.lemon.mcdevmanager.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.TextWhite
import com.lemon.mcdevmanager.utils.getFileSizeFormat
import com.lemon.mcdevmanager.utils.pxToDp
import com.lt.compose_views.other.VerticalSpace

@Composable
fun NewVersionDialog(
    title: String,
    content: String,
    size: Long = 0L,
    canTouchOutside: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    var contentHeight by remember { mutableStateOf(0) }
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(
                enabled = canTouchOutside,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = AppTheme.colors.card
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .onGloballyPositioned {
                        contentHeight = pxToDp(context, it.size.height.toFloat())
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.info),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_update),
                        contentDescription = "update img",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = AppTheme.colors.dividerColor,
                    thickness = 0.5.dp
                )
                VerticalSpace(dp = 8.dp)
                Text(text = title, fontSize = 16.sp, color = AppTheme.colors.textColor)
                VerticalSpace(dp = 8.dp)
                Text(
                    text = content,
                    fontSize = 14.sp,
                    color = AppTheme.colors.hintColor,
                    modifier = Modifier
                        .heightIn(min = 200.dp)
                        .padding(horizontal = 36.dp),
                    style = TextStyle.Default.copy(lineHeight = 24.sp)
                )
                VerticalSpace(dp = 24.dp)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onDismiss
                            )
                    ) {
                        Text(
                            text = "暂不更新",
                            fontSize = 14.sp,
                            color = AppTheme.colors.hintColor,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    VerticalSpace(dp = 8.dp)
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(0.95f),
                        color = AppTheme.colors.dividerColor,
                        thickness = 0.5.dp
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(AppTheme.colors.primaryColor)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(),
                                onClick = onConfirm
                            )
                    ) {
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "立即更新",
                                fontSize = 16.sp,
                                color = TextWhite
                            )
                            if (size > 0) {
                                Text(
                                    text = "(${getFileSizeFormat(size)})",
                                    fontSize = 14.sp,
                                    color = TextWhite,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun NewVersionDialogPreview() {
    NewVersionDialog(
        title = "发现新版本",
        content = "1. 修复了一些问题\n2. 优化了一些功能",
        size = 283173129,
        onDismiss = {},
        onConfirm = {}
    )
}