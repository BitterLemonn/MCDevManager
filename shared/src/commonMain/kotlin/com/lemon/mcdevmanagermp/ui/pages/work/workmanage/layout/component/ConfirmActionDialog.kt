package com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.lemon.mcdevmanagermp.data.consts.enums.WorkItemActionEnum
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData

/**
 * 确认上架操作弹窗。
 *
 * 注意：当前为占位实现，确认后仅通过 [onConfirm] 触发提示，不真实联网。
 * 后续接口补齐后由 ViewModel 的 PerformAction 改为真实调用。
 */
@Composable
internal fun ConfirmActionDialog(
    item: ResourceData,
    action: WorkItemActionEnum,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("确认${action.label}") },
        text = {
            val name = item.itemName.ifEmpty { "未命名" }
            Text(
                text = "即将对作品《$name》执行「${action.label}」操作。\n\n（操作接口占位，当前不会真实提交）",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text("确认") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
