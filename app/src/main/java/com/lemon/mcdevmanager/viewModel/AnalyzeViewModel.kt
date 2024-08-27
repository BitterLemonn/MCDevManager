package com.lemon.mcdevmanager.viewModel

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanager.data.database.database.GlobalDataBase
import com.lemon.mcdevmanager.data.database.entities.AnalyzeEntity
import com.lemon.mcdevmanager.data.global.AppContext
import com.lemon.mcdevmanager.data.netease.resource.ResDetailBean
import com.lemon.mcdevmanager.data.netease.resource.ResMonthDetailBean
import com.lemon.mcdevmanager.data.netease.resource.ResourceBean
import com.lemon.mcdevmanager.data.repository.DetailRepository
import com.lemon.mcdevmanager.ui.theme.TextWhite
import com.lemon.mcdevmanager.utils.CookiesExpiredException
import com.lemon.mcdevmanager.utils.NetworkState
import com.lemon.mcdevmanager.utils.logout
import com.orhanobut.logger.Logger
import com.zj.mvi.core.SharedFlowEvents
import com.zj.mvi.core.setEvent
import com.zj.mvi.core.setState
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.Line
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
import java.time.ZoneId
import java.time.ZonedDateTime

class AnalyzeViewModel : ViewModel() {
    private val detailRepository = DetailRepository.getInstance()
    private val _viewStates = MutableStateFlow(AnalyzeViewState())
    val viewStates = _viewStates.asStateFlow()
    private val _viewEvents = SharedFlowEvents<AnalyzeEvent>()
    val viewEvents = _viewEvents.asSharedFlow()

