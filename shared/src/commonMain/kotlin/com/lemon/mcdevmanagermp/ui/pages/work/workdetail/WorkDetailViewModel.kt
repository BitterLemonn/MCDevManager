package com.lemon.mcdevmanagermp.ui.pages.work.workdetail

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.repository.ResourceRepositoryImpl
import com.lemon.mcdevmanagermp.domain.work.WorkDetailUseCase
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import kotlinx.coroutines.launch

class WorkDetailViewModel :
    BaseViewModel<WorkDetailState, WorkDetailAction, WorkDetailEffect>(WorkDetailState()) {

    private val workDetailUseCase = WorkDetailUseCase(
        resourceRepository = ResourceRepositoryImpl.INSTANCE
    )

    override fun dispatch(action: WorkDetailAction) {
        when (action) {
            is WorkDetailAction.LoadDetail -> loadDetail(action.itemId)
            is WorkDetailAction.UpdateItemName -> setState { copy(itemName = action.value) }
            is WorkDetailAction.ToggleJoinShantou -> setState { copy(joinShantou = action.value) }
            is WorkDetailAction.ToggleOriginal -> setState { copy(isOriginal = action.value) }
            is WorkDetailAction.AddTag -> addTag(action.name)
            is WorkDetailAction.RemoveTag -> removeTag(action.index)
            is WorkDetailAction.UpdatePrerequisite -> setState { copy(prerequisite = action.value) }
            is WorkDetailAction.UpdateActivityDesc -> setState { copy(activityDesc = action.value) }
            is WorkDetailAction.ToggleRelatedMod -> setState { copy(isRelatedMod = action.value) }
            is WorkDetailAction.ToggleRelatedPackType -> setState { copy(relatedIsMaster = action.value) }
            is WorkDetailAction.UpdateRelatedSearch -> setState { copy(relatedSearchKey = action.value) }
            is WorkDetailAction.ToggleSyncPc -> setState { copy(syncPc = action.value) }
            WorkDetailAction.Submit -> sendEffect(WorkDetailEffect.ShowToast("更新接口暂未接入"))
        }
    }

    private fun loadDetail(itemId: String) {
        if (state.value.isLoading) return
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            when (val result = workDetailUseCase.getResourceDetail(itemId)) {
                is NetworkState.Success -> {
                    val d = result.data
                    if (d != null) {
                        setState {
                            copy(
                                isLoading = false,
                                detail = d,
                                itemId = d.itemId,
                                normalNumber = d.normalNumber,
                                itemVersion = d.itemVersion,
                                itemName = d.itemName,
                                isOriginal = d.isOriginal,
                                tags = d.tags.map { it.name }.filter { it.isNotEmpty() },
                                // prerequisiteItems 为 List<JsonElement>，结构未知，本期留空由用户手动填写
                                prerequisite = "",
                                activityDesc = d.activityDesc,
                                isRelatedMod = d.relateItemId.isNotEmpty(),
                                relatedIsMaster = d.dlcInfo.dlcType == "master",
                                syncPc = d.syncPcFlag
                            )
                        }
                    } else {
                        setState { copy(isLoading = false) }
                        sendEffect(WorkDetailEffect.ShowToast("获取详情失败"))
                    }
                }

                is NetworkState.Error -> {
                    setState { copy(isLoading = false) }
                    handleError(
                        result,
                        onNeedReLogin = { WorkDetailEffect.NeedReLogin },
                        onShowToast = { WorkDetailEffect.ShowToast(it) }
                    )
                }
            }
        }
    }

    private fun addTag(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        setState {
            if (tags.contains(trimmed)) this else copy(tags = tags + trimmed)
        }
    }

    private fun removeTag(index: Int) {
        setState {
            if (index !in tags.indices) this
            else copy(tags = tags.toMutableList().apply { removeAt(index) })
        }
    }
}
