package com.lemon.mcdevmanager.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanager.data.netease.income.OneResRealtimeIncomeBean
import com.lemon.mcdevmanager.data.netease.resource.ResourceBean
import com.lemon.mcdevmanager.data.repository.DetailRepository
import com.lemon.mcdevmanager.data.repository.RealtimeProfitRepository
import com.lemon.mcdevmanager.ui.widget.SNACK_ERROR
import com.lemon.mcdevmanager.utils.CookiesExpiredException
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

class RealtimeProfitViewModel : ViewModel() {
    private val realtimeRepository = RealtimeProfitRepository.getInstance()
    private val overviewRepository = DetailRepository.getInstance()
    private val _viewStates = MutableStateFlow(RealtimeProfitStates())
    val viewStates = _viewStates.asStateFlow()
    private val _viewEvents = SharedFlowEvents<RealtimeProfitEvent>()
    val viewEvents = _viewEvents.asSharedFlow()

    private val errorRequestList = mutableListOf<String>()

    fun dispatch(action: RealtimeProfitAction) {
        when (action) {
            RealtimeProfitAction.GetOneDayDetail -> getOneDayDetail()
            is RealtimeProfitAction.ModifyCheckList -> modifyCheckList(action.iid, action.isAdd)
            is RealtimeProfitAction.UpdateCheckDay -> _viewStates.setState { copy(checkDay = action.day) }
        }
    }

    private fun modifyCheckList(iid: String, isAdd: Boolean) {
        val checkList = _viewStates.value.checkList.toMutableList()
        if (isAdd) {
            checkList.add(iid)
        } else {
            checkList.remove(iid)
        }
        _viewStates.setState { copy(checkList = checkList) }
    }

    private fun getOneDayDetail() {
        viewModelScope.launch {
            flow<Unit> {
                getAllResourceLogic()
                getOneDayDetailLogic()
            }.onStart {
                _viewStates.setState {
                    copy(
                        isLoading = true,
                        lastRequestTime = System.currentTimeMillis()
                    )
                }
            }.catch {
                _viewEvents.setEvent(
                    RealtimeProfitEvent.ShowToast(it.message ?: "获取实时收益失败")
                )
            }.onCompletion {
                _viewStates.setState { copy(isLoading = false) }
                if (errorRequestList.isNotEmpty()) {
                    _viewEvents.setEvent(
                        RealtimeProfitEvent.ShowToast(
                            "以下组件收益获取失败: ${errorRequestList.joinToString(",")}"
                        )
                    )
                    errorRequestList.clear()
                }
            }.flowOn(Dispatchers.IO).collect()
        }
    }

    private suspend fun getAllResourceLogic() {
        when (val result = overviewRepository.getAllResource(_viewStates.value.platform)) {
            is NetworkState.Success -> {
                result.data?.let { _viewStates.setState { copy(resList = it.item) } }
            }

            is NetworkState.Error -> {
                Logger.e("获取资源列表失败: ${result.msg}")
                if (result.e is CookiesExpiredException) {
                    _viewEvents.setEvent(RealtimeProfitEvent.NeedReLogin(result.msg))
                    throw result.e
                } else {
                    throw Exception("获取资源列表失败: ${result.msg}")
                }
            }
        }
    }

    private suspend fun getOneDayDetailLogic() {
        val checkDay = _viewStates.value.checkDay
        var checkList = _viewStates.value.checkList
        if (checkList.isEmpty()) {
            Logger.d("checkList为空，使用resList: ${_viewStates.value.resList}")
            checkList = _viewStates.value.resList.map { it.itemId }
        }
        if (checkList.isEmpty()) {
            _viewEvents.setEvent(RealtimeProfitEvent.ShowToast("暂未查询到资源列表"))
            return
        }
        val platform = _viewStates.value.platform
        _viewStates.setState { copy(profitMap = emptyMap(), totalPoints = 0, totalDiamond = 0) }

        for (iid in checkList) {
            when (val result = realtimeRepository.getOneDayDetail(platform, iid, checkDay)) {
                is NetworkState.Success -> {
                    result.data?.let {
                        val map = _viewStates.value.profitMap.toMutableMap()
                        map[iid] = it
                        Logger.d("获取资源${iid}收益成功: 钻石:${it.totalDiamonds}，绿宝石:${it.totalPoints}")
                        _viewStates.setState {
                            copy(
                                profitMap = map,
                                totalDiamond = totalDiamond + it.totalDiamonds,
                                totalPoints = totalPoints + it.totalPoints
                            )
                        }
                    }
                }

                is NetworkState.Error -> {
                    Logger.e("获取资源${iid}收益失败: ${result.msg}")
                    if (result.e is CookiesExpiredException) {
                        _viewEvents.setEvent(RealtimeProfitEvent.NeedReLogin(result.msg))
                        throw result.e
                    } else {
                        errorRequestList.add(
                            _viewStates.value.resList.find { it.itemId == iid }?.itemName ?: iid
                        )
                    }
                }
            }
        }
    }
}

data class RealtimeProfitStates(
    val isLoading: Boolean = false,
    val resList: List<ResourceBean> = emptyList(),
    val profitMap: Map<String, OneResRealtimeIncomeBean> = emptyMap(),
    val checkList: List<String> = emptyList(),
    val checkDay: String = "",
    val platform: String = "pe",
    val lastRequestTime: Long = 0,

    val totalDiamond: Int = 0,
    val totalPoints: Int = 0
)

sealed class RealtimeProfitEvent {
    data class ShowToast(val message: String, val type: String = SNACK_ERROR) :
        RealtimeProfitEvent()

    data class NeedReLogin(val message: String) : RealtimeProfitEvent()
}

sealed class RealtimeProfitAction {
    data object GetOneDayDetail : RealtimeProfitAction()
    data class ModifyCheckList(val iid: String, val isAdd: Boolean = false) : RealtimeProfitAction()
    data class UpdateCheckDay(val day: String) : RealtimeProfitAction()
}