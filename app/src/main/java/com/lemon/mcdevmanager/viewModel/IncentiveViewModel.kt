package com.lemon.mcdevmanager.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanager.data.netease.income.IncentiveBean
import com.lemon.mcdevmanager.data.repository.IncomeRepository
import com.lemon.mcdevmanager.ui.widget.SNACK_ERROR
import com.lemon.mcdevmanager.ui.widget.SNACK_INFO
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

class IncentiveViewModel : ViewModel() {
    private val repository = IncomeRepository.getInstance()
    private val _viewStates = MutableStateFlow(IncentiveViewStates())
    val viewStates = _viewStates.asStateFlow()
    private val _viewEvents = SharedFlowEvents<IncentiveViewEvents>()
    val viewEvents = _viewEvents.asSharedFlow()

    fun dispatch(action: IncentiveViewActions) {
        when (action) {
            IncentiveViewActions.LoadData -> {
                loadData()
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            flow<Unit> {
                loadDataLogic()
            }.onStart {
                _viewStates.setState { copy(isLoading = true) }
            }.onCompletion {
                _viewStates.setState { copy(isLoading = false) }
            }.catch { e ->
                _viewEvents.setEvent(IncentiveViewEvents.ShowToast(e.message ?: "", SNACK_ERROR))
            }.flowOn(Dispatchers.IO).collect()
        }
    }

    private suspend fun loadDataLogic() {
        when (val result = repository.getIncentiveFund()) {
            is NetworkState.Success -> {
                result.data?.let {
                    val list = it.incentiveDetails
                    _viewStates.setState {
                        copy(incentiveList = list.sortedBy { it.updateTime }.reversed())
                    }
                } ?: _viewEvents.setEvent(IncentiveViewEvents.ShowToast("数据为空", SNACK_INFO))
            }

            is NetworkState.Error -> {
                _viewEvents.setEvent(
                    IncentiveViewEvents.ShowToast("获取数据失败: ${result.msg}", SNACK_ERROR)
                )
            }
        }
    }
}

data class IncentiveViewStates(
    val isLoading: Boolean = false,
    val incentiveList: List<IncentiveBean> = emptyList(),
)

sealed class IncentiveViewActions {
    data object LoadData : IncentiveViewActions()
}

sealed class IncentiveViewEvents {
    data class ShowToast(val message: String, val flag: String) : IncentiveViewEvents()
}