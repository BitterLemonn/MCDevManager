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
}

/**
 * 根据 [pending] 类型分发到对应操作对话框，三种布局统一复用。
 */
@Composable
internal fun WorkManageActionDialog(
    pending: WorkManagePendingOp?,
    onConfirmAction: (ResourceData, WorkItemActionEnum) -> Unit,
    onAdjustPrice: (ResourceData, Int) -> Unit,
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

        null -> {}
    }
}
