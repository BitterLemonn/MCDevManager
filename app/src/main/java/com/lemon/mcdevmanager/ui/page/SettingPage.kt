package com.lemon.mcdevmanager.ui.page

import android.content.ClipData
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.data.database.database.GlobalDataBase
import com.lemon.mcdevmanager.data.global.AppContext
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.MCDevManagerTheme
import com.lemon.mcdevmanager.ui.theme.TextWhite
import com.lemon.mcdevmanager.ui.widget.HeaderWidget
import com.lemon.mcdevmanager.ui.widget.SNACK_INFO
import com.lt.compose_views.other.VerticalSpace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingPage(
    navController: NavController = rememberNavController(),
    showToast: (String, String) -> Unit = { _, _ -> }
) {
    val clipboardManager = LocalClipboardManager.current.nativeClipboard
    val coroutineScope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize()) {
        HeaderWidget(title = "设置", leftAction = {
            Box(modifier = Modifier
                .padding(8.dp)
                .clip(CircleShape)
                .clickable(
                    indication = rememberRipple(),
                    interactionSource = remember { MutableInteractionSource() },
                ) {
                    navController.navigateUp()
                }) {
                Image(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .then(it),
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.tint(TextWhite)
                )
            }
        })
        SettingCard(
            type = "用户",
            content = listOf(
                SettingItemData("复制用户Cookies") {
                    val clip = ClipData.newPlainText(
                        "cookies",
                        AppContext.cookiesStore[AppContext.nowNickname]
                    )
                    clipboardManager.setPrimaryClip(clip)
                    coroutineScope.launch { showToast("已将cookies复制至剪贴板", SNACK_INFO) }
                }
            )
        )
        SettingCard(
            type = "调试",
            content = listOf(
                SettingItemData("查看日志") {},
                SettingItemData("清除缓存","仅支持清除本账号下的缓存") {
                    coroutineScope.launch(Dispatchers.IO) {
                        withContext(Dispatchers.IO) {
                            GlobalDataBase.database.infoDao()
                                .clearCacheOverviewByNickname(AppContext.nowNickname)
                            GlobalDataBase.database.infoDao()
                                .clearCacheAnalyzeByNicknamePlatform(AppContext.nowNickname, "pc")
                            GlobalDataBase.database.infoDao()
                                .clearCacheAnalyzeByNicknamePlatform(AppContext.nowNickname, "pe")
                        }
                        showToast("已清除本账号下的缓存", SNACK_INFO)
                    }
                }
            )
        )
        SettingCard(
            type = "关于",
            content = listOf(
                SettingItemData("关于MC开发者管理器") {}
            )
        )
    }
}

@Composable
private fun SettingCard(
    type: String? = null,
    content: List<SettingItemData>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 8.dp, end = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.card
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            type?.let {
                SettingType(text = it)
            }
            content.forEachIndexed { index, it ->
                SettingItem(text = it.text, hint = it.hint, onClick = it.onClick)
                if (index != content.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = AppTheme.colors.dividerColor
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingItem(
    text: String,
    hint: String? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .clickable(
                onClick = onClick,
                indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Text(
                text = text,
                fontSize = 16.sp,
                color = AppTheme.colors.textColor
            )
            if (hint != null) {
                Text(
                    text = hint,
                    fontSize = 12.sp,
                    color = AppTheme.colors.hintColor
                )
            }
        }
    }
}

@Composable
private fun SettingType(
    text: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = AppTheme.colors.hintColor
        )
    }
}

private data class SettingItemData(
    val text: String,
    val hint: String? = null,
    val onClick: () -> Unit
)

@Preview(showBackground = true)
@Composable
private fun SettingPagePreview() {
    MCDevManagerTheme {
        Box(modifier = Modifier.background(AppTheme.colors.background)) {
            SettingPage()
        }
    }
}