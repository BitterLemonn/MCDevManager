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
import java.time.ZonedDateTime

class AnalyzeViewModel : ViewModel() {
    private val detailRepository = DetailRepository.getInstance()
    private val _viewStates = MutableStateFlow(AnalyzeViewState())
    val viewStates = _viewStates.asStateFlow()
    private val _viewEvents = SharedFlowEvents<AnalyzeEvent>()
    val viewEvents = _viewEvents.asSharedFlow()

    fun dispatch(action: AnalyzeAction) {
        when (action) {
            is AnalyzeAction.GetAllResourceList -> getAllResourceList()
            is AnalyzeAction.UpdateStartDate -> {
                _viewStates.setState { copy(startDate = action.date) }
            }

            is AnalyzeAction.UpdatePlatform -> {
                _viewStates.setState { copy(platform = action.platform) }
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

    private fun getAllResourceList() {
        viewModelScope.launch {
            flow<Unit> {
                getAllResourceListLogic()
            }.onStart {
                _viewStates.setState { copy(isShowLoading = true) }
            }.onCompletion {
                _viewStates.setState { copy(isShowLoading = false) }
            }.catch {
                _viewEvents.setEvent(
                    AnalyzeEvent.ShowToast(it.message ?: "获取资源列表失败: 未知错误")
                )
            }.flowOn(Dispatchers.IO).collect()
        }
    }

    private suspend fun getAllResourceListLogic() {
        when (val res = detailRepository.getAllResource(viewStates.value.platform)) {
            is NetworkState.Success -> res.data?.let {
                _viewStates.setState { copy(allResourceList = it.item) }
            }

            is NetworkState.Error -> _viewEvents.setEvent(
                AnalyzeEvent.ShowToast("获取资源列表失败: ${res.msg}")
            )
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
                _viewEvents.setEvent(
                    AnalyzeEvent.ShowToast(it.message ?: "获取数据分析失败: 未知错误")
                )
            }.flowOn(Dispatchers.IO).collect()
        }
    }

    private suspend fun loadAnalyzeLogic() {
        if (viewStates.value.startDate > viewStates.value.endDate) {
            _viewEvents.setEvent(AnalyzeEvent.ShowToast("开始日期不能大于结束日期"))
            return
        }
        if (viewStates.value.filterResourceList.isEmpty()) {
            _viewEvents.setEvent(AnalyzeEvent.ShowToast("请先选择需要对比的组件"))
            return
        }
        when (val res = detailRepository.getDailyDetail(
            viewStates.value.platform,
            viewStates.value.startDate.split("T")[0].replace("-", ""),
            viewStates.value.endDate.split("T")[0].replace("-", ""),
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
    val platform: String = "pe",
    val startDate: String = ZonedDateTime.now().minusDays(7).toString(),
    val endDate: String = ZonedDateTime.now().toString(),
    val filterResourceList: List<String> = emptyList(),
    val allResourceList: List<ResourceBean> = listOf(
        ResourceBean(
            "2001-10-11",
            "123123123123",
            "神话之森",
            "20011011",
            2,
            300
        )
    ),
    val isShowLoading: Boolean = false
)

sealed class AnalyzeAction {
    data class UpdatePlatform(val platform: String) : AnalyzeAction()
    data class UpdateStartDate(val date: String) : AnalyzeAction()
    data class UpdateEndDate(val date: String) : AnalyzeAction()
    data class ChangeResourceList(val resId: String, val isDel: Boolean) : AnalyzeAction()
    data object LoadAnalyze : AnalyzeAction()
    data object GetAllResourceList : AnalyzeAction()
}

sealed class AnalyzeEvent {
    data class ShowToast(val msg: String, val isError: Boolean = true) : AnalyzeEvent()
}