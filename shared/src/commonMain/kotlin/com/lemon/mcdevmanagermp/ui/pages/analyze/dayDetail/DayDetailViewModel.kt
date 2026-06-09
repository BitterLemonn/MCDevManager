package com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.consts.CookiesExpiredException
import com.lemon.mcdevmanagermp.data.repository.ResourceRepositoryImpl
import com.lemon.mcdevmanagermp.domain.resource.DayDetailUseCase
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis.ChartType
import com.lemon.mcdevmanagermp.utils.Logger
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class DayDetailViewModel : BaseViewModel<DayDetailState, DayDetailAction, DayDetailEffect>(
    DayDetailState()
) {
    private val dayDetailUseCase = DayDetailUseCase(
        resourceRepository = ResourceRepositoryImpl.INSTANCE
    )

    companion object {
        private const val TAG = "DayDetailVM"
    }

    override fun dispatch(action: DayDetailAction) {
        when (action) {
            is DayDetailAction.InitLoad -> initLoad()
            is DayDetailAction.LoadData -> loadDetailData(action.iids)
            DayDetailAction.RefreshData -> {
                val s = state.value
                if (s.selectedIIDs.isNotEmpty()) {
                    loadDetailData(s.selectedIIDs)
                }
            }

            is DayDetailAction.ToggleResource -> toggleResource(action.iid)
            is DayDetailAction.SetPlatform -> {
                setState { copy(platform = action.platform, selectedIIDs = emptyList(), detailData = emptyMap()) }
                initLoad()
            }

            is DayDetailAction.SetDateRange -> setState {
                copy(startDate = action.start, endDate = action.end)
            }

            is DayDetailAction.SelectMetric -> setState { copy(metricType = action.type) }
            DayDetailAction.ToggleChartType -> setState {
                copy(chartType = if (chartType == ChartType.LINE) ChartType.COLUMN else ChartType.LINE)
            }

            DayDetailAction.ToggleResSelector -> setState {
                copy(isResSelectorVisible = !isResSelectorVisible)
            }
        }
    }

    /**
     * 初始化：加载资源列表和默认日期范围
     */
    private fun initLoad() {
        setState { copy(isResListLoading = true) }
        // 计算默认日期范围
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val (startDate, endDate) = dayDetailUseCase.getDefaultDateRange(today)

        setState {
            copy(
                startDate = startDate,
                endDate = endDate
            )
        }

        viewModelScope.launch {
            when (val result = dayDetailUseCase.getResourceList(state.value.platform)) {
                is NetworkState.Success -> {
                    setState { copy(resList = result.data ?: emptyList(), isResListLoading = false) }
                }

                is NetworkState.Error -> {
                    Logger.e("$TAG: 获取资源列表失败: ${result.msg}")
                    setState { copy(isResListLoading = false) }
                    handleError(result)
                }
            }
        }
    }

    /**
     * 切换资源选中状态
     */
    private fun toggleResource(iid: String) {
        val current = state.value.selectedIIDs
        val newList = if (iid in current) {
            current.filter { it != iid }
        } else {
            if (current.size >= 5) {
                viewModelScope.launch {
                    sendEffect(DayDetailEffect.ShowToast("最多选择 5 个资源"))
                }
                return
            }
            current + iid
        }
        setState { copy(selectedIIDs = newList) }
    }

    /**
     * 加载多资源每日详情数据
     */
    private fun loadDetailData(iids: List<String>) {
        if (iids.isEmpty()) {
            setState { copy(detailData = emptyMap()) }
            return
        }

        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val s = state.value

            when (val result = dayDetailUseCase.getGroupedDayDetail(
                platform = s.platform,
                startDate = s.startDate,
                endDate = s.endDate,
                iids = iids
            )) {
                is NetworkState.Success -> {
                    val grouped = result.data ?: emptyMap()
                    if (grouped.isEmpty()) {
                        setState { copy(isLoading = false, detailData = emptyMap()) }
                        sendEffect(DayDetailEffect.ShowToast("数据为空"))
                    } else {
                        setState { copy(isLoading = false, detailData = grouped) }
                    }
                }

                is NetworkState.Error -> {
                    Logger.e("$TAG: 获取日详情数据失败: $result")
                    setState { copy(isLoading = false) }
                    handleError(result)
                }
            }
        }
    }

    private fun handleError(result: NetworkState.Error<*>) {
        if (result.e is CookiesExpiredException) {
            sendEffect(DayDetailEffect.NeedReLogin)
        } else {
            sendEffect(DayDetailEffect.ShowToast("请求失败: ${result.msg}"))
        }
    }
}
