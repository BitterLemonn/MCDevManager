package com.lemon.mcdevmanagermp.data.vo.netease.resource

import com.lemon.mcdevmanagermp.data.consts.enums.PriceRankEnum
import com.lemon.mcdevmanagermp.data.consts.enums.PriceTypeEnum
import com.lemon.mcdevmanagermp.data.consts.enums.WorkItemStatusEnum
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject


/**
 * 资源列表
 */
@Serializable
data class ResourceListVO(
    val count: Int,
    val item: List<ResourceData>
)

@Serializable
data class ResourceData(
    // 发布时间
    @SerialName("create_time")
    val createTime: String = "",
    // 提审时间
    @SerialName("apply_review_time")
    val applyReviewTime: String = "",
    // 46id
    @SerialName("item_id")
    val itemId: String = "",
    // 作品名称
    @SerialName("item_name")
    val itemName: String = "",
    // 上架时间
    @SerialName("online_time")
    val onlineTime: String = "UNKNOWN",
    // 价格类型
    @SerialName("price_type")
    val priceType: String = "",
    // 价格
    val price: Int = 0,
    // 价格档位
    @SerialName("price_rank")
    val priceRank: Int = 0,
    // 作品真实状态 未查明 [ItemRealStatusEnum] TODO
    @SerialName("item_real_status")
    val itemRealStatus: Int = 0,
    // 状态字段
    @SerialName("status")
    val status: String = "",
    // 是否双端同步
    @SerialName("sync_pc_flag")
    val syncPcFlag: Boolean = false,
    // 是否弱下架
    @SerialName("weak_offline")
    val weakOffline: Boolean = false,
    // 弱下架原因
    @SerialName("weak_offline_reason")
    val weakOfflineReason: String = "",
    // 弱下架时间
    @SerialName("weak_offline_time")
    val weakOfflineTime: String = "",
    // 基本信息-是否为原创作品
    @SerialName("is_original")
    val isOriginal: Boolean = false,
) {
    fun getPriceType(): PriceTypeEnum {
        return PriceTypeEnum.fromStringType(priceType)
    }

    fun getPriceRank(): PriceRankEnum {
        return PriceRankEnum.fromIntType(priceRank)
    }

    fun getStatus(): WorkItemStatusEnum {
        return WorkItemStatusEnum.fromStatusString(status)
    }
}

/**
 * 资源详情
 */
