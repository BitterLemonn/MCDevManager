package com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.consts.CookiesExpiredException
import com.lemon.mcdevmanagermp.data.repository.ResourceRepositoryImpl
import com.lemon.mcdevmanagermp.domain.resource.ModAnalysisResult
import com.lemon.mcdevmanagermp.domain.resource.ModAnalysisUseCase
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import com.lemon.mcdevmanagermp.utils.Logger
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class ModAnalysisViewModel : BaseViewModel<ModAnalysisState, ModAnalysisAction, ModAnalysisEffect>(
    ModAnalysisState()
) {
    private val modAnalysisUseCase = ModAnalysisUseCase(
        resourceRepository = ResourceRepositoryImpl.INSTANCE
    )

    companion object {
        private const val TAG = "ModAnalysisVM"
    }

    override fun dispatch(action: ModAnalysisAction) {
        when (action) {
            is ModAnalysisAction.InitLoad -> initLoad(action.iid, action.platform)
            is ModAnalysisAction.LoadData -> loadAnalysisData(action.iid, action.platform)
            ModAnalysisAction.RefreshData -> {
                val s = state.value
                if (s.selectedIid.isNotEmpty()) {
                    loadAnalysisData(s.selectedIid, s.selectedPlatform)
                }
            }

            is ModAnalysisAction.SelectMetric -> setState { copy(metricType = action.type) }
            ModAnalysisAction.ToggleChartType -> setState {
                copy(chartType = if (chartType == ChartType.LINE) ChartType.COLUMN else ChartType.LINE)
            }

            is ModAnalysisAction.SelectResource -> {
                setState { copy(selectedIid = action.iid, isResourceSelectorExpanded = false) }
                loadAnalysisData(action.iid, state.value.selectedPlatform)
            }

            ModAnalysisAction.ToggleResourceSelector -> setState {
                copy(isResourceSelectorExpanded = !isResourceSelectorExpanded)
            }
        }
    }

    /**
     * 初始化：加载资源列表，若预设了 iid 则直接加载分析数据
     */
    private fun initLoad(iid: String, platform: String) {
        setState { copy(selectedPlatform = platform, isResListLoading = true) }
        viewModelScope.launch {
            when (val result = modAnalysisUseCase.getResourceList(platform)) {
                is NetworkState.Success -> {
                    setState { copy(resList = result.data ?: emptyList(), isResListLoading = false) }
                    // 若预设了 iid，直接加载分析数据
                    if (iid.isNotEmpty()) {
                        setState { copy(selectedIid = iid) }
                        loadAnalysisData(iid, platform)
                    }
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
     * 加载模组分析数据
     */
    private fun loadAnalysisData(iid: String, platform: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true) }

            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val (startDate, endDate) = modAnalysisUseCase.getAnalysisDateRange(today)

            when (val result = modAnalysisUseCase.getAnalysisData(platform, iid, startDate, endDate)) {
                is NetworkState.Success -> {
                    val data = result.data ?: ModAnalysisResult(emptyList(), SummaryMetrics())
                    if (data.analysisData.isNotEmpty()) {
                        val first = data.analysisData.first()
                        setState {
                            copy(
                                isLoading = false,
                                analysisData = data.analysisData,
                                modName = first.resName,
                                modScore = first.starAdjusted,
                                selectedIid = iid,
                                summaryMetrics = data.summaryMetrics,
                            )
                        }
                    } else {
                        setState { copy(isLoading = false, analysisData = emptyList()) }
                        sendEffect(ModAnalysisEffect.ShowToast("暂无分析数据"))
                    }
                }

                is NetworkState.Error -> {
                    Logger.e("$TAG: 获取分析数据失败: $result")
                    setState { copy(isLoading = false) }
                    handleError(result)
                }
            }
        }
    }

    private fun handleError(result: NetworkState.Error<*>) {
        if (result.e is CookiesExpiredException) {
            sendEffect(ModAnalysisEffect.NeedReLogin)
        } else {
            sendEffect(ModAnalysisEffect.ShowToast("请求失败: ${result.msg}"))
        }
    }
}
