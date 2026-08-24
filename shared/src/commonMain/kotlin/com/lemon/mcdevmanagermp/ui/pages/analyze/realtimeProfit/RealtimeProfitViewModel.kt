package com.lemon.mcdevmanagermp.ui.pages.analyze.realtimeProfit

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.consts.CookiesExpiredException
import com.lemon.mcdevmanagermp.data.repository.AnalyzeRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.ResourceRepositoryImpl
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import com.lemon.mcdevmanagermp.domain.analyze.RealtimeProfitUseCase
import com.lemon.mcdevmanagermp.domain.analyze.mergeRealtimeIncome
import com.lemon.mcdevmanagermp.domain.resource.GetResourceListUseCase
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import com.lemon.mcdevmanagermp.utils.Logger
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class RealtimeProfitViewModel :
    BaseViewModel<RealtimeProfitState, RealtimeProfitAction, RealtimeProfitEffect>(
        RealtimeProfitState()
    ) {
    private val realtimeProfitUseCase = RealtimeProfitUseCase(
        analyzeRepository = AnalyzeRepositoryImpl.INSTANCE
    )
    private val getResourceListUseCase = GetResourceListUseCase(ResourceRepositoryImpl.INSTANCE)
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

            // 1. 获取普通资源与联机大厅商业化作品
            val resources = when (val resourceResult =
                getResourceListUseCase(state.value.platform, onlineOnly = true)) {
                is NetworkState.Success -> {
                    resourceResult.data ?: emptyList()
                }

                is NetworkState.Error -> {
                    Logger.e("$TAG: 获取资源列表失败: ${resourceResult.msg}")
                    setState { copy(isLoading = false) }
                    handleError(
                        resourceResult,
                        onNeedReLogin = { RealtimeProfitEffect.NeedReLogin },
                        onShowToast = { RealtimeProfitEffect.ShowToast("获取资源列表失败: ${resourceResult.msg}") }
                    )
                    return@launch
                }
            }
            val lobbyResources = if (state.value.platform == "pe") {
                when (val result = realtimeProfitUseCase.getLobbyIncomeResources()) {
                    is NetworkState.Success -> result.data?.items.orEmpty().map {
                        ResourceData(itemId = it.itemId, itemName = it.itemName)
                    }

                    is NetworkState.Error -> {
                        Logger.e("$TAG: 获取联机大厅作品列表失败: ${result.msg}")
                        if (result.e is CookiesExpiredException) {
                            sendEffect(RealtimeProfitEffect.NeedReLogin)
                            setState { copy(isLoading = false) }
                            return@launch
                        }
                        failedResources.add("联机大厅作品列表")
                        emptyList()
                    }
                }
            } else {
                emptyList()
            }
            setState { copy(resList = (resources + lobbyResources).distinctBy { it.itemId }) }

            // ponytail: 同作品普通销售与大厅内购合并；需分账展示时在状态中保留收益来源。
            val incomeQueries = resources.map { it to false } + lobbyResources.map { it to true }
            if (incomeQueries.isEmpty()) {
                sendEffect(RealtimeProfitEffect.ShowToast("暂未查询到资源列表"))
                setState { copy(isLoading = false) }
                return@launch
            }

            val platform = state.value.platform
            val (beginTime, endTime) = realtimeProfitUseCase.computeTimeRange(state.value.checkDay)

            for ((res, isLobbyIncome) in incomeQueries) {
                val result = if (isLobbyIncome) {
                    realtimeProfitUseCase.getLobbyRealtimeIncome(
                        iid = res.itemId, beginTime = beginTime, endTime = endTime
                    )
                } else {
                    realtimeProfitUseCase.getRealtimeIncome(
                        platform = platform,
                        iid = res.itemId,
                        beginTime = beginTime,
                        endTime = endTime
                    )
                }
                when (result) {
                    is NetworkState.Success -> {
                        result.data?.let { data ->
                            if (data.totalDiamonds == 0 && data.totalPoints == 0) return@let
                            val map = state.value.profitMap.toMutableMap()
                            map[res.itemId] = mergeRealtimeIncome(map[res.itemId], data)
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
                        val incomeType = if (isLobbyIncome) "联机大厅收益" else "普通收益"
                        Logger.e("$TAG: 获取资源${res.itemId}${incomeType}失败: ${result.msg}")
                        if (result.e is CookiesExpiredException) {
                            sendEffect(RealtimeProfitEffect.NeedReLogin)
                            setState { copy(isLoading = false) }
                            return@launch
                        } else {
                            failedResources.add("${res.itemName}（$incomeType）")
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
}