@Serializable
data class ResourceDetailVO(
    // 基础信息-资源id
    @SerialName("item_id")
    val itemId: String = "",
    // 基本信息-资源名称
    @SerialName("item_name")
    val itemName: String = "",
    // 基本信息-资源版本
    @SerialName("item_version")
    val itemVersion: String = "",
    // 基本信息-模组码
    @SerialName("normal_number")
    val normalNumber: String = "",
    @SerialName("status")
    val status: String = "",
    @SerialName("running_status")
    val runningStatus: String = "",
    @SerialName("category")
    val category: String = "",
    @SerialName("sub_type")
    val subType: Int = 0,
    @SerialName("mod_second_type")
    val modSecondType: Int = 0,
    @SerialName("mod_version")
    val modVersion: String = "",
    @SerialName("mc_version")
    val mcVersion: String = "",
    @SerialName("body_type")
    val bodyType: String = "",
    @SerialName("item_real_status")
    val itemRealStatus: Int = 0,

    // 作者信息
    @SerialName("author_info")
    val authorInfo: String = "",

    // 授权信息
    @SerialName("corp_proof_image")
    val corpProofImage: String = "",

    // 描述信息
    @SerialName("info")
    val info: String = "",
    @SerialName("banner_pic")
    val bannerPic: String = "",
    @SerialName("update_summary")
    val updateSummary: String = "",
    @SerialName("charge_desc")
    val chargeDesc: String = "",
    @SerialName("activity_desc")
    val activityDesc: String = "",
    @SerialName("pe_game_introduction")
    val peGameIntroduction: String = "",

    // 价格信息
    @SerialName("price")
    val price: Int = 0,
    @SerialName("price_type")
    val priceType: String = "",
    @SerialName("price_rank")
    val priceRank: Int = 0,
    @SerialName("pri_type")
    val priType: Int = 0,
    @SerialName("charge_type")
    val chargeType: String = "",
    @SerialName("android_price")
    val androidPrice: Int = 0,
    @SerialName("android_price_type")
    val androidPriceType: String = "",
    @SerialName("ios_price")
    val iosPrice: Int = 0,
    @SerialName("ios_price_type")
    val iosPriceType: String = "",
    @SerialName("ios_jelly_id")
    val iosJellyId: String = "",

    // 统计信息
    @SerialName("score")
    val score: Double = 0.0,
    @SerialName("rating_level")
    val ratingLevel: Int = 0,

    // 时间信息
    @SerialName("create_time")
    val createTime: String = "",
    @SerialName("online_time")
    val onlineTime: String = "",
    @SerialName("first_online_time")
    val firstOnlineTime: String = "",
    @SerialName("first_accept_time")
    val firstAcceptTime: String = "",
    @SerialName("apply_review_time")
    val applyReviewTime: String = "",
    @SerialName("appoint_online_time")
    val appointOnlineTime: String? = null,

    // 标签与分类
    @SerialName("tags")
    val tags: List<ResourceDetailTag> = emptyList(),
    @SerialName("label_type_list")
    val labelTypeList: List<Int> = emptyList(),

    // 资源文件
    @SerialName("res")
    val res: List<ResourceDetailRes> = emptyList(),
    @SerialName("channel")
    val channel: List<ResourceDetailChannel> = emptyList(),
    @SerialName("video_info_list")
    val videoInfoList: List<ResourceDetailVideoInfo> = emptyList(),

    // 详情嵌套对象
    @SerialName("dlc_info")
    val dlcInfo: ResourceDetailDlcInfo = ResourceDetailDlcInfo(),
    @SerialName("perf_data")
    val perfData: ResourceDetailPerfData = ResourceDetailPerfData(),
    @SerialName("lobby_res")
    val lobbyRes: ResourceDetailLobbyRes = ResourceDetailLobbyRes(),
    @SerialName("sync_item_info")  // 同步PC基本信息
    val syncItemInfo: ResourceDetailSyncItemInfo = ResourceDetailSyncItemInfo(),

    // 大厅相关
    @SerialName("lobby_commercialize")
    val lobbyCommercialize: Boolean = false,
    @SerialName("lobby_force_max_num")
    val lobbyForceMaxNum: Int = 0,
    @SerialName("lobby_max_num")
    val lobbyMaxNum: Int = 0,
    @SerialName("lobby_min_num")
    val lobbyMinNum: Int = 0,
    @SerialName("lobby_normal_mode")
    val lobbyNormalMode: Boolean = false,
    @SerialName("lobby_player_num")
    val lobbyPlayerNum: Int = 0,
    @SerialName("lobby_reconnect_time")
    val lobbyReconnectTime: Int = 0,
    @SerialName("is_lobby_competitive")
    val isLobbyCompetitive: Boolean = false,
    @SerialName("main_city")
    val mainCity: Boolean = false,
    @SerialName("can_manage_server")
    val canManageServer: Boolean = false,

    // 布尔标记
    @SerialName("activity_only")
    val activityOnly: Boolean = false,
    @SerialName("can_silent_online")
    val canSilentOnline: Boolean = false,
    @SerialName("can_synchronize_pc_old")
    val canSynchronizePcOld: Boolean = false,
    @SerialName("dyeing_origin")
    val dyeingOrigin: Boolean = false,
    @SerialName("first_sell_rank_top")
    val firstSellRankTop: Boolean = false,
    @SerialName("force_encrypt")
    val forceEncrypt: Boolean = false,
    @SerialName("is_in_promotion_application")
    val isInPromotionApplication: Boolean = false,
    @SerialName("is_joint_activity")
    val isJointActivity: Boolean = false,
    @SerialName("is_lottery_reward")
    val isLotteryReward: Boolean = false,
    @SerialName("is_official_item")
    val isOfficialItem: Boolean = false,
    @SerialName("is_original")
    val isOriginal: Boolean = true,
    @SerialName("is_persona")
    val isPersona: Boolean = false,
    @SerialName("is_premium")
    val isPremium: Boolean = false,
    @SerialName("is_recommend")
    val isRecommend: Boolean = false,
    @SerialName("is_season_mod")
    val isSeasonMod: Boolean = false,
    @SerialName("is_silent_online")
    val isSilentOnline: Boolean = false,
    @SerialName("is_spigot")
    val isSpigot: Boolean = false,
    @SerialName("is_suitable_pc")
    val isSuitablePc: Boolean = false,
    @SerialName("is_sync")
    val isSync: Boolean = false,
    @SerialName("is_vip_benefit")
    val isVipBenefit: Boolean = false,
    @SerialName("item_update_push")
    val itemUpdatePush: Boolean = false,
    @SerialName("mount_call_enabled")
    val mountCallEnabled: Boolean = false,
    @SerialName("need_behaviour_uuid")
    val needBehaviourUuid: Boolean = false,
    @SerialName("need_method_uuid")
    val needMethodUuid: Boolean = false,
    @SerialName("ori_weak_offline")
    val oriWeakOffline: Boolean = false,
    @SerialName("pure")
    val pure: Boolean = false,
    @SerialName("relate_item_weak_offline")
    val relateItemWeakOffline: Boolean = false,
    @SerialName("searchable")
    val searchable: Boolean = false,
    @SerialName("sync_pc_flag")
    val syncPcFlag: Boolean = false,
    @SerialName("version_compatible_enable")
    val versionCompatibleEnable: Boolean = false,
    @SerialName("vip_only")
    val vipOnly: Boolean = false,
    @SerialName("weak_offline")
    val weakOffline: Boolean = false,
    @SerialName("pe_is_add_play_plan")
    val peIsAddPlayPlan: Boolean = false,

    // 整型字段
    @SerialName("achievement_enabled")
    val achievementEnabled: Int = 0,
    @SerialName("adv_obtain_num")
    val advObtainNum: Int = 0,
    @SerialName("anti_cheat_enable")
    val antiCheatEnable: Int = 0,
    @SerialName("claim_item_enabled")
    val claimItemEnabled: Int = 0,
    @SerialName("decompose_currency")
    val decomposeCurrency: Int = 0,
    @SerialName("exchange_currency")
    val exchangeCurrency: Int = 0,
    @SerialName("exempt_perf_review_num")
    val exemptPerfReviewNum: Int = 0,
    @SerialName("is_domain_server_item")
    val isDomainServerItem: Int = 0,
    @SerialName("is_ea")
    val isEa: Int = 0,
    @SerialName("lottery_id")
    val lotteryId: Int = 0,
    @SerialName("performance_service_available")
    val performanceServiceAvailable: Int = 0,
    @SerialName("performance_service_status")
    val performanceServiceStatus: Int = 0,
    @SerialName("persona_mtypeid")
    val personaMtypeid: Int = 0,
    @SerialName("persona_stypeid")
    val personaStypeid: Int = 0,
    @SerialName("play_plan_expire_time")
    val playPlanExpireTime: Int = 0,
    @SerialName("queue_position")
    val queuePosition: Int = 0,
    @SerialName("rarity")
    val rarity: Int = 0,
    @SerialName("season_begin")
    val seasonBegin: Int = 0,
    @SerialName("trial_duration")
    val trialDuration: Int = 0,
    @SerialName("urgent_status")
    val urgentStatus: Int = 0,

    // 字符串字段
    @SerialName("achievement_background_url")
    val achievementBackgroundUrl: String = "",
    @SerialName("collection_id")
    val collectionId: String = "",
    @SerialName("collection_name")
    val collectionName: String = "",
    @SerialName("decompose_currency_type")
    val decomposeCurrencyType: String = "",
    @SerialName("discount_activity_status")
    val discountActivityStatus: String = "",
    @SerialName("dyeing")
    val dyeing: String = "",
    @SerialName("exchange_currency_type")
    val exchangeCurrencyType: String = "",
    @SerialName("joint_activity_name")
    val jointActivityName: String = "",
    @SerialName("maintain")
    val maintain: String = "",
    @SerialName("multi_tags")
    val multiTags: String = "",
    @SerialName("ori_weak_offline_reason")
    val oriWeakOfflineReason: String = "",
    @SerialName("persona_mtype")
    val personaMtype: String = "",
    @SerialName("persona_stype")
    val personaStype: String = "",
    @SerialName("pre_review_video")
    val preReviewVideo: String = "",
    @SerialName("premium_apply_status")
    val premiumApplyStatus: String = "",
    @SerialName("relate_item_id")
    val relateItemId: String = "",
    @SerialName("subject_id")
    val subjectId: String = "",
    @SerialName("suit_id")
    val suitId: String = "",
    @SerialName("urgent_reason")
    val urgentReason: String = "",
    @SerialName("vanity_number")
    val vanityNumber: String = "",
    @SerialName("weak_offline_reason")
    val weakOfflineReason: String = "",
    @SerialName("whitelist")
    val whitelist: String = "",

    // 可空字段
    @SerialName("game_host")
    val gameHost: String? = null,
    @SerialName("level_data")
    val levelData: JsonObject? = null,
    @SerialName("level_data_sync_error")
    val levelDataSyncError: String? = null,
    @SerialName("level_data_sync_status")
    val levelDataSyncStatus: String? = null,
    @SerialName("level_data_version")
    val levelDataVersion: String? = null,
    @SerialName("pri_effect_type")
    val priEffectType: String? = null,
    @SerialName("sub_effect_type")
    val subEffectType: String? = null,

    // 数组字段（未知元素类型）
    @SerialName("achievement_configs")
    val achievementConfigs: List<JsonElement> = emptyList(),
    @SerialName("change_log")
    val changeLog: List<JsonElement> = emptyList(),
    @SerialName("discount")
    val discount: List<JsonElement> = emptyList(),
    @SerialName("intercept_fields")
    val interceptFields: List<JsonElement> = emptyList(),
    @SerialName("joint_activity_tag")
    val jointActivityTag: List<JsonElement> = emptyList(),
    @SerialName("lobby_camps")
    val lobbyCamps: List<JsonElement> = emptyList(),
    @SerialName("lobby_config_op_log")
    val lobbyConfigOpLog: List<JsonElement> = emptyList(),
    @SerialName("lobby_tags")
    val lobbyTags: List<JsonElement> = emptyList(),
    @SerialName("pe_activity_coupon")
    val peActivityCoupon: List<JsonElement> = emptyList(),
    @SerialName("pe_chat_bubble_id")
    val peChatBubbleId: List<JsonElement> = emptyList(),
    @SerialName("pe_emotes_id")
    val peEmotesId: List<JsonElement> = emptyList(),
    @SerialName("pe_frame_id")
    val peFrameId: List<JsonElement> = emptyList(),
    @SerialName("pe_furniture_id")
    val peFurnitureId: List<JsonElement> = emptyList(),
    @SerialName("pe_home_cash")
    val peHomeCash: List<JsonElement> = emptyList(),
    @SerialName("pe_item_id")
    val peItemId: List<JsonElement> = emptyList(),
    @SerialName("pe_lottery_chance")
    val peLotteryChance: List<JsonElement> = emptyList(),
    @SerialName("pe_mc_item_id")
    val peMcItemId: List<JsonElement> = emptyList(),
    @SerialName("pe_one_month_vip_id")
    val peOneMonthVipId: List<JsonElement> = emptyList(),
    @SerialName("pe_passport_ten_id")
    val pePassportTenId: List<JsonElement> = emptyList(),
    @SerialName("pe_six_month_vip_id")
    val peSixMonthVipId: List<JsonElement> = emptyList(),
    @SerialName("pe_user_background_id")
    val peUserBackgroundId: List<JsonElement> = emptyList(),
    @SerialName("prerequisite_items")
    val prerequisiteItems: List<JsonElement> = emptyList(),

    // JSON 对象字段
    @SerialName("dyeing_relation")
    val dyeingRelation: JsonObject = JsonObject(emptyMap()),
    @SerialName("joint_activity_detail")
    val jointActivityDetail: JsonObject = JsonObject(emptyMap()),
    @SerialName("lobby_sort_key")
    val lobbySortKey: JsonObject = JsonObject(emptyMap())
)

