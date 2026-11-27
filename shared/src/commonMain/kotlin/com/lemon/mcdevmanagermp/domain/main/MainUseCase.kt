package com.lemon.mcdevmanagermp.domain.main

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.consts.CookiesExpiredException
import com.lemon.mcdevmanagermp.data.consts.LoginException
import com.lemon.mcdevmanagermp.data.consts.enums.PriceTypeEnum
import com.lemon.mcdevmanagermp.data.repository.EcoFeeRecordRepository
import com.lemon.mcdevmanagermp.data.repository.isEcoFeeReverseSupported
import com.lemon.mcdevmanagermp.data.repository.parseMonthKey
import com.lemon.mcdevmanagermp.data.vo.netease.analyze.ResAnalyzeData
import com.lemon.mcdevmanagermp.data.vo.netease.income.IncomeVO
import com.lemon.mcdevmanagermp.data.vo.netease.user.LevelInfoVO
import com.lemon.mcdevmanagermp.data.vo.netease.user.OverviewVO
import com.lemon.mcdevmanagermp.data.vo.netease.user.UserInfoVO
import com.lemon.mcdevmanagermp.domain.analyze.AnalyzeRepository
import com.lemon.mcdevmanagermp.domain.analyze.LobbyGoodsByOwner
import com.lemon.mcdevmanagermp.domain.analyze.LobbyGoodsUseCase
import com.lemon.mcdevmanagermp.domain.resource.GetResourceListUseCase
import com.lemon.mcdevmanagermp.domain.user.UserRepository
import com.lemon.mcdevmanagermp.utils.ProfitData
import com.lemon.mcdevmanagermp.utils.calculateProfit
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus

data class MainDashboardData(
    val userInfo: NetworkState<UserInfoVO>,
    val overview: NetworkState<OverviewVO>,
    val levelInfo: NetworkState<LevelInfoVO>
)

data class ProfitResult(
    val thisMonth: ProfitData,
    val lastMonth: ProfitData? = null,
    val nextMonth: ProfitData? = null
)

internal data class ProfitMonth(val year: Int, val month: Int) : Comparable<ProfitMonth> {
    override fun compareTo(other: ProfitMonth): Int =
        compareValuesBy(this, other, ProfitMonth::year, ProfitMonth::month)

    fun shift(months: Int): ProfitMonth {
        val firstDay = LocalDate(year, month, 1)
        val shifted = if (months >= 0) {
            firstDay.plus(months, DateTimeUnit.MONTH)
        } else {
            firstDay.minus(-months, DateTimeUnit.MONTH)
        }
        return ProfitMonth(shifted.year, shifted.month.number)
    }
}

internal data class ProfitMonthWindow(
    val current: ProfitMonth,
    val showLastMonth: Boolean,
    val showNextMonth: Boolean
)

/** 计算周期是否已截止：该月已完整结束（当前自然月之前的月份） */
internal fun isMonthClosed(month: ProfitMonth, today: LocalDate): Boolean =
    month < ProfitMonth(today.year, today.month.number)

internal fun profitMonthWindow(today: LocalDate): ProfitMonthWindow {
    val current = ProfitMonth(today.year, today.month.number)
    val nextMonth = current.shift(1)
    val nextMonthStart = LocalDate(nextMonth.year, nextMonth.month, 1)
    return ProfitMonthWindow(
        current = current,
        showLastMonth = today.day <= 15,
        showNextMonth = today >= nextMonthStart.minus(9, DateTimeUnit.DAY)
    )
}

