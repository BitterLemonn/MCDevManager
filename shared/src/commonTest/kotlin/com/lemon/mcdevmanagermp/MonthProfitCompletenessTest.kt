package com.lemon.mcdevmanagermp

import com.lemon.mcdevmanagermp.data.common.JSONConverter
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.common.NoNeedData
import com.lemon.mcdevmanagermp.data.consts.enums.PriceTypeEnum
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
 * 月收益流水完整性：普通作品流水请求失败必须标记为不完整，且不完整数据不能反推生态费。
 * 商品化模组按定价分流：钻石定价模组的商品流水参与计算（其请求失败同样标记不完整），
 * 绿宝石定价模组的流水是面额折算口径、不参与计算，其请求失败不影响完整性。
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
        failingGoodsFlows: Set<String> = emptySet(),
        goodsDiamonds: Map<String, Int> = emptyMap(),
        extraWorks: List<ResourceData> = emptyList(),
        workRows: Map<String, List<ResAnalyzeData>> = emptyMap(),
        resourceRepository: ResourceRepository? = null
    ): MainUseCase {
        val analyze = FakeAnalyzeRepository(
            failingWorkIds, workDiamonds, lobbyOwners, lobbyGoods,
            failingGoodsOwners, failingGoodsFlows, goodsDiamonds, workRows
        )
        return MainUseCase(
            userRepository = UnusedUserRepository,
            analyzeRepository = analyze,
            getResourceListUseCase = GetResourceListUseCase(
                resourceRepository ?: FakeResourceRepository(works + extraWorks, resourcesFail)
            )
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
    fun emeraldGoodsListFailureKeepsDiamondFlowComplete() = runTest {
        val profit = useCase(
            extraWorks = listOf(
                work("m1", "模组1").copy(priceType = "diamond"),
                work("m2", "模组2").copy(priceType = "point")
            ),
            lobbyOwners = listOf(LobbyIncomeResourceVO("m1", "模组1"), LobbyIncomeResourceVO("m2", "模组2")),
            lobbyGoods = mapOf("m1" to listOf(LobbyGoodVO("g1", "商品1"))),
            failingGoodsOwners = setOf("m2"),
            goodsDiamonds = mapOf("g1" to 1_000)
        ).computeMonthProfit(2026, 10)
        assertTrue(profit.isFlowComplete)
        assertEquals(26_000.0, profit.sumProfit)
    }

    @Test
    fun diamondGoodsListFailureIsIncompleteAndCannotReverse() = runTest {
        val profit = useCase(
            extraWorks = listOf(work("m1", "模组1").copy(priceType = "diamond")),
            lobbyOwners = listOf(LobbyIncomeResourceVO("m1", "模组1")),
            failingGoodsOwners = setOf("m1")
        ).computeMonthProfit(2026, 10)
        assertFalse(profit.isFlowComplete)
        assertEquals(25_000.0, profit.sumProfit)
        assertNull(profit.reverseEcoFeeRate(183.75))
    }

    @Test
    fun partialDiamondGoodsListFailureKeepsAvailableFlowAndCannotReverse() = runTest {
        val profit = useCase(
            extraWorks = listOf(
                work("m1", "模组1").copy(priceType = "diamond"),
                work("m2", "模组2").copy(priceType = "diamond")
            ),
            lobbyOwners = listOf(LobbyIncomeResourceVO("m1", "模组1"), LobbyIncomeResourceVO("m2", "模组2")),
            lobbyGoods = mapOf("m1" to listOf(LobbyGoodVO("g1", "商品1"))),
            failingGoodsOwners = setOf("m2"),
            goodsDiamonds = mapOf("g1" to 1_000)
        ).computeMonthProfit(2026, 10)
        assertFalse(profit.isFlowComplete)
        assertEquals(26_000.0, profit.sumProfit)
        assertNull(profit.reverseEcoFeeRate(189.0))
    }

    @Test
    fun emeraldGoodsFlowFailureWithNoDiamondGoodsKeepsFlowComplete() = runTest {
        val profit = useCase(
            extraWorks = listOf(
                work("m1", "模组1").copy(priceType = "diamond"),
                work("m2", "模组2").copy(priceType = "point")
            ),
            lobbyOwners = listOf(LobbyIncomeResourceVO("m1", "模组1"), LobbyIncomeResourceVO("m2", "模组2")),
            lobbyGoods = mapOf("m2" to listOf(LobbyGoodVO("g2", "商品2"))),
            failingGoodsFlows = setOf("g2")
        ).computeMonthProfit(2026, 10)
        assertTrue(profit.isFlowComplete)
        assertEquals(0.3, profit.reverseEcoFeeRate(131.25)!!, 1e-9)
    }

    @Test
    fun emeraldGoodsFlowFailureKeepsAvailableDiamondFlow() = runTest {
        val profit = useCase(
            extraWorks = listOf(
                work("m1", "模组1").copy(priceType = "diamond"),
                work("m2", "模组2").copy(priceType = "point")
            ),
            lobbyOwners = listOf(LobbyIncomeResourceVO("m1", "模组1"), LobbyIncomeResourceVO("m2", "模组2")),
            lobbyGoods = mapOf(
                "m1" to listOf(LobbyGoodVO("g1", "商品1")),
                "m2" to listOf(LobbyGoodVO("g2", "商品2"))
            ),
            failingGoodsFlows = setOf("g2"),
            goodsDiamonds = mapOf("g1" to 1_000)
        ).computeMonthProfit(2026, 10)
        assertTrue(profit.isFlowComplete)
        assertEquals(26_000.0, profit.sumProfit)
    }

    @Test
    fun diamondModGoodsFlowFailureIsIncomplete() = runTest {
        val profit = useCase(
            extraWorks = listOf(work("m1", "模组1").copy(priceType = "diamond")),
            lobbyOwners = listOf(LobbyIncomeResourceVO("m1", "模组1")),
            lobbyGoods = mapOf("m1" to listOf(LobbyGoodVO("g1", "商品1"))),
            failingGoodsFlows = setOf("g1")
        ).computeMonthProfit(2026, 10)
        assertFalse(profit.isFlowComplete)
        assertNull(profit.reverseEcoFeeRate(131.25))
    }

    @Test
    fun emeraldPricedModFlowIsSeparated() = runTest {
        // 资源列表查不到定价的模组按绿宝石口径处理：流水仅留存、不参与计算
        val profit = useCase(
            lobbyOwners = listOf(LobbyIncomeResourceVO("m1", "模组1")),
            lobbyGoods = mapOf("m1" to listOf(LobbyGoodVO("g1", "商品1"), LobbyGoodVO("g2", "商品2"))),
            goodsDiamonds = mapOf("g1" to 1_000, "g2" to 500)
        ).computeMonthProfit(2026, 10)
        assertTrue(profit.isFlowComplete)
        assertEquals(1_500.0, profit.emeraldModDiamonds["模组1"])
        assertFalse(profit.moduleDiamonds.containsKey("模组1"))
        assertEquals(25_000.0, profit.sumProfit)
        assertEquals(131.25, profit.totalProfit, 1e-9)
    }

    @Test
    fun pointPricedModFlowIsSeparated() = runTest {
        val profit = useCase(
            extraWorks = listOf(work("m1", "模组1").copy(priceType = "point")),
            lobbyOwners = listOf(LobbyIncomeResourceVO("m1", "模组1")),
            lobbyGoods = mapOf("m1" to listOf(LobbyGoodVO("g1", "商品1"))),
            goodsDiamonds = mapOf("g1" to 1_500)
        ).computeMonthProfit(2026, 10)
        assertTrue(profit.isFlowComplete)
        assertEquals(1_500.0, profit.emeraldModDiamonds["模组1"])
        // 模组自身的普通流水为 0，绿宝石商品流水不并入钻石口径
        assertEquals(0.0, profit.moduleDiamonds["模组1"])
        assertEquals(25_000.0, profit.sumProfit)
        assertEquals(131.25, profit.totalProfit, 1e-9)
    }

    @Test
    fun mixedGoodsFlowsIncludeOnlyDiamondGoodsInProfit() = runTest {
        val profit = useCase(
            extraWorks = listOf(
                work("m1", "模组1").copy(priceType = "diamond"),
                work("m2", "模组2").copy(priceType = "point")
            ),
            lobbyOwners = listOf(LobbyIncomeResourceVO("m1", "模组1"), LobbyIncomeResourceVO("m2", "模组2")),
            lobbyGoods = mapOf(
                "m1" to listOf(LobbyGoodVO("g1", "商品1")),
                "m2" to listOf(LobbyGoodVO("g2", "商品2"))
            ),
            goodsDiamonds = mapOf("g1" to 1_000, "g2" to 5_000_000)
        ).computeMonthProfit(2026, 10)
        assertTrue(profit.isFlowComplete)
        assertEquals(1_000.0, profit.moduleDiamonds["模组1"])
        assertEquals(26_000.0, profit.sumProfit)
        assertEquals(mapOf("模组2" to 5_000_000.0), profit.emeraldModDiamonds)
        assertEquals(0.3, profit.reverseEcoFeeRate(136.5)!!, 1e-9)
    }

    @Test
    fun convertedRegularWorkParsesDiamondPricingAndKeepsPointSalesSeparate() = runTest {
        val resource = JSONConverter.decodeFromString(
            ResourceData.serializer(),
            """{
                "item_id":"converted-mod","item_name":"转价模组",
                "price_type":"diamond","price_rank":0,"price":300,"pri_type":2,
                "online_time":"2026-11-13T16:20:49.482+08:00",
                "first_online_time":"2026-09-28T18:29:04.818+08:00",
                "lobby_commercialize":false
            }"""
        )
        val rows = JSONConverter.decodeFromString(
            ResDetailVO.serializer(),
            """{"data":[
                {"iid":"converted-mod","res_name":"转价模组","dateid":"20261025",
                 "cnt_buy":1,"points":2000,"diamond":0},
                {"iid":"converted-mod","res_name":"转价模组","dateid":"20261114",
                 "cnt_buy":1,"points":0,"diamond":300,"refund_rate":0.1},
                {"iid":"converted-mod","res_name":"转价模组","dateid":"20261115",
                 "cnt_buy":2,"points":0,"diamond":600}
            ]}"""
        ).data
        assertEquals(PriceTypeEnum.DIAMOND, resource.getPriceType())

        val profit = useCase(
            extraWorks = listOf(resource),
            workRows = mapOf(resource.itemId to rows)
        ).computeMonthProfit(2026, 11)

        assertTrue(profit.isFlowComplete)
        assertEquals(870.0, profit.moduleDiamonds[resource.itemName])
        assertEquals(25_870.0, profit.sumProfit)
        assertTrue(profit.emeraldModDiamonds.isEmpty())
        assertEquals(0.3, profit.reverseEcoFeeRate(135.8175)!!, 1e-9)
    }

    @Test
    fun commercializedModUsesRefreshedPricingAfterConversion() = runTest {
        val oldResource = work("m1", "转价模组").copy(priceType = "point", priceRank = -5)
        val repository = FakeResourceRepository(works + oldResource, false)
        val mainUseCase = useCase(
            resourceRepository = repository,
            lobbyOwners = listOf(LobbyIncomeResourceVO("m1", "转价模组")),
            lobbyGoods = mapOf("m1" to listOf(LobbyGoodVO("g1", "商品1"))),
            goodsDiamonds = mapOf("g1" to 1_000)
        )

        val before = mainUseCase.computeMonthProfit(2026, 10)
        assertTrue(before.isFlowComplete)
        assertEquals(25_000.0, before.sumProfit)
        assertEquals(1_000.0, before.emeraldModDiamonds["转价模组"])

        repository.works = works + oldResource.copy(priceType = "diamond", priceRank = 0, price = 300)
        val after = mainUseCase.computeMonthProfit(2026, 11)
        assertTrue(after.isFlowComplete)
        assertEquals(1_000.0, after.moduleDiamonds["转价模组"])
        assertEquals(26_000.0, after.sumProfit)
        assertTrue(after.emeraldModDiamonds.isEmpty())
        assertEquals(0.3, after.reverseEcoFeeRate(136.5)!!, 1e-9)
    }

    @Test
    fun hugeEmeraldModFlowDoesNotDistortReverseRate() = runTest {
        // 绿宝石模组流水即便巨大也不能把反推费率推高
        val profit = useCase(
            lobbyOwners = listOf(LobbyIncomeResourceVO("m1", "模组1")),
            lobbyGoods = mapOf("m1" to listOf(LobbyGoodVO("g1", "商品1"))),
            goodsDiamonds = mapOf("g1" to 5_000_000)
        ).computeMonthProfit(2026, 10)
        assertEquals(0.3, profit.reverseEcoFeeRate(131.25)!!, 1e-9)
    }
}

/** 普通作品按 itemListStr 返回单作品流水；商品口径按逗号分隔的商品 ID 返回各商品流水 */
private class FakeAnalyzeRepository(
    private val failingWorkIds: Set<String>,
    private val workDiamonds: Map<String, Int>,
    private val lobbyOwners: List<LobbyIncomeResourceVO>,
    private val lobbyGoods: Map<String, List<LobbyGoodVO>>,
    private val failingGoodsOwners: Set<String>,
    private val failingGoodsFlows: Set<String>,
    private val goodsDiamonds: Map<String, Int>,
    private val workRows: Map<String, List<ResAnalyzeData>> = emptyMap()
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
            val goodsIds = itemListStr.split(",")
            if (goodsIds.any { it in failingGoodsFlows }) return NetworkState.Error("lobby flow failed")
            val rows = goodsIds.map { row(it, goodsDiamonds[it] ?: 0) }
            return NetworkState.Success(ResDetailVO(rows))
        }
        if (itemListStr in failingWorkIds) return NetworkState.Error("work flow failed")
        return NetworkState.Success(
            ResDetailVO(workRows[itemListStr] ?: listOf(row(itemListStr, workDiamonds[itemListStr] ?: 0)))
        )
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
    var works: List<ResourceData>,
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
