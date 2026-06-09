package com.lemon.mcdevmanagermp.ui.pages.analyze.realtimeProfit

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.api.AnalyzeApi
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.consts.CookiesExpiredException
import com.lemon.mcdevmanagermp.data.repository.ResourceRepositoryImpl
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import com.lemon.mcdevmanagermp.utils.Logger
import com.lemon.mcdevmanagermp.utils.UnifiedExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class RealtimeProfitViewModel : BaseViewModel<RealtimeProfitState, RealtimeProfitAction, RealtimeProfitEffect>(
    RealtimeProfitState()
) {
    private val analyzeApi = AnalyzeApi.INSTANCE
    private val resourceRepository = ResourceRepositoryImpl.INSTANCE
    private val failedResources = mutableListOf<String>()

    companion object {
        private const val TAG = "RealtimeProfitVM"
    }

    override fun dispatch(action: RealtimeProfitAction) {
        when (action) {
            is RealtimeProfitAction.LoadData -> {
                setState { copy(checkDay = action.day) }
                loadData(action.day)
            }
            RealtimeProfitAction.RefreshData -> refreshData()
            is RealtimeProfitAction.UpdateCheckDay -> setState { copy(checkDay = action.day) }
            RealtimeProfitAction.ToggleDateSelector -> setState {
                copy(isDateSelectorVisible = !isDateSelectorVisible)
            }
            is RealtimeProfitAction.SelectPlatform -> {
                setState { copy(platform = action.platform) }
                loadData(state.value.checkDay)
            }
        }
    }

    /**
     * 初始化加载：设置当天日期并加载数据
     */
    fun initLoad() {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val todayStr = today.toString() // yyyy-MM-dd
        setState { copy(checkDay = todayStr) }
        loadData(todayStr)
    }

    private fun loadData(day: String) {
        viewModelScope.launch {
            setState {
                copy(
                    isLoading = true,
                    lastRequestTime = Clock.System.now().toEpochMilliseconds(),
                    profitMap = emptyMap(),
                    totalDiamond = 0,
                    totalPoints = 0
                )
            }
            failedResources.clear()

            // 1. 获取资源列表
            when (val resourceResult = resourceRepository.getAllResources(state.value.platform)) {
                is NetworkState.Success -> {
                    resourceResult.data?.let {
                        setState { copy(resList = it.item) }
                    }
                }
                is NetworkState.Error -> {
                    Logger.e("$TAG: 获取资源列表失败: ${resourceResult.msg}")
                    if (resourceResult.e is CookiesExpiredException) {
                        sendEffect(RealtimeProfitEffect.NeedReLogin)
                        setState { copy(isLoading = false) }
                        return@launch
                    } else {
                        sendEffect(RealtimeProfitEffect.ShowToast("获取资源列表失败: ${resourceResult.msg}"))
                        setState { copy(isLoading = false) }
                        return@launch
                    }
                }
            }

            // 2. 对每个资源获取实时收益
            val resList = state.value.resList
            if (resList.isEmpty()) {
                sendEffect(RealtimeProfitEffect.ShowToast("暂未查询到资源列表"))
                setState { copy(isLoading = false) }
                return@launch
            }

            val platform = state.value.platform
            val checkDay = state.value.checkDay
            val apiPlatform = if (platform == "pe") "pe" else "comp"

            // 计算时间范围：前一天 16:00:00 ~ 当天 15:59:59
            val dateDate = computePrevDay(checkDay)
            val beginTime = "${dateDate}T16:00:00.000Z"
            val endTime = "${checkDay}T15:59:59.999Z"

            for (res in resList) {
                when (
                    val result = UnifiedExceptionHandler.handleRequest {
                        analyzeApi.getOneResRealtimeIncome(
                            platform = apiPlatform,
                            iid = res.itemId,
                            beginTime = beginTime,
                            endTime = endTime
                        )
                    }
                ) {
                    is NetworkState.Success -> {
                        result.data?.let { data ->
                            // 排除无任何收益的模组
                            if (data.totalDiamonds == 0 && data.totalPoints == 0) return@let
                            val map = state.value.profitMap.toMutableMap()
                            map[res.itemId] = data
                            setState {
                                copy(
                                    profitMap = map,
                                    totalDiamond = totalDiamond + data.totalDiamonds,
                                    totalPoints = totalPoints + data.totalPoints
                                )
                            }
                        }
                    }
                    is NetworkState.Error -> {
                        Logger.e("$TAG: 获取资源${res.itemId}收益失败: ${result.msg}")
                        if (result.e is CookiesExpiredException) {
                            sendEffect(RealtimeProfitEffect.NeedReLogin)
                            setState { copy(isLoading = false) }
                            return@launch
                        } else {
                            failedResources.add(res.itemName)
                        }
                    }
                }
            }

            setState { copy(isLoading = false) }

            if (failedResources.isNotEmpty()) {
                sendEffect(
                    RealtimeProfitEffect.ShowToast(
                        "以下资源收益获取失败: ${failedResources.joinToString(", ")}"
                    )
                )
                failedResources.clear()
            }
        }
    }

    private fun refreshData() {
        loadData(state.value.checkDay)
    }

    /**
     * 计算前一天日期（yyyy-MM-dd）
     */
    private fun computePrevDay(dateStr: String): String {
        val date = LocalDate.parse(dateStr)
        return date.minus(1, DateTimeUnit.DAY).toString()
    }
}
