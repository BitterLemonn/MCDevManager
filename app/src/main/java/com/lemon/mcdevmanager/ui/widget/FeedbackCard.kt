package com.lemon.mcdevmanager.ui.widget

import android.text.TextUtils
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.data.common.JSONConverter
import com.lemon.mcdevmanager.data.netease.feedback.ConflictModsBean
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.viewModel.FeedbackAction
import com.orhanobut.logger.Logger
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeedbackCard(
    modifier: Modifier = Modifier,
    modName: String,
    modUid: String,
    createTime: Long,
    type: String,
    nickname: String,
    content: String,
    picList: List<String> = emptyList(),
    reply: String? = null,
    onClickImg: (String) -> Unit = {},
    isShowReply: Boolean = false,
    extraContent: @Composable () -> Unit = {}
) {
    var contentStr by remember { mutableStateOf(content) }
    LaunchedEffect(key1 = content) {
        if (content.startsWith("{\"item_list\"")) {
            try {
                val mods = JSONConverter.decodeFromString<ConflictModsBean>(content)
                contentStr = "冲突mod:\n${mods.itemList.joinToString { it.name }}"
                if (!TextUtils.isEmpty(mods.detail)) {
                    contentStr += "\n\n冲突详情: ${mods.detail}"
                }
            } catch (e: Exception) {
                Logger.e(e, "FeedbackCard: 解析冲突mod失败")
                contentStr = "解析冲突mod失败"
            }
        } else {
            contentStr = content
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateContentSize()
            .then(modifier),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.card
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        // 工具头
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_mod),
                contentDescription = "mod icon",
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(24.dp)
                    .align(Alignment.CenterVertically),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.lighting(
                    add = Color.Transparent,
                    multiply = AppTheme.colors.primaryColor
                )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = modName,
                    fontSize = 16.sp,
                    color = AppTheme.colors.textColor,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp)
                )
                Text(
                    text = modUid,
                    fontSize = 12.sp,
                    color = AppTheme.colors.hintColor,
                    modifier = Modifier.padding(
                        start = 8.dp,
                        end = 8.dp,
                        top = 4.dp,
                        bottom = 8.dp
                    )
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(
                Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = ZonedDateTime.ofInstant(
                        Instant.ofEpochMilli(createTime * 1000),
                        ZoneId.of("Asia/Shanghai")
                    ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    fontSize = 12.sp,
                    color = AppTheme.colors.hintColor,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp, top = 8.dp)
                        .align(Alignment.End)
                )
                Text(
                    text = type,
                    fontSize = 12.sp,
                    color = AppTheme.colors.hintColor,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 8.dp)
                        .align(Alignment.End)
                )
            }
            Image(
                painter = painterResource(id = if (TextUtils.isEmpty(reply)) R.drawable.ic_no_reply else R.drawable.ic_replied),
                contentDescription = "reply state",
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(24.dp)
                    .align(Alignment.CenterVertically),
            )
        }

        Text(
            text = nickname,
            fontSize = 14.sp,
            color = AppTheme.colors.textColor,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Text(
            text = contentStr,
            fontSize = 16.sp,
            letterSpacing = 1.sp,
            color = AppTheme.colors.textColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        if (picList.isNotEmpty())
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                picList.forEach {
                    AsyncImage(
                        model = it,
                        contentDescription = "feedback image",
                        imageLoader = ImageLoader(context = LocalContext.current),
                        modifier = Modifier
                            .padding(8.dp)
                            .wrapContentHeight()
                            .heightIn(max = 160.dp)
                            .wrapContentWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberRipple()
                            ) {
                                onClickImg(it)
                            },
                        contentScale = ContentScale.Fit
                    )
                }
            }
        if (isShowReply) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                color = AppTheme.colors.dividerColor,
                thickness = 0.5.dp
            )
            if (!TextUtils.isEmpty(reply))
                Text(
                    text = reply!!,
                    fontSize = 16.sp,
                    color = AppTheme.colors.textColor,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    letterSpacing = 1.sp
                )
            extraContent()
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewFeedbackCard() {
    FeedbackCard(
        modName = "苦柠的奇异饰品",
        modUid = "4668241759157945097",
        createTime = 1716015309,
        nickname = "苦柠",
        type = "故障问题反馈",
        content = "123123123123123",
        reply = "2222222",
        isShowReply = true,
        extraContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, AppTheme.colors.hintColor, RoundedCornerShape(8.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple()
                    ) {}
            ) {
                Row(Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                            .padding(start = 8.dp)
                    ) {
                        Text(
                            text = "赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞赞",
                            fontSize = 16.sp,
                            color = AppTheme.colors.textColor,
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.CenterStart)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .padding(8.dp)
                            .align(Alignment.CenterVertically)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberRipple()
                            ) { }
                    ) {
                        Text(
                            text = "回复",
                            fontSize = 16.sp,
                            color = AppTheme.colors.primaryColor,
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    )
}