@Serializable
data class ResourceDetailChannel(
    @SerialName("channel_id")
    val channelId: Int = 0,
    @SerialName("channel_url")
    val channelUrl: String = "",
    val version: Int = 0
)

@Serializable
data class ResourceDetailDlcInfo(
    @SerialName("dlc_switch")
    val dlcSwitch: Boolean = false,
    @SerialName("dlc_type")
    val dlcType: String = "",
    val master: String? = null,
    @SerialName("slave_list")
    val slaveList: String? = null
) {
    @Serializable
    enum class DlcType(val type: String) {
        MASTER("master"),
        SLAVE("slave")
    }
}

@Serializable
data class ResourceDetailLobbyRes(
    @SerialName("lobby_manifest_version")
    val lobbyManifestVersion: String = "",
    @SerialName("lobby_res_md5")
    val lobbyResMd5: String = "",
    @SerialName("lobby_res_size")
    val lobbyResSize: Int = 0,
    @SerialName("lobby_res_url")
    val lobbyResUrl: String = "",
    @SerialName("mcp_signs")
    val mcpSigns: List<String> = emptyList()
)

@Serializable
data class ResourceDetailPerfData(
    @SerialName("mem_size")
    val memSize: Int = 0,
    @SerialName("mem_warning")
    val memWarning: Boolean = false
)

