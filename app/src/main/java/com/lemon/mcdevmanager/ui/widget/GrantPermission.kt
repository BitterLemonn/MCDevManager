package com.lemon.mcdevmanager.ui.widget

import android.os.Build
import androidx.compose.runtime.Composable
import android.Manifest.permission.*
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
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
    val notificationManager = NotificationManagerCompat.from(context)

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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationManager.areNotificationsEnabled()) {
                    BottomHintDialog(
                        hint = hint,
                        isShow = isShow,
                        onCancel = onCancel,
                        onConfirm = {
                            val intent =
                                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                            launcher.launch(intent)
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