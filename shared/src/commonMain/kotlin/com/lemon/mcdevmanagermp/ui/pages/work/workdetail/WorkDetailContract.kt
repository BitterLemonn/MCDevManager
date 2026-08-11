package com.lemon.mcdevmanagermp.ui.pages.work.workdetail

import com.lemon.mcdevmanagermp.data.consts.enums.PriceRankEnum
import com.lemon.mcdevmanagermp.data.consts.enums.PriceTypeEnum
import com.lemon.mcdevmanagermp.data.dto.netease.activity.FileInfoDTO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.AvailableScopeData
import com.lemon.mcdevmanagermp.data.vo.netease.resource.MCConstsCommonTitleData
import com.lemon.mcdevmanagermp.data.vo.netease.resource.MCConstsModSecondTypeData
import com.lemon.mcdevmanagermp.data.vo.netease.resource.MCConstsRecommendTagData
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailVO
import com.lemon.mcdevmanagermp.ui.components.ModSelectOption
import com.lemon.mcdevmanagermp.utils.extension.IUiAction
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import com.lemon.mcdevmanagermp.utils.extension.IUiState
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.datetime.LocalDate

data class WorkDetailState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val submittingMessage: String = "",
    val detail: ResourceDetailVO? = null,
    // —— 只读字段 ——
    val itemId: String = "",
    val normalNumber: String = "",   // 模组码
    val itemVersion: String = "",    // 资源版本
    // —— 可编辑字段 ——
    val itemName: String = "",                        // 资源名称
    val joinShantou: Boolean = false,                 // 是否加入「我的山头」专区
    val isOriginal: Boolean = false,                  // 是否原创作品
    // —— 授权信息（非原创必填） ——
    val corpProofImage: String = "",                  // 远端已上传授权图 URL（详情回显）
    val corpProofFile: PlatformFile? = null,          // 本地新选授权图（提交时上传），非空时预览优先
    val tags: List<String> = emptyList(),             // 模组标签
    val availableTags: List<String> = emptyList(),    // 默认模组标签（搜索建议，来自 item-tag 接口）
    val prerequisite: String = "",                    // 前置模组
    val activityDesc: String = "",                    // 活动参与说明
    val isRelatedMod: Boolean = false,                // 是否为关联模组
    val relatedIsMaster: Boolean = true,              // 关联模组类型：主包(true)/副包(false)，isRelatedMod=true 时生效
    val relatedSearchKey: String = "",                // 关联模组 - 搜索词（保留）
    val relatedItemId: String = "",                   // 关联模组 - 选中模组 iid（提交用）
    val relatedItemName: String = "",                 // 关联模组 - 选中模组名称（显示）
    val relatedSearchResults: List<ModSelectOption> = emptyList(),  // 关联模组搜索结果（pe, mcStatus=1）
    val isSearchingRelated: Boolean = false,          // 关联模组搜索中
    val syncPc: Boolean = false,                      // 是否同步生成 PC 模组（PC 区块显示开关）

    // —— PC 基本信息（syncPc=true 时编辑） ——
    val pcIncludeMap: Boolean = false,                // PC 是否包含地图
    val pcTags: List<String> = emptyList(),           // PC 模组标签
    val pcTagOptions: List<MCConstsCommonTitleData> = emptyList(),  // PC 模组可选标签（mc_consts.tag.comp，含 id+title；仅可选用、不可自定义）
    val pcHasPrerequisite: Boolean = false,           // PC 前置模组：包含/不包含
    val pcPrerequisites: List<ModSelectOption> = emptyList(),        // PC 前置模组（多选，syncItemInfo.requirement）
    val pcPrereqSearchResults: List<ModSelectOption> = emptyList(),  // PC 前置搜索结果（comp requirements 接口）
    val isSearchingPcPrereq: Boolean = false,         // PC 前置搜索中
    val pcBrief: String = "",                         // PC 模组简介
    val peDetail: String = "",                        // PE 详情信息（HTML 富文本，来自 ResourceDetailVO.info）
    val peUpdateSummary: String = "",                 // PE 更新纪要（来自 ResourceDetailVO.updateSummary，不允许空格/换行，≤200 字）
    val pcDetail: String = "",                        // PC 详细信息（HTML 富文本，来自 ResourceDetailVO.syncItemInfo.info）

    // —— PE 资源管理 ——
    val peResourceType: Int = 0,                      // PE 资源类别（priType id；选项来自 mc_consts.pri_type.pe）
    val peResourceSubType: Int = 0,                   // PE 具体类别（sub_type id；选项随资源类别联动，来自 mc_consts.sub_type.pe）
    val peResourceModSecondType: Int = 0,             // PE 次级分类（mod_second_type id；仅玩法组件 add_ons 需选，来自 mc_consts.mod_second_type）
    val peRecommendTags: List<Int> = emptyList(),     // PE 推荐标签（labelTypeList；选项来自 mc_consts.label_type 玩法+主题）
    val peAddPlayPlan: Boolean = false,               // 是否加入模组畅玩计划（peIsAddPlayPlan）
    val peMountCallEnabled: Boolean = false,          // 是否启用坐骑召唤功能（mountCallEnabled）
    val peAddVersion: Boolean = false,                // 本次上传是否提升版本（res.addVersion）
    val peResource: PeResourceFile? = null,              // PE 资源文件（单文件，res 首项或上传结果）
    val peResourceTypeOptions: List<MCConstsCommonTitleData> = emptyList(), // 资源类别选项（mc_consts.pri_type.pe）
    val pePriTypeFileTypes: Map<Int, Set<String>> = emptyMap(), // pri_type id → 接受的 file_type 集合（mc_consts.sub_type.pe；空集=该类别未声明 file_type，视为不限）
    val peResourceSubTypeOptions: Map<Int, List<MCConstsCommonTitleData>> = emptyMap(), // pri_type id → 具体类别选项（mc_consts.sub_type.pe）
    val peModSecondTypeOptions: List<MCConstsModSecondTypeData> = emptyList(), // 次级分类选项（mc_consts.mod_second_type；仅玩法组件 add_ons 用）
    val peRecommendTagOptions: MCConstsRecommendTagData = MCConstsRecommendTagData(), // 推荐标签选项（玩法 + 主题两组）
    val peRecommendTagLimit: Int = 0,                   // 推荐标签合计上限（mc_consts.item_tag_limit；0=未加载/不限制）
    val isUploadingPeZip: Boolean = false,            // zip 上传中

    // —— PC 资源管理（syncPc=true 时编辑） ——
    val pcResourceType: Int = 0,                      // PC 模组类别（priType id；选项来自 mc_consts.pri_type.comp）
    val pcAvailableScope: String = "",                // PC 适用范围（available_scope id；选项来自 mc_consts.available_scope）
    val pcResourceSubType: Int = 0,                   // PC 具体类别（sub_type id；随模组类别联动，来自 mc_consts.sub_type.comp）
    val pcResourceTypeOptions: List<MCConstsCommonTitleData> = emptyList(),  // 模组类别选项（mc_consts.pri_type.comp）
    val pcAvailableScopeOptions: List<AvailableScopeData> = emptyList(),     // 适用范围选项（mc_consts.available_scope）
    val pcResourceSubTypeOptions: Map<Int, List<MCConstsCommonTitleData>> = emptyMap(), // pri_type id → 具体类别选项（mc_consts.sub_type.pc）

    // —— 上架设置（弱下架） ——
    val peWeakOffline: Boolean = false,               // PE 弱下架（来自 ResourceDetailVO.weakOffline）
    val peWeakOfflineReason: String = "",             // PE 弱下架理由（来自 ResourceDetailVO.weakOfflineReason）
    val pcWeakOffline: Boolean = false,               // PC 弱下架（来自 ResourceDetailVO.syncItemInfo.weakOffline）
    val pcWeakOfflineReason: String = "",             // PC 弱下架理由（来自 ResourceDetailVO.syncItemInfo.weakOfflineReason）

    // —— 定价 ——
    val priceType: PriceTypeEnum = PriceTypeEnum.UNKNOWN,   // 定价类型（钻石/绿宝石/免费）
    val priceRank: PriceRankEnum = PriceRankEnum.UNKNOWN,   // 钻石档位（仅钻石有效）
    val emeraldPrice: Int = 0,                              // 绿宝石自填价格
    val discounts: List<DiscountConfig> = emptyList(),       // 折扣列表（仅钻石二档及以上可编辑）

    // —— PE/PC 宣传图 ——
    val peImageSlots: List<ChannelImageSlot> = emptyList(),  // PE 宣传图位（mc_consts.channel.pe+peMulti 定义，detail.channel 回显）
    val pcImageSlots: List<ChannelImageSlot> = emptyList(),  // PC 宣传图位（mc_consts.channel.comp+multi 定义，syncItemInfo.channel 回显）

    // —— 视频 ——
    val videos: List<VideoItem> = emptyList(),               // 宣传视频（video_info_list 回显 + 上传结果；上限 1）
    val isUploadingVideo: Boolean = false                    // 视频上传中
) : IUiState

