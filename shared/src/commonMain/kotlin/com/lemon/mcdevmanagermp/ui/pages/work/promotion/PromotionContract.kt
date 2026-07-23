package com.lemon.mcdevmanagermp.ui.pages.work.promotion

import com.lemon.mcdevmanagermp.data.vo.netease.promotion.UserApplyItemVO
import com.lemon.mcdevmanagermp.domain.promotion.PromotionTemplate
import com.lemon.mcdevmanagermp.ui.components.ModSelectOption
import com.lemon.mcdevmanagermp.utils.extension.IUiAction
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import com.lemon.mcdevmanagermp.utils.extension.IUiState
import io.github.vinceglb.filekit.PlatformFile

/** 轮播图申请页视图 tab */
enum class PromotionTab { APPLY, HISTORY }

data class PromotionState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val isUploading: Boolean = false,
    /** key=Unix 秒日期，value=是否可申请 */
    val permit: Map<String, Boolean> = emptyMap(),
    val reason: Map<String, String> = emptyMap(),
    val recentRecords: Map<String, Boolean> = emptyMap(),
    val searchResults: List<ModSelectOption> = emptyList(),
    val selectedItem: ModSelectOption? = null,
    val selectedStartTime: Long? = null,
    val promoImage: PlatformFile? = null,
    /** 宣传图上传后的 URL；套用模板时回填，提交时若无本地文件则直接复用，免重传 */
    val promoImageUrl: String = "",
    val extra: String = "",
    val activity: String = "",
    val feature: String = "",
    val update: String = "",
    val isSyncing: Boolean = false,
    /** 同步 PE 详情的回显版本号：每次同步 +1，触发各 RichDetailForm 回显新 HTML */
    val syncEchoKey: Int = 0,
    /** 已保存的文案模板（按 createdAt 降序） */
    val templates: List<PromotionTemplate> = emptyList(),
    val showSaveTemplateDialog: Boolean = false,
    val showTemplatePicker: Boolean = false,
    val saveTemplateName: String = "",
    /** 已申请的轮播图记录（getUserApply 返回） */
    val userApplies: List<UserApplyItemVO> = emptyList(),
    /** 申请记录总数（getUserApply.count），用于判断是否还有下一页 */
    val userApplyCount: Int = 0,
    val isLoadingMoreUserApply: Boolean = false,
    /** 修改模式：非 null 表示正在修改该 applicationId 的审核中申请；null 为新建申请 */
    val editingApplicationId: String? = null,
    /** 当前视图 tab：申请 / 历史 */
    val selectedTab: PromotionTab = PromotionTab.APPLY
) : IUiState {

    /** 可申请日期（Unix 秒），按时间升序 */
    val availableDates: List<Long>
        get() = permit.filterValues { it }.keys.mapNotNull { it.toLongOrNull() }.sorted()
}

sealed interface PromotionAction : IUiAction {
    data object LoadData : PromotionAction
    data object Refresh : PromotionAction
    data class SearchItems(val query: String) : PromotionAction
    data class SelectItem(val item: ModSelectOption) : PromotionAction
    data object RemoveItem : PromotionAction
    data class SelectDate(val timestamp: Long) : PromotionAction
    data class SelectPromoImage(val file: PlatformFile) : PromotionAction
    data object RemovePromoImage : PromotionAction
    data class UpdateExtra(val value: String) : PromotionAction
    data class UpdateActivity(val value: String) : PromotionAction
    data class UpdateFeature(val value: String) : PromotionAction
    data class UpdateUpdate(val value: String) : PromotionAction
    data object SyncFromPe : PromotionAction
    data object LoadTemplates : PromotionAction
    data object LoadUserApply : PromotionAction
    data object LoadMoreUserApply : PromotionAction
    data class SaveTemplate(val name: String) : PromotionAction
    data class ApplyTemplate(val id: Long) : PromotionAction
    data class DeleteTemplate(val id: Long) : PromotionAction
    data object DismissSaveTemplateDialog : PromotionAction
    data object ShowTemplatePicker : PromotionAction
    data object DismissTemplatePicker : PromotionAction
    data class StartModifyApply(val item: UserApplyItemVO) : PromotionAction
    data object CancelModify : PromotionAction
    data class SelectTab(val tab: PromotionTab) : PromotionAction
    data object Submit : PromotionAction
}

sealed interface PromotionEffect : IUiEffect {
    data class ShowToast(val message: String) : PromotionEffect
}
