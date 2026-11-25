package com.lemon.mcdevmanagermp.ui.pages.income

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.common.AppContext
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.repository.EcoFeeRecordRepository
import com.lemon.mcdevmanagermp.data.repository.IncomeRepositoryImpl
import com.lemon.mcdevmanagermp.domain.income.IncomeUseCase
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import com.lemon.mcdevmanagermp.utils.Logger
import kotlinx.coroutines.launch

class IncomeViewModel : BaseViewModel<IncomeState, IncomeAction, IncomeEffect>(IncomeState()) {

    private val incomeUseCase = IncomeUseCase(
        incomeRepository = IncomeRepositoryImpl.INSTANCE
    )

    override fun dispatch(action: IncomeAction) {
        when (action) {
            IncomeAction.LoadData -> loadData()
            is IncomeAction.SelectPlatform -> setState {
                copy(
                    selectedPlatform = action.platform,
                    statusFilter = IncomeStatusFilter.ALL,
                    selectedIncomeId = null
                )
            }

            is IncomeAction.SelectStatusFilter -> setState {
                copy(statusFilter = action.filter)
            }

            is IncomeAction.SelectIncome -> setState {
                copy(selectedIncomeId = action.incomeId)
            }

            is IncomeAction.GetApplyDetail -> getApplyDetail(action.incomeId)
            is IncomeAction.ApplyIncome -> applyIncome(action.incomeIds)
            IncomeAction.DismissApplyDetail -> {
                if (!state.value.isApplyingIncome) {
                    setState { copy(applyDetailList = emptyList()) }
                }
            }
        }
    }

    private fun loadData() {
        if (state.value.isLoading) return
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val result = incomeUseCase.loadAllData()
            setState {
                copy(
                    isLoading = false,
                    peList = result.peList,
                    pcList = result.pcList,
                    unExtractedIncome = result.unExtractedIncome ?: unExtractedIncome
                )
            }

            if (result.peError != null) {
                sendEffect(IncomeEffect.ShowToast("获取PE收益失败: ${result.peError}"))
            }
            if (result.pcError != null) {
                sendEffect(IncomeEffect.ShowToast("获取PC收益失败: ${result.pcError}"))
            }

            loadEcoFeeRates()
        }
    }

    /** 加载本地已反推的历史生态费率，展示在对应月份的结算条目上 */
    private fun loadEcoFeeRates() {
        val accountKey = AppContext.userInfo?.nickname ?: return
        viewModelScope.launch {
            try {
                val rates = EcoFeeRecordRepository.INSTANCE.getRated(accountKey)
                    .mapNotNull { record -> record.ecoFeeRate?.let { record.dataMonth to it } }
                    .toMap()
                setState { copy(ecoFeeRates = rates) }
            } catch (e: Exception) {
                Logger.e("加载历史生态费率失败", e)
            }
        }
    }

    private fun getApplyDetail(incomeId: String) {
        if (state.value.isLoadingApplyDetail) return
        val currentList = state.value.currentList
        val target = currentList.find { it.id == incomeId } ?: return
        val availableMonths = target.availableDetail.map { it.dataMonth }
        if (availableMonths.isEmpty()) {
            sendEffect(IncomeEffect.ShowToast("无可用结算月份"))
            return
        }
        val ids = currentList.filter { it.dataMonth in availableMonths }.map { it.id }

        viewModelScope.launch {
            setState { copy(isLoadingApplyDetail = true, applyDetailList = emptyList()) }
            val details = mutableListOf<com.lemon.mcdevmanagermp.data.vo.netease.income.ApplyIncomeDetailVO>()
            for (id in ids) {
                when (val result = incomeUseCase.getApplyDetail(id)) {
                    is NetworkState.Success -> {
                        result.data?.let { details.add(it) }
                    }

                    is NetworkState.Error -> {
                        sendEffect(IncomeEffect.ShowToast("获取结算详情失败: ${result.msg}"))
                    }
                }
            }
            setState {
                copy(
                    isLoadingApplyDetail = false,
                    applyDetailList = details
                )
            }
        }
    }

    private fun applyIncome(incomeIds: List<String>) {
        if (state.value.isApplyingIncome) return
        viewModelScope.launch {
            setState { copy(isApplyingIncome = true) }
            when (val result = incomeUseCase.applyIncome(incomeIds)) {
                is NetworkState.Success -> {
                    setState { copy(isApplyingIncome = false, applyDetailList = emptyList()) }
                    sendEffect(IncomeEffect.ShowToast("申请结算成功"))
                    loadData()
                }

                is NetworkState.Error -> {
                    sendEffect(IncomeEffect.ShowToast("申请结算失败: ${result.msg}"))
                    setState { copy(isApplyingIncome = false) }
                }
            }
        }
    }
}
