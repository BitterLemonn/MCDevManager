package com.lemon.mcdevmanager.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanager.data.netease.resource.ResDetailBean
import com.lemon.mcdevmanager.data.netease.resource.ResourceBean
import com.lemon.mcdevmanager.data.repository.DetailRepository
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

class AnalyzeViewModel : ViewModel() {
    private val detailRepository = DetailRepository.getInstance()
    private val _viewStates = MutableStateFlow(AnalyzeViewState())
    val viewStates = _viewStates.asStateFlow()
    private val _viewEvents = SharedFlowEvents<AnalyzeEvent>()
    val viewEvents = _viewEvents.asSharedFlow()

    fun dispatch(action: AnalyzeAction) {
        when (action) {
            is AnalyzeAction.UpdateStartDate -> {
                _viewStates.setState { copy(startDate = action.date) }
            }

            is AnalyzeAction.UpdateEndDate -> {
                _viewStates.setState { copy(endDate = action.date) }
            }

            is AnalyzeAction.ChangeResourceList -> {
                if (action.isDel) {
                    _viewStates.setState { copy(filterResourceList = filterResourceList - action.resId) }
                } else {
                    _viewStates.setState { copy(filterResourceList = filterResourceList + action.resId) }
                }
            }

            is AnalyzeAction.LoadAnalyze -> {
                loadAnalyze()
            }
        }
    }

    private fun loadAnalyze() {
        viewModelScope.launch {
            flow<Unit> {
                loadAnalyzeLogic()
            }.onStart {
                _viewStates.setState { copy(isShowLoading = true) }
            }.onCompletion {
                _viewStates.setState { copy(isShowLoading = false) }
            }.catch {
                _viewEvents.setEvent(AnalyzeEvent.ShowToast(it.message ?: "获取数据分析失败: 未知错误"))
            }.flowOn(Dispatchers.IO).collect()
        }
    }

    private suspend fun loadAnalyzeLogic() {
        when (val res = detailRepository.getDailyDetail(
            viewStates.value.platform,
            viewStates.value.startDate,
            viewStates.value.endDate,
            viewStates.value.filterResourceList
        )) {
            is NetworkState.Success -> res.data?.let {
                _viewStates.setState { copy(analyzeList = it.data) }
            }

            is NetworkState.Error -> _viewEvents.setEvent(
                AnalyzeEvent.ShowToast("获取数据分析失败: ${res.msg}")
            )
        }
    }
}

data class AnalyzeViewState(
    val analyzeList: List<ResDetailBean> = emptyList(),
    val platform: String = "PE",
    val startDate: String = "",
    val endDate: String = "",
    val filterResourceList: List<String> = emptyList(),
    val allResourceList: List<ResourceBean> = emptyList(),
    val isShowLoading: Boolean = false
)

sealed class AnalyzeAction {
    data class UpdateStartDate(val date: String) : AnalyzeAction()
    data class UpdateEndDate(val date: String) : AnalyzeAction()
    data class ChangeResourceList(val resId: String, val isDel: Boolean) : AnalyzeAction()
    data object LoadAnalyze : AnalyzeAction()
}

sealed class AnalyzeEvent {
    data class ShowToast(val msg: String, val isError: Boolean = true) : AnalyzeEvent()
}