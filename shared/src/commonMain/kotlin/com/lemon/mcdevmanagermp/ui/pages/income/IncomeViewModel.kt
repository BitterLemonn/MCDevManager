package com.lemon.mcdevmanagermp.ui.pages.income

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.api.IncomeApi
import com.lemon.mcdevmanagermp.data.api.InfoApi
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.dto.netease.income.ApplyIncomeDTO
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import com.lemon.mcdevmanagermp.utils.UnifiedExceptionHandler
import kotlinx.coroutines.launch

class IncomeViewModel : BaseViewModel<IncomeState, IncomeAction, IncomeEffect>(IncomeState()) {

    private val api = IncomeApi.INSTANCE
    private val infoApi = InfoApi.INSTANCE

    override fun dispatch(action: IncomeAction) {
        when (action) {
            IncomeAction.LoadData -> loadData()
            is IncomeAction.SelectPlatform -> setState { copy(selectedPlatform = action.platform) }
            is IncomeAction.GetApplyDetail -> getApplyDetail(action.incomeId)
            is IncomeAction.ApplyIncome -> applyIncome(action.incomeIds)
            IncomeAction.DismissApplyDetail -> setState { copy(applyDetailList = emptyList()) }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val peResult = UnifiedExceptionHandler.handleRequest { api.getIncome("pe") }
            val pcResult = UnifiedExceptionHandler.handleRequest { api.getIncome("pc") }
            val userInfoResult = UnifiedExceptionHandler.handleRequest { infoApi.getUserInfo() }
            setState { copy(isLoading = false) }

            when (peResult) {
                is NetworkState.Success -> {
                    peResult.data?.let {
                        setState { copy(peList = it.incomes.sortedByDescending { v -> v.dataMonth }) }
                    }
                }
                is NetworkState.Error -> sendEffect(IncomeEffect.ShowToast("获取PE收益失败: ${peResult.msg}"))
            }
            when (pcResult) {
                is NetworkState.Success -> {
                    pcResult.data?.let {
                        setState { copy(pcList = it.incomes.sortedByDescending { v -> v.dataMonth }) }
                    }
                }
                is NetworkState.Error -> sendEffect(IncomeEffect.ShowToast("获取PC收益失败: ${pcResult.msg}"))
            }
            when (userInfoResult) {
                is NetworkState.Success -> {
                    userInfoResult.data?.let {
                        setState { copy(unExtractedIncome = it.unExtractIncome) }
                    }
                }
                is NetworkState.Error -> { }
            }
        }
    }

    private fun getApplyDetail(incomeId: String) {
        val currentList = state.value.currentList
        val target = currentList.find { it.id == incomeId } ?: return
        val availableMonths = target.availableDetail.map { it.dataMonth }
        if (availableMonths.isEmpty()) {
            sendEffect(IncomeEffect.ShowToast("无可用结算月份"))
            return
        }
        val ids = currentList.filter { it.dataMonth in availableMonths }.map { it.id }

        viewModelScope.launch {
            setState { copy(isLoading = true, applyDetailList = emptyList()) }
            for (id in ids) {
                when (val result = UnifiedExceptionHandler.handleRequest { api.getApplyDetail(id) }) {
                    is NetworkState.Success -> {
                        result.data?.let { setState { copy(applyDetailList = applyDetailList + it) } }
                    }
                    is NetworkState.Error -> {
                        sendEffect(IncomeEffect.ShowToast("获取结算详情失败: ${result.msg}"))
                    }
                }
            }
            setState { copy(isLoading = false) }
        }
    }

    private fun applyIncome(incomeIds: List<String>) {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            when (val result = UnifiedExceptionHandler.handleRequest { api.applyIncome(ApplyIncomeDTO(incomeIds)) }) {
                is NetworkState.Success -> {
                    setState { copy(applyDetailList = emptyList()) }
                    sendEffect(IncomeEffect.ShowToast("申请结算成功"))
                    loadData()
                }
                is NetworkState.Error -> {
                    sendEffect(IncomeEffect.ShowToast("申请结算失败: ${result.msg}"))
                    setState { copy(isLoading = false) }
                }
            }
        }
    }
}
