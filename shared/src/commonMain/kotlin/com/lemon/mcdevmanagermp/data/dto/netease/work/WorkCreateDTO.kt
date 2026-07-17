package com.lemon.mcdevmanagermp.data.dto.netease.work

import com.lemon.mcdevmanagermp.data.dto.netease.activity.FileInfoDTO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailTag
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject

/**
 * 新建作品请求体（POST items/categories/pe/upload）。
 *
 * 与编辑保存 [WorkUpdateDTO] 的关键差异：
 * - `res[].res_url` / `channel[].channel_url` 为 [FileInfoDTO]（上传回执 {body,file_type,sign}），非 URL 字符串
 * - 扁平结构：`brief`/`available_scope`/`include_map`/`game_host` 在顶层（新建 sync_pc_flag=false，无 sync_item_info）
 * - `item_id` 默认空、`is_check_apply` 控制仅保存 / 保存并提审
 *
 * 字段照搬网易开平新建示例的默认值；mapper 仅覆盖 state 已捕获的字段。
 */
@Serializable
data class WorkCreateDTO(
    @SerialName("item_name") val itemName: String = "",
    @SerialName("mc_version") val mcVersion: List<JsonElement> = emptyList(),
    @SerialName("online_platform") val onlinePlatform: List<JsonElement> = emptyList(),
    @SerialName("label_type_list") val labelTypeList: List<Int> = emptyList(),
    @SerialName("pri_type") val priType: Int = 0,
    @SerialName("sub_type") val subType: Int = 0,
    val info: String = "",
    val rarity: Int = 0,
    @SerialName("lobby_min_num") val lobbyMinNum: Int = 0,
    @SerialName("lobby_max_num") val lobbyMaxNum: Int = 0,
    @SerialName("lobby_force_max_num") val lobbyForceMaxNum: Int = 10,
    @SerialName("lobby_tags") val lobbyTags: List<JsonElement> = emptyList(),
    @SerialName("include_map") val includeMap: Boolean = false,
    val tag: List<ResourceDetailTag> = emptyList(),
    @SerialName("multi_tags") val multiTags: String = "",
    @SerialName("requirement") val requirement: List<JsonElement> = emptyList(),
    @SerialName("mod_id") val modId: Int = 0,
    @SerialName("available_scope") val availableScope: String = "",
    @SerialName("force_encrypt") val forceEncrypt: Boolean = false,
    @SerialName("price_type") val priceType: String = "free",
    @SerialName("price_rank") val priceRank: Int = 0,
    @SerialName("trial_duration") val trialDuration: Int = 0,
    val price: Int = 0,
    @SerialName("ios_price") val iosPrice: Int = 0,
    @SerialName("ios_price_type") val iosPriceType: String = "",
    @SerialName("ios_jelly_id") val iosJellyId: String = "",
    @SerialName("android_price") val androidPrice: Int = 0,
    @SerialName("android_price_type") val androidPriceType: String = "",
    @SerialName("adv_obtain_num") val advObtainNum: Int = 0,
    val brief: String = "",
    @SerialName("pe_game_introduction") val peGameIntroduction: String = "",
    @SerialName("game_host") val gameHost: String = "",
    val pure: Boolean = false,
    @SerialName("java_version") val javaVersion: String = "",
    @SerialName("activity_desc") val activityDesc: String = "",
    @SerialName("claim_item_enabled") val claimItemEnabled: Boolean = false,
    @SerialName("body_type") val bodyType: String = "",
    @SerialName("current_change_log") val currentChangeLog: String = "",
    @SerialName("dyeing_relation") val dyeingRelation: JsonObject = JsonObject(emptyMap()),
    @SerialName("pe_item_id") val peItemId: List<JsonElement> = emptyList(),
    @SerialName("pe_emotes_id") val peEmotesId: List<JsonElement> = emptyList(),
    @SerialName("pe_mc_item_id") val peMcItemId: List<JsonElement> = emptyList(),
    @SerialName("pe_home_cash") val peHomeCash: List<JsonElement> = emptyList(),
    @SerialName("pe_frame_id") val peFrameId: List<JsonElement> = emptyList(),
    @SerialName("pe_furniture_id") val peFurnitureId: List<JsonElement> = emptyList(),
    @SerialName("pe_six_month_vip_id") val peSixMonthVipId: List<JsonElement> = emptyList(),
    @SerialName("pe_passport_ten_id") val pePassportTenId: List<JsonElement> = emptyList(),
    @SerialName("pe_user_background_id") val peUserBackgroundId: List<JsonElement> = emptyList(),
    @SerialName("pe_activity_coupon") val peActivityCoupon: List<JsonElement> = emptyList(),
    @SerialName("pe_chat_bubble_id") val peChatBubbleId: List<JsonElement> = emptyList(),
    @SerialName("pe_one_month_vip_id") val peOneMonthVipId: List<JsonElement> = emptyList(),
    @SerialName("pe_lottery_chance") val peLotteryChance: List<JsonElement> = emptyList(),
    @SerialName("prerequisite_item_ids") val prerequisiteItemIds: List<JsonElement> = emptyList(),
    @SerialName("prerequisite_items") val prerequisiteItems: List<JsonElement> = emptyList(),
    @SerialName("mod_version") val modVersion: String = "",
    @SerialName("is_lobby_competitive") val isLobbyCompetitive: Boolean = false,
    @SerialName("is_asymmetric") val isAsymmetric: Boolean = false,
    @SerialName("lobby_camps") val lobbyCamps: List<JsonElement> = emptyList(),
    @SerialName("lobby_player_num") val lobbyPlayerNum: Int = 0,
    @SerialName("lobby_normal_mode") val lobbyNormalMode: Boolean = false,
    @SerialName("lobby_reconnect_time") val lobbyReconnectTime: Int = 0,
    @SerialName("subject_id") val subjectId: JsonElement = JsonNull,
    @SerialName("is_sync") val isSync: Boolean = false,
    @SerialName("vip_only") val vipOnly: Boolean = false,
    @SerialName("is_season_mod") val isSeasonMod: Boolean = false,
    @SerialName("activity_only") val activityOnly: Boolean = false,
    val searchable: Boolean = true,
    @SerialName("is_original") val isOriginal: Boolean = true,
    @SerialName("anti_cheat_enable") val antiCheatEnable: Int = 0,
    @SerialName("lobby_commercialize") val lobbyCommercialize: Boolean = false,
    @SerialName("version_compatible_enable") val versionCompatibleEnable: Boolean = false,
    @SerialName("mount_call_enabled") val mountCallEnabled: Boolean = false,
    @SerialName("is_ea") val isEa: Int = 0,
    @SerialName("achievement_enabled") val achievementEnabled: Int = 0,
    @SerialName("achievement_configs") val achievementConfigs: List<JsonElement> = emptyList(),
    @SerialName("achievement_background_url") val achievementBackgroundUrl: String = "",
    @SerialName("is_spigot") val isSpigot: Boolean = false,
    @SerialName("banner_pic") val bannerPic: String = "",
    @SerialName("season_begin") val seasonBegin: Int = 0,
    @SerialName("is_lottery_reward") val isLotteryReward: Boolean = false,
    @SerialName("item_update_push") val itemUpdatePush: Boolean = true,
    @SerialName("lottery_id") val lotteryId: Int = 0,
    @SerialName("is_persona") val isPersona: Boolean = false,
    @SerialName("is_recommend") val isRecommend: Boolean = false,
    @SerialName("exchange_currency") val exchangeCurrency: Int = 0,
    @SerialName("exchange_currency_type") val exchangeCurrencyType: String = "ordinary",
    @SerialName("decompose_currency") val decomposeCurrency: Int = 0,
    @SerialName("decompose_currency_type") val decomposeCurrencyType: String = "ordinary",
    @SerialName("persona_mtypeid") val personaMtypeid: Int = 0,
    @SerialName("persona_stypeid") val personaStypeid: Int = 0,
    val dyeing: String = "",
    @SerialName("need_method_uuid") val needMethodUuid: Boolean = false,
    @SerialName("need_behaviour_uuid") val needBehaviourUuid: Boolean = false,
    @SerialName("dyeing_origin") val dyeingOrigin: Boolean = false,
    @SerialName("is_vip_benefit") val isVipBenefit: Boolean = false,
    @SerialName("is_test_server") val isTestServer: Boolean = false,
    @SerialName("is_access_by_uid") val isAccessByUid: Boolean = false,
    @SerialName("is_can_comment") val isCanComment: Boolean = false,
    @SerialName("weak_offline") val weakOffline: Boolean = false,
    @SerialName("weak_offline_reason") val weakOfflineReason: String = "",
    @SerialName("is_quick_upload") val isQuickUpload: Boolean = false,
    @SerialName("running_status") val runningStatus: String = "normal",
    @SerialName("white_list") val whiteList: List<JsonElement> = emptyList(),
    @SerialName("silent_white_list") val silentWhiteList: List<JsonElement> = emptyList(),
    @SerialName("is_joint_activity") val isJointActivity: Boolean = false,
    @SerialName("joint_activity_detail") val jointActivityDetail: JsonObject = JsonObject(emptyMap()),
    @SerialName("joint_activity_name") val jointActivityName: String = "",
    @SerialName("joint_activity_tag") val jointActivityTag: List<JsonElement> = emptyList(),
    @SerialName("collection_id") val collectionId: Int = 0,
    @SerialName("collection_name") val collectionName: String = "",
    @SerialName("is_official_item") val isOfficialItem: Boolean = false,
    @SerialName("openbeta_time") val openbetaTime: JsonElement = JsonNull,
    @SerialName("commercial_time") val commercialTime: JsonElement = JsonNull,
    @SerialName("pe_is_add_play_plan") val peIsAddPlayPlan: Boolean = false,
    @SerialName("mod_second_type") val modSecondType: Int = 0,
    val res: List<WorkCreateRes> = emptyList(),
    val channel: List<WorkCreateChannel> = emptyList(),
    @SerialName("video_info_list") val videoInfoList: List<JsonElement> = emptyList(),
    @SerialName("item_id") val itemId: String = "",
    @SerialName("vanity_number") val vanityNumber: String = "",
    @SerialName("normal_number") val normalNumber: String = "",
    @SerialName("remove_domain_server_reason") val removeDomainServerReason: String = "",
    @SerialName("item_version") val itemVersion: String = "",
    @SerialName("main_city") val mainCity: Boolean = false,
    @SerialName("is_domain_server_item") val isDomainServerItem: Int = 0,
    @SerialName("dlc_info") val dlcInfo: WorkUpdateDlcInfoDTO = WorkUpdateDlcInfoDTO(),
    @SerialName("sync_pc_flag") val syncPcFlag: Boolean = false,
    val tags: List<JsonElement> = emptyList(),
    @SerialName("update_summary") val updateSummary: String = "",
    @SerialName("guide_list") val guideList: List<JsonElement> = emptyList(),
    @SerialName("charge_type") val chargeType: String = "worlds",
    @SerialName("charge_desc") val chargeDesc: String = "",
    @SerialName("is_check_apply") val isCheckApply: Boolean = false
)

/** 新建资源项：res_url 为上传回执 FileInfoDTO。 */
@Serializable
data class WorkCreateRes(
    @SerialName("res_url") val resUrl: FileInfoDTO,
    @SerialName("res_name") val resName: String = "",
    @SerialName("mc_version") val mcVersion: List<JsonElement> = emptyList(),
    @SerialName("add_version") val addVersion: Boolean = true
)

/** 新建渠道项：channel_url 为上传回执 FileInfoDTO。 */
@Serializable
data class WorkCreateChannel(
    @SerialName("channel_id") val channelId: Int,
    @SerialName("channel_url") val channelUrl: FileInfoDTO,
    val version: Int = 1
)
