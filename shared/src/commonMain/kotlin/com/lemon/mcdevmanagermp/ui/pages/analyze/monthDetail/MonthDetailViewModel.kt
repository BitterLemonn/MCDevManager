package com.lemon.mcdevmanagermp.ui.pages.analyze.monthDetail

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.repository.AnalyzeRepositoryImpl
import com.lemon.mcdevmanagermp.domain.analyze.MonthDetailUseCase
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import com.lemon.mcdevmanagermp.utils.Logger
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class MonthDetailViewModel : BaseViewModel<MonthDetailState, MonthDetailAction, MonthDetailEffect>(
    MonthDetailState()
) {
    private val monthDetailUseCase = MonthDetailUseCase(
        analyzeRepository = AnalyzeRepositoryImpl.INSTANCE
    )

    companion object {
        private const val TAG = "MonthDetailVM"
    }

    override fun dispatch(action: MonthDetailAction) {
        when (action) {
            is MonthDetailAction.InitLoad -> initLoad()
            is MonthDetailAction.LoadData -> loadMonthData(action.platform, action.start, action.end)
            MonthDetailAction.RefreshData -> {
                val s = state.value
                loadMonthData(s.platform, s.startDate, s.endDate)
            }

            is MonthDetailAction.SetPlatform -> {
                setState { copy(platform = action.platform, monthData = emptyList()) }
                loadWithCurrentRange()
            }

            is MonthDetailAction.SetQuickTimeRange -> {
                setState { copy(quickTimeRange = action.range) }
                applyQuickTimeRange(action.range)
            }

            is MonthDetailAction.SelectMetric -> setState { copy(selectedMetric = action.type) }
        }
    }

    private fun initLoad() {
        applyQuickTimeRange(state.value.quickTimeRange)
    }

    /**
     * 根据快捷时间范围计算日期并加载数据
     */
    private fun applyQuickTimeRange(range: Int) {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val (startStr, endStr) = monthDetailUseCase.getQuickTimeRange(range, today)
        setState { copy(startDate = startStr, endDate = endStr) }
        loadMonthData(state.value.platform, startStr, endStr)
    }

    private fun loadWithCurrentRange() {
        val s = state.value
        if (s.startDate.isNotEmpty() && s.endDate.isNotEmpty()) {
            loadMonthData(s.platform, s.startDate, s.endDate)
        }
    }

    /**
     * 加载月度汇总数据
     */
    private fun loadMonthData(platform: String, startDate: String, endDate: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true) }

            when (val result = monthDetailUseCase.getMonthDetail(
                platform = platform,
                startDate = startDate,
                endDate = endDate
            )) {
                is NetworkState.Success -> {
                    val data = result.data ?: emptyList()
                    if (data.isEmpty()) {
                        setState { copy(isLoading = false, monthData = emptyList()) }
                        sendEffect(MonthDetailEffect.ShowToast("数据为空"))
                    } else {
                        setState { copy(isLoading = false, monthData = data) }
                    }
                }

                is NetworkState.Error -> {
                    Logger.e("$TAG: 获取月度数据失败: $result")
                    setState { copy(isLoading = false) }
                    handleError(
                        result,
                        onNeedReLogin = { MonthDetailEffect.NeedReLogin },
                        onShowToast = { MonthDetailEffect.ShowToast(it) }
                    )
                }
            }
        }
    }
}
