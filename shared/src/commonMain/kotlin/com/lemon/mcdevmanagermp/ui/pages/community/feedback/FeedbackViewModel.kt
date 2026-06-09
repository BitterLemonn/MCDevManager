package com.lemon.mcdevmanagermp.ui.pages.community.feedback

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.repository.FeedbackRepositoryImpl
import com.lemon.mcdevmanagermp.domain.feedback.FeedbackUseCase
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FeedbackViewModel : BaseViewModel<FeedbackState, FeedbackAction, FeedbackEffect>(FeedbackState()) {

    private val feedbackUseCase = FeedbackUseCase(
        feedbackRepository = FeedbackRepositoryImpl.INSTANCE
    )
    private var searchJob: Job? = null

    init {
        dispatch(FeedbackAction.LoadFeedback)
    }

    override fun dispatch(action: FeedbackAction) {
        when (action) {
            FeedbackAction.LoadFeedback -> loadFeedback(isRefresh = true)
            FeedbackAction.LoadMore -> loadFeedback(isRefresh = false)
            FeedbackAction.Refresh -> {
                setState { copy(isRefreshing = true) }
                loadFeedback(isRefresh = true)
            }
            is FeedbackAction.SelectFeedback -> setState { copy(selectedFeedback = action.feedback) }
            is FeedbackAction.UpdateReplyText -> setState { copy(replyText = action.text) }
            is FeedbackAction.SubmitReply -> replyFeedback(action.feedbackId, state.value.replyText)
            is FeedbackAction.UpdateFilterType -> {
                setState { copy(filterType = action.type) }
                loadFeedback(isRefresh = true)
            }
            is FeedbackAction.UpdateSearchKey -> {
                setState { copy(searchKey = action.key) }
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(400)
                    loadFeedback(isRefresh = true)
                }
            }
            is FeedbackAction.UpdateFilterReplied -> {
                setState {
                    copy(filterReplied = if (filterReplied == action.replied) null else action.replied)
                }
            }
            FeedbackAction.ToggleFilterPanel -> setState { copy(isFilterExpanded = !isFilterExpanded) }
            FeedbackAction.ClearFilters -> {
                setState {
                    copy(
                        filterType = null, searchKey = "",
                        filterReplied = null,
                        isFilterExpanded = false
                    )
                }
                loadFeedback(isRefresh = true)
            }
        }
    }

    private fun loadFeedback(isRefresh: Boolean) {
        val currentState = state.value
        if (currentState.isLoadOver && !isRefresh) return
        if (!isRefresh && currentState.totalCount > 0 && currentState.feedbackList.size >= currentState.totalCount) {
            setState { copy(isLoadOver = true) }
            return
        }

        if (isRefresh) {
            setState { copy(isLoading = true, currentPage = 0, isLoadOver = false) }
        }

        viewModelScope.launch {
            val page = if (isRefresh) 0 else state.value.currentPage
            val result = feedbackUseCase.loadFeedback(
                from = page * 20,
                size = 20,
                status = state.value.filterType,
                key = state.value.searchKey.takeIf { it.isNotBlank() }
            )

            when (result) {
                is NetworkState.Success -> {
                    val data = result.data
                    if (data != null) {
                        setState {
                            copy(
                                feedbackList = if (isRefresh) data.data else feedbackList + data.data,
                                totalCount = data.count,
                                currentPage = page + 1,
                                isLoadOver = (if (isRefresh) data.data.size else feedbackList.size + data.data.size) >= data.count,
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
                    sendEffect(FeedbackEffect.ShowToast(result.msg))
                }
            }
        }
    }

    private fun replyFeedback(feedbackId: String, content: String) {
        if (content.isBlank()) return
        setState { copy(isReplying = true) }
        viewModelScope.launch {
            when (feedbackUseCase.sendReply(feedbackId, content)) {
                is NetworkState.Success -> {
                    val updatedList = state.value.feedbackList.map {
                        if (it.id == feedbackId) it.copy(reply = content) else it
                    }
                    val updatedSelected = state.value.selectedFeedback?.let {
                        if (it.id == feedbackId) it.copy(reply = content) else it
                    }
                    setState {
                        copy(
                            feedbackList = updatedList,
                            selectedFeedback = updatedSelected,
                            replyText = "",
                            isReplying = false
                        )
                    }
                    sendEffect(FeedbackEffect.ReplySuccess)
                    sendEffect(FeedbackEffect.ShowToast("回复成功"))
                }
                is NetworkState.Error -> {
                    setState { copy(isReplying = false) }
                    sendEffect(FeedbackEffect.ShowToast("回复失败"))
                }
            }
        }
    }
}
