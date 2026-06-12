package com.lemon.mcdevmanagermp.ui.pages.work.activity.detail

import com.lemon.mcdevmanagermp.data.vo.netease.activity.ActivityItemVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ReviewActivityItemVO
import com.lemon.mcdevmanagermp.utils.extension.IUiAction
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import com.lemon.mcdevmanagermp.utils.extension.IUiState

data class ActivityDetailState(
    val isLoading: Boolean = false,
    val isLoadingModules: Boolean = false,
    val activity: ReviewActivityItemVO? = null,
    /**
     * 各赛道的参与模组列表，key 为 moduleId
     */
    val moduleItems: Map<Int, List<ActivityItemVO>> = emptyMap()
) : IUiState {

    /**
     * 审核中的模组，按赛道分组
     */
    val reviewingItems: Map<Int, List<ActivityItemVO>>
        get() = moduleItems.mapValues { (_, items) ->
            items.filter { it.status == "reviewing" }
        }.filterValues { it.isNotEmpty() }

    /**
     * 已通过的模组，按赛道分组
     */
    val approvedItems: Map<Int, List<ActivityItemVO>>
        get() = moduleItems.mapValues { (_, items) ->
            items.filter { it.status == "approved" }
        }.filterValues { it.isNotEmpty() }

    /**
     * 已拒绝的模组，按赛道分组
     */
    val rejectedItems: Map<Int, List<ActivityItemVO>>
        get() = moduleItems.mapValues { (_, items) ->
            items.filter { it.status == "rejected" }
        }.filterValues { it.isNotEmpty() }

    /**
     * 是否有已提交的模组（审核中 + 已通过 + 已拒绝）
     */
    val hasAnyItems: Boolean
        get() = moduleItems.any { (_, items) -> items.isNotEmpty() }
}

sealed interface ActivityDetailAction : IUiAction {
    data class LoadData(val activity: ReviewActivityItemVO) : ActivityDetailAction
    data class LoadModules(
        val activityId: String,
        val modules: List<com.lemon.mcdevmanagermp.data.vo.netease.activity.ReviewActivityModuleVO>
    ) :
        ActivityDetailAction

    data class Participate(val activity: ReviewActivityItemVO) : ActivityDetailAction
}

sealed interface ActivityDetailEffect : IUiEffect {
    data class ShowToast(val message: String) : ActivityDetailEffect
    data class NavigateToParticipate(val activity: ReviewActivityItemVO) : ActivityDetailEffect
}
