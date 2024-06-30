package com.lemon.mcdevmanager.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanager.data.netease.feedback.FeedbackBean
import com.lemon.mcdevmanager.data.repository.FeedbackRepository
import com.lemon.mcdevmanager.utils.NetworkState
import com.zj.mvi.core.SharedFlowEvents
import com.zj.mvi.core.setEvent
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
        }
    }

    private fun loadFeedback() {
        viewModelScope.launch {
            flow<Unit> {
                loadFeedbackLogic()
            }.onStart {
                _viewStates.value = _viewStates.value.copy(isLoading = true)
            }.onCompletion {
                _viewStates.value = _viewStates.value.copy(isLoading = false)
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
}

sealed class FeedbackAction {
    data object LoadFeedback : FeedbackAction()
}

data class FeedbackViewState(
    val isLoading: Boolean = false,
    val feedbackList: List<FeedbackBean> = emptyList(),
    val nowPage: Int = 1
)

sealed class FeedbackEvent {
    data class ShowToast(val msg: String) : FeedbackEvent()
    data class RouteToPath(val path: String) : FeedbackEvent()
}