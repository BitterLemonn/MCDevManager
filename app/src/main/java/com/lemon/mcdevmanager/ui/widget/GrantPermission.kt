package com.lemon.mcdevmanager.ui.widget

import android.Manifest.permission.FOREGROUND_SERVICE
import android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.REQUEST_INSTALL_PACKAGES
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.orhanobut.logger.Logger

enum class PermissionType {
    READ, WRITE, POST_NOTIFICATION, INSTALL_APK, FOREGROUND_SERVICE
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GrantPermission(
    isShow: Boolean,
    permissions: List<Pair<PermissionType, String>>,
    onCancel: () -> Unit,
    doAfterPermission: () -> Unit
) {
    val context = LocalContext.current

    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) doAfterPermission()
            else onCancel.invoke()
        }
    val packageManager = context.packageManager

    for (permissionData in permissions) {
        val permission = permissionData.first
        val hint = permissionData.second
        val permissionGet = rememberPermissionState(
            permission = when (permission) {
                PermissionType.READ -> READ_EXTERNAL_STORAGE
                PermissionType.WRITE -> WRITE_EXTERNAL_STORAGE
                PermissionType.FOREGROUND_SERVICE -> if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) FOREGROUND_SERVICE else READ_EXTERNAL_STORAGE
                PermissionType.POST_NOTIFICATION -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) POST_NOTIFICATIONS else READ_EXTERNAL_STORAGE
                PermissionType.INSTALL_APK -> REQUEST_INSTALL_PACKAGES
            }
        )

        if (permission in listOf(
                PermissionType.INSTALL_APK,
                PermissionType.POST_NOTIFICATION
            )
        ) {
            if (permission == PermissionType.INSTALL_APK) {
                if (!packageManager.canRequestPackageInstalls()) {
                    BottomHintDialog(
                        hint = hint,
                        isShow = isShow,
                        onCancel = onCancel,
                        onConfirm = {
                            val intent =
                                Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                            launcher.launch(intent)
                        }
                    )
                } else if (isShow) doAfterPermission()
            } else if (permission == PermissionType.POST_NOTIFICATION) {
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationManager.areNotificationsEnabled()) {
                    BottomHintDialog(
                        hint = hint,
                        isShow = isShow,
                        onCancel = onCancel,
                        onConfirm = {
                            try{
                                val intent =
                                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    }
                                launcher.launch(intent)
                            }catch (e: Exception){
                                Logger.e("打开通知设置失败: ${e.message}")
                                onCancel.invoke()
                            }
                        }
                    )
                } else if (isShow) doAfterPermission()
            }
        } else {
            when (permissionGet.status) {
                is PermissionStatus.Denied -> {
                    BottomHintDialog(
                        hint = hint,
                        isShow = isShow,
                        onCancel = onCancel,
                        onConfirm = { permissionGet.launchPermissionRequest() }
                    )
                }

                is PermissionStatus.Granted -> if (isShow) doAfterPermission()
            }
        }
    }
}