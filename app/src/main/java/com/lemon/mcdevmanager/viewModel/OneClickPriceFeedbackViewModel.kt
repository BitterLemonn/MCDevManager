package com.lemon.mcdevmanager.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanager.data.netease.resource.ResourceBean
import com.lemon.mcdevmanager.data.repository.DetailRepository
import com.lemon.mcdevmanager.data.repository.DeveloperFeedbackRepository
import com.lemon.mcdevmanager.ui.widget.SNACK_ERROR
import com.lemon.mcdevmanager.utils.NetworkState
import com.orhanobut.logger.Logger
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

class OneClickPriceFeedbackViewModel : ViewModel() {
    private val _viewStates = MutableStateFlow(OneClickPriceFeedbackViewStates())
    val viewStates = _viewStates.asStateFlow()
    private val _viewEvents = SharedFlowEvents<OneClickPriceFeedbackViewEvents>()
    val viewEvents = _viewEvents.asSharedFlow()

    private val resourceRepository = DetailRepository.getInstance()
    private val feedbackRepository = DeveloperFeedbackRepository.getInstance()

    fun dispatch(viewAction: OneClickPriceFeedbackViewActions) {
        when (viewAction) {
            is OneClickPriceFeedbackViewActions.LoadResourceList -> loadResourceList()
            is OneClickPriceFeedbackViewActions.ToggleSelectResource -> {
                if (viewStates.value.selectList.contains(viewAction.resId)) {
                    _viewStates.setState { copy(selectList = selectList.filter { it != viewAction.resId }) }
                } else {
                    _viewStates.setState { copy(selectList = selectList + viewAction.resId) }
                }
            }

            is OneClickPriceFeedbackViewActions.UpdateContent -> {
                _viewStates.setState { copy(content = viewAction.content) }
            }

            is OneClickPriceFeedbackViewActions.UpdateContact -> {
                _viewStates.setState { copy(contact = viewAction.contact) }
            }

            is OneClickPriceFeedbackViewActions.SubmitFeedback -> submitFeedback()
        }
    }

    private fun loadResourceList() {
        viewModelScope.launch {
            flow<Unit> {
                loadResourceListLogic()
            }.onStart {
                _viewStates.setState { copy(isShowLoading = true) }
            }.catch {
                Logger.e("加载资源列表失败: $it")
                _viewEvents.setEvent(OneClickPriceFeedbackViewEvents.ShowToast("加载资源列表失败: ${it.message ?: "未知错误"}"))
            }.onCompletion {
                _viewStates.setState { copy(isShowLoading = false) }
            }.flowOn(Dispatchers.IO).collect()
        }
    }

    private suspend fun loadResourceListLogic() {
        when (val result = resourceRepository.getAllResource("pe")) {
            is NetworkState.Success -> {
                result.data?.let {
                    _viewStates.setState { copy(resList = it.item) }
                }
            }

            is NetworkState.Error -> {
                _viewEvents.setEvent(OneClickPriceFeedbackViewEvents.ShowToast("加载资源列表失败: ${result.msg}"))
            }
        }
    }

    private fun submitFeedback() {
        viewModelScope.launch {
            flow<Unit> {
                submitFeedbackLogic()
            }.onStart {
                _viewStates.setState { copy(isShowLoading = true) }
            }.catch {
                Logger.e("提交反馈失败: $it")
                _viewEvents.setEvent(OneClickPriceFeedbackViewEvents.ShowToast("提交反馈失败: ${it.message ?: "未知错误"}"))
            }.onCompletion {
                _viewStates.setState { copy(isShowLoading = false) }
            }.flowOn(Dispatchers.IO).collect()
        }
    }

    private suspend fun submitFeedbackLogic() {
        val content = viewStates.value.content
        val contact = viewStates.value.contact
        val feedbackType = viewStates.value.feedbackType
        val functionType = viewStates.value.functionType
        if (content.isEmpty()) {
            _viewEvents.setEvent(OneClickPriceFeedbackViewEvents.ShowToast("请输入反馈内容"))
            return
        }

        when (val result =
            feedbackRepository.submitFeedback(content, contact, feedbackType, functionType)) {
            is NetworkState.Success -> {
                _viewEvents.setEvent(OneClickPriceFeedbackViewEvents.ShowToast("提交反馈成功, 反馈ID: ${result.data?.feedbackId}"))
            }

            is NetworkState.Error -> {
                _viewEvents.setEvent(OneClickPriceFeedbackViewEvents.ShowToast("提交反馈失败: ${result.msg}"))
            }
        }
    }
}

data class OneClickPriceFeedbackViewStates(
    val isShowLoading: Boolean = false,
    val content: String = "",
    val functionType: String = "价格申诉",
    val feedbackType: String = "其他类型",
    val contact: String = "",
    val resList: List<ResourceBean> = emptyList(),
    val selectList: List<String> = emptyList()
)

sealed class OneClickPriceFeedbackViewEvents {
    data class ShowToast(val message: String, val flag: String = SNACK_ERROR) :
        OneClickPriceFeedbackViewEvents()
}

sealed class OneClickPriceFeedbackViewActions {
    data object LoadResourceList : OneClickPriceFeedbackViewActions()
    data class ToggleSelectResource(val resId: String) : OneClickPriceFeedbackViewActions()
    data class UpdateContent(val content: String) : OneClickPriceFeedbackViewActions()
    data class UpdateContact(val contact: String) : OneClickPriceFeedbackViewActions()
    data object SubmitFeedback : OneClickPriceFeedbackViewActions()
}