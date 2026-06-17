package com.lemon.mcdevmanagermp.ui.pages.work.workdetail

import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailVO
import com.lemon.mcdevmanagermp.utils.extension.IUiAction
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import com.lemon.mcdevmanagermp.utils.extension.IUiState

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
    val tags: List<String> = emptyList(),             // 模组标签
    val prerequisite: String = "",                    // 前置模组
    val activityDesc: String = "",                    // 活动参与说明
    val isRelatedMod: Boolean = false,                // 是否为关联模组
    val relatedIsMaster: Boolean = true,              // 关联模组类型：主包(true)/副包(false)，isRelatedMod=true 时生效
    val relatedSearchKey: String = "",                // 关联模组 - 搜索模组
    val syncPc: Boolean = false                       // 是否同步生成 PC 模组（PC 区块显示开关）
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

    /** 顶部「更新」按钮（写接口占位） */
    data object Submit : WorkDetailAction
}

sealed interface WorkDetailEffect : IUiEffect {
    data class ShowToast(val message: String) : WorkDetailEffect
    data object NeedReLogin : WorkDetailEffect
}