@Serializable
data class ResourceDetailRes(
    @SerialName("add_version")
    val addVersion: Boolean = false,
    @SerialName("cdn_info")
    val cdnInfo: ResourceDetailResFileInfo = ResourceDetailResFileInfo(),
    @SerialName("cdn_url")
    val cdnUrl: String = "",
    @SerialName("mc_version")
    val mcVersion: List<String> = emptyList(),
    @SerialName("res_id")
    val resId: Int = 0,
    @SerialName("res_info")
    val resInfo: ResourceDetailResFileInfo = ResourceDetailResFileInfo(),
    @SerialName("res_name")
    val resName: String = "",
    @SerialName("res_url")
    val resUrl: String = ""
)

@Serializable
data class ResourceDetailResFileInfo(
    @SerialName("res_md5")
    val resMd5: String = "",
    @SerialName("res_size")
    val resSize: Int = 0,
    @SerialName("res_time")
    val resTime: String = ""
)

@Serializable
data class ResourceDetailSyncItemInfo(
    // PC模组信息-适用范围
    @SerialName("available_scope")
    val availableScope: String = "",
    // PC基本信息-模组简介
    val brief: String = "",
    // 未知(应该为comp)
    val category: String = "",
    // 宣传图
    val channel: List<ResourceDetailSyncChannel> = emptyList(),
    @SerialName("game_host")
    val gameHost: String? = null,
    // PC基本信息-是否包含地图
    @SerialName("include_map")
    val includeMap: Boolean = false,
    // PC详细信息(html)
    val info: String = "",
    @SerialName("item_id")
    val itemId: String = "",
    @SerialName("item_name")
    val itemName: String = "",
    @SerialName("mc_version")
    val mcVersion: List<String> = emptyList(),
    @SerialName("pri_type")
    val priType: Int = 0,
    val rarity: Int = 0,
    // PC基本信息-PC前置模组
    val requirement: List<ResourceRequirementData> = emptyList(),
    val status: String = "",
    @SerialName("sub_type")
    val subType: Int = 0,
    // PC基本信息-PC模组标签
    val tag: List<Int> = emptyList(),
    @SerialName("weak_offline")
    val weakOffline: Boolean = false,
    @SerialName("weak_offline_reason")
    val weakOfflineReason: String = ""
)

@Serializable
data class ResourceDetailSyncChannel(
    @SerialName("channel_id")
    val channelId: Int = 0,
    @SerialName("channel_url")
    val channelUrl: String = "",
    val version: Int? = null
)

@Serializable
data class ResourceDetailTag(
    val name: String = "",
    val source: Int = 0
)

@Serializable
data class ResourceDetailVideoInfo(
    val cover: String = "",
    val size: Int = 0,
    val url: String = ""
)

@Serializable
data class ResourceRequirementData(
    @SerialName("item_id")
    val itemId: String = "",
    @SerialName("item_name")
    val itemName: String = ""
)