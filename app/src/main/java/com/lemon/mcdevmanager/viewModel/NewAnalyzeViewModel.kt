package com.lemon.mcdevmanager.viewModel

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanager.data.netease.resource.NewResDetailBean
import com.lemon.mcdevmanager.data.repository.DetailRepository
import com.lemon.mcdevmanager.ui.theme.TextWhite
import com.lemon.mcdevmanager.ui.widget.SNACK_ERROR
import com.lemon.mcdevmanager.utils.NetworkState
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
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.Locale

class NewAnalyzeViewModel : ViewModel() {
    private val repository = DetailRepository.getInstance()
    private val _viewStates = MutableStateFlow(NewAnalyseState())
    val viewStates = _viewStates.asStateFlow()
    private val _viewEvents = SharedFlowEvents<NewAnalyseEvent>()
    val viewEvents = _viewEvents.asSharedFlow()

    fun dispatch(action: NewAnalyseAction) {
        when (action) {
            is NewAnalyseAction.GetAnalyseList -> {
                getAnalyseList(action.platform, action.iid)
            }

            is NewAnalyseAction.UpdateFilterType -> {
                setChartData(action.type)
                _viewStates.setState {
                    copy(filterType = action.type)
                }
            }

        }
    }

    private fun getAnalyseList(platform: String, iid: String) {
        viewModelScope.launch {
            flow<Unit> {
                getAnalyseListLogic(platform, iid)
            }.onStart {
                _viewEvents.setEvent(NewAnalyseEvent.ShowLoading)
            }.onCompletion {
                _viewEvents.setEvent(NewAnalyseEvent.DismissLoading)
            }.catch { e ->
                _viewEvents.setEvent(
                    NewAnalyseEvent.ShowToast(
                        "获取数据分析失败: ${e.message ?: "未知错误请联系管理员"}", SNACK_ERROR
                    )
                )
                Logger.e(e, "获取数据分析失败")
            }.flowOn(Dispatchers.IO).collect()
        }
    }

    private suspend fun getAnalyseListLogic(platform: String, iid: String) {
        val endDate = ZonedDateTime.now().minusDays(1).toString().split("T")[0].replace("-", "")
        val startDate = ZonedDateTime.now().minusDays(8).toString().split("T")[0].replace("-", "")

        when (val result = repository.getNewDailyDetail(
            platform = platform, startDate = startDate, endDate = endDate, item = iid
        )) {
            is NetworkState.Success -> {
                result.data?.let { resultData ->
                    if (resultData.data.isNotEmpty()) {
                        _viewStates.setState {
                            copy(
                                analyseList = resultData.data,
                                iid = resultData.data[0].iid,
                                modName = resultData.data[0].resName,
                                modScore = resultData.data[0].starAdjusted,
                                newPurchaseCount = resultData.data.sumOf { it.cntBuy } / resultData.data.size,
                                newPurchasePercent = resultData.data.sumOf { it.passBuyCntRatio } / resultData.data.size.toDouble(),
                                dau = resultData.data.sumOf { it.dau } / resultData.data.size,
                                dauPercent = resultData.data.sumOf { it.passCntRolePlayRatio } / resultData.data.size.toDouble(),
                                newFollowCount = resultData.data.sumOf { it.focusCnt } / resultData.data.size,
                                newFollowPercent = resultData.data.sumOf { it.passFocusCntRatio } / resultData.data.size.toDouble(),
                                avgPlayTime = resultData.data.sumOf { it.avgPlaytime } / resultData.data.size.toDouble(),
                                avgPlayTimePercent = resultData.data.sumOf { it.passAvgRoleTimeRatio } / resultData.data.size.toDouble())
                        }
                        setChartData(viewStates.value.filterType)
                    }
                }
            }

            is NetworkState.Error -> {
                _viewEvents.setEvent(
                    NewAnalyseEvent.ShowToast(
                        "获取数据分析失败: ${result.e?.message ?: "未知错误请联系管理员"}",
                        SNACK_ERROR
                    )
                )
            }
        }
    }

