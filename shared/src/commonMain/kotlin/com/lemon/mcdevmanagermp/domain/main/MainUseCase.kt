package com.lemon.mcdevmanagermp.domain.main

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.consts.CookiesExpiredException
import com.lemon.mcdevmanagermp.data.consts.LoginException
import com.lemon.mcdevmanagermp.data.dto.netease.income.LobbyGoodVO
import com.lemon.mcdevmanagermp.data.vo.netease.analyze.ResAnalyzeData
import com.lemon.mcdevmanagermp.data.vo.netease.user.LevelInfoVO
import com.lemon.mcdevmanagermp.data.vo.netease.user.OverviewVO
import com.lemon.mcdevmanagermp.data.vo.netease.user.UserInfoVO
import com.lemon.mcdevmanagermp.domain.analyze.AnalyzeRepository
import com.lemon.mcdevmanagermp.domain.analyze.LobbyGoodsUseCase
import com.lemon.mcdevmanagermp.domain.resource.GetResourceListUseCase
import com.lemon.mcdevmanagermp.domain.user.UserRepository
import com.lemon.mcdevmanagermp.utils.ProfitData
import com.lemon.mcdevmanagermp.utils.calculateProfit
import kotlinx.coroutines.async
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

class MainUseCase(
    private val userRepository: UserRepository,
    private val analyzeRepository: AnalyzeRepository,
    private val getResourceListUseCase: GetResourceListUseCase,
    private val lobbyGoodsUseCase: LobbyGoodsUseCase = LobbyGoodsUseCase(analyzeRepository)
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

    suspend fun computeMonthProfit(year: Int, month: Int): ProfitData =
        calculateProfit(getOneMonthComponentDiamonds(year, month))

    private suspend fun getOneMonthComponentDiamonds(year: Int, month: Int): Map<String, Double> =
        coroutineScope {
            val normalResources = async {
                getResourceListUseCase("pe", onlineOnly = true, excludePrerequisites = true)
            }
            // 联机大厅作品及其商品的获取（含作品列表请求）交由 lobbyGoodsUseCase 统一发起
            val lobbyGoods = async { lobbyGoodsUseCase.getGoodsByOwner() }
            val resList = when (val resources = normalResources.await()) {
                is NetworkState.Success -> resources.data ?: emptyList()
                is NetworkState.Error -> emptyList()
            }
            val goodsByOwner = when (val resources = lobbyGoods.await()) {
                is NetworkState.Success -> resources.data ?: emptyMap()
                is NetworkState.Error -> emptyMap()
            }

            val dateRange = monthDateRange(year, month)

            val normalDiamonds = resList.map { res ->
                async {
                    val result = analyzeRepository.getDayDetail(
                        platform = "pe",
                        category = "pe",
                        startDate = dateRange.first,
                        endDate = dateRange.second,
                        itemListStr = res.itemId
                    )
                    if (result is NetworkState.Success) {
                        res.itemName to (result.data?.data?.sumOf { it.diamond * (1 - it.refundRate) }
                            ?: 0.0)
                    } else {
                        res.itemName to 0.0
                    }
                }
            }.associate { it.await() }
            val lobbyDiamonds = lobbyItemDiamonds(goodsByOwner, dateRange)

            mergeProfitDiamonds(normalDiamonds, lobbyDiamonds)
        }

    /**
     * 计算联机大厅作品收益：作品下的各商品销售钻石累加回作品名。
     */
    private suspend fun lobbyItemDiamonds(
        goodsByOwner: Map<String, List<LobbyGoodVO>>,
        dateRange: Pair<String, String>
    ): Map<String, Double> {
        val goodsToOwner = goodsByOwner
            .flatMap { (name, goods) -> goods.map { it.goodsId to name } }
            .toMap()
        if (goodsToOwner.isEmpty()) return emptyMap()

        // 一次查询全部商品的日详情（item_list_str 支持逗号分隔）
        val result = analyzeRepository.getDayDetail(
            platform = "pe",
            category = "pe",
            startDate = dateRange.first,
            endDate = dateRange.second,
            itemListStr = goodsToOwner.keys.joinToString(","),
            isLobby = true
        )
        val rows = if (result is NetworkState.Success) {
            result.data?.data.orEmpty()
        } else {
            emptyList()
        }

        return aggregateGoodsDiamondsByOwner(rows, goodsToOwner)
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

internal fun mergeProfitDiamonds(
    normal: Map<String, Double>,
    lobby: Map<String, Double>
): Map<String, Double> = buildMap {
    putAll(normal)
    lobby.forEach { (name, diamonds) -> put(name, (get(name) ?: 0.0) + diamonds) }
}

/**
 * 把商品维度的销售明细按所属作品累加：作品名 → 该作品下全部商品的钻石之和。
 *
 * @param rows 商品口径销售明细（每行的 [ResAnalyzeData.iid] 是商品 ID）
 * @param goodsToOwner 商品 ID → 作品名
 */
internal fun aggregateGoodsDiamondsByOwner(
    rows: List<ResAnalyzeData>,
    goodsToOwner: Map<String, String>
): Map<String, Double> =
    rows.mapNotNull { row ->
        goodsToOwner[row.iid]?.let { it to row.diamond.toDouble() }
    }.groupBy({ it.first }, { it.second })
        .mapValues { (_, values) -> values.sum() }
