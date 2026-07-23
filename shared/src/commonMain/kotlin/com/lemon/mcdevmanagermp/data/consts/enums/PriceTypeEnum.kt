package com.lemon.mcdevmanagermp.data.consts.enums

import kotlinx.serialization.Serializable

@Serializable
enum class PriceTypeEnum(val label: String) {
    DIAMOND("钻石"),
    EMERALD("绿宝石"),
    FREE("免费"),
    UNKNOWN("未知");

    companion object {
        fun fromIntType(code: Int): PriceTypeEnum {
            return when (code) {
                0 -> FREE
                1 -> EMERALD
                2 -> DIAMOND
                else -> UNKNOWN
            }
        }

        fun fromStringType(code: String): PriceTypeEnum {
            return when (code) {
                "free" -> FREE
                "point" -> EMERALD
                "diamond" -> DIAMOND
                else -> UNKNOWN
            }
        }
    }
}

@Serializable
enum class PriceRankEnum(val type: Int, val label: String, val diamondPrice: Int = 0) {
    DIAMOND_TIER_ONE(0, "300 钻石", 300),
    DIAMOND_TIER_TWO(1, "600 钻石", 600),
    DIAMOND_TIER_THREE(2, "1000 钻石", 1000),
    DIAMOND_TIER_FOUR(3, "2000 钻石", 2000),
    DIAMOND_TIER_FIVE(4, "5000 钻石", 5000),
    DIAMOND_TIER_SIX(5, "10000 钻石", 10000),
    DIAMOND_TIER_SEVEN(6, "20000 钻石", 20000),
    FREE_TIER(-4, "免费"),
    EMERALD_TIER(-5, "绿宝石"),
    UNKNOWN(-999, "未知");

    companion object {
        fun fromIntType(code: Int): PriceRankEnum {
            return when (code) {
                0 -> DIAMOND_TIER_ONE
                1 -> DIAMOND_TIER_TWO
                2 -> DIAMOND_TIER_THREE
                3 -> DIAMOND_TIER_FOUR
                4 -> DIAMOND_TIER_FIVE
                5 -> DIAMOND_TIER_SIX
                6 -> DIAMOND_TIER_SEVEN
                -4 -> FREE_TIER
                -5 -> EMERALD_TIER
                else -> UNKNOWN
            }
        }
    }
}

/** priceType 字符串 → 中文展示文案；未知值原样返回（避免把异常值显示成"未知"）。 */
fun priceTypeLabel(priceType: String): String {
    val t = PriceTypeEnum.fromStringType(priceType)
    return if (t == PriceTypeEnum.UNKNOWN) priceType else t.label
}