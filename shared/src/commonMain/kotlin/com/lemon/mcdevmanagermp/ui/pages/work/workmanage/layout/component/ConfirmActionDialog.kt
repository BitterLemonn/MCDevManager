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
 * 确认上架操作弹窗。确认后由 ViewModel 的 PerformAction 执行真实联网。
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
                text = if (action == WorkItemActionEnum.DELETE) {
                    "删除后不可撤销，确认删除作品《$name》？"
                } else {
                    "即将对作品《$name》执行「${action.label}」操作，是否继续？"
                },
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
