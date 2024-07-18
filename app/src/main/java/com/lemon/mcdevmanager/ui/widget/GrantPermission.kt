package com.lemon.mcdevmanager.ui.widget

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState

enum class PermissionType {
    READ, WRITE
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GrantPermission(
    isShow: Boolean,
    permission: PermissionType,
    textDenied: String,
    textBlock: String,
    onCancel: () -> Unit,
    doAfterPermission: () -> Unit
) {
    val permissionGet = rememberPermissionState(
        permission = when (permission) {
            PermissionType.READ -> android.Manifest.permission.READ_EXTERNAL_STORAGE
            PermissionType.WRITE -> android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        }
    )

    when (permissionGet.status) {
        is PermissionStatus.Denied -> {
            val textShow =
                if ((permissionGet.status as PermissionStatus.Denied).shouldShowRationale) textDenied
                else textBlock
            BottomHintDialog(
                hint = textShow,
                isShow = isShow,
                onCancel = onCancel,
                onConfirm = { permissionGet.launchPermissionRequest() }
            )
        }
        is PermissionStatus.Granted -> if (isShow) doAfterPermission.invoke()
    }
}