package com.lemon.mcdevmanagermp.ui.pages.work.workmanage

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.consts.enums.PriceRankEnum
import com.lemon.mcdevmanagermp.data.consts.enums.PriceTypeEnum
import com.lemon.mcdevmanagermp.data.consts.enums.WorkItemActionEnum
import com.lemon.mcdevmanagermp.data.consts.enums.WorkItemStatusEnum
import com.lemon.mcdevmanagermp.data.dto.netease.work.ChangePriceDTO
import com.lemon.mcdevmanagermp.data.repository.ResourceRepositoryImpl
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import com.lemon.mcdevmanagermp.data.vo.netease.work.ReviewFeedbackVO
import com.lemon.mcdevmanagermp.domain.resource.GetResourceListUseCase
import com.lemon.mcdevmanagermp.domain.work.WorkManageUseCase
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import kotlinx.coroutines.launch

class WorkManageViewModel :
    BaseViewModel<WorkManageState, WorkManageAction, WorkManageEffect>(WorkManageState()) {

    private val workManageUseCase = WorkManageUseCase(
        getResourceListUseCase = GetResourceListUseCase(ResourceRepositoryImpl.INSTANCE),
        resourceRepository = ResourceRepositoryImpl.INSTANCE
    )

    override fun dispatch(action: WorkManageAction) {
        when (action) {
            WorkManageAction.LoadData -> loadWorks()
            WorkManageAction.RefreshData -> refreshWorks()
            is WorkManageAction.PerformAction -> performAction(action.item, action.action)
            is WorkManageAction.SubmitSelfTest -> submitSelfTest(action.item, action.passCheck)
            is WorkManageAction.AdjustPrice -> adjustPrice(action.item, action.newPrice)
            is WorkManageAction.AppointOnline -> appointOnline(action.item, action.time)
            is WorkManageAction.LoadFeedback -> loadFeedback(action.item)
            WorkManageAction.DismissFeedback -> dismissFeedback()
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
     * 执行仅需 itemId 的写操作，成功后刷新列表。
     */
    private fun performAction(item: ResourceData, action: WorkItemActionEnum) {
        if (state.value.isPerforming) return
        val itemId = item.itemId
        if (itemId.isEmpty()) {
            sendEffect(WorkManageEffect.ShowToast("作品 ID 为空"))
            return
        }
        if (action == WorkItemActionEnum.DELETE && item.getStatus() != WorkItemStatusEnum.INIT) {
            sendEffect(WorkManageEffect.ShowToast("仅草稿作品可删除"))
            return
        }
        viewModelScope.launch {
            setState { copy(isPerforming = true, performingMessage = "${action.label}中...") }
            val result = when (action) {
                WorkItemActionEnum.SUBMIT_REVIEW -> workManageUseCase.submitForReview(itemId)
                WorkItemActionEnum.CANCEL_REVIEW -> workManageUseCase.cancelReview(itemId)
                WorkItemActionEnum.CANCEL_TEST -> workManageUseCase.cancelSelfTest(itemId)
                WorkItemActionEnum.PUBLISH -> workManageUseCase.publish(itemId)
                WorkItemActionEnum.DELETE -> workManageUseCase.deleteItem(itemId)
                else -> {
                    setState { copy(isPerforming = false, performingMessage = "") }
                    sendEffect(WorkManageEffect.ShowToast("已${action.label}《${item.itemName}》（接口占位）"))
                    return@launch
                }
            }
            setState { copy(isPerforming = false, performingMessage = "") }
            when (result) {
                is NetworkState.Success -> {
                    sendEffect(WorkManageEffect.ShowToast("已${action.label}"))
                    refreshWorks()
                }

                is NetworkState.Error -> handleError(
                    result,
                    onNeedReLogin = { WorkManageEffect.NeedReLogin },
                    onShowToast = { WorkManageEffect.ShowToast(it) }
                )
            }
        }
    }

    private fun submitSelfTest(item: ResourceData, passCheck: Boolean) {
        if (state.value.isPerforming) return
        val itemId = item.itemId
        if (itemId.isEmpty()) {
            sendEffect(WorkManageEffect.ShowToast("作品 ID 为空"))
            return
        }
        viewModelScope.launch {
            setState { copy(isPerforming = true, performingMessage = "提交自测中...") }
            when (val result = workManageUseCase.applySelfTest(itemId, passCheck)) {
                is NetworkState.Success -> {
                    setState { copy(isPerforming = false, performingMessage = "") }
                    sendEffect(WorkManageEffect.ShowToast("已提交自测"))
                    refreshWorks()
                }

                is NetworkState.Error -> {
                    setState { copy(isPerforming = false, performingMessage = "") }
                    handleError(
                        result,
                        onNeedReLogin = { WorkManageEffect.NeedReLogin },
                        onShowToast = { WorkManageEffect.ShowToast(it) }
                    )
                }
            }
        }
    }

    /** 定时上架(7)。time 格式 "YYYY-MM-DD HH:mm:ss"。成功后刷新列表。 */
    private fun appointOnline(item: ResourceData, time: String) {
        if (state.value.isPerforming) return
        val itemId = item.itemId
        if (itemId.isEmpty()) {
            sendEffect(WorkManageEffect.ShowToast("作品 ID 为空"))
            return
        }
        viewModelScope.launch {
            setState { copy(isPerforming = true, performingMessage = "定时上架中...") }
            when (val result = workManageUseCase.appointOnline(itemId, time)) {
                is NetworkState.Success -> {
                    setState { copy(isPerforming = false, performingMessage = "") }
                    sendEffect(WorkManageEffect.ShowToast("定时上架已设置"))
                    refreshWorks()
                }

                is NetworkState.Error -> {
                    setState { copy(isPerforming = false, performingMessage = "") }
                    handleError(
                        result,
                        onNeedReLogin = { WorkManageEffect.NeedReLogin },
                        onShowToast = { WorkManageEffect.ShowToast(it) }
                    )
                }
            }
        }
    }

    /** 查看审核反馈(5)。拉取后写入 state，由 UI 渲染 FeedbackDialog。 */
    private fun loadFeedback(item: ResourceData) {
        if (state.value.isPerforming) return
        val itemId = item.itemId
        if (itemId.isEmpty()) {
            sendEffect(WorkManageEffect.ShowToast("作品 ID 为空"))
            return
        }
        viewModelScope.launch {
            setState { copy(isPerforming = true, performingMessage = "查看反馈中...") }
            when (val result = workManageUseCase.getReviewFeedback(itemId)) {
                is NetworkState.Success -> setState {
                    // data 为 null（后端异常）时回退空 VO，保证弹窗仍可弹出展示「暂无审核反馈」
                    copy(
                        isPerforming = false,
                        performingMessage = "",
                        feedback = result.data ?: ReviewFeedbackVO(),
                        feedbackItemName = item.itemName
                    )
                }

                is NetworkState.Error -> {
                    setState { copy(isPerforming = false, performingMessage = "") }
                    handleError(
                        result,
                        onNeedReLogin = { WorkManageEffect.NeedReLogin },
                        onShowToast = { WorkManageEffect.ShowToast(it) }
                    )
                }
            }
        }
    }

    private fun dismissFeedback() {
        setState { copy(feedback = null, feedbackItemName = "") }
    }

    private fun adjustPrice(item: ResourceData, newPrice: Int) {
        if (state.value.isPerforming) return
        val itemId = item.itemId
        if (itemId.isEmpty()) {
            sendEffect(WorkManageEffect.ShowToast("作品 ID 为空"))
            return
        }
        if (newPrice <= 0) {
            sendEffect(WorkManageEffect.ShowToast("请输入有效价格"))
            return
        }
        if (newPrice == item.price) {
            sendEffect(WorkManageEffect.ShowToast("新价格与当前价格相同"))
            return
        }
        val priceRank = if (
            PriceTypeEnum.fromStringType(item.priceType) == PriceTypeEnum.DIAMOND
        ) {
            val rank = PriceRankEnum.fromDiamondPrice(newPrice)
            if (rank == PriceRankEnum.UNKNOWN) {
                sendEffect(WorkManageEffect.ShowToast("请选择有效的钻石定价档位"))
                return
            }
            rank.type
        } else {
            item.priceRank
        }
        viewModelScope.launch {
            setState { copy(isPerforming = true, performingMessage = "调整定价中...") }
            when (
                val result = workManageUseCase.changePrice(
                    itemId,
                    ChangePriceDTO(newPrice, priceRank, item.priceType)
                )
            ) {
                is NetworkState.Success -> {
                    setState { copy(isPerforming = false, performingMessage = "") }
                    sendEffect(WorkManageEffect.ShowToast("已调整定价"))
                    refreshWorks()
                }

                is NetworkState.Error -> {
                    setState { copy(isPerforming = false, performingMessage = "") }
                    handleError(
                        result,
                        onNeedReLogin = { WorkManageEffect.NeedReLogin },
                        onShowToast = { WorkManageEffect.ShowToast(it) }
                    )
                }
            }
        }
    }
}
