package com.lemon.mcdevmanager.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanager.data.common.LOGIN_PAGE
import com.lemon.mcdevmanager.data.netease.feedback.FeedbackBean
import com.lemon.mcdevmanager.data.repository.FeedbackRepository
import com.lemon.mcdevmanager.utils.CookiesExpiredException
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
                _viewStates.setState {
                    copy(
                        feedbackList = emptyList(),
                        nowPage = 1,
                        totalCount = Int.MAX_VALUE
                    )
                }
                loadFeedback()
            }

            is FeedbackAction.UpdateReplyContent -> {
                _viewStates.setState { copy(replyContent = action.content) }
            }

            is FeedbackAction.UpdateReplyId -> {
                _viewStates.setState { copy(replyId = action.id) }
            }

            is FeedbackAction.UpdateKeyword -> {
                _viewStates.setState { copy(keyword = action.keyword) }
            }

            is FeedbackAction.UpdateOrder -> {
                _viewStates.setState { copy(order = action.order) }
                checkFilterUsed()
            }

            is FeedbackAction.UpdateSortReplyCount -> {
                _viewStates.setState { copy(sortReplyCount = action.count) }
                checkFilterUsed()
            }

            is FeedbackAction.UpdateType -> {
                _viewStates.setState {
                    copy(
                        types = if (action.isAdd) _viewStates.value.types + action.type
                        else _viewStates.value.types - action.type
                    )
                }
                checkFilterUsed()
            }

            is FeedbackAction.ReplyFeedback -> replyFeedback()
        }
    }

    private fun loadFeedback() {
        if (_viewStates.value.feedbackList.size < _viewStates.value.totalCount)
            viewModelScope.launch {
                flow<Unit> {
                    loadFeedbackLogic()
                }.onStart {
                    _viewStates.setState { copy(isLoadingReply = true) }
                }.onCompletion {
                    _viewStates.setState { copy(isLoadingReply = false) }
                }.catch {
                    _viewEvents.setEvent(FeedbackEvent.ShowToast(it.message ?: "获取反馈失败: $it"))
                }.flowOn(Dispatchers.IO).collect()
            }
    }

    private suspend fun loadFeedbackLogic() {
        when (val result = repository.loadFeedback(
            page = _viewStates.value.nowPage,
            keyword = _viewStates.value.keyword,
            order = _viewStates.value.order,
            types = _viewStates.value.types,
            replyCount = _viewStates.value.sortReplyCount
        )) {
            is NetworkState.Success -> {
                result.data?.let {
                    _viewStates.value = _viewStates.value.copy(
                        feedbackList = _viewStates.value.feedbackList + it.data,
                        nowPage = _viewStates.value.nowPage + 1,
                        totalCount = it.count
                    )
                }
            }

            is NetworkState.Error -> {
                if (result.e is CookiesExpiredException) {
                    _viewEvents.setEvent(FeedbackEvent.NeedReLogin)
                    _viewEvents.setEvent(FeedbackEvent.ShowToast("登录过期, 请重新登录"))
                } else {
                    _viewEvents.setEvent(FeedbackEvent.ShowToast(result.msg))
                }
            }
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
                _viewStates.setState { copy(isLoadingReply = true) }
            }.catch {
                _viewEvents.setEvent(FeedbackEvent.ShowToast(it.message ?: "回复反馈失败: $it"))
                _viewStates.setState { copy(isLoadingReply = false) }
            }.flowOn(Dispatchers.IO).collect()
        }
    }

    private suspend fun replyFeedbackLogic() {
        when (val result =
            repository.sendReply(_viewStates.value.replyId, _viewStates.value.replyContent)) {
            is NetworkState.Success -> {
                _viewEvents.setEvent(FeedbackEvent.ShowToast("回复成功", false))
                _viewStates.setState { copy(isLoadingReply = false) }
                _viewEvents.setEvent(FeedbackEvent.ReplySuccess)
            }

            is NetworkState.Error -> {
                if (result.e is CookiesExpiredException) {
                    _viewEvents.setEvent(FeedbackEvent.NeedReLogin)
                    _viewEvents.setEvent(FeedbackEvent.ShowToast("登录过期, 请重新登录"))
                } else {
                    _viewEvents.setEvent(FeedbackEvent.ShowToast(result.msg))
                }
                throw Exception(result.msg)
            }
        }
    }

    private fun checkFilterUsed() {
        _viewStates.value = _viewStates.value.copy(
            isFilterUsed = _viewStates.value.order == "ASC" ||
                    _viewStates.value.sortReplyCount != -1 ||
                    _viewStates.value.types.isNotEmpty()
        )
    }
}

sealed class FeedbackAction {
    data object LoadFeedback : FeedbackAction()
    data object RefreshFeedback : FeedbackAction()
    data class UpdateReplyContent(val content: String) : FeedbackAction()
    data class UpdateReplyId(val id: String) : FeedbackAction()
    data class UpdateKeyword(val keyword: String) : FeedbackAction()
    data class UpdateOrder(val order: String) : FeedbackAction()
    data class UpdateSortReplyCount(val count: Int) : FeedbackAction()
    data class UpdateType(val type: Int, val isAdd: Boolean) : FeedbackAction()
    data object ReplyFeedback : FeedbackAction()
}

data class FeedbackViewState(
    val isLoadingList: Boolean = false,
    val feedbackList: List<FeedbackBean> = emptyList(),
    val nowPage: Int = 1,
    val totalCount: Int = Int.MAX_VALUE,

    val keyword: String = "",
    val order: String = "DESC",
    val sortReplyCount: Int = -1,
    val types: List<Int> = emptyList(),
    val isFilterUsed: Boolean = false,

    val replyContent: String = "",
    val replyId: String = "",
    val isLoadingReply: Boolean = false
)

sealed class FeedbackEvent {
    data class ShowToast(val msg: String, val isError: Boolean = true) : FeedbackEvent()
    data class RouteToPath(val path: String, val needPop: Boolean = false) : FeedbackEvent()
    data object NeedReLogin : FeedbackEvent()
    data object ReplySuccess : FeedbackEvent()
}