package com.lemon.mcdevmanager.ui.base

import android.content.Intent
import android.os.Bundle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lemon.mcdevmanager.MainActivity
import com.lemon.mcdevmanager.data.common.ABOUT_PAGE
import com.lemon.mcdevmanager.data.common.ANALYZE_PAGE
import com.lemon.mcdevmanager.data.common.COMMENT_PAGE
import com.lemon.mcdevmanager.data.common.FEEDBACK_PAGE
import com.lemon.mcdevmanager.data.common.INCENTIVE_PAGE
import com.lemon.mcdevmanager.data.common.INCOME_DETAIL_PAGE
import com.lemon.mcdevmanager.data.common.LICENSE_PAGE
import com.lemon.mcdevmanager.data.common.LOGIN_PAGE
import com.lemon.mcdevmanager.data.common.LOG_PAGE
import com.lemon.mcdevmanager.data.common.MAIN_PAGE
import com.lemon.mcdevmanager.data.common.OPEN_SOURCE_INFO_PAGE
import com.lemon.mcdevmanager.data.common.PROFIT_PAGE
import com.lemon.mcdevmanager.data.common.REALTIME_PROFIT_PAGE
import com.lemon.mcdevmanager.data.common.SETTING_PAGE
import com.lemon.mcdevmanager.data.common.SPLASH_PAGE
import com.lemon.mcdevmanager.service.DownloadService
import com.lemon.mcdevmanager.ui.page.AboutPage
import com.lemon.mcdevmanager.ui.page.AnalyzePage
import com.lemon.mcdevmanager.ui.page.CommentPage
import com.lemon.mcdevmanager.ui.page.FeedbackPage
import com.lemon.mcdevmanager.ui.page.IncentivePage
import com.lemon.mcdevmanager.ui.page.IncomePage
import com.lemon.mcdevmanager.ui.page.LicensePage
import com.lemon.mcdevmanager.ui.page.LogViewPage
import com.lemon.mcdevmanager.ui.page.LoginPage
import com.lemon.mcdevmanager.ui.page.MainPage
import com.lemon.mcdevmanager.ui.page.OpenSourceInfoPage
import com.lemon.mcdevmanager.ui.page.ProfitPage
import com.lemon.mcdevmanager.ui.page.RealtimeProfitPage
import com.lemon.mcdevmanager.ui.page.SettingPage
import com.lemon.mcdevmanager.ui.page.SplashPage
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.widget.AppSnackbar
import com.lemon.mcdevmanager.ui.widget.GrantPermission
import com.lemon.mcdevmanager.ui.widget.NewVersionDialog
import com.lemon.mcdevmanager.ui.widget.PermissionType
import com.lemon.mcdevmanager.ui.widget.SNACK_INFO
import com.lemon.mcdevmanager.ui.widget.SNACK_WARN
import com.lemon.mcdevmanager.ui.widget.popupSnackBar
import com.lemon.mcdevmanager.viewModel.UpdateViewActions
import com.lemon.mcdevmanager.viewModel.UpdateViewEvents
import com.lemon.mcdevmanager.viewModel.UpdateViewmodel
import com.zj.mvi.core.observeEvent
import java.io.File

