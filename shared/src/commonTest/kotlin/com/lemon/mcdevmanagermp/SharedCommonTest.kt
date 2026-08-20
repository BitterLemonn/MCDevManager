package com.lemon.mcdevmanagermp

import com.lemon.mcdevmanagermp.data.common.JSONConverter
import com.lemon.mcdevmanagermp.data.consts.enums.PriceTypeEnum
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
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailVO
import com.lemon.mcdevmanagermp.domain.analyze.mergeRealtimeIncome
import com.lemon.mcdevmanagermp.domain.main.mergeProfitDiamonds
import com.lemon.mcdevmanagermp.domain.work.PeImageCompletenessPolicy
import com.lemon.mcdevmanagermp.domain.work.WorkSaveValidationInput
import com.lemon.mcdevmanagermp.domain.work.hasUnversionedPcImages
import com.lemon.mcdevmanagermp.domain.work.validateWorkSave
import com.lemon.mcdevmanagermp.domain.work.withCurrentPcImageChannels
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailState
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.buildUpdatePayload
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.buildWorkCreatePayload
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.validateLobbySettings
import com.lemon.mcdevmanagermp.utils.extension.dumpAndGetCookiesValue
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
    fun pePlayPlanIsAlwaysFalseInUpdateJson() {
        val json = JSONConverter.encodeToJsonElement(
            ResourceDetailVO(peIsAddPlayPlan = true).toWorkUpdateDTO(false)
        ).jsonObject

        assertFalse(json["pe_is_add_play_plan"]!!.jsonPrimitive.boolean)
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
}