class MainUseCase internal constructor(
    private val userRepository: UserRepository,
    private val analyzeRepository: AnalyzeRepository,
    private val getResourceListUseCase: GetResourceListUseCase,
    private val lobbyGoodsUseCase: LobbyGoodsUseCase = LobbyGoodsUseCase(analyzeRepository),
    private val ecoFeeRecordRepository: EcoFeeRecordRepository = EcoFeeRecordRepository.INSTANCE
) {
    suspend fun loadDashboard(): MainDashboardData = coroutineScope {
        val userInfoDeferred = async { userRepository.getUserInfo() }
        val overviewDeferred = async { userRepository.getOverview() }
        val levelDeferred = async { userRepository.getLevelInfo() }

        MainDashboardData(
            userInfo = userInfoDeferred.await(),
            overview = overviewDeferred.await(),
            levelInfo = levelDeferred.await()
        )
    }

    suspend fun computeProfit(
        year: Int,
        month: Int,
        includeLastMonth: Boolean,
        includeNextMonth: Boolean
    ): ProfitResult = coroutineScope {
        val currentMonth = ProfitMonth(year, month)
        val thisMonth = async { computeMonthProfit(currentMonth.year, currentMonth.month) }
        val lastMonth = if (includeLastMonth) {
            val target = currentMonth.shift(-1)
            async { computeMonthProfit(target.year, target.month) }
        } else null
        val nextMonth = if (includeNextMonth) {
            val target = currentMonth.shift(1)
            async { computeMonthProfit(target.year, target.month) }
        } else null

        ProfitResult(
            thisMonth = thisMonth.await(),
            lastMonth = lastMonth?.await(),
            nextMonth = nextMonth?.await()
        )
    }

    /**
     * 计算某月收益预估。
     * 任一流水请求失败时仍返回已拿到部分的估算（首页速算可用），但 [ProfitData.isFlowComplete] 置为 false。
     */
    suspend fun computeMonthProfit(year: Int, month: Int): ProfitData {
        val flow = getOneMonthComponentDiamonds(year, month)
        return calculateProfit(flow.diamonds).copy(
            emeraldModDiamonds = flow.emeraldModDiamonds,
            isFlowComplete = flow.isComplete
        )
    }

    /**
     * 计算已出账月份的生态费：对给定结算单逐月反推实际生态费率并写入本地记录。
     *
     * 已反推出费率的月份跳过，避免重复计算与重复请求。已截止月份优先用周期截止时保存的预估快照
     * 反推（出账时平台流水可能已调整）；无快照时回落到实时计算。流水不完整或反推结果无效的月份不写入。
     *
     * @param accountKey 账号标识（nickname）
     * @param settlements 平台结算单列表
     * @return 本次新写入的记录
     */
    internal suspend fun syncEcoFeeRecords(
        accountKey: String,
        settlements: List<IncomeVO>,
        today: LocalDate
    ): List<ProfitMonth> {
        if (accountKey.isBlank()) return emptyList()
        val pending = settlements.mapNotNull { settlement ->
            val month = parseMonthKey(settlement.dataMonth) ?: return@mapNotNull null
            if (!isEcoFeeReverseSupported(month)) return@mapNotNull null
            if (ecoFeeRecordRepository.matchesSettlement(accountKey, month, settlement)) null else month to settlement
        }
        if (pending.isEmpty()) return emptyList()

        // 逐月串行：同一账号写同一张表，且每月的流水请求本身已是并发发起的
        return pending.mapNotNull { (month, settlement) ->
            val cached = ecoFeeRecordRepository.getSnapshot(accountKey, month)
            val profitData = cached ?: computeMonthProfit(month.year, month.month)
            // 已截止月份实时计算后补存快照，后续无需重复请求
            if (cached == null && isMonthClosed(month, today)) {
                ecoFeeRecordRepository.saveSnapshot(accountKey, month, profitData)
            }
            if (ecoFeeRecordRepository.saveReverseResult(accountKey, month, profitData, settlement)) {
                month
            } else {
                null
            }
        }
    }

    /**
     * 汇总某月各作品流水（钻石）。
     * 普通作品流水、钻石定价模组的商品流水任一请求失败，失败部分按 0 计入并标记为不完整，
     * 由调用方决定是否接受不完整数据；绿宝石定价模组的商品流水不参与计算，其失败不影响完整性。
     */
    private suspend fun getOneMonthComponentDiamonds(year: Int, month: Int): MonthFlow =
        coroutineScope {
            val normalResources = async {
                getResourceListUseCase("pe", onlineOnly = true, excludePrerequisites = true)
            }
            // 商品化模组及其商品的获取（含模组列表请求）交由 lobbyGoodsUseCase 统一发起
            val lobbyGoods = async { lobbyGoodsUseCase.getGoodsByOwner() }
            val resourcesResult = normalResources.await()
            val resList = when (resourcesResult) {
                is NetworkState.Success -> resourcesResult.data ?: emptyList()
                is NetworkState.Error -> emptyList()
            }
            val lobbyGoodsResult = lobbyGoods.await()
            val goodsByOwner = when (lobbyGoodsResult) {
                is NetworkState.Success -> lobbyGoodsResult.data.orEmpty()
                is NetworkState.Error -> emptyList()
            }

            val dateRange = monthDateRange(year, month)

            // 作品名 → 流水；null 表示该作品的流水请求失败
            val normalResults = resList.map { res ->
                async {
                    val result = analyzeRepository.getDayDetail(
                        platform = "pe",
                        category = "pe",
                        startDate = dateRange.first,
                        endDate = dateRange.second,
                        itemListStr = res.itemId
                    )
                    res.itemName to when (result) {
                        is NetworkState.Success ->
                            result.data?.data?.sumOf { it.diamond * (1 - it.refundRate) } ?: 0.0

                        is NetworkState.Error -> null
                    }
                }
            }.awaitAll()
            val normalDiamonds = normalResults.associate { (name, diamonds) -> name to (diamonds ?: 0.0) }

            // 商品化模组按当前定价分流：平台允许旧绿宝石模组改为钻石模组，已改为钻石定价的模组
            // 其商品流水为真钻石口径、参与计算；仍为绿宝石定价的模组，接口返回的钻石数是
            // 绿宝石销售按面额折算的结果，与实际结算不符，不参与计算。
            // 资源列表中查不到的模组无法确认定价，按绿宝石口径处理（宁少算不错算）。
            val resById = resList.associateBy { it.itemId }
            val (diamondOwners, emeraldOwners) = goodsByOwner.partition { (owner, _) ->
                resById[owner.itemId]?.getPriceType() == PriceTypeEnum.DIAMOND
            }
            val diamondGoods = async { lobbyItemDiamonds(diamondOwners, dateRange) }
            val emeraldGoods = async { lobbyItemDiamonds(emeraldOwners, dateRange) }
            val diamondModDiamonds = diamondGoods.await()

            MonthFlow(
                diamonds = mergeProfitDiamonds(normalDiamonds, diamondModDiamonds.orEmpty()),
                emeraldModDiamonds = emeraldGoods.await().orEmpty(),
                isComplete = resourcesResult is NetworkState.Success &&
                        lobbyGoodsResult is NetworkState.Success &&
                        normalResults.none { (_, diamonds) -> diamonds == null } &&
                        diamondOwners.none { it.goods == null } &&
                        diamondModDiamonds != null
            )
        }

    /**
     * 计算商品化模组流水：模组下的各商品销售钻石累加回模组名。
     * @return 模组名 → 钻石；商品流水请求失败时返回 null（无商品时为空表，属于完整数据）
     */
    private suspend fun lobbyItemDiamonds(
        goodsByOwner: List<LobbyGoodsByOwner>,
        dateRange: Pair<String, String>
    ): Map<String, Double>? {
        val goodsToOwner = goodsByOwner
            .flatMap { (owner, goods) -> goods.orEmpty().map { it.goodsId to owner.itemName } }
            .toMap()
        if (goodsToOwner.isEmpty()) return emptyMap()

        // 一次查询同一口径下全部商品的日详情（item_list_str 支持逗号分隔）
        val result = analyzeRepository.getDayDetail(
            platform = "pe",
            category = "pe",
            startDate = dateRange.first,
            endDate = dateRange.second,
            itemListStr = goodsToOwner.keys.joinToString(","),
            isLobby = true
        )
        return when (result) {
            is NetworkState.Success ->
                aggregateGoodsDiamondsByOwner(result.data?.data.orEmpty(), goodsToOwner)

            is NetworkState.Error -> null
        }
    }

    private fun monthDateRange(year: Int, month: Int): Pair<String, String> {
        val firstDay = LocalDate(year, month, 1)
        val endDate = firstDay.plus(1, DateTimeUnit.MONTH).minus(10, DateTimeUnit.DAY)
        val startDate = firstDay.minus(9, DateTimeUnit.DAY)
        return startDate.toString().replace("-", "") to endDate.toString().replace("-", "")
    }

    fun isSessionExpired(vararg states: NetworkState<*>): Boolean {
        return states.any { state ->
            state is NetworkState.Error &&
                    (state.e is CookiesExpiredException || state.e is LoginException)
        }
    }
}