    private fun setChartData(type: Int) {
        when (type) {
            0 -> {
                // 新增购买
                val analyseList = _viewStates.value.analyseList
                if (analyseList.isNotEmpty()) {
                    val chartData = mutableMapOf<String, List<Pair<String, Float>>>()
                    chartData[analyseList[0].resName] =
                        analyseList.map { it.dateId to it.cntBuy.toFloat() }
                    chartData["同类作品"] =
                        analyseList.map { it.dateId to it.avgFirstTypeBuy.toFloat() }
                    calChartData(chartData)
                }
            }

            1 -> {
                // 日活
                val analyseList = _viewStates.value.analyseList
                if (analyseList.isNotEmpty()) {
                    val chartData = mutableMapOf<String, List<Pair<String, Float>>>()
                    chartData[analyseList[0].resName] =
                        analyseList.map { it.dateId to it.dau.toFloat() }
                    chartData["同类作品"] =
                        analyseList.map { it.dateId to it.avgFirstTypeRolePlay.toFloat() }
                    calChartData(chartData)
                }
            }

            2 -> {
                // 组件涨粉
                val analyseList = _viewStates.value.analyseList
                if (analyseList.isNotEmpty()) {
                    val chartData = mutableMapOf<String, List<Pair<String, Float>>>()
                    chartData[analyseList[0].resName] =
                        analyseList.map { it.dateId to it.focusCnt.toFloat() }
                    chartData["同类作品"] =
                        analyseList.map { it.dateId to it.avgFirstTypeFocus.toFloat() }
                    calChartData(chartData)
                }
            }

            3 -> {
                // 人均游玩时长
                val analyseList = _viewStates.value.analyseList
                if (analyseList.isNotEmpty()) {
                    val chartData = mutableMapOf<String, List<Pair<String, Float>>>()
                    chartData[analyseList[0].resName] =
                        analyseList.map { it.dateId to it.avgPlaytime.toFloat() }
                    chartData["同类作品"] =
                        analyseList.map { it.dateId to it.firstTypeAvgRoleTime.toFloat() }
                    calChartData(chartData)
                }
            }
        }
    }

    private fun calChartData(chartData: Map<String, List<Pair<String, Float>>>) {
        val lineParams = mutableListOf<Line>()
        val barParams = mutableListOf<Bars>()
        val colorList =
            listOf(Color(0xFFE57373), Color(0xFFB39DDB))

        val analyzeYValueList = emptyList<List<Double>>().toMutableList()
        val analyzeXValueList = emptyList<List<String>>().toMutableList()
        val analyzeResNameList = emptyList<String>().toMutableList()
        chartData.entries.forEach { item ->
            val yValue = mutableListOf<Float>()
            val xValue = mutableListOf<String>()
            for (data in item.value) {
                yValue.add(data.second)
                val sdf = SimpleDateFormat("yyyyMMdd", Locale.CHINESE)
                val date = sdf.parse(data.first)
                val calendar = Calendar.getInstance().apply { time = date }

                val month = calendar.get(Calendar.MONTH) + 1
                var day = calendar.get(Calendar.DAY_OF_MONTH).toString()
                if (day.toInt() < 10) {
                    day = "0$day"
                }
                xValue.add("$month/$day")
            }
            analyzeYValueList.add(yValue.map { it.toDouble() })
            analyzeXValueList.add(xValue)
            analyzeResNameList.add(item.key)
        }

        if (analyzeXValueList.isNotEmpty()) {
            for (i in analyzeResNameList.indices) {
                lineParams.add(
                    Line(
                        label = analyzeResNameList[i],
                        values = analyzeYValueList[i],
                        color = SolidColor(colorList[i]),
                        curvedEdges = false,
                        dotProperties = DotProperties(
                            enabled = true,
                            color = SolidColor(colorList[i]),
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
                                color = SolidColor(colorList[resIndex])
                            )
                        )
                    }
                }
                if (barData.isNotEmpty()) barParams.add(
                    Bars(
                        label = analyzeXValueList[0][dateIndex], values = barData
                    )
                )
            }

            _viewStates.setState {
                copy(
                    chartDateList = analyzeXValueList[0],
                    lineParams = lineParams,
                    barParams = barParams
                )
            }
        }
    }
}

data class NewAnalyseState(
    val filterType: Int = 0,

    val iid: String = "",
    val analyseList: List<NewResDetailBean> = emptyList(),
    val modName: String = "",
    val modScore: Double = 0.0,
    val newPurchaseCount: Int = 0,
    val newPurchasePercent: Double = 0.0,
    val dau: Int = 0,
    val dauPercent: Double = 0.0,
    val newFollowCount: Int = 0,
    val newFollowPercent: Double = 0.0,
    val avgPlayTime: Double = 0.0,
    val avgPlayTimePercent: Double = 0.0,

    val chartDateList: List<String> = emptyList(),
    val lineParams: List<Line> = emptyList(),
    val barParams: List<Bars> = emptyList(),
)

sealed class NewAnalyseAction {
    data class GetAnalyseList(val platform: String, val iid: String) : NewAnalyseAction()
    data class UpdateFilterType(val type: Int) : NewAnalyseAction()
}

sealed class NewAnalyseEvent {
    data class ShowToast(val message: String, val flag: String) : NewAnalyseEvent()
    data object ShowLoading : NewAnalyseEvent()
    data object DismissLoading : NewAnalyseEvent()
}