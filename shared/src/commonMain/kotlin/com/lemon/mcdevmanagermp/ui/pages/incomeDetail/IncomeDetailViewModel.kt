package com.lemon.mcdevmanagermp.ui.pages.incomeDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.repository.AnalyzeRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.ResourceRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.UserRepositoryImpl
import com.lemon.mcdevmanagermp.data.vo.netease.user.UserInfoVO
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
    val errorMessage: String? = null,
    /** 企业开发者自行开票缴税，页面不展示按个人劳务报酬预估的税后收益 */
    val isEnterprise: Boolean = false
)

/**
 * 收益详情 ViewModel：按月加载并缓存收益预估数据，同时确定账号是否为企业开发者。
 *
 * @param initialData 首页已计算好的该月收益，非空时直接复用避免重复请求
 * @param initialUserInfo 首页已加载的用户信息，为空时会单独请求以判定企业开发者
 */
internal class IncomeDetailViewModel(
    initialMonth: ProfitMonth,
    initialData: ProfitData?,
    initialUserInfo: UserInfoVO?
) : ViewModel() {
    private val userRepository = UserRepositoryImpl.INSTANCE
    private val mainUseCase = MainUseCase(
        userRepository = userRepository,
        analyzeRepository = AnalyzeRepositoryImpl.INSTANCE,
        getResourceListUseCase = GetResourceListUseCase(ResourceRepositoryImpl.INSTANCE)
    )
    private val cache = mutableMapOf<ProfitMonth, ProfitData>()
    private val _state = MutableStateFlow(
        IncomeDetailState(
            selectedMonth = initialMonth,
            profitData = initialData ?: ProfitData(),
            isLoading = initialData == null,
            isEnterprise = initialUserInfo?.isEnterprise == true
        )
    )
    val state = _state.asStateFlow()

    init {
        if (initialData != null) cache[initialMonth] = initialData else load(initialMonth)
        if (initialUserInfo == null) loadEnterpriseFlag()
    }

    /**
     * 首页用户信息未就绪时单独拉取 users/me 判定企业开发者。
     * 失败时保持默认（按个人开发者展示扣税预估），不影响收益数据本身的加载。
     */
    private fun loadEnterpriseFlag() {
        viewModelScope.launch {
            when (val result = userRepository.getUserInfo()) {
                is NetworkState.Success -> _state.update {
                    it.copy(isEnterprise = result.data?.isEnterprise == true)
                }

                is NetworkState.Error -> Logger.e("收益详情获取用户信息失败: ${result.msg}", result.e)
            }
        }
    }

    fun selectMonth(month: ProfitMonth) {
        if (month == _state.value.selectedMonth) return
        load(month)
    }

    fun retry() = load(_state.value.selectedMonth, force = true)

    private fun load(month: ProfitMonth, force: Boolean = false) {
        val cached = if (force) null else cache[month]
        // 用 copy 而非重建 state，保留与月份无关的 isEnterprise
        if (cached != null) {
            _state.update {
                it.copy(selectedMonth = month, profitData = cached, isLoading = false, errorMessage = null)
            }
            return
        }

        _state.update {
            it.copy(selectedMonth = month, profitData = ProfitData(), isLoading = true, errorMessage = null)
        }
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
