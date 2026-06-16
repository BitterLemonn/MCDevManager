package com.lemon.mcdevmanagermp.ui.pages.work.workmanage

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.consts.enums.WorkItemActionEnum
import com.lemon.mcdevmanagermp.data.repository.ResourceRepositoryImpl
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import com.lemon.mcdevmanagermp.domain.work.WorkManageUseCase
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import kotlinx.coroutines.launch

class WorkManageViewModel :
    BaseViewModel<WorkManageState, WorkManageAction, WorkManageEffect>(WorkManageState()) {

    private val workManageUseCase = WorkManageUseCase(
        resourceRepository = ResourceRepositoryImpl.INSTANCE
    )

    override fun dispatch(action: WorkManageAction) {
        when (action) {
            WorkManageAction.LoadData -> loadWorks()
            WorkManageAction.RefreshData -> refreshWorks()
            is WorkManageAction.PerformAction -> performAction(action.item, action.action)
            is WorkManageAction.AdjustPrice -> adjustPrice(action.item, action.newPrice)
        }
    }

    private fun loadWorks() {
        if (state.value.isLoading) return
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            when (val result = workManageUseCase.getWorkList()) {
                is NetworkState.Success -> setState {
                    copy(isLoading = false, isRefreshing = false, items = result.data ?: emptyList())
                }

                is NetworkState.Error -> {
                    setState { copy(isLoading = false, isRefreshing = false) }
                    handleError(
                        result,
                        onNeedReLogin = { WorkManageEffect.NeedReLogin },
                        onShowToast = { WorkManageEffect.ShowToast(it) }
                    )
                }
            }
        }
    }

    private fun refreshWorks() {
        if (state.value.isRefreshing) return
        viewModelScope.launch {
            setState { copy(isRefreshing = true) }
            when (val result = workManageUseCase.getWorkList()) {
                is NetworkState.Success -> setState {
                    copy(isRefreshing = false, isLoading = false, items = result.data ?: emptyList())
                }

                is NetworkState.Error -> {
                    setState { copy(isRefreshing = false, isLoading = false) }
                    handleError(
                        result,
                        onNeedReLogin = { WorkManageEffect.NeedReLogin },
                        onShowToast = { WorkManageEffect.ShowToast(it) }
                    )
                }
            }
        }
    }

    /**
     * 执行上架操作（占位：当前仅提示，不真实联网）。
     * 后续接口补齐时改为调用 WorkManageUseCase，成功后 dispatch(RefreshData) 刷新列表。
     */
    private fun performAction(item: ResourceData, action: WorkItemActionEnum) {
        sendEffect(WorkManageEffect.ShowToast("已${action.label}《${item.itemName}》（接口占位）"))
    }

    /**
     * 调整定价（占位：当前仅提示，不真实联网）。
     * 后续接口补齐时改为调用 WorkManageUseCase，成功后 dispatch(RefreshData) 刷新列表。
     */
    private fun adjustPrice(item: ResourceData, newPrice: Int) {
        sendEffect(WorkManageEffect.ShowToast("已调整《${item.itemName}》定价为 $newPrice（接口占位）"))
    }
}