sealed interface WorkDetailAction : IUiAction {
    data class LoadDetail(val itemId: String) : WorkDetailAction
    data object InitNewWork : WorkDetailAction      // 新建模式：仅加载表单选项，不加载详情
    data class UpdateItemName(val value: String) : WorkDetailAction
    data class ToggleJoinShantou(val value: Boolean) : WorkDetailAction
    data class ToggleOriginal(val value: Boolean) : WorkDetailAction
    data class AddTag(val name: String) : WorkDetailAction
    data class RemoveTag(val index: Int) : WorkDetailAction
    data class UpdatePrerequisite(val value: String) : WorkDetailAction
    data class UpdateActivityDesc(val value: String) : WorkDetailAction
    data class ToggleRelatedMod(val value: Boolean) : WorkDetailAction
    data class ToggleRelatedPackType(val value: Boolean) : WorkDetailAction  // 关联模组类型 主包/副包
    data class UpdateRelatedSearch(val value: String) : WorkDetailAction
    data class ToggleSyncPc(val value: Boolean) : WorkDetailAction

    // —— 授权信息（非原创必填） ——
    data class SelectCorpProof(val file: PlatformFile) : WorkDetailAction
    data object RemoveCorpProof : WorkDetailAction

    // —— PC 基本信息（syncPc=true 时） ——
    data class TogglePcIncludeMap(val value: Boolean) : WorkDetailAction
    data class AddPcTag(val name: String) : WorkDetailAction
    data class RemovePcTag(val index: Int) : WorkDetailAction
    data class TogglePcPrerequisite(val value: Boolean) : WorkDetailAction  // 包含 / 不包含
    data class UpdatePcIntro(val value: String) : WorkDetailAction
    data class UpdatePeDetail(val value: String) : WorkDetailAction   // PE 详情信息（HTML 富文本）
    data class UpdatePeUpdateSummary(val value: String) : WorkDetailAction   // PE 更新纪要
    data class UpdatePcDetail(val value: String) : WorkDetailAction   // PC 详细信息（HTML 富文本）

