package com.lemon.mcdevmanagermp.ui.pages.work.activity.discount

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.repository.ActivityRepositoryImpl
import com.lemon.mcdevmanagermp.domain.activity.ActivityUseCase
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import kotlinx.coroutines.launch

class DiscountActivityViewModel :
    BaseViewModel<DiscountActivityState, DiscountActivityAction, DiscountActivityEffect>(
        DiscountActivityState()
    ) {

    private val activityUseCase = ActivityUseCase(ActivityRepositoryImpl.INSTANCE)

    override fun dispatch(action: DiscountActivityAction) {
        when (action) {
            is DiscountActivityAction.LoadData -> loadActivity()
            is DiscountActivityAction.Refresh -> loadActivity()
            is DiscountActivityAction.SelectModule -> selectModule(action.moduleId)
            is DiscountActivityAction.ToggleItem -> toggleItem(action.itemId)
            is DiscountActivityAction.UpdateDiscount -> updateDiscount(action.discount)
            is DiscountActivityAction.SelectPartition -> selectPartition(action.partitionId)
            is DiscountActivityAction.UpdateIntro -> updateIntro(action.intro)
            is DiscountActivityAction.CancelJoin -> cancelJoin(action.itemId)
            is DiscountActivityAction.Submit -> submit()
        }
    }

    private fun loadActivity() {
        if (state.value.isLoading) return
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val result = activityUseCase.loadDiscountActivity()
            val activity = result.activity
            if (activity == null) {
                setState { copy(isLoading = false) }
                result.error?.let { sendEffect(DiscountActivityEffect.ShowToast("加载折扣特卖失败: $it")) }
                return@launch
            }
            val firstModule = activity.modules.firstOrNull()
            setState {
                copy(
                    isLoading = false,
                    activity = activity,
                    selectedModuleId = firstModule?.moduleId,
                    candidates = emptyList(),
                    joinedItems = emptyList(),
                    selectedItemIds = emptySet(),
                    discount = firstModule?.minDiscount ?: 0,
                    selectedPartitionId = null,
                    intro = ""
                )
            }
            if (firstModule != null) {
                loadModuleData(activity.activityId, firstModule.moduleId)
            }
        }
    }

    private fun selectModule(moduleId: String) {
        val activity = state.value.activity ?: return
        setState {
            copy(
                selectedModuleId = moduleId,
                candidates = emptyList(),
                joinedItems = emptyList(),
                selectedItemIds = emptySet(),
                discount = 0,
                selectedPartitionId = null,
                intro = ""
            )
        }
        loadModuleData(activity.activityId, moduleId)
    }

    private fun loadModuleData(activityId: String, moduleId: String) {
        if (state.value.isLoadingModule) return
        viewModelScope.launch {
            setState { copy(isLoadingModule = true) }
            val result = activityUseCase.loadDiscountModuleData(activityId, moduleId)
            val module = state.value.currentModule
            val defaultDiscount = module?.minDiscount ?: 0
            val defaultPartition = state.value.availablePartitions.firstOrNull()?.partitionId
            setState {
                copy(
                    isLoadingModule = false,
                    candidates = result.candidates,
                    joinedItems = result.joinedItems,
                    selectedItemIds = emptySet(),
                    discount = defaultDiscount,
                    selectedPartitionId = defaultPartition,
                    intro = ""
                )
            }
            result.error?.let { sendEffect(DiscountActivityEffect.ShowToast("加载项目失败: $it")) }
        }
    }

    private fun toggleItem(itemId: String) {
        setState {
            copy(
                selectedItemIds = if (itemId in selectedItemIds) {
                    selectedItemIds - itemId
                } else {
                    selectedItemIds + itemId
                }
            )
        }
    }

    private fun updateDiscount(value: Int) {
        // 不在此钳制：输入框需要保留用户中间输入态；范围由 UI 提示并在 submit 时校验
        setState { copy(discount = value) }
    }

    private fun selectPartition(partitionId: String) {
        // 点击已选分区取消选择（分区可选）
        setState {
            copy(selectedPartitionId = if (selectedPartitionId == partitionId) null else partitionId)
        }
    }

    private fun updateIntro(intro: String) {
        setState { copy(intro = intro) }
    }

    private fun cancelJoin(itemId: String) {
        val activityId = state.value.activity?.activityId ?: return
        val moduleId = state.value.selectedModuleId ?: return
        if (state.value.isSubmitting) return
        viewModelScope.launch {
            setState { copy(isSubmitting = true) }
            val error = activityUseCase.cancelDiscountJoin(activityId, itemId)
            setState { copy(isSubmitting = false) }
            if (error != null) {
                sendEffect(DiscountActivityEffect.ShowToast("取消失败: $error"))
            } else {
                sendEffect(DiscountActivityEffect.ShowToast("已取消参与"))
                loadModuleData(activityId, moduleId)
            }
        }
    }

    private fun submit() {
        val current = state.value
        val activity = current.activity ?: return
        val moduleId = current.selectedModuleId ?: return
        if (current.isSubmitting || current.isLoadingModule) return

        if (current.selectedItemIds.isEmpty()) {
            sendEffect(DiscountActivityEffect.ShowToast("请选择要参与的作品"))
            return
        }
        val module = current.currentModule
        if (module != null && (current.discount < module.minDiscount || current.discount > module.maxDiscount)) {
            sendEffect(DiscountActivityEffect.ShowToast("折扣需在 ${module.minDiscount}% ~ ${module.maxDiscount}% 之间"))
            return
        }
        // 分区可选：未选时传空串，由后端按默认分区处理
        val partitionId = current.selectedPartitionId ?: ""

        viewModelScope.launch {
            setState { copy(isSubmitting = true) }
            val error = activityUseCase.joinDiscount(
                activityId = activity.activityId,
                moduleId = moduleId,
                itemIdList = current.selectedItemIds.toList(),
                discount = current.discount,
                partitionId = partitionId,
                intro = current.intro
            )
            setState { copy(isSubmitting = false) }
            if (error != null) {
                sendEffect(DiscountActivityEffect.ShowToast("参与失败: $error"))
            } else {
                sendEffect(DiscountActivityEffect.ShowToast("参与成功"))
                loadModuleData(activity.activityId, moduleId)
            }
        }
    }
}
