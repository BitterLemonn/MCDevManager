package com.lemon.mcdevmanagermp.ui.pages.community.comment

import com.lemon.mcdevmanagermp.data.vo.netease.comment.CommentData
import com.lemon.mcdevmanagermp.utils.extension.IUiAction
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import com.lemon.mcdevmanagermp.utils.extension.IUiState

data class CommentState(
    val commentList: List<CommentData> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadOver: Boolean = false,
    val currentPage: Int = 0,
    val totalCount: Int = 0,
    val selectedComment: CommentData? = null,
    val replyText: String = "",
    val isReplying: Boolean = false,
    // Server-side filters (trigger API reload)
    val filterTag: String? = null,
    val searchKey: String = "",
    val startDate: String? = null,
    val endDate: String? = null,
    // Client-side filters (applied to loaded data)
    val filterStars: Set<String> = emptySet(),
    // UI state
    val isFilterExpanded: Boolean = false,
    val dateRange: String? = null,
) : IUiState

sealed interface CommentAction : IUiAction {
    data object LoadComments : CommentAction
    data object LoadMore : CommentAction
    data object Refresh : CommentAction
    data class SelectComment(val comment: CommentData?) : CommentAction
    data class UpdateReplyText(val text: String) : CommentAction
    data class SubmitReply(val commentId: String) : CommentAction
    // Server-side filter changes (trigger reload)
    data class UpdateFilterTag(val tag: String?) : CommentAction
    data class UpdateSearchKey(val key: String) : CommentAction
    data class UpdateDateRange(val range: String?) : CommentAction
    // Client-side filter changes (no reload)
    data class ToggleFilterStars(val stars: String) : CommentAction
    // UI
    data object ToggleFilterPanel : CommentAction
    data object ClearFilters : CommentAction
}

sealed interface CommentEffect : IUiEffect {
    data class ShowToast(val message: String) : CommentEffect
    data object ReplySuccess : CommentEffect
}
