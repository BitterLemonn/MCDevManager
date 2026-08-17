package com.lemon.mcdevmanagermp.domain.work

import com.lemon.mcdevmanagermp.data.consts.enums.PriceTypeEnum

data class WorkSaveValidationInput(
    val itemName: String,
    val tags: List<String>,
    val priceType: PriceTypeEnum,
    val priceRank: Int,
    val price: Int,
    val detailHtml: String,
    val peResourceType: Int,
    val recommendTagIds: List<Int>,
    val gameplayTagIds: Set<Int>,
    val themeTagIds: Set<Int>,
    val modVersion: String,
    val hasResource: Boolean,
    val channelsLoaded: Boolean,
    val requiredChannelIds: Set<Int>,
    val availableChannelIds: Set<Int>,
    val hasVideo: Boolean,
    val syncPc: Boolean = false,
    val originallySyncedToPc: Boolean = false,
    val pcResourceType: Int = 0,
    val validPcResourceTypeIds: Set<Int> = emptySet(),
    val pcResourceSubType: Int = 0,
    val validPcResourceSubTypeIds: Set<Int> = emptySet(),
)

enum class PeImageCompletenessPolicy {
    REQUIRE_COMPLETE,
    SKIP_UNTOUCHED_CHANNELS,
}

fun validateWorkSave(
    input: WorkSaveValidationInput,
    peImagePolicy: PeImageCompletenessPolicy = PeImageCompletenessPolicy.REQUIRE_COMPLETE,
): String? {
    if (input.itemName.isBlank()) return "请输入资源名称"
    if (input.tags.none { it.isNotBlank() }) return "请至少添加一个模组标签"
    if (input.priceType == PriceTypeEnum.UNKNOWN) return "请选择定价类型"
    when (input.priceType) {
        PriceTypeEnum.DIAMOND -> if (input.priceRank !in 0..6) {
            return "请选择有效的钻石定价档位"
        }

        PriceTypeEnum.EMERALD -> if (input.price <= 0) {
            return "请输入大于 0 的绿宝石价格"
        }

        else -> Unit
    }
    if (!hasVisibleHtmlText(input.detailHtml)) return "PE 详情至少需要 1 个字符"
    if (input.peResourceType <= 0) return "请选择 PE 资源类别"
    if (input.recommendTagIds.none { it in input.gameplayTagIds }) {
        return "请至少选择 1 个玩法推荐标签"
    }
    if (input.recommendTagIds.none { it in input.themeTagIds }) {
        return "请至少选择 1 个主题推荐标签"
    }
    if (input.modVersion.isBlank()) return "请选择 modAPI 版本"
    if (!input.hasResource) return "请上传 PE 资源文件"
    if (peImagePolicy == PeImageCompletenessPolicy.REQUIRE_COMPLETE) {
        if (!input.channelsLoaded) return "PE 图片位配置尚未加载，请稍后重试"
        if (!input.availableChannelIds.containsAll(input.requiredChannelIds)) return "请上传全部 PE 图片"
    }
    if (input.priceType == PriceTypeEnum.DIAMOND && !input.hasVideo) {
        return "付费资源必须上传视频"
    }
    if (!input.syncPc) return null
    if (input.pcResourceType <= 0) {
        return "无法匹配 PC 模组类别，请确认 PE 资源类别后重试"
    }
    if (input.originallySyncedToPc) return null
    if (input.pcResourceType !in input.validPcResourceTypeIds) {
        return "无法匹配 PC 模组类别，请确认 PE 资源类别后重试"
    }
    if (
        input.validPcResourceSubTypeIds.isNotEmpty() &&
        input.pcResourceSubType !in input.validPcResourceSubTypeIds
    ) {
        return "请选择 PC 具体类别"
    }
    return null
}

private fun hasVisibleHtmlText(html: String): Boolean = html
    .replace(HTML_TAG, "")
    .replace("&nbsp;", " ", ignoreCase = true)
    .replace("&#160;", " ", ignoreCase = true)
    .replace("&amp;", "&", ignoreCase = true)
    .replace("&lt;", "<", ignoreCase = true)
    .replace("&gt;", ">", ignoreCase = true)
    .trim()
    .isNotEmpty()

private val HTML_TAG = Regex("<[^>]*>")
