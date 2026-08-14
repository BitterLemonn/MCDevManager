package com.lemon.mcdevmanagermp

import com.lemon.mcdevmanagermp.data.common.JSONConverter
import com.lemon.mcdevmanagermp.data.consts.enums.PriceTypeEnum
import com.lemon.mcdevmanagermp.data.dto.netease.activity.FileInfoDTO
import com.lemon.mcdevmanagermp.data.dto.netease.work.toWorkUpdateDTO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.MCConstsChannelData
import com.lemon.mcdevmanagermp.data.vo.netease.resource.MCConstsChannelDataList
import com.lemon.mcdevmanagermp.data.vo.netease.resource.MCConstsVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailChannel
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailDlcInfo
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailSyncChannel
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailSyncItemInfo
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailVO
import com.lemon.mcdevmanagermp.domain.work.PeImageCompletenessPolicy
import com.lemon.mcdevmanagermp.domain.work.WorkSaveValidationInput
import com.lemon.mcdevmanagermp.domain.work.hasUnversionedPcImages
import com.lemon.mcdevmanagermp.domain.work.validateWorkSave
import com.lemon.mcdevmanagermp.domain.work.withCurrentPcImageChannels
import com.lemon.mcdevmanagermp.utils.extension.dumpAndGetCookiesValue
import kotlinx.serialization.json.encodeToJsonElement
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
    fun paidResourceRequiresPriceAndVideo() {
        val diamond = validInput.copy(priceType = PriceTypeEnum.DIAMOND, priceRank = 0)
        assertEquals("付费资源必须上传视频", validateWorkSave(diamond))
        assertNull(validateWorkSave(diamond.copy(hasVideo = true)))

        val emerald = validInput.copy(priceType = PriceTypeEnum.EMERALD, priceRank = -5)
        assertEquals("请输入大于 0 的绿宝石价格", validateWorkSave(emerald))
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
