package com.lemon.mcdevmanagermp.ui.pages.incomeDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.repository.AnalyzeRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.ResourceRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.UserRepositoryImpl
import com.lemon.mcdevmanagermp.domain.main.MainUseCase
import com.lemon.mcdevmanagermp.domain.main.ProfitMonth
import com.lemon.mcdevmanagermp.domain.resource.GetResourceListUseCase
import com.lemon.mcdevmanagermp.utils.Logger
import com.lemon.mcdevmanagermp.utils.ProfitData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class IncomeDetailState(
    val selectedMonth: ProfitMonth,
    val profitData: ProfitData = ProfitData(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

internal class IncomeDetailViewModel(
    initialMonth: ProfitMonth,
    initialData: ProfitData?
) : ViewModel() {
    private val mainUseCase = MainUseCase(
        userRepository = UserRepositoryImpl.INSTANCE,
        analyzeRepository = AnalyzeRepositoryImpl.INSTANCE,
        getResourceListUseCase = GetResourceListUseCase(ResourceRepositoryImpl.INSTANCE)
    )
    private val cache = mutableMapOf<ProfitMonth, ProfitData>()
    private val _state = MutableStateFlow(
        IncomeDetailState(
            selectedMonth = initialMonth,
            profitData = initialData ?: ProfitData(),
            isLoading = initialData == null
        )
    )
    val state = _state.asStateFlow()

    init {
        if (initialData != null) cache[initialMonth] = initialData else load(initialMonth)
    }

    fun selectMonth(month: ProfitMonth) {
        if (month == _state.value.selectedMonth) return
        load(month)
    }

    fun retry() = load(_state.value.selectedMonth, force = true)

    private fun load(month: ProfitMonth, force: Boolean = false) {
        val cached = if (force) null else cache[month]
        if (cached != null) {
            _state.value = IncomeDetailState(selectedMonth = month, profitData = cached)
            return
        }

        _state.value = IncomeDetailState(selectedMonth = month, isLoading = true)
        viewModelScope.launch {
            try {
                val data = mainUseCase.computeMonthProfit(month.year, month.month)
                cache[month] = data
                if (_state.value.selectedMonth == month) {
                    _state.update { it.copy(profitData = data, isLoading = false) }
                }
            } catch (e: Exception) {
                Logger.e("收益详情加载失败", e)
                if (_state.value.selectedMonth == month) {
                    _state.update {
                        it.copy(isLoading = false, errorMessage = "收益加载失败，请重试")
                    }
                }
            }
        }
    }
}
