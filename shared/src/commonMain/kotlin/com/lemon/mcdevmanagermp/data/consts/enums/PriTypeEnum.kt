package com.lemon.mcdevmanagermp.data.consts.enums

import kotlinx.serialization.Serializable

/**
 * PE「前置模组」资源类别 id。
 *
 * 注意：它**不在** `mc_consts.pri_type.pe` 中声明（服务端只暴露 1/2/3/4/6/7）。网易开发者平台
 * 前端在账号具备 `users/me.prerequisite_switch`（新建）或作品本身已是该类别（编辑）时，
 * 动态向下拉注入 `{id:9, title:"前置模组"}` 后提交，服务端原样落库，因此详情回读 `pri_type=9`。
 * 编辑既有作品时若不下拉注入，用户一旦重选类别就会把它降级成常规类别，故这里必须补回。
 */
const val PE_PREREQUISITE_PRI_TYPE: Int = 9

/** 见 [PE_PREREQUISITE_PRI_TYPE]。 */
const val PE_PREREQUISITE_LABEL: String = "前置模组"

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
