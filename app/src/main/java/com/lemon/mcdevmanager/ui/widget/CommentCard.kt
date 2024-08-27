package com.lemon.mcdevmanager.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.MCDevManagerTheme
import com.lemon.mcdevmanager.utils.getNoScaleTextSize
import com.lt.compose_views.other.HorizontalSpace
import com.lt.compose_views.other.VerticalSpace
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CommentCard(
    modifier: Modifier = Modifier,
    resName: String = "",
    iid: String = "",
    tag: String = "",
    comment: String = "",
    nickname: String = "",
    stars: Int = 0,
    publishTime: Long = 0
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .then(modifier),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.card
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_mod),
                    contentDescription = "",
                    modifier = Modifier
                        .size(18.dp),
                    colorFilter = ColorFilter.tint(AppTheme.colors.primaryColor)
                )
                HorizontalSpace(dp = 8.dp)
                Column {
                    Text(
                        text = resName,
                        color = AppTheme.colors.textColor,
                        fontSize = getNoScaleTextSize(context, 14f).sp
                    )
                    HorizontalSpace(dp = 8.dp)

                    Text(
                        text = iid,
                        color = AppTheme.colors.hintColor,
                        fontSize = getNoScaleTextSize(context, 12f).sp
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = tag,
                        color = AppTheme.colors.hintColor,
                        fontSize = getNoScaleTextSize(context, 12f).sp
                    )
                    HorizontalSpace(dp = 8.dp)
                    Text(
                        text = SimpleDateFormat(
                            "yyyy-MM-dd", Locale.CHINA
                        ).format(publishTime * 1000),
                        color = AppTheme.colors.hintColor,
                        fontSize = getNoScaleTextSize(context, 12f).sp
                    )
                }
            }

            VerticalSpace(dp = 8.dp)
            if (comment.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_comment),
                        contentDescription = "",
                        modifier = Modifier
                            .size(18.dp),
                        colorFilter = ColorFilter.tint(AppTheme.colors.primaryColor)
                    )
                    HorizontalSpace(dp = 8.dp)
                    Text(
                        text = nickname,
                        color = AppTheme.colors.textColor,
                        fontSize = getNoScaleTextSize(context, 14f).sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    LazyRow {
                        items(stars) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_star),
                                contentDescription = "",
                                modifier = Modifier
                                    .size(18.dp),
                                colorFilter = ColorFilter.tint(AppTheme.colors.primaryColor)
                            )
                        }
                    }
                }
                VerticalSpace(dp = 8.dp)
                Text(
                    text = comment,
                    color = AppTheme.colors.hintColor,
                    fontSize = getNoScaleTextSize(context, 14f).sp
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_user),
                        contentDescription = "用户",
                        modifier = Modifier.size(18.dp),
                        colorFilter = ColorFilter.tint(AppTheme.colors.primaryColor)
                    )
                    HorizontalSpace(dp = 8.dp)
                    Text(
                        text = nickname,
                        color = AppTheme.colors.textColor,
                        fontSize = getNoScaleTextSize(context, 14f).sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    LazyRow {
                        items(stars) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_star),
                                contentDescription = "",
                                modifier = Modifier
                                    .size(18.dp),
                                colorFilter = ColorFilter.tint(AppTheme.colors.primaryColor)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun CommentCardPreview() {
    MCDevManagerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
        ) {
            CommentCard(
                resName = "苦柠的奇异饰品",
                iid = "4668241759157945097",
                tag = "默认类型",
                comment = "",
                nickname = "nickname",
                stars = 5,
                publishTime = System.currentTimeMillis() / 1000
            )
        }
    }
}