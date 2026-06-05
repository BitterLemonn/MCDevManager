package com.lemon.mcdevmanagermp.ui.pages.community.feedback

import com.lemon.mcdevmanagermp.data.vo.netease.feedback.FeedbackData
import com.lemon.mcdevmanagermp.utils.extension.IUiAction
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import com.lemon.mcdevmanagermp.utils.extension.IUiState

data class FeedbackState(
    val feedbackList: List<FeedbackData> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadOver: Boolean = false,
    val currentPage: Int = 0,
    val totalCount: Int = 0,
    val selectedFeedback: FeedbackData? = null,
    val replyText: String = "",
    val isReplying: Boolean = false,
    // Server-side filters (trigger API reload)
    val filterType: String? = null,
    val searchKey: String = "",
    // Client-side filters (applied to loaded data)
    val filterReplied: Boolean? = null, // null=all, true=replied, false=unreplied
    // UI state
    val isFilterExpanded: Boolean = false,
) : IUiState

sealed interface FeedbackAction : IUiAction {
    data object LoadFeedback : FeedbackAction
    data object LoadMore : FeedbackAction
    data object Refresh : FeedbackAction
    data class SelectFeedback(val feedback: FeedbackData?) : FeedbackAction
    data class UpdateReplyText(val text: String) : FeedbackAction
    data class SubmitReply(val feedbackId: String) : FeedbackAction
    // Server-side filter changes (trigger reload)
    data class UpdateFilterType(val type: String?) : FeedbackAction
    data class UpdateSearchKey(val key: String) : FeedbackAction
    // Client-side filter changes (no reload)
    data class UpdateFilterReplied(val replied: Boolean?) : FeedbackAction
    // UI
    data object ToggleFilterPanel : FeedbackAction
    data object ClearFilters : FeedbackAction
}

sealed interface FeedbackEffect : IUiEffect {
    data class ShowToast(val message: String) : FeedbackEffect
    data object ReplySuccess : FeedbackEffect
}
