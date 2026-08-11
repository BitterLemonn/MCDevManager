package com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.consts.enums.WorkItemActionEnum
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData

/**
 * 卡片操作触发的待确认操作，决定弹出哪种对话框。
 */
sealed interface WorkManagePendingOp {
    val item: ResourceData

    /** 普通操作（提交审核/上架/更新等）→ 确认弹窗 */
    data class Confirm(override val item: ResourceData, val action: WorkItemActionEnum) :
        WorkManagePendingOp

    /** 提交自测 → 免机审选择弹窗 */
    data class SubmitSelfTest(override val item: ResourceData) : WorkManagePendingOp

    /** 调整定价 → 价格输入弹窗 */
    data class AdjustPrice(override val item: ResourceData) : WorkManagePendingOp

    /** 定时上架 → 日期+时间选择弹窗 */
    data class AppointOnline(override val item: ResourceData) : WorkManagePendingOp
}

/**
 * 根据 [pending] 类型分发到对应操作对话框，三种布局统一复用。
 */
@Composable
internal fun WorkManageActionDialog(
    pending: WorkManagePendingOp?,
    onConfirmAction: (ResourceData, WorkItemActionEnum) -> Unit,
    onSubmitSelfTest: (ResourceData, Boolean) -> Unit,
    onAdjustPrice: (ResourceData, Int) -> Unit,
    onAppointOnline: (ResourceData, String) -> Unit,
    onDismiss: () -> Unit
) {
    when (pending) {
        is WorkManagePendingOp.Confirm -> ConfirmActionDialog(
            item = pending.item,
            action = pending.action,
            onConfirm = { onConfirmAction(pending.item, pending.action) },
            onDismiss = onDismiss
        )

        is WorkManagePendingOp.SubmitSelfTest -> SubmitSelfTestDialog(
            item = pending.item,
            onConfirm = { passCheck -> onSubmitSelfTest(pending.item, passCheck) },
            onDismiss = onDismiss
        )

        is WorkManagePendingOp.AdjustPrice -> PriceAdjustDialog(
            item = pending.item,
            onConfirm = { newPrice -> onAdjustPrice(pending.item, newPrice) },
            onDismiss = onDismiss
        )

        is WorkManagePendingOp.AppointOnline -> AppointOnlineDialog(
            item = pending.item,
            onConfirm = { time -> onAppointOnline(pending.item, time) },
            onDismiss = onDismiss
        )

        null -> {}
    }
}

@Composable
private fun SubmitSelfTestDialog(
    item: ResourceData,
    onConfirm: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var passCheck by remember(item.itemId) { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("确认提交自测") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "即将对作品《${item.itemName.ifEmpty { "未命名" }}》提交自测，是否继续？",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { passCheck = !passCheck },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = passCheck, onCheckedChange = { passCheck = it })
                    Text("免机审")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(passCheck) }) { Text("确认") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
