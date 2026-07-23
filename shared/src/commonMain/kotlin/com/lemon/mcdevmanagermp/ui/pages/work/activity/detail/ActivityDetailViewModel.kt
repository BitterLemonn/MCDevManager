package com.lemon.mcdevmanagermp.ui.pages.work.activity.detail

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.repository.ActivityRepositoryImpl
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ActivityReviewItemVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ActivityReviewModuleVO
import com.lemon.mcdevmanagermp.domain.activity.ActivityUseCase
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import kotlinx.coroutines.launch

class ActivityDetailViewModel :
    BaseViewModel<ActivityDetailState, ActivityDetailAction, ActivityDetailEffect>(
        ActivityDetailState()
    ) {
    private val activityUseCase = ActivityUseCase(
        activityRepository = ActivityRepositoryImpl.INSTANCE
    )

    override fun dispatch(action: ActivityDetailAction) {
        when (action) {
            is ActivityDetailAction.LoadData -> loadActivity(action.activity)
            is ActivityDetailAction.LoadModules -> loadModules(action.activityId, action.modules)
            is ActivityDetailAction.Participate -> {
                sendEffect(ActivityDetailEffect.NavigateToParticipate(action.activity))
            }
        }
    }

    private fun loadActivity(activity: ActivityReviewItemVO) {
        setState { copy(activity = activity) }
        // 自动加载模组数据
        if (activity.modules.isNotEmpty()) {
            dispatch(ActivityDetailAction.LoadModules(activity.id, activity.modules))
        }
    }

    private fun loadModules(activityId: String, modules: List<ActivityReviewModuleVO>) {
        if (state.value.isLoadingModules) return
        viewModelScope.launch {
            setState { copy(isLoadingModules = true) }
            val result = activityUseCase.loadActivityModules(activityId, modules)
            setState {
                copy(
                    isLoadingModules = false,
                    moduleItems = result.itemsMap
                )
            }
            if (result.errors.isNotEmpty()) {
                sendEffect(ActivityDetailEffect.ShowToast("加载模组信息失败: ${result.errors.first()}"))
            }
        }
    }
}