    fun dispatch(action: AnalyzeAction) {
        when (action) {
            is AnalyzeAction.GetLastAnalyzeParams -> getLastAnalyzePlatform()
            is AnalyzeAction.GetAllResourceList -> getAllResourceList()
            is AnalyzeAction.UpdateChartColor -> _viewStates.setState { copy(chartColor = action.color) }
            is AnalyzeAction.UpdateStartDate -> _viewStates.setState { copy(startDate = action.date) }
            is AnalyzeAction.UpdateFromMonth -> _viewStates.setState { copy(fromMonth = action.date) }
            is AnalyzeAction.UpdateToMonth -> _viewStates.setState { copy(toMonth = action.date) }

            is AnalyzeAction.UpdatePlatformAnalyze -> {
                _viewStates.setState {
                    copy(
                        platform = action.platform,
                        filterResourceList = emptyList(),
                        lineParams = emptyList(),
                        barParams = emptyList()
                    )
                }
                getLastAnalyzeParam()
                getAllResourceList()
            }

            is AnalyzeAction.UpdatePlatformOverview -> {
                _viewStates.setState {
                    copy(
                        platform = action.platform,
                        analyzeList = emptyList()
                    )
                }
                loadMonthAnalyze()
            }

            is AnalyzeAction.UpdateEndDate -> _viewStates.setState { copy(endDate = action.date) }

            is AnalyzeAction.UpdateFilterType -> {
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

            is AnalyzeAction.GetMonthlyAnalyze -> loadMonthAnalyze()

            is AnalyzeAction.LoadAnalyze -> loadAnalyze()
        }
    }

    private fun getLastAnalyzePlatform() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                GlobalDataBase.database.infoDao()
                    .getLastAnalyzePlatformByNickname(AppContext.nowNickname)
            }?.let {
                _viewStates.setState { copy(platform = it) }
            }
            getLastAnalyzeParam()
        }
    }

    private fun getLastAnalyzeParam() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                GlobalDataBase.database.infoDao().getLastAnalyzeParamsByNicknamePlatform(
                    AppContext.nowNickname, viewStates.value.platform
                )?.let {
                    val nowDate = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"))
                    _viewStates.setState {
                        copy(
                            platform = it.platform,
                            startDate = it.startDate,
                            endDate = nowDate.minusDays(1L).toString(),
                            filterResourceList = it.filterResourceList.split(",")
                        )
                    }
                    loadAnalyze()
                }
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

    private fun loadMonthAnalyze() {
        viewModelScope.launch {
            flow<Unit> {
                loadMonthAnalyzeLogic()
            }.onStart {
                _viewStates.setState { copy(isShowLoading = true) }
            }.onCompletion {
                _viewStates.setState { copy(isShowLoading = false) }
            }.catch {
                Logger.e(it, "loadMonthAnalyze")
                _viewEvents.setEvent(
                    AnalyzeEvent.ShowToast(it.message ?: "获取数据分析失败: 未知错误")
                )
            }.flowOn(Dispatchers.IO).collect()
        }
    }

    private suspend fun loadMonthAnalyzeLogic() {
        if (viewStates.value.startDate > viewStates.value.endDate) {
            _viewEvents.setEvent(AnalyzeEvent.ShowToast("开始日期不能大于结束日期"))
            return
        }
        if (viewStates.value.filterResourceList.isEmpty()) {
            _viewEvents.setEvent(AnalyzeEvent.ShowToast("请先选择需要对比的组件"))
            return
        }

        val fromMonthDate = viewStates.value.fromMonth.replace("-", "") + "01"
        val toMonthDate = viewStates.value.toMonth.replace("-", "") + "01"
        when (
            val res = detailRepository.getMonthDetail(
                viewStates.value.platform,
                fromMonthDate,
                toMonthDate,
            )
        ) {
            is NetworkState.Success -> res.data?.let {
                _viewStates.setState { copy(monthAnalyseList = it.data.reversed()) }
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

    private fun setChartData(type: Int) {
        when (type) {
            0 -> {
                val chartData = viewStates.value.analyzeList.groupBy { it.resName }
                    .mapValues { it.value.map { it.dateId to it.cntBuy.toFloat() } }
                calChartData(chartData)
            }

            1 -> {
                val chartData = viewStates.value.analyzeList.groupBy { it.resName }
                    .mapValues { it.value.map { it.dateId to it.downloadNum.toFloat() } }
                calChartData(chartData)
            }

            2 -> {
                val chartData = viewStates.value.analyzeList.groupBy { it.resName }
                    .mapValues { it.value.map { it.dateId to it.diamond.toFloat() } }
                calChartData(chartData)
            }

            3 -> {
                val chartData = viewStates.value.analyzeList.groupBy { it.resName }
                    .mapValues { it.value.map { it.dateId to it.points.toFloat() } }
                calChartData(chartData)
            }

            4 -> {
                val chartData = viewStates.value.analyzeList.groupBy { it.resName }
                    .mapValues { it.value.map { it.dateId to it.dau.toFloat() } }
                calChartData(chartData)
            }

            5 -> {
                val chartData = viewStates.value.analyzeList.groupBy { it.resName }
                    .mapValues { it.value.map { it.dateId to it.refundRate.toFloat() } }
                calChartData(chartData)
            }
        }
    }

    private fun calChartData(chartData: Map<String, List<Pair<String, Float>>>) {
        val lineParams = mutableListOf<Line>()
        val barParams = mutableListOf<Bars>()

        val analyzeYValueList = emptyList<List<Double>>().toMutableList()
        val analyzeXValueList = emptyList<List<String>>().toMutableList()
        val analyzeResNameList = emptyList<String>().toMutableList()
        chartData.entries.forEach { item ->
            val yValue = mutableListOf<Float>()
            val xValue = mutableListOf<String>()
            for (data in item.value) {
                yValue.add(data.second)
                val xValueStr = data.first.substring(5)
                val date = xValueStr.substring(xValueStr.length - 2, xValueStr.length)
                val month = xValueStr.substring(0, xValueStr.length - date.length)
                xValue.add("$month/$date")
            }
            analyzeYValueList.add(yValue.map { it.toDouble() })
            analyzeXValueList.add(xValue)
            analyzeResNameList.add(item.key)
        }

        if (analyzeXValueList.isNotEmpty()) {
            val chartColor = viewStates.value.chartColor
            for (i in analyzeResNameList.indices) {
                lineParams.add(
                    Line(
                        label = analyzeResNameList[i],
                        values = analyzeYValueList[i],
                        color = SolidColor(chartColor[i % chartColor.size]),
                        curvedEdges = false,
                        dotProperties = DotProperties(
                            enabled = true,
                            color = SolidColor(chartColor[i % chartColor.size]),
                            radius = 4.dp,
                            strokeWidth = 2.dp,
                            strokeColor = SolidColor(TextWhite)
                        )
                    )
                )
            }

            for (dateIndex in analyzeXValueList[0].indices) {
                val barData = mutableListOf<Bars.Data>()
                for (resIndex in analyzeResNameList.indices) {
                    val value = analyzeYValueList[resIndex][dateIndex]
                    if (value != 0.0) {
                        barData.add(
                            Bars.Data(
                                label = analyzeResNameList[resIndex],
                                value = analyzeYValueList[resIndex][dateIndex],
                                color = SolidColor(chartColor[resIndex % chartColor.size])
                            )
                        )
                    }
                }
                if (barData.isNotEmpty())
                    barParams.add(
                        Bars(
                            label = analyzeXValueList[0][dateIndex],
                            values = barData
                        )
                    )
            }

            _viewStates.setState {
                copy(
                    lineParams = lineParams,
                    barParams = barParams,
                    chartDateList = analyzeXValueList[0]
                )
            }
        }
    }
}

data class AnalyzeViewState(
    val isShowLoading: Boolean = false,

    val platform: String = "pe",
    val filterType: Int = 0,
    val startDate: String = ZonedDateTime.now().minusDays(7).toString(),
    val endDate: String = ZonedDateTime.now().toString(),

    val fromMonth: String = "2024-07",
    val toMonth: String = "2024-07",
    val monthAnalyseList: List<ResMonthDetailBean> = emptyList(),

    val analyzeList: List<ResDetailBean> = emptyList(),
    val filterResourceList: List<String> = emptyList(),
    val allResourceList: List<ResourceBean> = emptyList(),

    val chartDateList: List<String> = emptyList(),
    val lineParams: List<Line> = emptyList(),
    val barParams: List<Bars> = emptyList(),
    val chartColor: List<Color> = emptyList()
)

sealed class AnalyzeAction {
    data class UpdateChartColor(val color: List<Color>) : AnalyzeAction()
    data class UpdatePlatformAnalyze(val platform: String) : AnalyzeAction()
    data class UpdatePlatformOverview(val platform: String) : AnalyzeAction()
    data class UpdateStartDate(val date: String) : AnalyzeAction()
    data class UpdateEndDate(val date: String) : AnalyzeAction()
    data class ChangeResourceList(val resId: String, val isDel: Boolean) : AnalyzeAction()
    data class UpdateFilterType(val type: Int) : AnalyzeAction()
    data class UpdateFromMonth(val date: String) : AnalyzeAction()
    data class UpdateToMonth(val date: String) : AnalyzeAction()

    data object GetLastAnalyzeParams : AnalyzeAction()
    data object LoadAnalyze : AnalyzeAction()
    data object GetAllResourceList : AnalyzeAction()
    data object GetMonthlyAnalyze : AnalyzeAction()
}

sealed class AnalyzeEvent {
    data class ShowToast(val msg: String, val isError: Boolean = true) : AnalyzeEvent()
    data object NeedReLogin : AnalyzeEvent()
}