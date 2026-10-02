package com.lemon.mcdevmanagermp

import com.lemon.mcdevmanagermp.data.common.JSONConverter
import com.lemon.mcdevmanagermp.data.common.ResponseData
import com.lemon.mcdevmanagermp.data.consts.enums.PE_PREREQUISITE_PRI_TYPE
import com.lemon.mcdevmanagermp.data.consts.enums.PriceRankEnum
import com.lemon.mcdevmanagermp.data.consts.enums.PriceTypeEnum
import com.lemon.mcdevmanagermp.data.consts.enums.WorkItemActionEnum
import com.lemon.mcdevmanagermp.data.consts.enums.WorkItemStatusEnum
import com.lemon.mcdevmanagermp.data.dto.netease.activity.FileInfoDTO
import com.lemon.mcdevmanagermp.data.dto.netease.income.LobbyIncomeResourceListVO
import com.lemon.mcdevmanagermp.data.dto.netease.income.OneResRealtimeIncomeVO
import com.lemon.mcdevmanagermp.data.dto.netease.work.toWorkUpdateDTO
import com.lemon.mcdevmanagermp.data.vo.netease.analyze.ResDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.analyze.ResMonthDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.MCConstsChannelData
import com.lemon.mcdevmanagermp.data.vo.netease.resource.MCConstsChannelDataList
import com.lemon.mcdevmanagermp.data.vo.netease.resource.MCConstsVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailChannel
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailDlcInfo
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailSyncChannel
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailSyncItemInfo
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailTag
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailVideoInfo
import com.lemon.mcdevmanagermp.domain.analyze.mergeRealtimeIncome
import com.lemon.mcdevmanagermp.domain.main.mergeProfitDiamonds
import com.lemon.mcdevmanagermp.domain.resource.MCConstsCache
import com.lemon.mcdevmanagermp.domain.work.PeImageCompletenessPolicy
import com.lemon.mcdevmanagermp.domain.work.WorkSaveValidationInput
import com.lemon.mcdevmanagermp.domain.work.hasUnversionedPcImages
import com.lemon.mcdevmanagermp.domain.work.validateWorkSave
import com.lemon.mcdevmanagermp.domain.work.withCurrentPcImageChannels
import com.lemon.mcdevmanagermp.ui.components.normalizeStyleCss
import com.lemon.mcdevmanagermp.ui.components.sanitizeDetailHtml
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailState
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.buildUpdatePayload
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.buildWorkCreatePayload
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.normalizedForPrerequisite
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.parsePrerequisiteItemId
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.parsePrerequisiteItemName
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.validateLobbySettings
import com.lemon.mcdevmanagermp.utils.extension.dumpAndGetCookiesValue
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

        val emerald = validInput.copy(priceType = PriceTypeEnum.EMERALD, priceRank = -5, price = 1)
        assertNull(validateWorkSave(emerald))
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
            videoInfoList = listOf(ResourceDetailVideoInfo(url = "v"))
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
}
