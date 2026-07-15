package com.lemon.mcdevmanagermp.ui.pages.work.workmanage

import com.lemon.mcdevmanagermp.data.consts.enums.WorkItemActionEnum
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import com.lemon.mcdevmanagermp.data.vo.netease.work.ReviewFeedbackVO
import com.lemon.mcdevmanagermp.utils.extension.IUiAction
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import com.lemon.mcdevmanagermp.utils.extension.IUiState

data class WorkManageState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isPerforming: Boolean = false,
    val performingMessage: String = "",
    val items: List<ResourceData> = emptyList(),
    val feedback: ReviewFeedbackVO? = null,
    val feedbackItemName: String = ""
) : IUiState

sealed interface WorkManageAction : IUiAction {
    data object LoadData : WorkManageAction
    data object RefreshData : WorkManageAction

    /** 执行开平写操作：提交审核(3)/撤销审核(4)/上架(6)（itemId-only） */
    data class PerformAction(val item: ResourceData, val action: WorkItemActionEnum) :
        WorkManageAction

    /** 调整定价（当前占位，无对应文档接口） */
    data class AdjustPrice(val item: ResourceData, val newPrice: Int) : WorkManageAction

    /** 定时上架(7)：time 格式 "YYYY-MM-DD HH:mm:ss" */
    data class AppointOnline(val item: ResourceData, val time: String) : WorkManageAction

    /** 查看审核反馈(5)：拉取后写入 state.feedback 展示 */
    data class LoadFeedback(val item: ResourceData) : WorkManageAction

    /** 关闭审核反馈弹窗 */
    data object DismissFeedback : WorkManageAction
}

sealed interface WorkManageEffect : IUiEffect {
    data class ShowToast(val message: String) : WorkManageEffect
    data object NeedReLogin : WorkManageEffect
}
