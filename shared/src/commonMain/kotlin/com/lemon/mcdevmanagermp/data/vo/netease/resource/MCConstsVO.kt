package com.lemon.mcdevmanagermp.data.vo.netease.resource

import com.lemon.mcdevmanagermp.data.common.NoNeedData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class MCConstsVO(
    // 未知
    @SerialName("admin_recover_machine_time")
    val adminRecoverMachineTime: Int = 0,
    // PC模组适用范围
    @SerialName("available_scope")
    val availableScope: List<AvailableScopeData> = emptyList(),
    // 模组上传宣传图对应频道
    val channel: MCConstsChannelDataList = MCConstsChannelDataList(),
    // 商品类型
    @SerialName("charge_type")
    val chargeType: MCConstsChargeTypeData = MCConstsChargeTypeData(),
    // 开发者等级
    @SerialName("developer_class")
    val developerClass: List<MCConstsDeveloperClassData> = emptyList(),
    // 未知
    @SerialName("game_host")
    val gameHost: List<String> = emptyList(),
    // 皮肤品质(网易内部使用)
    @SerialName("item_rarities")
    val itemRarities: List<JsonObject> = emptyList(),
    // 模组标签上限
    @SerialName("item_tag_limit")
    val itemTagLimit: Int = 0,
    // PC模组JAVA版本（元素含字符串 id，与数字 id 的通用类不兼容，用 JsonObject 宽松承载）
    @SerialName("java_version")
    val javaVersion: List<JsonObject> = emptyList(),
    // 模组推荐标签列表
    @SerialName("label_type")
    val labelType: MCConstsRecommendTagData = MCConstsRecommendTagData(),
    // 联机大厅标签列表
    @SerialName("lobby_tags")
    val lobbyTags: List<MCConstsCommonTitleData> = emptyList(),
    // 模组对应游戏版本
    @SerialName("mc_version")
    val mcVersion: List<String> = emptyList(),
    // 玩法模组新子类别
    @SerialName("mod_second_type")
    val modSecondType: MCConstsModSecondTypeDataList = MCConstsModSecondTypeDataList(),
    // 基岩版引擎版本
    @SerialName("mod_version")
    val modVersion: List<String> = emptyList(),
    // 未知
    @SerialName("multi_tags")
    val multiTags: List<String> = emptyList(),
    // 未知
    @SerialName("need_rookie_time")
    val needRookieTime: String = "",
    // 基岩同步PC版本
    @SerialName("pc_relate_version")
    val pcRelateVersion: List<String> = emptyList(),
    // PE网络游戏版本
    @SerialName("pe_multi_version")
    val peMultiVersion: List<String> = emptyList(),
    // 特效资源类型(网易内部使用)
    @SerialName("pri_effect_type")
    val priEffectType: JsonObject = JsonObject(emptyMap()),
    // 套装资源类型(网易内部使用)
    @SerialName("pri_persona_type")
    val priPersonaType: JsonObject = JsonObject(emptyMap()),
    // 资源类型（按平台分组：comp/multi/pe/single）
    @SerialName("pri_type")
    val priType: MCConstsPriTypeData = MCConstsPriTypeData(),
    // 价格类型
    @SerialName("price_type")
    val priceType: List<MCConstsPriceTypeData> = emptyList(),
    // 未知
    @SerialName("realms_only_play_type")
    val realmsOnlyPlayType: NoNeedData = NoNeedData,
    // 未知
    @SerialName("self_recover_machine_time")
    val selfRecoverMachineTime: Int = 0,
    // 未知
    @SerialName("special_channel")
    val specialChannel: JsonObject = JsonObject(emptyMap()),
    // 未知
    @SerialName("start_rookie")
    val startRookie: Boolean = false,
    // 次要特效类型(网易内部使用)
    @SerialName("sub_effect_type")
    val subEffectType: JsonObject = JsonObject(emptyMap()),
    // 次要套装类型(网易内部使用)
    @SerialName("sub_persona_type")
    val subPersonaType: JsonObject = JsonObject(emptyMap()),
    // 次要资源类型
    @SerialName("sub_type")
    val subType: MCConstsCommonPlatformData = MCConstsCommonPlatformData(),
    // 资源标签（按平台分组：comp=电脑端模组 / multi=网络游戏 / single=单机）
    val tag: MCConstsTagData = MCConstsTagData(),
)

/**
 * PC模组适用范围
 */
@Serializable
data class AvailableScopeData(
    @SerialName("id")
    val id: String = "",
    @SerialName("title")
    val title: String = ""
)

/**
 * 模组上传宣传图对应频道
 */
