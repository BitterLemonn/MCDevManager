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

    /** 执行仅需 itemId 的写操作 */
    data class PerformAction(val item: ResourceData, val action: WorkItemActionEnum) :
        WorkManageAction

    /** 提交自测，passCheck=true 表示免机审 */
    data class SubmitSelfTest(val item: ResourceData, val passCheck: Boolean) : WorkManageAction

    /** 调整定价 */
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
