package com.lemon.mcdevmanagermp

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.common.NoNeedData
import com.lemon.mcdevmanagermp.data.dto.netease.income.LobbyGoodVO
import com.lemon.mcdevmanagermp.data.dto.netease.income.LobbyGoodsListVO
import com.lemon.mcdevmanagermp.data.dto.netease.income.LobbyIncomeResourceListVO
import com.lemon.mcdevmanagermp.data.dto.netease.income.LobbyIncomeResourceVO
import com.lemon.mcdevmanagermp.data.dto.netease.income.OneResRealtimeIncomeVO
import com.lemon.mcdevmanagermp.data.dto.netease.work.ApplyReviewDTO
import com.lemon.mcdevmanagermp.data.dto.netease.work.ApplySelfTestDTO
import com.lemon.mcdevmanagermp.data.dto.netease.work.AppointOnlineDTO
import com.lemon.mcdevmanagermp.data.dto.netease.work.ChangePriceDTO
import com.lemon.mcdevmanagermp.data.dto.netease.work.OnlineItemDTO
import com.lemon.mcdevmanagermp.data.dto.netease.work.WorkCreateDTO
import com.lemon.mcdevmanagermp.data.dto.netease.work.WorkUpdateDTO
import com.lemon.mcdevmanagermp.data.vo.netease.analyze.ResAnalyzeData
import com.lemon.mcdevmanagermp.data.vo.netease.analyze.ResDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.analyze.ResMonthDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ItemTagVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.MCConstsVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.RequirementVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceListVO
import com.lemon.mcdevmanagermp.data.vo.netease.user.LevelInfoVO
import com.lemon.mcdevmanagermp.data.vo.netease.user.OverviewVO
import com.lemon.mcdevmanagermp.data.vo.netease.user.UserInfoVO
import com.lemon.mcdevmanagermp.data.vo.netease.work.ReviewApplyResultVO
import com.lemon.mcdevmanagermp.data.vo.netease.work.ReviewFeedbackVO
import com.lemon.mcdevmanagermp.domain.analyze.AnalyzeRepository
import com.lemon.mcdevmanagermp.domain.analyze.DayDetailConfig
import com.lemon.mcdevmanagermp.domain.main.MainUseCase
import com.lemon.mcdevmanagermp.domain.resource.GetResourceListUseCase
import com.lemon.mcdevmanagermp.domain.resource.ResourceRepository
import com.lemon.mcdevmanagermp.domain.user.UserRepository
import com.lemon.mcdevmanagermp.utils.reverseEcoFeeRate
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 月收益流水完整性：任一流水相关请求失败都必须标记为不完整，且不完整数据不能反推生态费。
 * 场景数据：两个普通作品 A=20,000、B=5,000 钻石，按 30% 生态费的预估收益为 ¥131.25。
 */
class MonthProfitCompletenessTest {
    private val works = listOf(work("a", "作品A"), work("b", "作品B"))
    private val workDiamonds = mapOf("a" to 20_000, "b" to 5_000)

    private fun work(id: String, name: String) =
        ResourceData(itemId = id, itemName = name, onlineTime = "2026-01-01 00:00:00")

    private fun useCase(
        resourcesFail: Boolean = false,
        failingWorkIds: Set<String> = emptySet(),
        lobbyOwners: List<LobbyIncomeResourceVO> = emptyList(),
        lobbyGoods: Map<String, List<LobbyGoodVO>> = emptyMap(),
        failingGoodsOwners: Set<String> = emptySet(),
        lobbyFlowFails: Boolean = false,
        goodsDiamonds: Map<String, Int> = emptyMap()
    ): MainUseCase {
        val analyze = FakeAnalyzeRepository(
            failingWorkIds, workDiamonds, lobbyOwners, lobbyGoods,
            failingGoodsOwners, lobbyFlowFails, goodsDiamonds
        )
        return MainUseCase(
            userRepository = UnusedUserRepository,
            analyzeRepository = analyze,
            getResourceListUseCase = GetResourceListUseCase(FakeResourceRepository(works, resourcesFail))
        )
    }