    // —— PE 资源管理 ——
    data class UpdatePeResourceType(val id: Int) : WorkDetailAction
    data class UpdatePeResourceSubType(val id: Int) : WorkDetailAction   // 具体类别（sub_type）
    data class UpdatePeResourceModSecondType(val id: Int) :
        WorkDetailAction   // 次级分类（mod_second_type，仅玩法组件）

    data class TogglePeRecommendTag(val id: Int) : WorkDetailAction   // 推荐标签多选 toggle
    data class TogglePePlayPlan(val value: Boolean) : WorkDetailAction
    data class TogglePeMountCall(val value: Boolean) : WorkDetailAction
    data class TogglePeAddVersion(val value: Boolean) : WorkDetailAction
    data class UploadPeZip(val file: PlatformFile) : WorkDetailAction
    data object RemovePeResource : WorkDetailAction

    // —— PC 资源管理 ——
    data class UpdatePcResourceType(val id: Int) : WorkDetailAction   // 模组类别（pri_type.comp）
    data class UpdatePcAvailableScope(val id: String) :
        WorkDetailAction   // 适用范围（available_scope，id 为 String）

    data class UpdatePcResourceSubType(val id: Int) : WorkDetailAction   // 具体类别（sub_type.pc）

    // —— 视频 ——
    data class UploadVideo(val file: PlatformFile) : WorkDetailAction
    data class RemoveVideo(val index: Int) : WorkDetailAction
    data class UploadVideoCover(val index: Int, val file: PlatformFile, val mimeType: String) :
        WorkDetailAction

