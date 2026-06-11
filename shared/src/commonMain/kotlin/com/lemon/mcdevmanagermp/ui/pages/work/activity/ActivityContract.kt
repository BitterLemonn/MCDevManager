package com.lemon.mcdevmanagermp.ui.pages.work.activity

import com.lemon.mcdevmanagermp.data.vo.netease.activity.ReviewActivityItemVO
import com.lemon.mcdevmanagermp.utils.extension.IUiAction
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import com.lemon.mcdevmanagermp.utils.extension.IUiState

data class ActivityState(
    val isLoading: Boolean = false,
    val activities: List<ReviewActivityItemVO> = emptyList(),
    val hasMore: Boolean = true,
    val isRefreshing: Boolean = false,
    val currentStart: Int = 0,
    val totalCount: Int = 0
) : IUiState

sealed interface ActivityAction : IUiAction {
    data object LoadData : ActivityAction
    data object LoadMore : ActivityAction
    data object RefreshData : ActivityAction
}

sealed interface ActivityEffect : IUiEffect {
    data class ShowToast(val message: String) : ActivityEffect
}
