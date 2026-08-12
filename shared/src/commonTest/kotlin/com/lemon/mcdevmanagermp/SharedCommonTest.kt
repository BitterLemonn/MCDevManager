package com.lemon.mcdevmanagermp

import com.lemon.mcdevmanagermp.data.consts.enums.PriceRankEnum
import com.lemon.mcdevmanagermp.data.consts.enums.PriceTypeEnum
import com.lemon.mcdevmanagermp.data.vo.netease.resource.MCConstsCommonTitleData
import com.lemon.mcdevmanagermp.data.vo.netease.resource.MCConstsRecommendTagData
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.ChannelImageSlot
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.PeResourceFile
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.VideoItem
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailState
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.validateWorkDetail
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SharedCommonTest {

    private val validState = WorkDetailState(
        itemName = "测试资源",
        tags = listOf("玩法"),
        priceType = PriceTypeEnum.FREE,
        priceRank = PriceRankEnum.FREE_TIER,
        peDetail = "<p>详情</p>",
        peResourceType = 1,
        peRecommendTags = listOf(11, 21),
        peRecommendTagOptions = MCConstsRecommendTagData(
            gameplayTag = listOf(MCConstsCommonTitleData(11, "玩法")),
            themeTag = listOf(MCConstsCommonTitleData(21, "主题"))
        ),
        peModVersion = "1.0",
        peResource = PeResourceFile(name = "resource.zip", url = "https://example/res"),
        peImageSlots = listOf(
            ChannelImageSlot(1, "封面", 100, 100, "https://example/img")
        )
    )

    @Test
    fun validFreeResourcePasses() {
        assertNull(validateWorkDetail(validState))
    }

    @Test
    fun requiredFieldsReturnFirstError() {
        assertEquals("请输入资源名称", validateWorkDetail(validState.copy(itemName = " ")))
        assertEquals(
            "PE 详情至少需要 1 个字符",
            validateWorkDetail(validState.copy(peDetail = "<p><br></p>"))
        )
        assertEquals(
            "请上传全部 PE 图片",
            validateWorkDetail(
                validState.copy(
                    peImageSlots = validState.peImageSlots.map { it.copy(channelUrl = "") }
                )
            )
        )
    }

    @Test
    fun paidResourceRequiresPriceAndVideo() {
        val diamond = validState.copy(
            priceType = PriceTypeEnum.DIAMOND,
            priceRank = PriceRankEnum.DIAMOND_TIER_ONE
        )
        assertEquals("付费资源必须上传视频", validateWorkDetail(diamond))
        assertNull(
            validateWorkDetail(
                diamond.copy(videos = listOf(VideoItem(url = "https://example/video")))
            )
        )

        val emerald = validState.copy(priceType = PriceTypeEnum.EMERALD, emeraldPrice = 0)
        assertEquals("请输入大于 0 的绿宝石价格", validateWorkDetail(emerald))
    }
}
