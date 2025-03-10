package com.lemon.mcdevmanager.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanager.data.netease.income.ApplyIncomeDetailBean
import com.lemon.mcdevmanager.data.netease.income.IncomeBean
import com.lemon.mcdevmanager.data.repository.IncomeRepository
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

class IncomeDetailViewModel : ViewModel() {
    private val repository = IncomeRepository.getInstance()
    val _viewStates = MutableStateFlow(IncomeDetailStates())
    val viewStates = _viewStates.asStateFlow()
    private val _viewEvents = SharedFlowEvents<IncomeDetailEvents>()
    val viewEvents = _viewEvents.asSharedFlow()

    fun dispatch(action: IncomeDetailActions) {
        when (action) {
            is IncomeDetailActions.LoadIncomeDetail -> {
                loadIncomeDetail()
            }

            is IncomeDetailActions.ApplyIncome -> {
                applyIncome(action.incomeIds)
            }

            is IncomeDetailActions.GetApplyDetail -> {
                getApplyDetail(action.incomeId, action.platform)
            }

            is IncomeDetailActions.DismissApplyDetail -> {
                _viewStates.setState { copy(applyIncomeDetail = emptyList()) }
            }
        }
    }

    private fun loadIncomeDetail() {
        viewModelScope.launch {
            flow<Unit> {
                loadIncomeDetailLogic()
            }.catch { e ->
                Logger.e("获取结算详情失败: $e")
                _viewEvents.setEvent(
                    IncomeDetailEvents.ShowToast(
                        "获取结算详情失败: ${e.message ?: "未知错误"}",
                        SNACK_ERROR
                    )
                )
            }.onStart {
                _viewStates.setState { copy(isLoading = true) }
            }.onCompletion {
                _viewStates.setState { copy(isLoading = false) }
            }.flowOn(Dispatchers.IO).collect()
        }
    }

    private suspend fun loadIncomeDetailLogic() {
        when (val result = repository.getIncomeDetail("pe")) {
            is NetworkState.Success -> {
                result.data?.let {
                    _viewStates.setState {
                        copy(peDetailList = it.incomes.sortedBy { it.dataMonth }.reversed())
                    }
                }
            }

            is NetworkState.Error -> {
                _viewEvents.setEvent(
                    IncomeDetailEvents.ShowToast("获取PE收益信息失败:${result.msg}", SNACK_ERROR)
                )
            }
        }
        when (val result = repository.getIncomeDetail("pc")) {
            is NetworkState.Success -> {
                result.data?.let {
                    _viewStates.setState {
                        copy(pcDetailList = it.incomes.sortedBy { it.dataMonth }.reversed())
                    }
                }
            }

            is NetworkState.Error -> {
                _viewEvents.setEvent(
                    IncomeDetailEvents.ShowToast("获取PC收益消息失败:${result.msg}", SNACK_ERROR)
                )
            }
        }
    }

    private fun getApplyDetail(incomeId: String, platform: String) {
        viewModelScope.launch {
            val dataList =
                if (platform == "pe") _viewStates.value.peDetailList else _viewStates.value.pcDetailList
            val availableMoth =
                dataList.find { it.id == incomeId }?.availableDetail?.map { it.dataMonth }
            // 无可结算月份
            if (availableMoth.isNullOrEmpty()) {
                _viewEvents.setEvent(
                    IncomeDetailEvents.ShowToast("获取结算详情失败: 可结算月份", SNACK_ERROR)
                )
                return@launch
            }

            // 获取所有可结算月份的结算详情
            val applyIds = dataList.filter { it.dataMonth in availableMoth }.map { it.id }
            flow {
                for (id in applyIds) {
                    getApplyDetailLogic(id)?.let { emit(it) }
                }
            }.catch { e ->
                Logger.e("获取结算详情失败: $e")
                _viewEvents.setEvent(
                    IncomeDetailEvents.ShowToast(
                        "获取结算详情失败: ${e.message ?: "未知错误"}", SNACK_ERROR
                    )
                )
            }.onStart {
                _viewStates.setState { copy(isLoading = true, applyIncomeDetail = emptyList()) }
            }.onCompletion {
                _viewStates.setState { copy(isLoading = false) }
            }.flowOn(Dispatchers.IO).collect {
                val detailList = _viewStates.value.applyIncomeDetail.toMutableList()
                detailList.add(it)
                _viewStates.setState { copy(applyIncomeDetail = detailList) }
                Logger.w("结算详情: $detailList")
            }
        }
    }

    private suspend fun getApplyDetailLogic(incomeId: String): ApplyIncomeDetailBean? {
        when (val result = repository.getApplyIncomeDetail(incomeId)) {
            is NetworkState.Success -> {
                result.data?.let { return it }
            }

            is NetworkState.Error -> {
                _viewEvents.setEvent(
                    IncomeDetailEvents.ShowToast(
                        "获取结算详情失败:${result.msg}",
                        SNACK_ERROR
                    )
                )
                return null
            }
        }
        return null
    }

    private fun applyIncome(incomeIds: List<String>) {
        viewModelScope.launch {
            flow<Unit> {
                when (val result = repository.applyIncome(incomeIds)) {
                    is NetworkState.Success -> {
                        _viewStates.setState { copy(applyIncomeDetail = emptyList()) }
                        loadIncomeDetail()
                    }

                    is NetworkState.Error -> {
                        _viewEvents.setEvent(
                            IncomeDetailEvents.ShowToast(
                                "申请结算失败:${result.msg}",
                                SNACK_ERROR
                            )
                        )
                    }
                }
            }.catch { e ->
                Logger.e("申请结算失败: $e")
                _viewEvents.setEvent(
                    IncomeDetailEvents.ShowToast(
                        "申请结算失败: ${e.message ?: "未知错误"}",
                        SNACK_ERROR
                    )
                )
            }.onStart {
                _viewStates.setState { copy(isLoading = true) }
            }.onCompletion {
                _viewStates.setState { copy(isLoading = false) }
            }.flowOn(Dispatchers.IO).collect()
        }
    }
}

data class IncomeDetailStates(
    val isLoading: Boolean = false,
    val peDetailList: List<IncomeBean> = emptyList(),
    val pcDetailList: List<IncomeBean> = emptyList(),
    val applyIncomeDetail: List<ApplyIncomeDetailBean> = emptyList()
)

sealed class IncomeDetailActions {
    data object LoadIncomeDetail : IncomeDetailActions()
    data class ApplyIncome(val incomeIds: List<String>) : IncomeDetailActions()
    data class GetApplyDetail(val incomeId: String, val platform: String) : IncomeDetailActions()
    data object DismissApplyDetail : IncomeDetailActions()
}

sealed class IncomeDetailEvents {
    data class ShowToast(val message: String, val type: String) : IncomeDetailEvents()
    data object DismissApplyDetail : IncomeDetailEvents()
}