    // —— 上架设置（弱下架） ——
    data class TogglePeWeakOffline(val value: Boolean) : WorkDetailAction
    data class UpdatePeWeakOfflineReason(val value: String) : WorkDetailAction
    data class TogglePcWeakOffline(val value: Boolean) : WorkDetailAction
    data class UpdatePcWeakOfflineReason(val value: String) : WorkDetailAction

    // —— 模组搜索选择（关联模组 pe / PC 前置 comp，mcStatus=1） ——
    data class SearchRelatedMods(val query: String) : WorkDetailAction
    data class SelectRelatedMod(val option: ModSelectOption) : WorkDetailAction
    data object ClearRelatedMod : WorkDetailAction
    data class SearchPcPrereqMods(val query: String) : WorkDetailAction
    data class SelectPcPrereqMod(val option: ModSelectOption) : WorkDetailAction
    data class RemovePcPrereqMod(val option: ModSelectOption) : WorkDetailAction

    // —— 定价 ——
    data class ChangePriceType(val type: PriceTypeEnum) : WorkDetailAction
    data class ChangePriceRank(val rank: PriceRankEnum) : WorkDetailAction
    data class UpdateEmeraldPrice(val value: Int) : WorkDetailAction

    // —— 折扣（仅钻石二档及以上） ——
    data object AddDiscount : WorkDetailAction
    data class RemoveDiscount(val index: Int) : WorkDetailAction
    data class UpdateDiscountPercent(val index: Int, val percent: Int) : WorkDetailAction
    data class UpdateDiscountBegin(val index: Int, val date: LocalDate) : WorkDetailAction
    data class UpdateDiscountEnd(val index: Int, val date: LocalDate) : WorkDetailAction

    // —— PE/PC 宣传图（file 为裁剪后的 PlatformFile，ViewModel 立即上传） ——
    data class SelectPeChannelImage(
        val channelId: Int,
        val file: PlatformFile,
        val mimeType: String
    ) : WorkDetailAction

    data class RemovePeChannelImage(val channelId: Int) : WorkDetailAction
    data class SelectPcChannelImage(
        val channelId: Int,
        val file: PlatformFile,
        val mimeType: String
    ) : WorkDetailAction

    data class RemovePcChannelImage(val channelId: Int) : WorkDetailAction

    data object Save : WorkDetailAction              // 保存：仅保存并返回列表
    data object SaveAndReview : WorkDetailAction     // 提审：保存成功后发起提审再返回
}

sealed interface WorkDetailEffect : IUiEffect {
    data class ShowToast(val message: String) : WorkDetailEffect
    data object NeedReLogin : WorkDetailEffect
    data object NavigateBack : WorkDetailEffect   // 保存/提审成功后返回作品列表
}

data class DiscountConfig(
    val percent: Int = 90,
    val beginDate: LocalDate,
    val endDate: LocalDate
)

/** PE 资源文件（对应 ResourceDetailRes，回显与上传结果统一模型）。 */
data class PeResourceFile(
    val name: String = "",
    val url: String = "",
    val mcVersion: List<String> = emptyList(),
    val size: Long = 0,
    val addVersion: Boolean = false,
    val fileInfo: FileInfoDTO? = null
)

/**
 * 宣传图图片位（PE/PC 通用）。channel 定义来自 mc_consts.channel.*（title/width/height），
 * channelUrl 来自详情回显或上传结果。
 */
data class ChannelImageSlot(
    val channelId: Int,
    val title: String,
    val width: Int,
    val height: Int,
    val channelUrl: String,
    val version: Int? = null,
    val isUploading: Boolean = false,
    val fileInfo: FileInfoDTO? = null
)

/** 宣传视频（对应 ResourceDetailVideoInfo，回显与上传结果统一模型；cover 由用户上传封面图得到）。 */
data class VideoItem(
    val cover: String = "",
    val size: Long = 0,
    val url: String = "",
    val isUploadingCover: Boolean = false
)
