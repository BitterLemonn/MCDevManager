package com.lemon.mcdevmanagermp.ui.pages.work.workmanage

import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.model.WorkItemAction
import com.lemon.mcdevmanagermp.utils.extension.IUiAction
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import com.lemon.mcdevmanagermp.utils.extension.IUiState

data class WorkManageState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val items: List<ResourceData> = emptyList()
) : IUiState

sealed interface WorkManageAction : IUiAction {
    data object LoadData : WorkManageAction
    data object RefreshData : WorkManageAction

    /** 执行上架操作（当前占位，后续接真实接口） */
    data class PerformAction(val item: ResourceData, val action: WorkItemAction) : WorkManageAction
}

sealed interface WorkManageEffect : IUiEffect {
    data class ShowToast(val message: String) : WorkManageEffect
    data object NeedReLogin : WorkManageEffect
}
