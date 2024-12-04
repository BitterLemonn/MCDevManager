package com.lemon.mcdevmanager.ui.page

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material.ripple
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lemon.mcdevmanager.BuildConfig
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.data.common.LICENSE_PAGE
import com.lemon.mcdevmanager.data.common.OPEN_SOURCE_INFO_PAGE
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.MCDevManagerTheme
import com.lemon.mcdevmanager.ui.widget.AppLoadingWidget
import com.lemon.mcdevmanager.ui.widget.HeaderWidget
import com.lt.compose_views.other.HorizontalSpace
import com.lt.compose_views.other.VerticalSpace

@Composable
fun AboutPage(
    navController: NavController = rememberNavController(),
    showToast: (String, String) -> Unit = { _, _ -> },
    isShowLoading: Boolean = false,
    checkUpdate: () -> Unit = {}
) {
    val context = LocalContext.current

    Column {
        HeaderWidget(title = "关于", leftAction = {
            Box(modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
                .clip(CircleShape)
                .clickable(indication = ripple(),
                    interactionSource = remember { MutableInteractionSource() }) { navController.navigateUp() }) {
                Image(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "back"
                )
            }
        })
        Box(
            modifier = Modifier
                .fillMaxHeight(0.3f)
                .fillMaxWidth()
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_mc),
                    contentDescription = "icon",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxHeight(0.5f)
                        .aspectRatio(1f),
                    colorFilter = ColorFilter.lighting(
                        multiply = AppTheme.colors.imgTintColor,
                        add = Color.Transparent
                    )
                )
                VerticalSpace(dp = 16.dp)
                Text(
                    text = "当前版本  V${BuildConfig.VERSION_NAME}",
                    fontSize = 16.sp,
                    color = AppTheme.colors.textColor,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.card)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { checkUpdate() }
                        .padding(16.dp)
                ) {
                    Text(
                        text = "检查更新",
                        fontSize = 16.sp,
                        color = AppTheme.colors.textColor,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth(0.9f),
                    thickness = 0.5.dp,
                    color = AppTheme.colors.dividerColor
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(16.dp)
                ) {
                    Text(
                        text = "关于作者",
                        fontSize = 16.sp,
                        color = AppTheme.colors.textColor,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth(0.9f),
                    thickness = 0.5.dp,
                    color = AppTheme.colors.dividerColor
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://github.com/BitterLemonn/MCDevManager")
                            }
                            context.startActivity(intent)
                        }
                        .padding(16.dp)) {
                    Row(
                        modifier = Modifier.align(Alignment.CenterStart),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "给个星星",
                            fontSize = 16.sp,
                            color = AppTheme.colors.textColor
                        )
                        HorizontalSpace(dp = 8.dp)
                        Image(
                            painter = painterResource(id = R.drawable.ic_star),
                            contentDescription = "star",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth(0.9f),
                    thickness = 0.5.dp,
                    color = AppTheme.colors.dividerColor
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(16.dp)
                ) {
                    Text(
                        text = "免责声明",
                        fontSize = 16.sp,
                        color = AppTheme.colors.textColor,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row {
                Text(
                    text = "第三方开源库",
                    fontSize = 14.sp,
                    color = Color(0xFF4169E1),
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        navController.navigate(OPEN_SOURCE_INFO_PAGE) {
                            launchSingleTop = true
                        }
                    }
                )
                HorizontalSpace(dp = 8.dp)
                Text(text = "·", fontSize = 14.sp, color = Color(0xFF4169E1))
                HorizontalSpace(dp = 8.dp)
                Text(
                    text = "开源协议",
                    fontSize = 14.sp,
                    color = Color(0xFF4169E1),
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        navController.navigate(LICENSE_PAGE) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            VerticalSpace(dp = 8.dp)
            Text(
                text = "© 2024 BitterLemon 苦柠",
                fontSize = 12.sp,
                color = AppTheme.colors.textColor
            )
            VerticalSpace(dp = 16.dp)
        }
    }

    AnimatedVisibility(
        visible = isShowLoading,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        AppLoadingWidget()
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun AboutPagePreview() {
    MCDevManagerTheme {
        Box(
            modifier = Modifier
                .background(AppTheme.colors.background)
                .fillMaxSize()
        ) {
            AboutPage()
        }
    }
}