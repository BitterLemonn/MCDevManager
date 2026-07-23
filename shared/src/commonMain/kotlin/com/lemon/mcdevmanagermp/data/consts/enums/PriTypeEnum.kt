package com.lemon.mcdevmanagermp.data.consts.enums

import kotlinx.serialization.Serializable

/** PE（手游端）资源类别，对应 mc_consts.pri_type.pe。value 为网易接口的字符串 id。 */
@Serializable
enum class PePriTypeEnum(val value: String, val label: String) {
    MAP("1", "地图"),
    ADD_ONS("2", "add_ons"),
    MATERIAL_LIGHT("3", "材质光影"),
    SKIN("4", "皮肤"),
    GIFT_PACK("5", "礼包"),
    LOBBY("6", "联机大厅"),
    PERSONALIZE("7", "个性化");

    companion object {
        fun fromValue(value: String): PePriTypeEnum? = entries.find { it.value == value }
    }
}

/** PC（端游）模组类别，对应 mc_consts.pri_type.comp。value 为网易接口的字符串 id。 */
@Serializable
enum class PcPriTypeEnum(val value: String, val label: String) {
    FUNCTION("3", "功能模组"),
    MAP("5", "地图模组"),
    AVATAR("10", "形象模组"),
    GAMEPLAY("6", "玩法模组"),
    LOBBY("11", "联机大厅"),
    VISUAL("4", "视觉模组");

    companion object {
        fun fromValue(value: String): PcPriTypeEnum? = entries.find { it.value == value }
    }
}
