package com.lemon.mcdevmanager.ui.page

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lemon.mcdevmanager.BuildConfig
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.service.DownloadService
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.MCDevManagerTheme
import com.lemon.mcdevmanager.ui.widget.AppLoadingWidget
import com.lemon.mcdevmanager.ui.widget.GrantPermission
import com.lemon.mcdevmanager.ui.widget.HeaderWidget
import com.lemon.mcdevmanager.ui.widget.NewVersionDialog
import com.lemon.mcdevmanager.ui.widget.PermissionType
import com.lemon.mcdevmanager.ui.widget.SNACK_ERROR
import com.lemon.mcdevmanager.ui.widget.SNACK_INFO
import com.lemon.mcdevmanager.ui.widget.SNACK_WARN
import com.lemon.mcdevmanager.viewModel.AboutViewActions
import com.lemon.mcdevmanager.viewModel.AboutViewEvents
import com.lemon.mcdevmanager.viewModel.AboutViewModel
import com.lt.compose_views.other.HorizontalSpace
import com.lt.compose_views.other.VerticalSpace
import com.zj.mvi.core.observeEvent
import java.io.File

@Composable
fun AboutPage(
    navController: NavController = rememberNavController(),
    showToast: (String, String) -> Unit = { _, _ -> },
    viewModel: AboutViewModel = viewModel()
) {

    val states by viewModel.viewStates.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    var isShowNewVersionDialog by remember { mutableStateOf(false) }
    var isShowGrandPermissionDialog by remember { mutableStateOf(false) }
    var isShowGrandInstallPermissionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        viewModel.viewEvents.observeEvent(lifecycleOwner) { event ->
            when (event) {
                is AboutViewEvents.ShowToast -> showToast(event.msg, event.type)
                is AboutViewEvents.DownloadFailed ->
                    showToast("下载失败: ${event.msg}", SNACK_ERROR)

                is AboutViewEvents.DownloadStart -> {
                    val fileName = event.downloadUrl.substringAfterLast("/")
                    val targetPath =
                        context.getExternalFilesDir("update" + File.separator + "apk")?.absolutePath + File.separator + fileName
                    val downloadIntent = Intent(context, DownloadService::class.java)
                    downloadIntent.putExtras(Bundle().apply {
                        putString("url", event.downloadUrl)
                        putString("targetPath", targetPath)
                    })

                    context.startService(downloadIntent)
                    showToast("开始下载", SNACK_INFO)
                }

                is AboutViewEvents.ShowNewVersionDialog -> isShowNewVersionDialog = true
            }
        }
    }


    Column {
        HeaderWidget(title = "关于", leftAction = {
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
                        .clickable { viewModel.dispatch(AboutViewActions.CheckUpdate) }
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
                    ) {}
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
                    ) {}
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
        visible = isShowNewVersionDialog,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        states.latestBean?.let {
            NewVersionDialog(
                canTouchOutside = true,
                title = "发现新版本 " + it.tagName,
                content = it.body,
                size = it.assets[0].size,
                onDismiss = { isShowNewVersionDialog = false }
            ) { isShowGrandPermissionDialog = true }
        }
    }

    GrantPermission(
        isShow = isShowGrandPermissionDialog,
        permissions = listOf(
            Pair(
                PermissionType.POST_NOTIFICATION,
                "MC开发者管理器需要使用通知权限确保发送下载进度通知"
            )
        ),
        onCancel = {
            isShowGrandPermissionDialog = false
            showToast("未授予通知权限, 无法通知下载进度", SNACK_WARN)
        },
    ) {
        isShowGrandPermissionDialog = false
        isShowGrandInstallPermissionDialog = true
    }

    GrantPermission(
        isShow = isShowGrandInstallPermissionDialog,
        permissions = listOf(
            Pair(
                PermissionType.INSTALL_APK,
                "MC开发者管理器需要使用安装权限确保自动安装下载的APK"
            )
        ),
        onCancel = {
            isShowGrandInstallPermissionDialog = false
            isShowNewVersionDialog = false
            viewModel.dispatch(AboutViewActions.DownloadAsset)
            showToast("未授予安装权限, 无法自动安装下载的APK", SNACK_WARN)
        }
    ) {
        isShowGrandInstallPermissionDialog = false
        isShowNewVersionDialog = false
        viewModel.dispatch(AboutViewActions.DownloadAsset)
    }

    AnimatedVisibility(
        visible = states.isLoading,
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