/**
 * 某月各作品流水汇总。
 * @property diamonds 钻石口径作品名 → 钻石（失败的作品按 0 计入；含钻石定价模组的商品流水）
 * @property emeraldModDiamonds 绿宝石计价模组名 → 商品流水（面额口径，不参与收益计算）
 * @property isComplete 钻石口径的流水请求是否都成功
 */
internal data class MonthFlow(
    val diamonds: Map<String, Double>,
    val emeraldModDiamonds: Map<String, Double>,
    val isComplete: Boolean
)

/** 同名作品的普通流水与商品流水相加合并 */
internal fun mergeProfitDiamonds(
    normal: Map<String, Double>,
    goods: Map<String, Double>
): Map<String, Double> = buildMap {
    putAll(normal)
    goods.forEach { (name, diamonds) -> put(name, (get(name) ?: 0.0) + diamonds) }
}

/**
 * 把商品维度的销售明细按所属模组累加：模组名 → 该模组下全部商品的钻石之和。
 * 与普通作品流水同口径，扣除退款率。
 *
 * @param rows 商品口径销售明细（每行的 [ResAnalyzeData.iid] 是商品 ID）
 * @param goodsToOwner 商品 ID → 模组名
 */
internal fun aggregateGoodsDiamondsByOwner(
    rows: List<ResAnalyzeData>,
    goodsToOwner: Map<String, String>
): Map<String, Double> =
    rows.mapNotNull { row ->
        goodsToOwner[row.iid]?.let { it to row.diamond * (1 - row.refundRate) }
    }.groupBy({ it.first }, { it.second })
        .mapValues { (_, values) -> values.sum() }
