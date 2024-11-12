package com.lemon.mcdevmanager.ui.page

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.MCDevManagerTheme
import com.lemon.mcdevmanager.ui.widget.HeaderWidget
import com.lt.compose_views.other.HorizontalSpace
import com.lt.compose_views.other.VerticalSpace

@Composable
fun OpenSourceInfoPage(
    navController: NavController = rememberNavController(),
    showToast: (String, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val openSourceInfo = listOf(
        OpenSourceInfo(
            name = "Coil-Compose",
            license = "Apache 2.0",
            link = "https://github.com/coil-kt/coil"
        ),
        OpenSourceInfo(
            name = "ComposeCharts",
            license = "Apache 2.0",
            link = "https://github.com/ehsannarmani/ComposeCharts"
        ),
        OpenSourceInfo(
            name = "ComposeViews",
            license = "Apache 2.0",
            link = "https://github.com/ltttttttttttt/ComposeViews"
        ),
        OpenSourceInfo(
            name = "Logger",
            license = "Apache 2.0",
            link = "https://github.com/orhanobut/logger"
        ),
        OpenSourceInfo(
            name = "Retrofit2",
            license = "Apache 2.0",
            link = "https://github.com/square/retrofit"
        )
    )

    Column {
        // 标题
        HeaderWidget(title = "开源库", leftAction = {
            Box(modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
                .clip(CircleShape)
                .clickable(indication = rememberRipple(),
                    interactionSource = remember { MutableInteractionSource() }) { navController.navigateUp() }) {
                Image(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "back"
                )
            }
        })
        Box(
            modifier = Modifier
                .fillMaxHeight(0.25f)
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_open_sources),
                contentDescription = "open source",
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f),
                colorFilter = ColorFilter.tint(AppTheme.colors.primaryColor)
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "感谢以下开源库, 排名不分先后",
                    fontSize = 16.sp,
                    color = AppTheme.colors.textColor
                )
                VerticalSpace(dp = 8.dp)
                Column {
                    for (item in openSourceInfo) {
                        SourceItem(
                            source = item,
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.link))
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}

data class OpenSourceInfo(
    val name: String,
    val license: String,
    val link: String
)

@Composable
private fun SourceItem(
    source: OpenSourceInfo,
    onClick: () -> Unit
) {
    val linkString = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                color = AppTheme.colors.info,
                textDecoration = TextDecoration.Underline
            )
        ) { append(source.link) }
    }
    Column(
        modifier = Modifier
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = source.name,
                fontSize = 18.sp,
                color = AppTheme.colors.textColor
            )
            HorizontalSpace(dp = 16.dp)
            Text(
                text = source.license,
                fontSize = 14.sp,
                color = AppTheme.colors.hintColor
            )
        }
        VerticalSpace(dp = 8.dp)
        Box(
            modifier = Modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
        ) {
            Text(
                text = linkString,
                fontSize = 14.sp,
                color = AppTheme.colors.info
            )
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun OpenSourceInfoPagePreview() {
    MCDevManagerTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
        ) {
            OpenSourceInfoPage()
        }
    }
}
