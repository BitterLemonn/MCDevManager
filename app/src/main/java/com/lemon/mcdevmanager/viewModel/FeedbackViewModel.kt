package com.lemon.mcdevmanager.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanager.data.netease.feedback.FeedbackBean
import com.lemon.mcdevmanager.data.repository.FeedbackRepository
import com.lemon.mcdevmanager.utils.NetworkState
import com.zj.mvi.core.SharedFlowEvents
import com.zj.mvi.core.setEvent
import com.zj.mvi.core.setState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class FeedbackViewModel : ViewModel() {
    private val repository = FeedbackRepository.getInstance()
    private val _viewStates = MutableStateFlow(FeedbackViewState())
    val viewStates = _viewStates.asStateFlow()
    private val _viewEvents = SharedFlowEvents<FeedbackEvent>()
    val viewEvents = _viewEvents.asSharedFlow()

    fun dispatch(action: FeedbackAction) {
        when (action) {
            is FeedbackAction.LoadFeedback -> loadFeedback()
            is FeedbackAction.RefreshFeedback -> {
                _viewStates.setState { copy(feedbackList = emptyList(), nowPage = 1) }
                loadFeedback()
            }

            is FeedbackAction.UpdateReplyContent -> {
                _viewStates.setState { copy(replyContent = action.content) }
            }

            is FeedbackAction.UpdateReplyId -> {
                _viewStates.setState { copy(replyId = action.id) }
            }

            is FeedbackAction.ReplyFeedback -> replyFeedback()
        }
    }

    private fun loadFeedback() {
        viewModelScope.launch {
            flow<Unit> {
                loadFeedbackLogic()
            }.onStart {
                _viewStates.value = _viewStates.value.copy(isLoadingList = true)
            }.onCompletion {
                _viewStates.value = _viewStates.value.copy(isLoadingList = false)
            }.catch {
                _viewEvents.setEvent(FeedbackEvent.ShowToast(it.message ?: "获取反馈失败: $it"))
            }.flowOn(Dispatchers.IO).collect()
        }
    }

    private suspend fun loadFeedbackLogic() {
        when (val result = repository.loadFeedback(_viewStates.value.nowPage)) {
            is NetworkState.Success -> {
                result.data?.let {
                    _viewStates.value = _viewStates.value.copy(
                        feedbackList = _viewStates.value.feedbackList + it.data,
                        nowPage = _viewStates.value.nowPage + 1
                    )
                }
            }

            is NetworkState.Error -> throw Exception(result.msg)
        }
    }

    private fun replyFeedback() {
        viewModelScope.launch {
            flow<Unit> {
                if (_viewStates.value.replyContent.isEmpty()) {
                    throw Exception("回复内容不能为空")
                }
                replyFeedbackLogic()
            }.onStart {
                _viewStates.value = _viewStates.value.copy(isLoadingReply = true)
            }.onCompletion {
                _viewStates.value = _viewStates.value.copy(isLoadingReply = false)
            }.catch {
                _viewEvents.setEvent(FeedbackEvent.ShowToast(it.message ?: "回复反馈失败: $it"))
            }.flowOn(Dispatchers.IO).collect()
        }
    }

    private suspend fun replyFeedbackLogic() {
        when (val result =
            repository.sendReply(_viewStates.value.replyId, _viewStates.value.replyContent)) {
            is NetworkState.Success -> {
                _viewEvents.setEvent(FeedbackEvent.ShowToast("回复成功"))
                _viewEvents.setEvent(FeedbackEvent.ReplySuccess)
            }
            is NetworkState.Error -> throw Exception(result.msg)
        }
    }
}

sealed class FeedbackAction {
    data object LoadFeedback : FeedbackAction()
    data object RefreshFeedback : FeedbackAction()
    data class UpdateReplyContent(val content: String) : FeedbackAction()
    data class UpdateReplyId(val id: String) : FeedbackAction()
    data object ReplyFeedback : FeedbackAction()
}

data class FeedbackViewState(
    val isLoadingList: Boolean = false,
    val feedbackList: List<FeedbackBean> = emptyList(),
    val nowPage: Int = 1,

    val replyContent: String = "",
    val replyId: String = "",
    val isLoadingReply: Boolean = false
)

sealed class FeedbackEvent {
    data class ShowToast(val msg: String) : FeedbackEvent()
    data class RouteToPath(val path: String) : FeedbackEvent()
    data object ReplySuccess : FeedbackEvent()
}