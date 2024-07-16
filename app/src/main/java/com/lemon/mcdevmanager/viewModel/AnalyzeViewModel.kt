package com.lemon.mcdevmanager.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanager.data.database.database.GlobalDataBase
import com.lemon.mcdevmanager.data.database.entities.AnalyzeEntity
import com.lemon.mcdevmanager.data.global.AppContext
import com.lemon.mcdevmanager.data.netease.resource.ResDetailBean
import com.lemon.mcdevmanager.data.netease.resource.ResourceBean
import com.lemon.mcdevmanager.data.repository.DetailRepository
import com.lemon.mcdevmanager.utils.CookiesExpiredException
import com.lemon.mcdevmanager.utils.NetworkState
import com.lemon.mcdevmanager.utils.logout
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
import kotlinx.coroutines.withContext
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
            is AnalyzeAction.UpdateStartDate ->
                _viewStates.setState { copy(startDate = action.date) }

            is AnalyzeAction.UpdatePlatform ->
                _viewStates.setState { copy(platform = action.platform) }

            is AnalyzeAction.UpdateEndDate ->
                _viewStates.setState { copy(endDate = action.date) }

            is AnalyzeAction.UpdateFilterType ->{
                _viewStates.setState { copy(filterType = action.type) }
                setChartData(action.type)
            }

            is AnalyzeAction.ChangeResourceList -> {
                if (action.isDel) {
                    _viewStates.setState { copy(filterResourceList = filterResourceList - action.resId) }
                } else {
                    _viewStates.setState { copy(filterResourceList = filterResourceList + action.resId) }
                }
            }

            is AnalyzeAction.LoadAnalyze -> loadAnalyze()
        }
    }

    private fun getAllResourceList() {
        viewModelScope.launch {
            flow<Unit> {
                withContext(Dispatchers.IO) {
                    GlobalDataBase.database.infoDao()
                        .getLastAnalyzeParmaByNickname(AppContext.nowNickname)?.let {
                            _viewStates.setState {
                                copy(
                                    platform = it.platform,
                                    startDate = it.startDate,
                                    endDate = it.endDate,
                                    filterResourceList = it.filterResourceList.split(",")
                                )
                            }
                        }
                }
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

            is NetworkState.Error -> if (res.e is CookiesExpiredException) {
                _viewEvents.setEvent(AnalyzeEvent.NeedReLogin)
                _viewEvents.setEvent(AnalyzeEvent.ShowToast("登录过期, 请重新登录"))
                logout(AppContext.nowNickname)
            } else {
                _viewEvents.setEvent(AnalyzeEvent.ShowToast("获取资源列表失败: ${res.msg}"))
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
                setChartData(viewStates.value.filterType)
                withContext(Dispatchers.IO) {
                    GlobalDataBase.database.infoDao().insertAnalyzeParam(
                        AnalyzeEntity(
                            nickname = AppContext.nowNickname,
                            filterType = viewStates.value.filterType,
                            platform = viewStates.value.platform,
                            startDate = viewStates.value.startDate,
                            endDate = viewStates.value.endDate,
                            filterResourceList = viewStates.value.filterResourceList.joinToString(",")
                        )
                    )
                }
            }

            is NetworkState.Error -> if (res.e is CookiesExpiredException) {
                _viewEvents.setEvent(AnalyzeEvent.NeedReLogin)
                _viewEvents.setEvent(AnalyzeEvent.ShowToast("登录过期, 请重新登录"))
                logout(AppContext.nowNickname)
            } else {
                _viewEvents.setEvent(AnalyzeEvent.ShowToast("获取数据分析失败: ${res.msg}"))
            }
        }
    }

    private fun setChartData(type: Int){
        when (type){
            0 -> _viewStates.setState { copy(chartData = viewStates.value.analyzeList.groupBy { it.resName }.mapValues { it.value.map { it.dateId to it.cntBuy.toFloat() } }) }
            1 -> _viewStates.setState { copy(chartData = viewStates.value.analyzeList.groupBy { it.resName }.mapValues { it.value.map { it.dateId to it.downloadNum.toFloat() } }) }
            2 -> _viewStates.setState { copy(chartData = viewStates.value.analyzeList.groupBy { it.resName }.mapValues { it.value.map { it.dateId to it.diamond.toFloat() } }) }
            3 -> _viewStates.setState { copy(chartData = viewStates.value.analyzeList.groupBy { it.resName }.mapValues { it.value.map { it.dateId to it.points.toFloat() } }) }
            4 -> _viewStates.setState { copy(chartData = viewStates.value.analyzeList.groupBy { it.resName }.mapValues { it.value.map { it.dateId to it.dau.toFloat() } }) }
            5 -> _viewStates.setState { copy(chartData = viewStates.value.analyzeList.groupBy { it.resName }.mapValues { it.value.map { it.dateId to it.refundRate.toFloat() } }) }
        }
    }
}

data class AnalyzeViewState(
    val analyzeList: List<ResDetailBean> = emptyList(),
    val platform: String = "pe",
    val filterType: Int = 0,
    val startDate: String = ZonedDateTime.now().minusDays(7).toString(),
    val endDate: String = ZonedDateTime.now().toString(),
    val filterResourceList: List<String> = emptyList(),
    val allResourceList: List<ResourceBean> = emptyList(),
    val isShowLoading: Boolean = false,
    val chartData: Map<String, List<Pair<String, Float>>> = emptyMap()
)

sealed class AnalyzeAction {
    data class UpdatePlatform(val platform: String) : AnalyzeAction()
    data class UpdateStartDate(val date: String) : AnalyzeAction()
    data class UpdateEndDate(val date: String) : AnalyzeAction()
    data class ChangeResourceList(val resId: String, val isDel: Boolean) : AnalyzeAction()
    data class UpdateFilterType(val type: Int) : AnalyzeAction()
    data object LoadAnalyze : AnalyzeAction()
    data object GetAllResourceList : AnalyzeAction()
}

sealed class AnalyzeEvent {
    data class ShowToast(val msg: String, val isError: Boolean = true) : AnalyzeEvent()
    data object NeedReLogin : AnalyzeEvent()
}