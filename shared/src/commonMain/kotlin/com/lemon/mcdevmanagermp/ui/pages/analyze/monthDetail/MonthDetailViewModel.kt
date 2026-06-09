package com.lemon.mcdevmanagermp.ui.pages.analyze.monthDetail

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.consts.CookiesExpiredException
import com.lemon.mcdevmanagermp.data.repository.ResourceRepositoryImpl
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import com.lemon.mcdevmanagermp.utils.Logger
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class MonthDetailViewModel : BaseViewModel<MonthDetailState, MonthDetailAction, MonthDetailEffect>(
    MonthDetailState()
) {
    private val resourceRepository = ResourceRepositoryImpl.INSTANCE

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
        val (start, end) = when (range) {
            QuickTimeRange.THIS_MONTH -> {
                val start = LocalDate(today.year, today.month.number, 1)
                start to today.minus(1, DateTimeUnit.DAY)
            }

            QuickTimeRange.LAST_3_MONTHS -> {
                val end = today.minus(1, DateTimeUnit.DAY)
                val start = LocalDate(today.year, today.month.number, 1)
                    .minus(2, DateTimeUnit.MONTH)
                start to end
            }

            QuickTimeRange.LAST_6_MONTHS -> {
                val end = today.minus(1, DateTimeUnit.DAY)
                val start = LocalDate(today.year, today.month.number, 1)
                    .minus(5, DateTimeUnit.MONTH)
                start to end
            }

            QuickTimeRange.LAST_12_MONTHS -> {
                val end = today.minus(1, DateTimeUnit.DAY)
                val start = LocalDate(today.year, today.month.number, 1)
                    .minus(11, DateTimeUnit.MONTH)
                start to end
            }

            else -> {
                val end = today.minus(1, DateTimeUnit.DAY)
                val start = end.minus(90, DateTimeUnit.DAY)
                start to end
            }
        }

        val startStr = formatDateParam(start)
        val endStr = formatDateParam(end)
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
            val apiPlatform = if (platform == "pe") "pe" else "comp"

            when (val result = resourceRepository.getMonthDetail(
                platform = apiPlatform,
                category = apiPlatform,
                startDate = startDate,
                endDate = endDate,
                dayDateId = endDate
            )) {
                is NetworkState.Success -> {
                    result.data?.let { data ->
                        // 按 monthId 降序排列
                        val sorted = data.data.sortedByDescending { it.monthId }
                        setState { copy(isLoading = false, monthData = sorted) }
                    } ?: run {
                        setState { copy(isLoading = false, monthData = emptyList()) }
                        sendEffect(MonthDetailEffect.ShowToast("数据为空"))
                    }
                }

                is NetworkState.Error -> {
                    Logger.e("$TAG: 获取月度数据失败: $result")
                    setState { copy(isLoading = false) }
                    handleError(result)
                }
            }
        }
    }

    private fun handleError(result: NetworkState.Error<*>) {
        if (result.e is CookiesExpiredException) {
            sendEffect(MonthDetailEffect.NeedReLogin)
        } else {
            sendEffect(MonthDetailEffect.ShowToast("请求失败: ${result.msg}"))
        }
    }

    private fun formatDateParam(date: LocalDate): String {
        return date.toString().replace("-", "")
    }
}
