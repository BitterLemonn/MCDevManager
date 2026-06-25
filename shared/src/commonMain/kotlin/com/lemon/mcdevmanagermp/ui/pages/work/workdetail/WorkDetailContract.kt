package com.lemon.mcdevmanagermp.ui.pages.work.workdetail

import com.lemon.mcdevmanagermp.data.consts.enums.PriceRankEnum
import com.lemon.mcdevmanagermp.data.consts.enums.PriceTypeEnum
import com.lemon.mcdevmanagermp.data.vo.netease.resource.MCConstsCommonTitleData
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailVO
import com.lemon.mcdevmanagermp.utils.extension.IUiAction
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import com.lemon.mcdevmanagermp.utils.extension.IUiState
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.datetime.LocalDate

data class WorkDetailState(
    val isLoading: Boolean = false,
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
    val relatedSearchKey: String = "",                // 关联模组 - 搜索模组
    val syncPc: Boolean = false,                      // 是否同步生成 PC 模组（PC 区块显示开关）

    // —— PC 基本信息（syncPc=true 时编辑） ——
    val pcIncludeMap: Boolean = false,                // PC 是否包含地图
    val pcTags: List<String> = emptyList(),           // PC 模组标签
    val pcTagOptions: List<MCConstsCommonTitleData> = emptyList(),  // PC 模组可选标签（mc_consts.tag.comp，含 id+title；仅可选用、不可自定义）
    val pcHasPrerequisite: Boolean = false,           // PC 前置模组：包含/不包含
    val pcPrerequisiteIid: String = "",               // PC 前置模组 iid（提交映射 relate_item_id）
    val pcBrief: String = "",                         // PC 模组简介

    // —— 定价 ——
    val priceType: PriceTypeEnum = PriceTypeEnum.UNKNOWN,   // 定价类型（钻石/绿宝石/免费）
    val priceRank: PriceRankEnum = PriceRankEnum.UNKNOWN,   // 钻石档位（仅钻石有效）
    val emeraldPrice: Int = 0,                              // 绿宝石自填价格
    val discounts: List<DiscountConfig> = emptyList()       // 折扣列表（仅钻石二档及以上可编辑）
) : IUiState

sealed interface WorkDetailAction : IUiAction {
    data class LoadDetail(val itemId: String) : WorkDetailAction
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
    data class UpdatePcPrerequisiteIid(val value: String) : WorkDetailAction
    data class UpdatePcIntro(val value: String) : WorkDetailAction

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

    /** 顶部「更新」按钮（写接口占位） */
    data object Submit : WorkDetailAction
}

sealed interface WorkDetailEffect : IUiEffect {
    data class ShowToast(val message: String) : WorkDetailEffect
    data object NeedReLogin : WorkDetailEffect
}

/**
 * 折扣配置（UI 状态）。
 *
 * - [percent]：售价占原价百分比，范围 60..99（60 = 6 折最大优惠，99 = 99 折几乎不打折），
 *   仅设上限（最多 6 折），与后端 `discount` 字段语义一致、直接回传。
 * - [beginDate]/[endDate]：折扣起止日期。
 */
data class DiscountConfig(
    val percent: Int = 90,
    val beginDate: LocalDate,
    val endDate: LocalDate
)
