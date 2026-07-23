package com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component

import androidx.compose.runtime.Composable
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