    @Test
    fun allRequestsSucceedIsComplete() = runTest {
        val profit = useCase().computeMonthProfit(2026, 10)
        assertTrue(profit.isFlowComplete)
        assertEquals(25_000.0, profit.sumProfit)
        assertEquals(131.25, profit.totalProfit, 1e-9)
        assertEquals(0.3, profit.reverseEcoFeeRate(131.25)!!, 1e-9)
    }

    @Test
    fun singleWorkFlowFailureIsIncompleteAndCannotReverse() = runTest {
        val profit = useCase(failingWorkIds = setOf("b")).computeMonthProfit(2026, 10)
        assertFalse(profit.isFlowComplete)
        // 失败作品按 0 计入：若仍允许反推，会得出 12.5% 的错误费率
        assertEquals(20_000.0, profit.sumProfit)
        assertNull(profit.reverseEcoFeeRate(131.25))
    }

    @Test
    fun resourceListFailureIsIncomplete() = runTest {
        assertFalse(useCase(resourcesFail = true).computeMonthProfit(2026, 10).isFlowComplete)
    }

    @Test
    fun lobbyGoodsListFailureIsIncomplete() = runTest {
        val profit = useCase(
            lobbyOwners = listOf(LobbyIncomeResourceVO("m1", "大厅1"), LobbyIncomeResourceVO("m2", "大厅2")),
            lobbyGoods = mapOf("m1" to listOf(LobbyGoodVO("g1", "商品1"))),
            failingGoodsOwners = setOf("m2"),
            goodsDiamonds = mapOf("g1" to 1_000)
        ).computeMonthProfit(2026, 10)
        assertFalse(profit.isFlowComplete)
    }

    @Test
    fun lobbyGoodsFlowFailureIsIncomplete() = runTest {
        val profit = useCase(
            lobbyOwners = listOf(LobbyIncomeResourceVO("m1", "大厅1")),
            lobbyGoods = mapOf("m1" to listOf(LobbyGoodVO("g1", "商品1"))),
            lobbyFlowFails = true
        ).computeMonthProfit(2026, 10)
        assertFalse(profit.isFlowComplete)
    }

    @Test
    fun lobbyGoodsFlowSucceedsIsCompleteAndMerged() = runTest {
        val profit = useCase(
            lobbyOwners = listOf(LobbyIncomeResourceVO("m1", "大厅1")),
            lobbyGoods = mapOf("m1" to listOf(LobbyGoodVO("g1", "商品1"), LobbyGoodVO("g2", "商品2"))),
            goodsDiamonds = mapOf("g1" to 1_000, "g2" to 500)
        ).computeMonthProfit(2026, 10)
        assertTrue(profit.isFlowComplete)
        assertEquals(1_500.0, profit.moduleDiamonds["大厅1"])
        assertEquals(26_500.0, profit.sumProfit)
    }
}

