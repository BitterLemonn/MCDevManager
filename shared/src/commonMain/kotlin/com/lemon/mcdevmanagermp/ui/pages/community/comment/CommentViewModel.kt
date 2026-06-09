package com.lemon.mcdevmanagermp.ui.pages.community.comment

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.repository.CommentRepositoryImpl
import com.lemon.mcdevmanagermp.domain.comment.CommentUseCase
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import com.lemon.mcdevmanagermp.ui.pages.community.components.computeDateRange
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CommentViewModel : BaseViewModel<CommentState, CommentAction, CommentEffect>(CommentState()) {

    private val commentUseCase = CommentUseCase(
        commentRepository = CommentRepositoryImpl.INSTANCE
    )
    private var searchJob: Job? = null

    init {
        dispatch(CommentAction.LoadComments)
    }

    override fun dispatch(action: CommentAction) {
        when (action) {
            CommentAction.LoadComments -> loadComments(isRefresh = true)
            CommentAction.LoadMore -> loadComments(isRefresh = false)
            CommentAction.Refresh -> {
                setState { copy(isRefreshing = true) }
                loadComments(isRefresh = true)
            }
            is CommentAction.SelectComment -> setState { copy(selectedComment = action.comment) }
            is CommentAction.UpdateReplyText -> setState { copy(replyText = action.text) }
            is CommentAction.SubmitReply -> replyComment(action.commentId, state.value.replyText)
            is CommentAction.UpdateFilterTag -> {
                setState { copy(filterTag = action.tag, filterStars = emptySet()) }
                loadComments(isRefresh = true)
            }
            is CommentAction.UpdateSearchKey -> {
                setState { copy(searchKey = action.key) }
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(400)
                    loadComments(isRefresh = true)
                }
            }
            is CommentAction.UpdateDateRange -> {
                val (start, end) = computeDateRange(action.range)
                setState { copy(dateRange = action.range, startDate = start, endDate = end) }
                loadComments(isRefresh = true)
            }
            is CommentAction.ToggleFilterStars -> {
                setState {
                    val stars = filterStars.toMutableSet()
                    if (stars.contains(action.stars)) stars.remove(action.stars) else stars.add(action.stars)
                    copy(filterStars = stars)
                }
            }
            CommentAction.ToggleFilterPanel -> setState { copy(isFilterExpanded = !isFilterExpanded) }
            CommentAction.ClearFilters -> {
                setState {
                    copy(
                        filterTag = null, searchKey = "", startDate = null, endDate = null,
                        filterStars = emptySet(), dateRange = null,
                        isFilterExpanded = false
                    )
                }
                loadComments(isRefresh = true)
            }
        }
    }

    private fun loadComments(isRefresh: Boolean) {
        val currentState = state.value
        if (currentState.isLoadOver && !isRefresh) return
        if (!isRefresh && currentState.totalCount > 0 && currentState.commentList.size >= currentState.totalCount) {
            setState { copy(isLoadOver = true) }
            return
        }

        if (isRefresh) {
            setState { copy(isLoading = true, currentPage = 0, isLoadOver = false) }
        }

        viewModelScope.launch {
            val page = if (isRefresh) 0 else state.value.currentPage
            val result = commentUseCase.getCommentList(
                start = page * 20,
                span = 20,
                key = state.value.searchKey.takeIf { it.isNotBlank() },
                tag = state.value.filterTag,
                startDate = state.value.startDate,
                endDate = state.value.endDate
            )

            when (result) {
                is NetworkState.Success -> {
                    val data = result.data
                    if (data != null) {
                        setState {
                            copy(
                                commentList = if (isRefresh) data.data else commentList + data.data,
                                totalCount = data.count,
                                currentPage = page + 1,
                                isLoadOver = (if (isRefresh) data.data.size else commentList.size + data.data.size) >= data.count,
                                isLoading = false,
                                isRefreshing = false
                            )
                        }
                    } else {
                        setState { copy(isLoading = false, isRefreshing = false, isLoadOver = true) }
                    }
                }
                is NetworkState.Error -> {
                    setState { copy(isLoading = false, isRefreshing = false) }
                    sendEffect(CommentEffect.ShowToast(result.msg))
                }
            }
        }
    }

    private fun replyComment(commentId: String, content: String) {
        if (content.isBlank()) return
        setState { copy(isReplying = true) }
        viewModelScope.launch {
            when (commentUseCase.replyComment(commentId, content)) {
                is NetworkState.Success -> {
                    setState { copy(replyText = "", isReplying = false) }
                    sendEffect(CommentEffect.ReplySuccess)
                    sendEffect(CommentEffect.ShowToast("回复成功"))
                }
                is NetworkState.Error -> {
                    setState { copy(isReplying = false) }
                    sendEffect(CommentEffect.ShowToast("回复失败"))
                }
            }
        }
    }
}
