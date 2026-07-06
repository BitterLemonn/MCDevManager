package com.lemon.mcdevmanagermp.ui.pages.work.activity.discount

import com.lemon.mcdevmanagermp.data.vo.netease.activity.DiscountActivityModuleVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.DiscountActivityPartitionVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.DiscountActivityVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.DiscountCandidatesItemVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.DiscountItemVO
import com.lemon.mcdevmanagermp.utils.extension.IUiAction
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import com.lemon.mcdevmanagermp.utils.extension.IUiState

data class DiscountActivityState(
    val isLoading: Boolean = false,
    val isLoadingModule: Boolean = false,
    val isSubmitting: Boolean = false,
    val activity: DiscountActivityVO? = null,
    val selectedModuleId: String? = null,
    val candidates: List<DiscountCandidatesItemVO> = emptyList(),
    val joinedItems: List<DiscountItemVO> = emptyList(),
    val selectedItemIds: Set<String> = emptySet(),
    val discount: Int = 0,
    val selectedPartitionId: String? = null,
    val intro: String = ""
) : IUiState {

    /** 当前选中的赛道 */
    val currentModule: DiscountActivityModuleVO?
        get() = activity?.modules?.find { it.moduleId == selectedModuleId }

    /**
     * 当前赛道可用的分区列表。
     * ponytail: 按 module 的 itemPriTypeList 与 partition 取交集过滤；全不匹配时回退展示全部分区，
     * 升级路径：若后端返回明确的 module→partition 映射，可替换此启发式。
     */
    val availablePartitions: List<DiscountActivityPartitionVO>
        get() {
            val all = activity?.partition ?: emptyList()
            val module = currentModule ?: return all
            val filtered = all.filter { p ->
                p.itemPriTypeList.any { it in module.itemPriTypeList }
            }
            return filtered.ifEmpty { all }
        }
}

sealed interface DiscountActivityAction : IUiAction {
    data object LoadData : DiscountActivityAction
    data object Refresh : DiscountActivityAction
    data class SelectModule(val moduleId: String) : DiscountActivityAction
    data class ToggleItem(val itemId: String) : DiscountActivityAction
    data class UpdateDiscount(val discount: Int) : DiscountActivityAction
    data class SelectPartition(val partitionId: String) : DiscountActivityAction
    data class UpdateIntro(val intro: String) : DiscountActivityAction
    data class CancelJoin(val itemId: String) : DiscountActivityAction
    data object Submit : DiscountActivityAction
}

sealed interface DiscountActivityEffect : IUiEffect {
    data class ShowToast(val message: String) : DiscountActivityEffect
}