/** 普通作品按 itemListStr 返回单作品流水；联机大厅按逗号分隔的商品 ID 返回各商品流水 */
private class FakeAnalyzeRepository(
    private val failingWorkIds: Set<String>,
    private val workDiamonds: Map<String, Int>,
    private val lobbyOwners: List<LobbyIncomeResourceVO>,
    private val lobbyGoods: Map<String, List<LobbyGoodVO>>,
    private val failingGoodsOwners: Set<String>,
    private val lobbyFlowFails: Boolean,
    private val goodsDiamonds: Map<String, Int>
) : AnalyzeRepository {
    private fun row(iid: String, diamond: Int) =
        ResAnalyzeData(cntBuy = 0, dateId = "20261001", diamond = diamond, iid = iid, resName = iid)

    override suspend fun getDayDetail(
        platform: String,
        category: String,
        startDate: String,
        endDate: String,
        itemListStr: String,
        isLobby: Boolean
    ): NetworkState<ResDetailVO> {
        if (isLobby) {
            if (lobbyFlowFails) return NetworkState.Error("lobby flow failed")
            val rows = itemListStr.split(",").map { row(it, goodsDiamonds[it] ?: 0) }
            return NetworkState.Success(ResDetailVO(rows))
        }
        if (itemListStr in failingWorkIds) return NetworkState.Error("work flow failed")
        return NetworkState.Success(ResDetailVO(listOf(row(itemListStr, workDiamonds[itemListStr] ?: 0))))
    }

    override suspend fun getLobbyIncomeResources(): NetworkState<LobbyIncomeResourceListVO> =
        NetworkState.Success(LobbyIncomeResourceListVO(count = lobbyOwners.size, items = lobbyOwners))

    override suspend fun getLobbyGoodsList(iid: String): NetworkState<LobbyGoodsListVO> =
        if (iid in failingGoodsOwners) NetworkState.Error("goods list failed")
        else NetworkState.Success(LobbyGoodsListVO(lobbyGoods[iid].orEmpty()))

    override suspend fun getMonthDetail(
        platform: String,
        category: String,
        startDate: String,
        endDate: String,
        dayDateId: String,
        isLobby: Boolean
    ): NetworkState<ResMonthDetailVO> = error("unused")

    override suspend fun getOneResRealtimeIncome(
        platform: String,
        iid: String,
        beginTime: String,
        endTime: String
    ): NetworkState<OneResRealtimeIncomeVO> = error("unused")

    override suspend fun getLobbyRealtimeIncome(
        iid: String,
        beginTime: String,
        endTime: String
    ): NetworkState<OneResRealtimeIncomeVO> = error("unused")

    override suspend fun getDayDetailConfig(accountKey: String, platform: String): DayDetailConfig? =
        error("unused")

    override suspend fun saveDayDetailConfig(config: DayDetailConfig) = error("unused")
}

private class FakeResourceRepository(
    private val works: List<ResourceData>,
    private val fail: Boolean
) : ResourceRepository {
    override suspend fun getResources(
        platform: String,
        itemName: String?,
        mcStatus: Int?
    ): NetworkState<ResourceListVO> =
        if (fail) NetworkState.Error("resource list failed")
        else NetworkState.Success(ResourceListVO(count = works.size, item = works))

    override suspend fun getCompRequirements(itemName: String): NetworkState<RequirementVO> = error("unused")
    override suspend fun getPePrerequisites(priType: Int, itemName: String): NetworkState<RequirementVO> =
        error("unused")

    override suspend fun getResourceDetail(itemId: String): NetworkState<ResourceDetailVO> = error("unused")
    override suspend fun getItemTag(): NetworkState<ItemTagVO> = error("unused")
    override suspend fun getMCConsts(): NetworkState<MCConstsVO> = error("unused")
    override suspend fun deleteItem(itemId: String): NetworkState<NoNeedData> = error("unused")
    override suspend fun updateItem(itemId: String, item: WorkUpdateDTO): NetworkState<NoNeedData> =
        error("unused")

    override suspend fun createItem(body: WorkCreateDTO): NetworkState<NoNeedData> = error("unused")
    override suspend fun applyReview(
        itemId: String,
        content: ApplyReviewDTO
    ): NetworkState<ReviewApplyResultVO> = error("unused")

    override suspend fun cancelReview(itemId: String): NetworkState<NoNeedData> = error("unused")
    override suspend fun applySelfTest(itemId: String, content: ApplySelfTestDTO): NetworkState<NoNeedData> =
        error("unused")

    override suspend fun cancelSelfTest(itemId: String): NetworkState<NoNeedData> = error("unused")
    override suspend fun getReviewFeedback(itemId: String): NetworkState<ReviewFeedbackVO> = error("unused")
    override suspend fun onlineItem(itemId: String, content: OnlineItemDTO): NetworkState<NoNeedData> =
        error("unused")

    override suspend fun changePrice(itemId: String, content: ChangePriceDTO): NetworkState<NoNeedData> =
        error("unused")

    override suspend fun appointOnlineItem(
        itemId: String,
        content: AppointOnlineDTO
    ): NetworkState<NoNeedData> = error("unused")
}

/** computeMonthProfit 不访问用户接口 */
private object UnusedUserRepository : UserRepository {
    override suspend fun getUserInfo(): NetworkState<UserInfoVO> = error("unused")
    override suspend fun getOverview(): NetworkState<OverviewVO> = error("unused")
    override suspend fun getLevelInfo(): NetworkState<LevelInfoVO> = error("unused")
}
