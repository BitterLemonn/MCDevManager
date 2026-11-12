package com.lemon.mcdevmanagermp.ui.pages.incomeDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.common.AppContext
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.db.entity.EcoFeeRecordEntity
import com.lemon.mcdevmanagermp.data.repository.AnalyzeRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.EcoFeeRecordRepository
import com.lemon.mcdevmanagermp.data.repository.IncomeRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.ResourceRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.UserRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.monthKey
import com.lemon.mcdevmanagermp.data.repository.normalizeMonthKey
import com.lemon.mcdevmanagermp.data.vo.netease.income.IncomeVO
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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

internal data class IncomeDetailState(
    val selectedMonth: ProfitMonth,
    val profitData: ProfitData = ProfitData(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    /** 企业开发者自行开票缴税，页面不展示按个人劳务报酬预估的税后收益 */
    val isEnterprise: Boolean = false,
    /** 当前月份的结算单；未出账时为 null */
    val settlement: IncomeVO? = null,
    val isSettlementLoading: Boolean = true,
    val settlementError: String? = null,
    /** 已反推出费率的历史生态费记录，按月份倒序 */
    val ecoFeeHistory: List<EcoFeeRecordEntity> = emptyList()
)

/**
 * 收益详情 ViewModel：按月加载并缓存收益预估数据，同时确定账号是否为企业开发者。
 * 已截止月份优先使用周期截止后保存的预估快照，结算单出账后以快照反推生态费。
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
    private val incomeRepository = IncomeRepositoryImpl.INSTANCE
    private val ecoFeeRecordRepository = EcoFeeRecordRepository.INSTANCE
    private val mainUseCase = MainUseCase(
        userRepository = userRepository,
        analyzeRepository = AnalyzeRepositoryImpl.INSTANCE,
        getResourceListUseCase = GetResourceListUseCase(ResourceRepositoryImpl.INSTANCE)
    )
    private val cache = mutableMapOf<ProfitMonth, ProfitData>()

    /** pe 平台全量结算列表缓存，切月份时本地匹配，避免重复请求 */
    private var settlementCache: List<IncomeVO>? = null

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
        if (initialData != null) {
            cache[initialMonth] = initialData
            trySaveEcoFeeRecord()
        } else {
            load(initialMonth)
        }
        if (initialUserInfo == null) loadEnterpriseFlag()
        loadSettlement()
        loadEcoFeeHistory()
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
        // 结算列表已缓存时仅本地匹配，不重复请求
        if (settlementCache != null) matchSettlement()
    }

    fun retry() {
        // 快照可能与登录时的最新数据不一致，重试时跳过快照强制走网络
        load(_state.value.selectedMonth, force = true)
        loadSettlement(force = true)
    }

    /**
     * 拉取 pe 平台结算列表并匹配当前月份的结算单。
     * 结算单未出账（如本月、下月）时匹配结果为 null，由界面提示。
     */
    private fun loadSettlement(force: Boolean = false) {
        val cached = if (force) null else settlementCache
        if (cached != null) {
            matchSettlement()
            return
        }

        _state.update { it.copy(isSettlementLoading = true, settlementError = null) }
        viewModelScope.launch {
            when (val result = incomeRepository.getIncome("pe")) {
                is NetworkState.Success -> {
                    settlementCache = result.data?.incomes.orEmpty()
                    matchSettlement()
                }

                is NetworkState.Error -> {
                    Logger.e("收益详情获取结算数据失败: ${result.msg}", result.e)
                    _state.update {
                        it.copy(
                            isSettlementLoading = false,
                            settlementError = "结算数据获取失败，请重试",
                            settlement = null
                        )
                    }
                }
            }
        }
    }

    private fun matchSettlement() {
        val list = settlementCache ?: return
        val month = _state.value.selectedMonth
        _state.update {
            it.copy(
                settlement = list.firstOrNull { income -> normalizeMonthKey(income.dataMonth) == monthKey(month) },
                isSettlementLoading = false,
                settlementError = null
            )
        }
        trySaveEcoFeeRecord()
    }

    private fun load(month: ProfitMonth, force: Boolean = false) {
        val cached = if (force) null else cache[month]
        // 用 copy 而非重建 state，保留与月份无关的 isEnterprise
        if (cached != null) {
            _state.update {
                it.copy(selectedMonth = month, profitData = cached, isLoading = false, errorMessage = null)
            }
            trySaveEcoFeeRecord()
            return
        }

        _state.update {
            it.copy(selectedMonth = month, profitData = ProfitData(), isLoading = true, errorMessage = null)
        }
        viewModelScope.launch {
            try {
                val accountKey = AppContext.userInfo?.nickname
                // 已截止月份优先使用周期截止后保存的快照，出账后反推以其为准
                val snapshot = if (accountKey != null && !force && isMonthClosed(month)) {
                    ecoFeeRecordRepository.getSnapshot(accountKey, month)
                } else {
                    null
                }
                val data = snapshot ?: mainUseCase.computeMonthProfit(month.year, month.month)
                cache[month] = data
                if (_state.value.selectedMonth == month) {
                    _state.update { it.copy(profitData = data, isLoading = false) }
                }
                // 网络拉取的已截止月份保存快照；快照来源无需重复保存
                if (snapshot == null && accountKey != null && isMonthClosed(month)) {
                    ecoFeeRecordRepository.saveSnapshot(accountKey, month, data)
                }
                trySaveEcoFeeRecord()
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

    /**
     * 该月流水与结算单都就绪且反推有效时，把结果写入本地数据库，形成历史生态费记录。
     * 同月重复触发时按主键覆盖，保留最新一次反推结果。
     */
    private fun trySaveEcoFeeRecord() {
        val settlement = _state.value.settlement ?: return
        val accountKey = AppContext.userInfo?.nickname ?: return
        val month = _state.value.selectedMonth
        val profitData = _state.value.profitData
        if (!profitData.isFlowComplete) return
        viewModelScope.launch {
            try {
                ecoFeeRecordRepository.saveReverseResult(accountKey, month, profitData, settlement)
                loadEcoFeeHistory()
            } catch (e: Exception) {
                Logger.e("保存生态费记录失败", e)
            }
        }
    }

    private fun loadEcoFeeHistory() {
        val accountKey = AppContext.userInfo?.nickname ?: return
        viewModelScope.launch {
            try {
                val history = ecoFeeRecordRepository.getRated(accountKey)
                _state.update { it.copy(ecoFeeHistory = history) }
            } catch (e: Exception) {
                Logger.e("加载生态费历史失败", e)
            }
        }
    }

    /** 计算周期是否已截止：该月已完整结束（当前自然月之前的月份） */
    private fun isMonthClosed(month: ProfitMonth): Boolean {
        val now = Clock.System.now().toLocalDateTime(TimeZone.of("Asia/Shanghai"))
        return month < ProfitMonth(now.year, now.month.number)
    }
}
