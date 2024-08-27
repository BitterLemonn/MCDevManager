package com.lemon.mcdevmanager.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanager.data.netease.comment.CommentBean
import com.lemon.mcdevmanager.data.repository.CommentRepository
import com.lemon.mcdevmanager.ui.widget.SNACK_ERROR
import com.lemon.mcdevmanager.utils.CookiesExpiredException
import com.lemon.mcdevmanager.utils.NetworkState
import com.orhanobut.logger.Logger
import com.zj.mvi.core.SharedFlowEvents
import com.zj.mvi.core.setEvent
import com.zj.mvi.core.setState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
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

class CommentPageViewModel : ViewModel() {
    private val repository = CommentRepository.getInstance()
    private val _viewStates = MutableStateFlow(CommentPageState())
    val viewStates = _viewStates.asStateFlow()
    private val _viewEvents = SharedFlowEvents<CommentPageEvent>()
    val viewEvents = _viewEvents.asSharedFlow()

    fun dispatch(action: CommentPageAction) {
        when (action) {
            is CommentPageAction.LoadComment -> loadComment()
            is CommentPageAction.RefreshComment -> loadComment(true)
            is CommentPageAction.ReplayComment -> {}

            is CommentPageAction.UpdateKey -> {
                _viewStates.setState { copy(key = action.key) }
                checkFilter()
            }

            is CommentPageAction.UpdateTag -> {
                _viewStates.value.apply {
                    val tag = tag.toMutableList()
                    if (action.isAdd) {
                        tag.add(action.tag)
                    } else {
                        tag.remove(action.tag)
                    }
                    _viewStates.setState { copy(tag = tag) }
                }
                checkFilter()
            }

            is CommentPageAction.UpdateState -> {
                _viewStates.setState { copy(state = action.state) }
                checkFilter()
            }

            is CommentPageAction.UpdateStartDate -> {
                _viewStates.setState { copy(startDate = action.date) }
                checkFilter()
            }

            is CommentPageAction.UpdateEndDate -> {
                _viewStates.setState { copy(endDate = action.date) }
                checkFilter()
            }

            is CommentPageAction.UpdateStarFilter -> {
                _viewStates.setState { copy(starFilter = action.star) }
                checkFilter()
            }
        }
    }

    private fun checkFilter() {
        _viewStates.value.let {
            if (it.key != null || it.tag.isEmpty() || it.state != null || it.startDate != null || it.endDate != null || it.starFilter != 0) {
                _viewStates.setState { copy(isUseFilter = true) }
            } else {
                _viewStates.setState { copy(isUseFilter = false) }
            }
        }
    }

    private fun loadComment(isRefresh: Boolean = false) {
        _viewStates.value.let {
            if (it.isLoadOver || (it.nowPage * 20 >= it.commentCount && it.commentCount != 0)) {
                _viewStates.setState { copy(isLoading = true) }
                Logger.d("已加载全部评论")
                return
            }
        }

        viewModelScope.launch {
            flow<Unit> {
                loadCommentLogic(isRefresh)
            }.onStart {
                if (isRefresh) {
                    _viewStates.setState { copy(isLoading = true) }
                }
            }.onCompletion {
                if (isRefresh) {
                    _viewStates.setState { copy(isLoading = false) }
                }
            }.catch { error ->
                if (error is CookiesExpiredException) {
                    _viewEvents.setEvent(CommentPageEvent.NeedReLogin)
                } else {
                    _viewEvents.setEvent(CommentPageEvent.ShowToast(error.message ?: "未知错误"))
                }
            }.flowOn(Dispatchers.IO).collect()
        }
    }

    private suspend fun loadCommentLogic(isRefresh: Boolean) {
        _viewStates.value.let {
            when (
                val result = repository.getCommentList(
                    page = if (isRefresh) 0 else it.nowPage,
                    key = it.key,
                    tag = if (it.tag.isEmpty()) null else it.tag.joinToString("__"),
                    state = it.state,
                    startDate = it.startDate,
                    endDate = it.endDate
                )
            ) {
                is NetworkState.Success -> {
                    val commentList = if (isRefresh) {
                        val targetList =
                            result.data?.data?.filter { item -> if (it.starFilter != 0) item.stars.toInt() == it.starFilter else true }
                                ?: mutableListOf()
                        if (targetList.isEmpty()) {
                            loadComment()
                        }
                        targetList
                    } else {
                        it.commentList.toMutableList().apply {
                            val targetList =
                                result.data?.data?.filter { item -> if (it.starFilter != 0) item.stars.toInt() == it.starFilter else true }
                                    ?: mutableListOf()
                            if (targetList.isEmpty()) {
                                loadComment()
                            }
                            addAll(targetList)
                        }
                    }
                    _viewStates.setState {
                        copy(
                            commentList = commentList,
                            commentCount = result.data?.count ?: 0,
                            nowPage = if (isRefresh) 1 else nowPage + 1,
                            isLoadOver = commentList.size >= (result.data?.count ?: 0)
                        )
                    }
                }

                is NetworkState.Error -> {
                    throw result.e ?: Exception(result.msg)
                }
            }
        }
    }
}

data class CommentPageState(
    val commentList: List<CommentBean> = mutableListOf(),
    val commentCount: Int = 0,
    val nowPage: Int = 0,
    val isLoading: Boolean = false,
    val isLoadOver: Boolean = false,
    val startDate: String? = null,
    val endDate: String? = null,
    val key: String? = null,
    val tag: List<Int> = mutableListOf(),
    val state: Int? = null,
    val isUseFilter: Boolean = false,
    val starFilter: Int = 0
)

sealed class CommentPageAction {
    data object LoadComment : CommentPageAction()
    data object RefreshComment : CommentPageAction()
    data class ReplayComment(val comment: String, val commentId: String) : CommentPageAction()

    data class UpdateKey(val key: String?) : CommentPageAction()
    data class UpdateTag(val tag: Int, val isAdd: Boolean = false) : CommentPageAction()
    data class UpdateState(val state: Int?) : CommentPageAction()
    data class UpdateStartDate(val date: String?) : CommentPageAction()
    data class UpdateEndDate(val date: String?) : CommentPageAction()
    data class UpdateStarFilter(val star: Int) : CommentPageAction()
}

sealed class CommentPageEvent {
    data class ShowToast(val message: String, val tag: String = SNACK_ERROR) : CommentPageEvent()
    data object NeedReLogin : CommentPageEvent()
}