@Composable
fun BaseScaffold() {
    val activity = LocalContext.current as MainActivity

    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    val viewModel: UpdateViewmodel = viewModel()
    val states by viewModel.viewStates.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    var isShowNewVersionDialog by remember { mutableStateOf(false) }
    var isShowGrandPermissionDialog by remember { mutableStateOf(false) }
    var isShowGrandInstallPermissionDialog by remember { mutableStateOf(false) }

    fun showToast(msg: String, flag: String) = popupSnackBar(
        scope = scope, snackbarHostState = snackBarHostState, message = msg, label = flag
    )

    LaunchedEffect(key1 = Unit) {
        viewModel.dispatch(UpdateViewActions.CheckUpdate)
        viewModel.viewEvents.observeEvent(lifecycleOwner) { event ->
            when (event) {
                is UpdateViewEvents.ShowToast -> showToast(event.msg, event.type)
                is UpdateViewEvents.ShowNewVersionDialog -> isShowNewVersionDialog = true
                is UpdateViewEvents.DownloadStart -> {
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

                is UpdateViewEvents.DownloadFailed -> {
                    showToast("下载失败: ${event.msg}", SNACK_INFO)
                }
            }
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize(), snackbarHost = {
        SnackbarHost(hostState = snackBarHostState, snackbar = { AppSnackbar(data = it) })
    }) { padding ->
        NavHost(
            navController = navController,
            modifier = Modifier
                .background(color = AppTheme.colors.background)
                .fillMaxSize()
                .padding(padding),
            startDestination = SPLASH_PAGE
        ) {
            // 启动页
            composable(route = SPLASH_PAGE) {
                activity.overrideStatusBarStyle(
                    lightColor = Color(0xFF50C878).toArgb(),
                    darkColor = Color(0xFF417C54).toArgb()
                )
                SplashPage(
                    navController = navController,
                    showToast = { msg, flag -> showToast(msg, flag) }
                )
            }
            // 登录页
            composable(route = LOGIN_PAGE) {
                activity.overrideStatusBarStyle(
                    lightColor = Color.Transparent.toArgb(),
                    darkColor = Color.Transparent.toArgb()
                )
                LoginPage(
                    navController = navController,
                    showToast = { msg, flag -> showToast(msg, flag) })
            }
            // 主页
            composable(
                route = MAIN_PAGE,
                exitTransition = {
                    slideOutHorizontally(animationSpec = tween(200), targetOffsetX = { -it })
                }
            ) {
                activity.overrideStatusBarStyle(
                    lightColor = Color.Transparent.toArgb(),
                    darkColor = Color.Transparent.toArgb()
                )
                MainPage(
                    navController = navController,
                    showToast = { msg, flag -> showToast(msg, flag) }
                )
            }
            // 玩家反馈页
            composable(
                route = FEEDBACK_PAGE,
                enterTransition = {
                    slideInHorizontally(animationSpec = tween(200), initialOffsetX = { -it })
                }, popExitTransition = {
                    slideOutHorizontally(animationSpec = tween(200), targetOffsetX = { -it })
                }
            ) {
                activity.resetStatusBarStyle()
                FeedbackPage(
                    navController = navController,
                    showToast = { msg, flag -> showToast(msg, flag) }
                )
            }
            // 玩家评论页
            composable(
                route = COMMENT_PAGE,
                enterTransition = {
                    slideInHorizontally(animationSpec = tween(200), initialOffsetX = { -it })
                }, popExitTransition = {
                    slideOutHorizontally(animationSpec = tween(200), targetOffsetX = { -it })
                }
            ) {
                activity.resetStatusBarStyle()
                CommentPage(
                    navController = navController,
                    showToast = { msg, flag -> showToast(msg, flag) }
                )
            }
            // 数据分析页
            composable(
                route = ANALYZE_PAGE,
                enterTransition = {
                    slideInHorizontally(animationSpec = tween(200), initialOffsetX = { -it })
                }, popExitTransition = {
                    slideOutHorizontally(animationSpec = tween(200), targetOffsetX = { -it })
                }
            ) {
                activity.resetStatusBarStyle()
                AnalyzePage(
                    navController = navController,
                    showToast = { msg, flag -> showToast(msg, flag) }
                )
            }
            // 实时收益页
            composable(
                route = REALTIME_PROFIT_PAGE,
                enterTransition = {
                    slideInHorizontally(animationSpec = tween(200), initialOffsetX = { -it })
                }, popExitTransition = {
                    slideOutHorizontally(animationSpec = tween(200), targetOffsetX = { -it })
                }
            ) {
                activity.resetStatusBarStyle()
                RealtimeProfitPage(
                    navController = navController,
                    showToast = { msg, flag -> showToast(msg, flag) }
                )
            }
            // 收益管理页
            composable(
                route = PROFIT_PAGE,
                enterTransition = {
                    slideInHorizontally(animationSpec = tween(200), initialOffsetX = { -it })
                }, popExitTransition = {
                    slideOutHorizontally(animationSpec = tween(200), targetOffsetX = { -it })
                },
                exitTransition = {
                    slideOutHorizontally(animationSpec = tween(200), targetOffsetX = { -it })
                }
            ) {
                activity.resetStatusBarStyle()
                ProfitPage(navController = navController)
            }
            // 收益详情页
            composable(
                route = INCOME_DETAIL_PAGE,
                enterTransition = {
                    slideInHorizontally(animationSpec = tween(200), initialOffsetX = { -it })
                }, popExitTransition = {
                    slideOutHorizontally(animationSpec = tween(200), targetOffsetX = { -it })
                },
                exitTransition = {
                    slideOutHorizontally(animationSpec = tween(200), targetOffsetX = { -it })
                }
            ) {
                activity.resetStatusBarStyle()
                IncomePage(navController = navController)
            }
            // 激励历史页
            composable(
                route = INCENTIVE_PAGE,
                enterTransition = {
                    slideInHorizontally(animationSpec = tween(200), initialOffsetX = { -it })
                }, popExitTransition = {
                    slideOutHorizontally(animationSpec = tween(200), targetOffsetX = { -it })
                },
                exitTransition = {
                    slideOutHorizontally(animationSpec = tween(200), targetOffsetX = { -it })
                }
            ) {
                activity.resetStatusBarStyle()
                IncentivePage(
                    navController = navController,
                    showToast = { msg, flag -> showToast(msg, flag) }
                )
            }
            // 设置页
            composable(
                route = SETTING_PAGE,
                enterTransition = {
                    slideInHorizontally(animationSpec = tween(200), initialOffsetX = { -it })
                }, popExitTransition = {
                    slideOutHorizontally(animationSpec = tween(200), targetOffsetX = { -it })
                },
                exitTransition = {
                    slideOutHorizontally(animationSpec = tween(200), targetOffsetX = { -it })
                }
            ) {
                activity.resetStatusBarStyle()
                SettingPage(
                    navController = navController,
                    showToast = { msg, flag -> showToast(msg, flag) }
                )
            }
            // 日志页
            composable(
                route = LOG_PAGE,
                enterTransition = {
                    slideInHorizontally(animationSpec = tween(200), initialOffsetX = { -it })
                }, popExitTransition = {
                    slideOutHorizontally(animationSpec = tween(200), targetOffsetX = { -it })
                },
                exitTransition = {
                    slideOutHorizontally(animationSpec = tween(200), targetOffsetX = { -it })
                }
            ) {
                activity.resetStatusBarStyle()
                LogViewPage(
                    navController = navController,
                    showToast = { msg, flag -> showToast(msg, flag) }
                )
            }
            // 关于页
            composable(
                route = ABOUT_PAGE,
                enterTransition = {
                    slideInHorizontally(animationSpec = tween(200), initialOffsetX = { -it })
                }, popExitTransition = {
                    slideOutHorizontally(animationSpec = tween(200), targetOffsetX = { -it })
                },
                exitTransition = {
                    slideOutHorizontally(animationSpec = tween(200), targetOffsetX = { -it })
                }
            ) {
                activity.resetStatusBarStyle()
                AboutPage(
                    navController = navController,
                    showToast = { msg, flag -> showToast(msg, flag) },
                    checkUpdate = { viewModel.dispatch(UpdateViewActions.CheckUpdate) },
                    isShowLoading = states.isLoading
                )
            }
            // 开源信息页
            composable(
                route = OPEN_SOURCE_INFO_PAGE,
                enterTransition = {
                    slideInHorizontally(animationSpec = tween(200), initialOffsetX = { -it })
                }, popExitTransition = {
                    slideOutHorizontally(animationSpec = tween(200), targetOffsetX = { -it })
                },
                exitTransition = {
                    slideOutHorizontally(animationSpec = tween(200), targetOffsetX = { -it })
                }
            ) {
                activity.resetStatusBarStyle()
                OpenSourceInfoPage(
                    navController = navController,
                    showToast = { msg, flag -> showToast(msg, flag) }
                )
            }
            // 开源许可页
            composable(
                route = LICENSE_PAGE,
                enterTransition = {
                    slideInHorizontally(animationSpec = tween(200), initialOffsetX = { -it })
                }, popExitTransition = {
                    slideOutHorizontally(animationSpec = tween(200), targetOffsetX = { -it })
                },
                exitTransition = {
                    slideOutHorizontally(animationSpec = tween(200), targetOffsetX = { -it })
                }
            ) {
                activity.resetStatusBarStyle()
                LicensePage(
                    navController = navController,
                    showToast = { msg, flag -> showToast(msg, flag) }
                )
            }
        }

        // 检查更新
        AnimatedVisibility(
            visible = isShowNewVersionDialog && states.latestBean != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutHorizontally() + fadeOut()
        ) {
            NewVersionDialog(
                title = "发现新版本 " + states.latestBean?.tagName,
                content = states.latestBean?.body ?: "",
                size = states.latestBean?.let { it.assets[0].size } ?: 0,
                onDismiss = { isShowNewVersionDialog = false },
            ) {
                isShowGrandPermissionDialog = true
            }
        }
        if (isShowGrandPermissionDialog) {
            GrantPermission(
                isShow = true,
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
        }
        if (isShowGrandInstallPermissionDialog) {
            GrantPermission(
                isShow = true,
                permissions = listOf(
                    Pair(
                        PermissionType.INSTALL_APK,
                        "MC开发者管理器需要使用安装权限确保自动安装下载的APK"
                    )
                ),
                onCancel = {
                    isShowGrandInstallPermissionDialog = false
                    isShowNewVersionDialog = false
                    viewModel.dispatch(UpdateViewActions.DownloadAsset)
                    showToast("未授予安装权限, 无法自动安装下载的APK", SNACK_WARN)
                }
            ) {
                isShowGrandInstallPermissionDialog = false
                isShowNewVersionDialog = false
                viewModel.dispatch(UpdateViewActions.DownloadAsset)
            }
        }
    }
}