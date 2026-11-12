package com.lemon.mcdevmanagermp

import com.lemon.mcdevmanagermp.data.common.JSONConverter
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.common.ResponseData
import com.lemon.mcdevmanagermp.data.consts.enums.PE_PREREQUISITE_PRI_TYPE
import com.lemon.mcdevmanagermp.data.consts.enums.PriceRankEnum
import com.lemon.mcdevmanagermp.data.consts.enums.PriceTypeEnum
import com.lemon.mcdevmanagermp.data.consts.enums.WorkItemActionEnum
import com.lemon.mcdevmanagermp.data.consts.enums.WorkItemStatusEnum
import com.lemon.mcdevmanagermp.data.dto.netease.activity.FileInfoDTO
import com.lemon.mcdevmanagermp.data.dto.netease.income.LobbyGoodsListVO
import com.lemon.mcdevmanagermp.data.dto.netease.income.LobbyIncomeResourceListVO
import com.lemon.mcdevmanagermp.data.dto.netease.income.OneResRealtimeIncomeVO
import com.lemon.mcdevmanagermp.data.dto.netease.work.toWorkUpdateDTO
import com.lemon.mcdevmanagermp.data.vo.netease.analyze.ResAnalyzeData
import com.lemon.mcdevmanagermp.data.vo.netease.analyze.ResDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.analyze.ResMonthAnalyzeData
import com.lemon.mcdevmanagermp.data.vo.netease.analyze.ResMonthDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.MCConstsChannelData
import com.lemon.mcdevmanagermp.data.vo.netease.resource.MCConstsChannelDataList
import com.lemon.mcdevmanagermp.data.vo.netease.resource.MCConstsVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailChannel
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailDlcInfo
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailSyncChannel
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailSyncItemInfo
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailTag
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailVideoInfo
import com.lemon.mcdevmanagermp.domain.analyze.excludePrerequisiteRows
import com.lemon.mcdevmanagermp.domain.analyze.mergeRealtimeIncome
import com.lemon.mcdevmanagermp.domain.main.aggregateGoodsDiamondsByOwner
import com.lemon.mcdevmanagermp.domain.main.mergeProfitDiamonds
import com.lemon.mcdevmanagermp.domain.resource.MCConstsCache
import com.lemon.mcdevmanagermp.domain.resource.filterResourceList
import com.lemon.mcdevmanagermp.domain.upload.FileUploadRepository
import com.lemon.mcdevmanagermp.domain.work.EMERALD_PRICE_REJECTED_MESSAGE
import com.lemon.mcdevmanagermp.domain.work.PeImageCompletenessPolicy
import com.lemon.mcdevmanagermp.domain.work.WorkSaveValidationInput
import com.lemon.mcdevmanagermp.domain.work.hasUnversionedPcImages
import com.lemon.mcdevmanagermp.domain.work.validateWorkSave
import com.lemon.mcdevmanagermp.domain.work.withCurrentPcImageChannels
import com.lemon.mcdevmanagermp.ui.components.ensureHtmlImagesOnFilepicker
import com.lemon.mcdevmanagermp.ui.components.normalizeStyleCss
import com.lemon.mcdevmanagermp.ui.components.sanitizeDetailHtml
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.ChannelImageSlot
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.VideoItem
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailState
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.buildUpdatePayload
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.buildWorkCreatePayload
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.normalizedForPrerequisite
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.parsePrerequisiteItemId
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.parsePrerequisiteItemName
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.validateLobbySettings
import com.lemon.mcdevmanagermp.utils.extension.dumpAndGetCookiesValue
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SharedCommonTest {

    private val validInput = WorkSaveValidationInput(
        itemName = "测试资源",
        tags = listOf("玩法"),
        priceType = PriceTypeEnum.FREE,
        priceRank = -4,
        price = 0,
        detailHtml = "<p>详情</p>",
        peResourceType = 1,
        recommendTagIds = listOf(11, 21),
        gameplayTagIds = setOf(11),
        themeTagIds = setOf(21),
        modVersion = "1.0",
        hasResource = true,
        channelsLoaded = true,
        requiredChannelIds = setOf(1),
        availableChannelIds = setOf(1),
        hasVideo = false,
    )

    @Test
    fun cookieValueAllowsEqualsInValue() {
        assertEquals("abc==", "foo=bar; NTES_SESS=abc==; path=/".dumpAndGetCookiesValue("NTES_SESS"))
    }

    @Test
    fun lobbyIncomeSummaryDecodesAndIncomeStreamsMerge() {
        val resources = JSONConverter.decodeFromString<LobbyIncomeResourceListVO>(
            """{"count":1,"items":[{"item_id":"46","item_name":"大厅"}]}"""
        )
        val merged = mergeRealtimeIncome(
            OneResRealtimeIncomeVO(count = 2, totalDiamonds = 10, totalPoints = 3),
            OneResRealtimeIncomeVO(count = 4, totalDiamonds = 20, totalPoints = 7)
        )

        assertEquals("46", resources.items.single().itemId)
        assertEquals(6, merged.count)
        assertEquals(30, merged.totalDiamonds)
        assertEquals(10, merged.totalPoints)
    }

    @Test
    fun lobbyAnalyzeMinimalResponsesDecode() {
        val day = JSONConverter.decodeFromString<ResDetailVO>(
            """{"data":[{"dateid":"20260816","iid":"46","res_name":"大厅作品","cnt_buy":2,"diamond":20}]}"""
        ).data.single()
        val month = JSONConverter.decodeFromString<ResMonthDetailVO>(
            """{"data":[{"monthid":"202608","iid":"46","res_name":"大厅作品","avg_day_buy":2,"total_diamond":20}]}"""
        ).data.single()

        assertEquals(20, day.diamond)
        assertEquals(0, day.dau)
        assertEquals(2, month.avgDayBuy)
        assertEquals(0, month.totalPoints)
    }

    @Test
    fun normalAndLobbyProfitWithSameNameAddsTogether() {
        assertEquals(
            mapOf("同名作品" to 30.0),
            mergeProfitDiamonds(
                normal = mapOf("同名作品" to 10.0),
                lobby = mapOf("同名作品" to 20.0)
            )
        )
    }

    @Test
    fun lobbyGoodsSumIntoSingleOwnerProfit() {
        // 一个联机大厅作品下挂多个商品，作品收益应为各商品钻石之和
        val rows = listOf(
            ResAnalyzeData(iid = "g1", diamond = 30, cntBuy = 1, dateId = "20260816", resName = "钻石礼包"),
            ResAnalyzeData(iid = "g2", diamond = 12, cntBuy = 1, dateId = "20260816", resName = "皮肤"),
            ResAnalyzeData(iid = "g3", diamond = 8, cntBuy = 1, dateId = "20260816", resName = "坐骑")
        )
        val result = aggregateGoodsDiamondsByOwner(
            rows = rows,
            goodsToOwner = mapOf("g1" to "大厅A", "g2" to "大厅A", "g3" to "大厅B")
        )

        assertEquals(mapOf("大厅A" to 42.0, "大厅B" to 8.0), result)
    }

    @Test
    fun lobbyGoodsNotBelongingToAnyOwnerAreIgnored() {
        // 接口可能返回未纳入映射的商品（例如其他作品），不应污染结果
        val rows = listOf(
            ResAnalyzeData(iid = "g1", diamond = 30, cntBuy = 1, dateId = "20260816", resName = "钻石礼包"),
            ResAnalyzeData(iid = "unknown", diamond = 999, cntBuy = 1, dateId = "20260816", resName = "未知")
        )

        assertEquals(
            mapOf("大厅A" to 30.0),
            aggregateGoodsDiamondsByOwner(rows, mapOf("g1" to "大厅A"))
        )
    }

    @Test
    fun lobbyGoodsListDecodesGoodsIdAndName() {
        val goods = JSONConverter.decodeFromString<LobbyGoodsListVO>(
            """{"goods":[{"goods_id":"1001","name":"钻石礼包"},{"goods_id":"1002","name":"皮肤"}]}"""
        )

        assertEquals(2, goods.goods.size)
        assertEquals("1001", goods.goods[0].goodsId)
        assertEquals("钻石礼包", goods.goods[0].name)
    }

    @Test
    fun emptyCorpProofImageIsOmittedFromUpdateJson() {
        val json = JSONConverter.encodeToJsonElement(
            ResourceDetailVO(corpProofImage = " ").toWorkUpdateDTO(false)
        ).jsonObject

        assertFalse("corp_proof_image" in json)
    }

    @Test
    fun corpProofImageUrlIsKeptInUpdateJson() {
        val url = "https://example.com/proof.png"
        val json = JSONConverter.encodeToJsonElement(
            ResourceDetailVO(corpProofImage = url).toWorkUpdateDTO(false)
        ).jsonObject

        assertEquals(url, json["corp_proof_image"]?.jsonPrimitive?.content)
    }

    @Test
    fun pePlayPlanAndExpireTimeRoundTripInUpdateJsonWhenNotExpired() {
        val json = JSONConverter.encodeToJsonElement(
            ResourceDetailVO(peIsAddPlayPlan = true, playPlanExpireTime = 2000000000)
                .toWorkUpdateDTO(false, currentEpochSeconds = 1700000000)
        ).jsonObject

        assertTrue(json["pe_is_add_play_plan"]!!.jsonPrimitive.boolean)
        assertEquals(2000000000, json["play_plan_expire_time"]!!.jsonPrimitive.int)
        assertNull(json["play_plan_expire_month"])
    }

    @Test
    fun pePlayPlanResetsExpireMonthWhenExpired() {
        val json = JSONConverter.encodeToJsonElement(
            ResourceDetailVO(peIsAddPlayPlan = true, playPlanExpireTime = 1700000000)
                .toWorkUpdateDTO(false, currentEpochSeconds = 1800000000)
        ).jsonObject

        assertTrue(json["pe_is_add_play_plan"]!!.jsonPrimitive.boolean)
        assertNull(json["play_plan_expire_time"])
        assertEquals(0, json["play_plan_expire_month"]!!.jsonPrimitive.int)
    }

    @Test
    fun everyWorkStatusExposesViewDetailAction() {
        // 所有状态下都必须能只读查看详情，包括原本没有任何可写操作的状态
        WorkItemStatusEnum.entries.forEach { status ->
            assertTrue(
                WorkItemActionEnum.VIEW_DETAIL in status.actions(isFree = true),
                "状态 $status 缺少 VIEW_DETAIL"
            )
            assertTrue(
                WorkItemActionEnum.VIEW_DETAIL in status.actions(isFree = false),
                "状态 $status（付费）缺少 VIEW_DETAIL"
            )
        }
    }

    @Test
    fun viewDetailIsTheOnlyActionForStatusesWithoutWriteOperations() {
        // 上架准备中 / 未知：无可写操作，仅保留只读查看
        listOf(WorkItemStatusEnum.ONLINE_PREPARING, WorkItemStatusEnum.UNKNOWN).forEach { status ->
            assertEquals(
                listOf(WorkItemActionEnum.VIEW_DETAIL),
                status.actions(isFree = true),
                "状态 $status 应仅有 VIEW_DETAIL"
            )
        }
    }

    @Test
    fun workManagementUpdateDoesNotInheritPePlayPlan() {
        val payload = buildUpdatePayload(
            s = WorkDetailState(peAddPlayPlan = true),
            d = ResourceDetailVO(peIsAddPlayPlan = true),
            corpProofUrl = ""
        )

        assertFalse(payload.peIsAddPlayPlan)
    }

    @Test
    fun lobbySettingsReachCreateAndUpdatePayloads() {
        val state = WorkDetailState(
            peResourceType = 6,
            lobbyMinNum = 2,
            lobbyMaxNum = 8,
            lobbyForceMaxNum = 10,
            lobbyTags = listOf(9),
            isLobbyCompetitive = true,
            lobbyIsAsymmetric = true,
            lobbyCamps = listOf("红队", "蓝队"),
            lobbyPlayerNum = 6,
            lobbyNormalMode = true,
            lobbyReconnectTime = 5,
        )

        assertNull(validateLobbySettings(state))
        val create = buildWorkCreatePayload(state, isCheckApply = false)
        val update = buildUpdatePayload(state, ResourceDetailVO(priType = 6), "")

        assertEquals(2, create.lobbyMinNum)
        assertEquals(8, create.lobbyMaxNum)
        assertEquals(10, create.lobbyForceMaxNum)
        assertEquals(listOf(9), create.lobbyTags.map { it.jsonPrimitive.int })
        assertEquals(listOf("红队", "蓝队"), create.lobbyCamps.map { it.jsonPrimitive.content })
        assertEquals(6, create.lobbyPlayerNum)
        assertEquals(true, create.lobbyNormalMode)
        assertEquals(5, update.lobbyReconnectTime)
        assertEquals(true, update.isAsymmetric)
    }

    @Test
    fun nonLobbyUpdatePreservesExistingLobbyData() {
        val detail = ResourceDetailVO(
            priType = 1,
            lobbyMinNum = 2,
            lobbyMaxNum = 4,
            lobbyTags = listOf(kotlinx.serialization.json.JsonPrimitive(5)),
        )
        val update = buildUpdatePayload(
            WorkDetailState(peResourceType = 1),
            detail,
            ""
        )

        assertEquals(2, update.lobbyMinNum)
        assertEquals(4, update.lobbyMaxNum)
        assertEquals(listOf(5), update.lobbyTags.map { it.jsonPrimitive.int })
    }

    @Test
    fun uploadedChannelUsesFileReceiptInUpdateJson() {
        val fileInfo = FileInfoDTO(body = "uploaded-body", fileType = "image", sign = "sign")
        val json = JSONConverter.encodeToJsonElement(
            ResourceDetailVO(
                channel = listOf(
                    ResourceDetailChannel(
                        channelId = 3,
                        channelUrl = "https://example.com/icon.png",
                        version = 2,
                        fileInfo = fileInfo
                    )
                )
            ).toWorkUpdateDTO(false)
        ).jsonObject
        val channel = json["channel"]!!.jsonArray.single().jsonObject

        assertEquals("uploaded-body", channel["channel_url"]!!.jsonObject["body"]?.jsonPrimitive?.content)
        assertEquals("image", channel["channel_url"]!!.jsonObject["file_type"]?.jsonPrimitive?.content)
    }

    @Test
    fun existingChannelKeepsUrlInUpdateJson() {
        val url = "https://example.com/existing.png"
        val json = JSONConverter.encodeToJsonElement(
            ResourceDetailVO(
                channel = listOf(ResourceDetailChannel(channelId = 3, channelUrl = url, version = 2))
            ).toWorkUpdateDTO(false)
        ).jsonObject
        val channel = json["channel"]!!.jsonArray.single().jsonObject

        assertEquals(url, channel["channel_url"]?.jsonPrimitive?.content)
    }

    @Test
    fun syncedPcImagesUseCurrentChannelConfigurationBeforeUpdate() {
        val detail = ResourceDetailVO(
            syncPcFlag = true,
            syncItemInfo = ResourceDetailSyncItemInfo(
                channel = listOf(
                    ResourceDetailSyncChannel(
                        channelId = 7,
                        channelUrl = "https://example.com/existing-pc.png",
                        version = 1,
                    ),
                    ResourceDetailSyncChannel(
                        channelId = 999,
                        channelUrl = "https://example.com/obsolete-pc.png",
                        version = 1,
                    ),
                    ResourceDetailSyncChannel(channelId = 8, version = 1),
                )
            ),
        )
        val normalized = detail.withCurrentPcImageChannels(
            MCConstsVO(
                channel = MCConstsChannelDataList(
                    comp = listOf(
                        MCConstsChannelData(id = 7, version = 3),
                        MCConstsChannelData(id = 8, version = 2),
                    )
                )
            )
        )
        val json = JSONConverter.encodeToJsonElement(
            normalized.toWorkUpdateDTO(false)
        ).jsonObject
        val channel = json["sync_item_info"]!!.jsonObject["channel"]!!.jsonArray.single().jsonObject

        assertFalse(normalized.hasUnversionedPcImages())
        assertEquals(1, normalized.syncItemInfo.channel.size)
        assertEquals("https://example.com/existing-pc.png", channel["channel_url"]?.jsonPrimitive?.content)
        assertEquals("3", channel["version"]?.jsonPrimitive?.content)
    }

    @Test
    fun dlcInfoAcceptsObjectOrStringValues() {
        val objectValue = JSONConverter.decodeFromString<ResourceDetailDlcInfo>(
            """{"master":{"item_id":"1"},"slave_list":[{"item_id":"2"}]}"""
        )
        val stringValue = JSONConverter.decodeFromString<ResourceDetailDlcInfo>(
            """{"master":"main","slave_list":"slave"}"""
        )

        assertEquals("""{"item_id":"1"}""", objectValue.master)
        assertEquals("""[{"item_id":"2"}]""", objectValue.slaveList)
        assertEquals("main", stringValue.master)
        assertEquals("slave", stringValue.slaveList)
    }

    @Test
    fun validFreeResourcePasses() {
        assertNull(validateWorkSave(validInput))
    }

    @Test
    fun requiredFieldsReturnFirstError() {
        assertEquals("请输入资源名称", validateWorkSave(validInput.copy(itemName = " ")))
        assertEquals(
            "请至少添加一个模组标签",
            validateWorkSave(validInput.copy(tags = listOf(" ")))
        )
        assertEquals(
            "PE 详情至少需要 1 个字符",
            validateWorkSave(validInput.copy(detailHtml = "<p><br>&nbsp;</p>"))
        )
        assertEquals(
            "请上传全部 PE 图片",
            validateWorkSave(validInput.copy(availableChannelIds = emptySet()))
        )
    }

    @Test
    fun partialUpdateCanSkipPeImageCompleteness() {
        assertNull(
            validateWorkSave(
                validInput.copy(
                    channelsLoaded = false,
                    availableChannelIds = emptySet(),
                ),
                peImagePolicy = PeImageCompletenessPolicy.SKIP_UNTOUCHED_CHANNELS,
            )
        )
    }

    @Test
    fun onlyDiamondResourceRequiresVideo() {
        val diamond = validInput.copy(priceType = PriceTypeEnum.DIAMOND, priceRank = 0)
        assertEquals("付费资源必须上传视频", validateWorkSave(diamond))
        assertNull(validateWorkSave(diamond.copy(hasVideo = true)))
    }

    /** 绿宝石定价停止提交：新建与更新一律拦截，存量作品需先改定价才能保存。 */
    @Test
    fun emeraldPriceIsRejectedForSave() {
        val emerald = validInput.copy(priceType = PriceTypeEnum.EMERALD, priceRank = -5, price = 1)
        assertEquals(EMERALD_PRICE_REJECTED_MESSAGE, validateWorkSave(emerald))
    }

    /** 存量绿宝石作品仍可编辑，但原详情判定为绿宝石；普通作品不受影响。 */
    @Test
    fun legacyEmeraldDetailIsDetectedForForm() {
        val legacy = WorkDetailState(
            detail = ResourceDetailVO(itemId = "1", priceType = "point", priceRank = -5, price = 10),
            priceType = PriceTypeEnum.EMERALD,
            priceRank = PriceRankEnum.EMERALD_TIER,
            emeraldPrice = 10,
        )

        assertTrue(legacy.isLegacyEmeraldPricing)
        // 编辑态未被强制只读，用户仍可修改其他字段并改定价后保存
        assertFalse(legacy.readOnly)
        val normal = legacy.copy(
            detail = ResourceDetailVO(itemId = "2", priceType = "diamond", priceRank = 0, price = 300),
            priceType = PriceTypeEnum.DIAMOND,
            priceRank = PriceRankEnum.DIAMOND_TIER_ONE,
        )
        assertFalse(normal.isLegacyEmeraldPricing)
    }

    @Test
    fun pcSyncRequiresValidCategories() {
        val syncing = validInput.copy(syncPc = true)
        assertEquals(
            "无法匹配 PC 模组类别，请确认 PE 资源类别后重试",
            validateWorkSave(syncing)
        )
        assertEquals(
            "请选择 PC 具体类别",
            validateWorkSave(
                syncing.copy(
                    pcResourceType = 2,
                    validPcResourceTypeIds = setOf(2),
                    pcResourceSubType = 0,
                    validPcResourceSubTypeIds = setOf(3),
                )
            )
        )
        assertNull(
            validateWorkSave(
                syncing.copy(
                    pcResourceType = 2,
                    validPcResourceTypeIds = setOf(2),
                    pcResourceSubType = 3,
                    validPcResourceSubTypeIds = setOf(3),
                )
            )
        )
    }

    // ===== PE 前置模组（pri_type=9） =====

    /** 前置模组豁免标签/推荐标签/宣传图/定价/视频必填，仅名称+详情+资源文件校验。 */
    @Test
    fun prerequisiteItemSkipsStandardListingValidation() {
        val prerequisite = WorkSaveValidationInput(
            itemName = "【前置】自定义任务框架",
            tags = emptyList(),
            priceType = PriceTypeEnum.FREE,
            priceRank = PriceRankEnum.FREE_TIER.type,
            price = 0,
            detailHtml = "<p>前置组件</p>",
            peResourceType = PE_PREREQUISITE_PRI_TYPE,
            recommendTagIds = emptyList(),
            gameplayTagIds = emptySet(),
            themeTagIds = emptySet(),
            modVersion = "",
            hasResource = true,
            channelsLoaded = false,
            requiredChannelIds = emptySet(),
            availableChannelIds = emptySet(),
            hasVideo = false,
        )
        assertNull(validateWorkSave(prerequisite))
        // 名称 / 详情 / 资源文件仍为必填
        assertEquals(
            "请输入资源名称",
            validateWorkSave(prerequisite.copy(itemName = ""))
        )
        assertEquals(
            "PE 详情至少需要 1 个字符",
            validateWorkSave(prerequisite.copy(detailHtml = "<p></p>"))
        )
        assertEquals(
            "请上传 PE 资源文件",
            validateWorkSave(prerequisite.copy(hasResource = false))
        )
        // 同类数据走常规类别时仍按常规必填拦截（对照）
        assertEquals(
            "请至少添加一个模组标签",
            validateWorkSave(prerequisite.copy(peResourceType = 2))
        )
    }

    /** 前置模组新建请求体：强制免费，且回写 prerequisite_items / prerequisite_item_ids。 */
    @Test
    fun prerequisiteItemForcesFreePriceInCreatePayload() {
        val state = WorkDetailState(
            peResourceType = PE_PREREQUISITE_PRI_TYPE,
            itemName = "【前置】自定义任务框架",
            priceType = PriceTypeEnum.DIAMOND,
            priceRank = PriceRankEnum.DIAMOND_TIER_FIVE,
            emeraldPrice = 1000,
            prerequisiteItemId = "4679556122208731470",
            prerequisiteItemName = "【苦柠】自定义任务",
        )

        val create = buildWorkCreatePayload(state, isCheckApply = false)

        assertEquals("free", create.priceType)
        assertEquals(PriceRankEnum.FREE_TIER.type, create.priceRank)
        assertEquals(0, create.price)
        assertEquals(listOf("4679556122208731470"), create.prerequisiteItemIds.map { it.jsonPrimitive.content })
        assertEquals(
            "【苦柠】自定义任务",
            create.prerequisiteItems.first().jsonObject["item_name"]?.jsonPrimitive?.content
        )
    }

    /** 普通作品同样能声明前置（前置关系由普通作品持有，前置模组自身是被依赖方）。 */
    @Test
    fun normalItemCarriesPrerequisiteItemsInPayloads() {
        val state = WorkDetailState(
            peResourceType = 2,
            prerequisiteItemId = "4690901940785715551",
            prerequisiteItemName = "【前置】自定义任务框架",
            priceType = PriceTypeEnum.FREE,
            priceRank = PriceRankEnum.FREE_TIER,
        )

        val create = buildWorkCreatePayload(state, isCheckApply = false)
        val update = buildUpdatePayload(state, ResourceDetailVO(priType = 2), "")

        assertEquals(
            "4690901940785715551",
            create.prerequisiteItems.first().jsonObject["item_id"]?.jsonPrimitive?.content
        )
        assertEquals(
            "4690901940785715551",
            update.prerequisiteItems.first().jsonObject["item_id"]?.jsonPrimitive?.content
        )
        // 未选前置时不下发该字段，避免清空既有关系
        val none = buildWorkCreatePayload(WorkDetailState(peResourceType = 2), isCheckApply = false)
        assertEquals(emptyList(), none.prerequisiteItems)
        assertEquals(emptyList(), none.prerequisiteItemIds)
    }

    /** 前置模组更新请求体：强制免费档，且不残留旧折扣。 */
    @Test
    fun prerequisiteItemForcesFreePriceInUpdatePayload() {
        val detail = ResourceDetailVO(
            itemId = "4690901940785715551",
            priType = PE_PREREQUISITE_PRI_TYPE,
            priceType = "diamond",
            priceRank = 4,
            price = 5000,
        )
        val update = buildUpdatePayload(
            WorkDetailState(
                peResourceType = PE_PREREQUISITE_PRI_TYPE,
                itemName = "【前置】自定义任务框架",
                priceType = PriceTypeEnum.DIAMOND,
                priceRank = PriceRankEnum.DIAMOND_TIER_FIVE,
            ),
            detail,
            "",
        )

        assertEquals(PriceRankEnum.FREE_TIER.type, update.priceRank)
        assertEquals(0, update.price)
    }

    /** 详情回显：prerequisite_items[{item_id,item_name}] → state 的两个字符串字段。 */
    @Test
    fun prerequisiteItemsParseBackToState() {
        val items = listOf(
            kotlinx.serialization.json.buildJsonObject {
                put("item_id", kotlinx.serialization.json.JsonPrimitive("4690901940785715551"))
                put("item_name", kotlinx.serialization.json.JsonPrimitive("【前置】自定义任务框架"))
            }
        )
        assertEquals("4690901940785715551", parsePrerequisiteItemId(items))
        assertEquals("【前置】自定义任务框架", parsePrerequisiteItemName(items))
        // 结构异常 / 空列表 → 空串，不抛异常
        assertEquals("", parsePrerequisiteItemId(emptyList()))
        assertEquals(
            "",
            parsePrerequisiteItemId(listOf(kotlinx.serialization.json.JsonPrimitive("x")))
        )
    }

    /** 前置模组提交归一化：隐藏区块的字段不得沿用详情旧值。 */
    @Test
    fun prerequisiteNormalizationClearsHiddenFields() {
        val detail = ResourceDetailVO(
            itemId = "4690901940785715551",
            priType = PE_PREREQUISITE_PRI_TYPE,
            isOriginal = false,
            corpProofImage = "https://example.com/proof.png",
            priceType = "diamond",
            priceRank = 4,
            price = 5000,
            antiCheatEnable = 1,
            achievementEnabled = 1,
            achievementBackgroundUrl = "https://example.com/bg.png",
            mountCallEnabled = true,
            peIsAddPlayPlan = true,
            syncPcFlag = true,
            tags = listOf(ResourceDetailTag(name = "任务")),
            labelTypeList = listOf(101, 209),
            channel = listOf(ResourceDetailChannel(channelId = 3, channelUrl = "url")),
            videoInfoList = listOf(ResourceDetailVideoInfo.of(url = "v", size = 0))
        )

        val normalized = detail.normalizedForPrerequisite()

        assertTrue(normalized.isOriginal)
        assertEquals("", normalized.corpProofImage)
        assertEquals("free", normalized.priceType)
        assertEquals(PriceRankEnum.FREE_TIER.type, normalized.priceRank)
        assertEquals(0, normalized.price)
        assertEquals(emptyList(), normalized.discount)
        assertEquals(emptyList(), normalized.channel)
        assertEquals(emptyList(), normalized.videoInfoList)
        assertEquals(0, normalized.antiCheatEnable)
        assertEquals(0, normalized.achievementEnabled)
        assertEquals("", normalized.achievementBackgroundUrl)
        assertEquals(false, normalized.mountCallEnabled)
        assertEquals(false, normalized.peIsAddPlayPlan)
        assertEquals(false, normalized.syncPcFlag)
        assertEquals(emptyList(), normalized.tags)
        assertEquals(emptyList(), normalized.labelTypeList)
        // 名称与资源文件不受影响
        assertEquals("4690901940785715551", normalized.itemId)
    }

    /** 非前置作品不被归一化改写。 */
    @Test
    fun normalizationLeavesNonPrerequisiteUntouched() {
        val detail = ResourceDetailVO(
            priType = 2,
            priceType = "diamond",
            priceRank = 4,
            price = 5000,
            tags = listOf(ResourceDetailTag(name = "任务")),
            channel = listOf(ResourceDetailChannel(channelId = 3, channelUrl = "url"))
        )
        assertEquals(detail, detail.normalizedForPrerequisite())
    }

    /** 前置模组新建 payload：不携带定价/成就/反作弊/同步等隐藏字段。 */
    @Test
    fun prerequisiteCreatePayloadDropsHiddenFields() {
        val state = WorkDetailState(
            peResourceType = PE_PREREQUISITE_PRI_TYPE,
            itemName = "【前置】自定义任务框架",
            isOriginal = false,
            tags = listOf("任务"),
            syncPc = true,
            peMountCallEnabled = true,
            priceType = PriceTypeEnum.DIAMOND,
            priceRank = PriceRankEnum.DIAMOND_TIER_FIVE,
        )

        val create = buildWorkCreatePayload(state, isCheckApply = false)

        assertEquals("free", create.priceType)
        assertEquals(PriceRankEnum.FREE_TIER.type, create.priceRank)
        assertEquals(0, create.price)
        // 归一化在提交链路完成（buildWorkCreatePayload 之后），此处校验归一化后的结果
        val normalized = buildUpdatePayload(state, ResourceDetailVO(priType = PE_PREREQUISITE_PRI_TYPE), "")
            .normalizedForPrerequisite()
        assertEquals(false, normalized.syncPcFlag)
        assertEquals(false, normalized.mountCallEnabled)
        assertEquals(0, normalized.antiCheatEnable)
        assertEquals(emptyList(), normalized.tags)
    }

    // ===== 详情 HTML 清洗（平台白名单） =====

    /** 编辑器输出的 b/i/u/s 收敛为平台接受的 strong/em/样式 span（否则提交报 bad tag）。 */
    @Test
    fun sanitizeConvertsEditorInlineTags() {
        assertEquals("<strong>x</strong>", sanitizeDetailHtml("<b>x</b>"))
        assertEquals("<em>x</em>", sanitizeDetailHtml("<i>x</i>"))
        assertEquals(
            "<span style=\"text-decoration: underline;\">x</span>",
            sanitizeDetailHtml("<u>x</u>")
        )
        assertEquals(
            "<span style=\"text-decoration: line-through;\">x</span>",
            sanitizeDetailHtml("<s>x</s>")
        )
        // 已是白名单的标签原样保留
        assertEquals("<p><strong>x</strong></p>", sanitizeDetailHtml("<p><strong>x</strong></p>"))
        assertEquals("<em>x</em>", sanitizeDetailHtml("<em>x</em>"))
    }

    /** 图片只保留 src（去 width/height/alt），其余标签去壳保留文本。 */
    @Test
    fun sanitizeStripsImageAttrsAndUnknownTags() {
        assertEquals(
            "<img src=\"data:image/png;base64,iVBORw0KGgo=\">",
            sanitizeDetailHtml(
                "<img src=\"data:image/png;base64,iVBORw0KGgo=\" width=\"0\" height=\"0\" alt=\"\">"
            )
        )
        // 未知标签去壳保留内容，不丢用户文本
        assertEquals("x", sanitizeDetailHtml("<div>x</div>"))
        assertEquals("x", sanitizeDetailHtml("<ul><li>x</li></ul>"))
        // 去壳后文本保留，与相邻白名单标签拼接
        assertEquals("x<p>x</p>", sanitizeDetailHtml("<h1>x</h1><p>x</p>"))
        assertEquals("x", sanitizeDetailHtml("<a href=\"u\">x</a>"))
        // script/style 连同内容整体丢弃
        assertEquals("p", sanitizeDetailHtml("<script>alert(1)</script>p"))
        assertEquals("p", sanitizeDetailHtml("<style>a{}</style>p"))
    }

    /** 平台实际放行的样例必须原样通过（p / img / span[style] / br）。 */
    @Test
    fun sanitizeAcceptsPlatformAcceptedMarkup() {
        val accepted = "<p><img src=\"https://x19.fp.ps.netease.com/file/698a0d59aa74a4881fef19760fY2slO407\"></p>" +
            "<p><img src=\"https://x19.fp.ps.netease.com/file/6ab123b37996919380c50dc4phmKMM4207\"></p>" +
            "<p>附魔书越攒越多，箱子翻来翻去，却总找不到想要的那一本？</p>" +
            "<p><span style=\"background-color: white; color: white;\">魔咒图书馆、苦柠</span></p>"
        assertEquals(accepted, sanitizeDetailHtml(accepted))

        assertEquals("<p>a<br>b</p>", sanitizeDetailHtml("<p>a<br>b</p>"))
    }

    /**
     * 内联样式归一化：键名统一为 `background-color`（线上实测只用该键名，编辑器会简写成
     * `background`），并给 `rgb()`/`rgba()` 的逗号补空格 —— 编辑器解码器要求逗号后带空白，
     * 否则 `rgb(255,255,255)` 会被解析成 `#225555` 这类错误颜色。
     */
    @Test
    fun normalizeStyleCssKeepsEditorOutputReparsable() {
        assertEquals(
            "background-color: rgba(255, 0, 0, 1.0)",
            normalizeStyleCss("background: rgba(255, 0, 0, 1.0)")
        )
        assertEquals(
            "background-color: rgb(255, 255, 255)",
            normalizeStyleCss("background-color: rgb(255,255,255)")
        )
        assertEquals("color: #FF0000", normalizeStyleCss("color: #FF0000"))
        // 已规范的写法不再改动
        assertEquals(
            "background-color: rgba(0, 0, 0, 0.5)",
            normalizeStyleCss("background-color: rgba(0, 0, 0, 0.5)")
        )
        // `background-clip` 等含 background 前缀的属性名不被误伤
        assertEquals("background-clip: text", normalizeStyleCss("background-clip: text"))
    }

    /** 编辑器输出的 rgba 颜色经清洗后仍能通过白名单，且键名已归一。 */
    @Test
    fun sanitizeNormalizesEditorStyle() {
        assertEquals(
            "<p><span style=\"background-color: rgba(255, 0, 0, 1.0);\">x</span></p>",
            sanitizeDetailHtml("<p><span style=\"background: rgba(255, 0, 0, 1.0);\">x</span></p>")
        )
    }

    // ===== mc_consts 请求缓存 =====

    /** 缓存命中后不再请求；clear() 后重新请求。 */
    @Test
    fun mcConstsCacheServesSecondCallWithoutRefetch() = runTest {
        MCConstsCache.clear()
        var fetches = 0
        val consts = MCConstsVO(itemTagLimit = 3)
        val fetch: suspend () -> ResponseData<MCConstsVO> = {
            fetches++
            ResponseData("ok", consts)
        }

        assertEquals(consts, MCConstsCache.loadOrFetch(fetch).data)
        assertEquals(consts, MCConstsCache.loadOrFetch(fetch).data)
        assertEquals(1, fetches)

        MCConstsCache.clear()
        MCConstsCache.loadOrFetch(fetch)
        assertEquals(2, fetches)
    }

    /** 失败响应不写入缓存，下次仍会重试；成功后按正常缓存。 */
    @Test
    fun mcConstsCacheDoesNotCacheFailures() = runTest {
        MCConstsCache.clear()
        var fetches = 0
        val consts = MCConstsVO(itemTagLimit = 3)

        MCConstsCache.loadOrFetch {
            fetches++
            ResponseData("error", msg = "服务器开小差了")
        }
        MCConstsCache.loadOrFetch {
            fetches++
            ResponseData("401")
        }
        assertEquals(2, fetches)

        MCConstsCache.loadOrFetch {
            fetches++
            ResponseData("ok", consts)
        }
        assertEquals(3, fetches)

        assertEquals(consts, MCConstsCache.loadOrFetch { error("命中缓存时不应再请求") }.data)
        assertEquals(3, fetches)
    }

    // ===== 收益/数据总览剔除前置模组 =====

    @Test
    fun resourceDataRecognizesPrerequisiteType() {
        assertTrue(ResourceData(priType = PE_PREREQUISITE_PRI_TYPE).isPrerequisite())
        assertFalse(ResourceData(priType = 1).isPrerequisite())
        assertFalse(ResourceData().isPrerequisite())
    }

    /** 排除开关开启时剔除前置模组；关闭时保留（作品管理需可见）。 */
    @Test
    fun filterResourceListExcludesPrerequisitesOnlyWhenRequested() {
        val normal = ResourceData(itemId = "1", itemName = "普通", onlineTime = "2026-01-01", priType = 1)
        val prerequisite = ResourceData(itemId = "2", itemName = "前置", onlineTime = "2026-01-01", priType = 9)
        val draft = ResourceData(itemId = "3", itemName = "草稿", onlineTime = "UNKNOWN", priType = 1)
        val items = listOf(normal, prerequisite, draft)

        assertEquals(
            listOf(normal, draft),
            filterResourceList(items, onlineOnly = false, excludePrerequisites = true)
        )
        assertEquals(items, filterResourceList(items, onlineOnly = false, excludePrerequisites = false))
        // 前置模组自身也已上架时，onlineOnly 与剔除条件同时生效
        assertEquals(
            listOf(normal),
            filterResourceList(items, onlineOnly = true, excludePrerequisites = true)
        )
    }

    @Test
    fun excludePrerequisiteRowsDropsMatchingIids() {
        val keep = ResMonthAnalyzeData(avgDayBuy = 1, iid = "1", monthId = "202608", resName = "普通", totalDiamond = 100)
        val drop = ResMonthAnalyzeData(avgDayBuy = 0, iid = "9", monthId = "202608", resName = "前置", totalDiamond = 0)
        val rows = listOf(keep, drop)

        assertEquals(listOf(keep), excludePrerequisiteRows(rows, setOf("9")))
        // 空集合短路：原样返回
        assertEquals(rows, excludePrerequisiteRows(rows, emptySet()))
    }

    // ===== 详情富文本图片 filepicker 上传校验 =====

    @Test
    fun ensureHtmlImagesOnFilepickerLeavesNonBase64HtmlIntact() = runTest {
        var uploadCalls = 0
        val fakeRepo = object : FileUploadRepository {
            override suspend fun uploadFile(
                fileType: String,
                fileName: String,
                file: PlatformFile,
                mimeType: String,
                secure: String
            ): NetworkState<FileInfoDTO> = error("不应调用")

            override suspend fun uploadBytes(
                fileType: String,
                fileName: String,
                bytes: ByteArray,
                mimeType: String,
                secure: String
            ): NetworkState<FileInfoDTO> {
                uploadCalls++
                return error("不应调用")
            }
        }

        val html = "<p><img src=\"https://x19.fp.ps.netease.com/file/test\"></p><p>纯文本</p>"
        val result = ensureHtmlImagesOnFilepicker(html, fakeRepo)
        assertTrue(result is NetworkState.Success)
        assertEquals(html, result.data)
        assertEquals(0, uploadCalls)
    }

    @Test
    fun ensureHtmlImagesOnFilepickerUploadsBase64Images() = runTest {
        var uploadCalls = 0
        val fakeRepo = object : FileUploadRepository {
            override suspend fun uploadFile(
                fileType: String,
                fileName: String,
                file: PlatformFile,
                mimeType: String,
                secure: String
            ): NetworkState<FileInfoDTO> = error("不应调用")

            override suspend fun uploadBytes(
                fileType: String,
                fileName: String,
                bytes: ByteArray,
                mimeType: String,
                secure: String
            ): NetworkState<FileInfoDTO> {
                uploadCalls++
                assertEquals("image", fileType)
                assertEquals("image/png", mimeType)
                return NetworkState.Success(
                    FileInfoDTO(
                        body = """{"url":"https://x19.fp.ps.netease.com/file/uploaded_$uploadCalls"}""",
                        fileType = "image",
                        sign = "sign"
                    )
                )
            }
        }

        val html = "<p><img src=\"data:image/png;base64,aGVsbG8=\"></p>"
        val result = ensureHtmlImagesOnFilepicker(html, fakeRepo)
        assertTrue(result is NetworkState.Success)
        assertEquals(
            "<p><img src=\"https://x19.fp.ps.netease.com/file/uploaded_1\"></p>",
            result.data
        )
        assertEquals(1, uploadCalls)
    }

    @Test
    fun buildUpdatePayloadFiltersEmptyPeImageSlots() {
        val detail = ResourceDetailVO(
            itemId = "item-1",
            channel = listOf(
                ResourceDetailChannel(channelId = 1, channelUrl = "https://x19.fp.ps.netease.com/1", version = 1)
            )
        )
        val state = WorkDetailState(
            itemId = "item-1",
            peImageSlots = listOf(
                ChannelImageSlot(channelId = 1, title = "t1", width = 100, height = 100, channelUrl = "https://x19.fp.ps.netease.com/1", version = 1),
                ChannelImageSlot(channelId = 2, title = "t2", width = 100, height = 100, channelUrl = "", version = 1), // 空槽位
            )
        )
        val payload = buildUpdatePayload(state, detail, "")
        assertEquals(1, payload.channel.size)
        assertEquals(1, payload.channel.first().channelId)
    }

    @Test
    fun videoInfoListDecodesPlainStringForm() {
        val detail = JSONConverter.decodeFromString<ResourceDetailVO>(
            """{"item_id":"1","video_info_list":[{"cover":"https://x19.fp.ps.netease.com/c.jpg","size":123,"url":"https://x19.fp.ps.netease.com/v.mp4"}]}"""
        )

        assertEquals(1, detail.videoInfoList.size)
        assertEquals("https://x19.fp.ps.netease.com/v.mp4", detail.videoInfoList.first().urlAddress)
        assertEquals("https://x19.fp.ps.netease.com/c.jpg", detail.videoInfoList.first().coverAddress)
        assertEquals(123, detail.videoInfoList.first().size)
    }

    @Test
    fun videoInfoListDecodesUploadReceiptObjectForm() {
        val detail = JSONConverter.decodeFromString<ResourceDetailVO>(
            """{"item_id":"1","video_info_list":[{"url":{"url":"https://x19.fp.ps.netease.com/v.mp4","body":"{\"fsize\":123}"},"size":123,"cover":{"url":"https://x19.fp.ps.netease.com/c.jpg"}}]}"""
        )

        assertEquals(1, detail.videoInfoList.size)
        assertEquals("https://x19.fp.ps.netease.com/v.mp4", detail.videoInfoList.first().urlAddress)
        assertEquals("https://x19.fp.ps.netease.com/c.jpg", detail.videoInfoList.first().coverAddress)
        assertEquals(123, detail.videoInfoList.first().size)
    }

    @Test
    fun videoInfoListDecodesElementWithoutCover() {
        val detail = JSONConverter.decodeFromString<ResourceDetailVO>(
            """{"item_id":"1","video_info_list":[{"url":"https://x19.fp.ps.netease.com/v.mp4","size":123}]}"""
        )

        assertEquals(1, detail.videoInfoList.size)
        assertEquals("https://x19.fp.ps.netease.com/v.mp4", detail.videoInfoList.first().urlAddress)
        assertEquals("", detail.videoInfoList.first().coverAddress)
    }

    @Test
    fun videoInfoListSerializesUrlAndCoverAsPlainStrings() {
        val element = JSONConverter.encodeToJsonElement(
            ResourceDetailVideoInfo.of(
                cover = "https://x19.fp.ps.netease.com/c.jpg",
                size = 123,
                url = "https://x19.fp.ps.netease.com/v.mp4"
            )
        ).jsonObject

        assertEquals("https://x19.fp.ps.netease.com/v.mp4", element["url"]?.jsonPrimitive?.content)
        assertEquals("https://x19.fp.ps.netease.com/c.jpg", element["cover"]?.jsonPrimitive?.content)
        assertEquals(123, element["size"]?.jsonPrimitive?.int)
    }

    @Test
    fun updatePayloadSubmitsUploadReceiptForNewlyUploadedVideo() {
        val detail = ResourceDetailVO(itemId = "item-1")
        val state = WorkDetailState(
            itemId = "item-1",
            videos = listOf(
                VideoItem(
                    cover = "https://x19.fp.ps.netease.com/c.jpg",
                    size = 123,
                    url = "https://x19.fp.ps.netease.com/v.mp4",
                    fileInfo = FileInfoDTO(
                        body = """{"url":"https://x19.fp.ps.netease.com/v.mp4","fsize":123}""",
                        fileType = "video",
                        sign = "sig-1"
                    ),
                    coverFileInfo = FileInfoDTO(
                        body = """{"url":"https://x19.fp.ps.netease.com/c.jpg"}""",
                        fileType = "image",
                        sign = "sig-2"
                    )
                )
            )
        )

        val payload = buildUpdatePayload(state, detail, "")
        val video = JSONConverter.encodeToJsonElement(payload.videoInfoList.single()).jsonObject

        val url = video["url"]!!.jsonObject
        assertEquals("""{"url":"https://x19.fp.ps.netease.com/v.mp4","fsize":123}""", url["body"]?.jsonPrimitive?.content)
        assertEquals("video", url["file_type"]?.jsonPrimitive?.content)
        assertEquals("sig-1", url["sign"]?.jsonPrimitive?.content)
        val cover = video["cover"]!!.jsonObject
        assertEquals("image", cover["file_type"]?.jsonPrimitive?.content)
        assertEquals("sig-2", cover["sign"]?.jsonPrimitive?.content)
    }

    @Test
    fun updatePayloadSubmitsAddressStringForExistingVideo() {
        val detail = ResourceDetailVO(itemId = "item-1")
        val state = WorkDetailState(
            itemId = "item-1",
            videos = listOf(
                VideoItem(
                    cover = "https://x19.fp.ps.netease.com/c.jpg",
                    size = 123,
                    url = "https://x19.fp.ps.netease.com/v.mp4"
                )
            )
        )

        val payload = buildUpdatePayload(state, detail, "")
        val video = JSONConverter.encodeToJsonElement(payload.videoInfoList.single()).jsonObject

        assertEquals("https://x19.fp.ps.netease.com/v.mp4", video["url"]?.jsonPrimitive?.content)
        assertEquals("https://x19.fp.ps.netease.com/c.jpg", video["cover"]?.jsonPrimitive?.content)
    }

    /**
     * 用线上 detail 接口的完整真实响应回放，验证整条读取链路（含 res/lobby_res/perf_data/dlc_info 等嵌套结构）不因任何字段解析失败，
     * 且宣传视频被正确读出。
     */
    @Test
    fun realWorldDetailResponseWithVideoDecodesEndToEnd() {
        val realResponse = """
            {"achievement_background_url":"","achievement_configs":[],"achievement_enabled":0,
            "activity_desc":"","activity_only":false,"adv_obtain_num":0,"android_price":0,
            "android_price_type":"","anti_cheat_enable":0,
            "apply_review_time":"2026-11-10T08:10:33.661000+00:00",
            "author_info":"<p>作者</p>","banner_pic":"","body_type":"","can_manage_server":false,
            "can_silent_online":true,"can_synchronize_pc_old":false,"category":"pe","change_log":[],
            "channel":[{"channel_id":3,"channel_url":"https://x19.fp.ps.netease.com/file/a","version":1},
            {"channel_id":5,"channel_url":"https://x19.fp.ps.netease.com/file/b","version":1}],
            "charge_desc":"付费备注","charge_type":"worlds","claim_item_enabled":0,
            "collection_id":"0","collection_name":"",
            "create_time":"2026-11-08T06:07:23.935000+00:00","decompose_currency":0,
            "decompose_currency_type":"ordinary","discount":[],"discount_activity_status":"init",
            "dlc_info":{"dlc_switch":false,"dlc_type":"off","master":null,"slave_list":null},
            "dyeing":"","dyeing_origin":false,"dyeing_relation":{},"exchange_currency":0,
            "exchange_currency_type":"ordinary","exempt_perf_review_num":0,"first_online_time":null,
            "first_sell_rank_top":false,"force_encrypt":false,"game_host":null,
            "info":"<p>详情</p>","intercept_fields":[],"ios_jelly_id":"","ios_price":0,
            "ios_price_type":"","is_domain_server_item":0,"is_ea":0,
            "is_in_promotion_application":false,"is_joint_activity":false,
            "is_lobby_competitive":false,"is_lottery_reward":false,"is_official_item":false,
            "is_original":true,"is_persona":false,"is_premium":false,"is_recommend":false,
            "is_season_mod":false,"is_silent_online":false,"is_spigot":false,
            "is_suitable_pc":false,"is_sync":false,"is_vip_benefit":false,
            "item_id":"4691723405681290277","item_name":"【苦柠】魔咒图书馆",
            "item_real_status":99,"item_update_push":true,"item_version":"0.5",
            "joint_activity_name":"","joint_activity_tag":[],"label_type_list":[101,209],
            "level_data":null,"level_data_sync_error":null,"level_data_sync_status":null,
            "level_data_version":null,"lobby_camps":[],"lobby_commercialize":false,
            "lobby_config_op_log":[],"lobby_force_max_num":10,"lobby_max_num":0,"lobby_min_num":0,
            "lobby_normal_mode":false,"lobby_player_num":0,"lobby_reconnect_time":0,
            "lobby_res":{"lobby_manifest_version":"0.0.f0840479","lobby_res_md5":"d10847f5",
            "lobby_res_size":134341,"lobby_res_url":"https://x19.gph.netease.com/item.zip",
            "mcp_signs":["AYhloha9sv19BHgc"]},
            "lobby_sort_key":{},"lobby_tags":[],"lottery_id":0,"main_city":false,
            "maintain":"normal","mc_version":"","mod_second_type":205,"mod_version":"3.9",
            "mount_call_enabled":false,"multi_tags":"0","need_behaviour_uuid":false,
            "need_method_uuid":false,"normal_number":"","ori_weak_offline":false,
            "ori_weak_offline_reason":"","pe_activity_coupon":[],"pe_chat_bubble_id":[],
            "pe_emotes_id":[],"pe_frame_id":[],"pe_furniture_id":[],"pe_game_introduction":"",
            "pe_home_cash":[],"pe_is_add_play_plan":false,"pe_item_id":[],"pe_lottery_chance":[],
            "pe_mc_item_id":[],"pe_one_month_vip_id":[],"pe_passport_ten_id":[],
            "pe_six_month_vip_id":[],"pe_user_background_id":[],
            "perf_data":{"mem_size":60,"mem_warning":false},
            "performance_service_available":0,"performance_service_status":0,
            "persona_mtype":"","persona_mtypeid":0,"persona_stype":"","persona_stypeid":0,
            "play_plan_expire_time":-1,"pre_review_video":"{}","premium_apply_status":"no_apply",
            "prerequisite_items":[],"pri_effect_type":null,"pri_type":2,"price":0,"price_rank":-4,
            "price_type":"free","pure":false,"queue_position":0,"rarity":0,"rating_level":0,
            "res":[{"add_version":true,"cdn_info":{"res_md5":"ed6ff1d3","res_size":127617,
            "res_time":"2026-11-09T09:43:41.348000+00:00"},
            "cdn_url":"https://x19.gph.netease.com/item.original.zip","mc_version":[],"res_id":2,
            "res_info":{"res_md5":"ed6ff1d3","res_size":85707,
            "res_time":"2026-11-09T09:42:25.148000+00:00"},
            "res_name":"enchantLibrary_free.zip","res_url":"res_url"}],
            "running_status":"normal","searchable":true,"season_begin":0,"status":"reviewing",
            "sub_effect_type":null,"sub_type":6,"subject_id":"","suit_id":"0","sync_pc_flag":false,
            "tags":[],"trial_duration":0,"update_summary":"修复漏斗传入问题","urgent_reason":"",
            "urgent_status":0,"vanity_number":"","version_compatible_enable":false,
            "video_info_list":[{"cover":"https://x19.fp.ps.netease.com/file/6af2d16f0638af1de15e50d5t50TF2Nt07",
            "size":41523884,"url":"https://x19.fp.ps.netease.com/file/6af2d1723a1b7c93821b1923N0YxgWea07"}],
            "vip_only":false,"weak_offline":false,"weak_offline_reason":"","whitelist":""}
        """.trimIndent()

        val detail = JSONConverter.decodeFromString<ResourceDetailVO>(realResponse)

        assertEquals(1, detail.videoInfoList.size)
        val video = detail.videoInfoList.first()
        assertEquals("https://x19.fp.ps.netease.com/file/6af2d1723a1b7c93821b1923N0YxgWea07", video.urlAddress)
        assertEquals("https://x19.fp.ps.netease.com/file/6af2d16f0638af1de15e50d5t50TF2Nt07", video.coverAddress)
        assertEquals(41523884, video.size)
        assertEquals("reviewing", detail.status)
    }
}