@Serializable
data class MCConstsChannelDataList(
    // 电脑端模组宣传图频道
    val comp: List<MCConstsChannelData> = emptyList(),
    // 电脑端网络游戏宣传图频道
    val multi: List<MCConstsChannelData> = emptyList(),
    // 移动端模组宣传图频道
    val pe: List<MCConstsChannelData> = emptyList(),
    // 移动端网络游戏宣传图频道
    @SerialName("pe_multi")
    val peMulti: List<MCConstsChannelData> = emptyList(),
    // 未知
    @SerialName("resource_component")
    val resourceComponent: List<MCConstsChannelData> = emptyList(),
    // 未知
    val single: List<MCConstsChannelData> = emptyList()
)

/**
 * 模组上传宣传图对应频道数据
 */
@Serializable
data class MCConstsChannelData(
    val id: Int = 0,
    val height: Int = 0,  // 图片高度
    val width: Int = 0,  // 图片宽度
    val title: String = "",  // 图片标题
    val version: Int = 0,  // 图片版本
    @SerialName("vip_only")
    val vipOnly: Boolean = false
)

const val VIDEO_COVER_CHANNEL_ID = 7

fun MCConstsChannelDataList.requiredPeImageChannels(): List<MCConstsChannelData> =
    (pe + peMulti).filterNot { it.id == VIDEO_COVER_CHANNEL_ID }

/**
 * 商品类型
 */
@Serializable
data class MCConstsChargeTypeData(
    val pc: List<String> = emptyList(),
    val pe: List<String> = emptyList()
)

/**
 * 开发者等级
 */
@Serializable
data class MCConstsDeveloperClassData(
    @SerialName("_id")
    val id: String = "",
    // 等阶
    @SerialName("class")
    val clazz: Int = 0,
    // 等阶名称
    @SerialName("class_name")
    val className: String = "",
    // 该等级经验上限
    @SerialName("exp_ceiling")
    val expCeiling: Long = 0L,
    // 该等级经验下限
    @SerialName("exp_floor")
    val expFloor: Long = 0L,
    // 等级
    val level: Int = 0,
    // 该等级总共经验
    @SerialName("level_exp")
    val levelExp: Long = 0L,
    // 该等级回退经验
    @SerialName("level_fallback_exp")
    val levelFallbackExp: Int = 0,
    // 该等级名称
    @SerialName("level_name")
    val levelName: String = "",
)

/**
 * 推荐标签
 */

@Serializable
data class MCConstsRecommendTagData(
    @SerialName("1")
    val gameplayTag: List<MCConstsCommonTitleData> = emptyList(),
    @SerialName("2")
    val themeTag: List<MCConstsCommonTitleData> = emptyList()
)

/**
 * 玩法模组新子类别
 */
@Serializable
data class MCConstsModSecondTypeDataList(
    @SerialName("2")
    val subTag: List<MCConstsModSecondTypeData> = emptyList(),
)

/**
 * 玩法模组新子类别数据
 */
@Serializable
data class MCConstsModSecondTypeData(
    // 文件类型
    @SerialName("file_type")
    val fileType: String = "",
    // fp储存文件类型
    @SerialName("fp_type")
    val fpType: String = "",
    val id: Int = 0,
    @SerialName("mod_version")
    val modVersion: Boolean = false,
    val title: String = ""
)

/**
 * 资源类型
 */
@Serializable
data class MCConstsPriTypeData(
    val pe: List<MCConstsCommonTitleData> = emptyList(),
    val comp: List<MCConstsCommonTitleData> = emptyList(),
    val single: List<MCConstsCommonTitleData> = emptyList(),
    val multi: List<MCConstsCommonTitleData> = emptyList(),
)

/**
 * 价格类型
 */
@Serializable
data class MCConstsPriceTypeData(
    val id: String = "",
    val max: Int? = null,
    val min: Int = 0,
    val step: Int = 0,
    val title: String = ""
)

/**
 * 资源标签列表
 */
@Serializable
data class MCConstsTagData(
    val comp: List<MCConstsCommonTitleData> = emptyList(),
    val multi: List<MCConstsCommonTitleData> = emptyList(),
    val pe: List<MCConstsCommonTitleData> = emptyList(),
)

/**
 * 通用平台分类数据
 */
@Serializable
data class MCConstsCommonPlatformData(
    val pe: JsonObject = JsonObject(emptyMap()),
    val pc: JsonObject = JsonObject(emptyMap()),
    val comp: JsonObject = JsonObject(emptyMap()),
    val single: JsonObject = JsonObject(emptyMap()),
    val multi: JsonObject = JsonObject(emptyMap())
)

/**
 * 通用id标签类数据
 */
@Serializable
data class MCConstsCommonTitleData(
    val id: Int = 0,
    val title: String = ""
)
