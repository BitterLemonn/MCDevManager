package com.lemon.mcdevmanagermp.ui.pages.work.activity

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.repository.ActivityRepositoryImpl
import com.lemon.mcdevmanagermp.domain.activity.ActivityUseCase
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import kotlinx.coroutines.launch

class ActivityViewModel :
    BaseViewModel<ActivityState, ActivityAction, ActivityEffect>(ActivityState()) {

    private val activityUseCase = ActivityUseCase(
        activityRepository = ActivityRepositoryImpl.INSTANCE
    )

    companion object {
        private const val PAGE_SIZE = 10
    }

    override fun dispatch(action: ActivityAction) {
        when (action) {
            ActivityAction.LoadData -> loadActivities()
            ActivityAction.LoadMore -> loadMore()
            ActivityAction.RefreshData -> refreshActivities()
        }
    }

    private fun loadActivities() {
        if (state.value.isLoading) return
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val result = activityUseCase.loadActivities(start = 0, span = PAGE_SIZE)
            setState {
                copy(
                    isLoading = false,
                    activities = result.activities,
                    totalCount = result.totalCount,
                    currentStart = result.activities.size,
                    hasMore = result.activities.size < result.totalCount,
                    isRefreshing = false
                )
            }
            if (result.error != null) {
                sendEffect(ActivityEffect.ShowToast("加载失败: ${result.error}"))
            }
        }
    }

    private fun loadMore() {
        val current = state.value
        if (current.isLoading || !current.hasMore) return
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val result =
                activityUseCase.loadActivities(start = current.currentStart, span = PAGE_SIZE)
            setState {
                copy(
                    isLoading = false,
                    activities = activities + result.activities,
                    currentStart = currentStart + result.activities.size,
                    hasMore = (activities + result.activities).size < maxOf(
                        result.totalCount,
                        totalCount
                    )
                )
            }
            if (result.error != null) {
                sendEffect(ActivityEffect.ShowToast("加载更多失败: ${result.error}"))
            }
        }
    }

    private fun refreshActivities() {
        if (state.value.isRefreshing) return
        viewModelScope.launch {
            setState { copy(isRefreshing = true) }
            val result = activityUseCase.loadActivities(start = 0, span = PAGE_SIZE)
            setState {
                copy(
                    isRefreshing = false,
                    isLoading = false,
                    activities = result.activities,
                    totalCount = result.totalCount,
                    currentStart = result.activities.size,
                    hasMore = result.activities.size < result.totalCount
                )
            }
            if (result.error != null) {
                sendEffect(ActivityEffect.ShowToast("刷新失败: ${result.error}"))
            }
        }
    }
}
