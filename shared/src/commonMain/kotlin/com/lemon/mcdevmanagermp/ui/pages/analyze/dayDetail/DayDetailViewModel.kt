package com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.common.AppContext
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.repository.AnalyzeRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.ResourceRepositoryImpl
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import com.lemon.mcdevmanagermp.domain.analyze.DayDetailConfig
import com.lemon.mcdevmanagermp.domain.analyze.DayDetailUseCase
import com.lemon.mcdevmanagermp.domain.analyze.formatYmd
import com.lemon.mcdevmanagermp.domain.analyze.spanDays
import com.lemon.mcdevmanagermp.domain.resource.GetResourceListUseCase
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis.ChartType
import com.lemon.mcdevmanagermp.utils.Logger
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class DayDetailViewModel : BaseViewModel<DayDetailState, DayDetailAction, DayDetailEffect>(
    DayDetailState()
) {
    private val dayDetailUseCase = DayDetailUseCase(
        analyzeRepository = AnalyzeRepositoryImpl.INSTANCE
    )
    private val getResourceListUseCase = GetResourceListUseCase(ResourceRepositoryImpl.INSTANCE)

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
                setState {
                    copy(
                        platform = action.platform,
                        selectedIIDs = emptyList(),
                        detailData = emptyMap(),
                        metricType = DayDetailMetricType.NEW_PURCHASE
                    )
                }
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
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val accountKey = AppContext.userInfo?.nickname
        val platform = state.value.platform

        viewModelScope.launch {
            // 恢复上次查询配置（按账号 + 平台）：以「昨天」为终点按跨度重算区间并回选资源
            val saved = if (accountKey != null) {
                dayDetailUseCase.getDayDetailConfig(accountKey, platform)
            } else null
            if (saved != null && saved.dateSpanDays > 0) {
                val end = today.minus(1, DateTimeUnit.DAY)
                val start = end.minus(saved.dateSpanDays - 1, DateTimeUnit.DAY)
                setState {
                    copy(
                        startDate = formatYmd(start),
                        endDate = formatYmd(end),
                        selectedIIDs = saved.selectedIIDs
                    )
                }
            } else {
                val (start, end) = dayDetailUseCase.getDefaultDateRange(today)
                setState { copy(startDate = start, endDate = end) }
            }

            val resourceResult = if (platform == "lobby") {
                when (val result = AnalyzeRepositoryImpl.INSTANCE.getLobbyIncomeResources()) {
                    is NetworkState.Success -> NetworkState.Success(
                        result.data?.items?.map { ResourceData(itemId = it.itemId, itemName = it.itemName) }
                            ?: emptyList()
                    )

                    is NetworkState.Error -> NetworkState.Error(result.msg, result.e)
                }
            } else {
                getResourceListUseCase(platform, onlineOnly = true)
            }
            when (val result = resourceResult) {
                is NetworkState.Success -> {
                    setState { copy(resList = result.data ?: emptyList(), isResListLoading = false) }
                }

                is NetworkState.Error -> {
                    Logger.e("$TAG: 获取资源列表失败: ${result.msg}")
                    setState { copy(isResListLoading = false) }
                    handleDayDetailError(result)
                }
            }

            // 恢复出选中资源则自动查询展示
            val iids = state.value.selectedIIDs
            if (iids.isNotEmpty()) loadDetailData(iids)
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
            val s = state.value
            // 保存本次查询配置（按账号 + 平台；未登录则跳过）
            val accountKey = AppContext.userInfo?.nickname
            if (accountKey != null) {
                dayDetailUseCase.saveDayDetailConfig(
                    DayDetailConfig(
                        accountKey = accountKey,
                        platform = s.platform,
                        dateSpanDays = spanDays(s.startDate, s.endDate),
                        selectedIIDs = iids
                    )
                )
            }

            setState { copy(isLoading = true) }

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
                    handleDayDetailError(result)
                }
            }
        }
    }

    private fun handleDayDetailError(result: NetworkState.Error<*>) {
        handleError(
            result,
            onNeedReLogin = { DayDetailEffect.NeedReLogin },
            onShowToast = { DayDetailEffect.ShowToast(it) }
        )
    }
}
