package com.lemon.mcdevmanagermp.ui.pages.work.workmanage

import com.lemon.mcdevmanagermp.data.consts.enums.WorkItemActionEnum
import com.lemon.mcdevmanagermp.data.consts.enums.WorkItemStatusEnum
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import com.lemon.mcdevmanagermp.data.vo.netease.work.ReviewFeedbackVO
import com.lemon.mcdevmanagermp.utils.extension.IUiAction
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import com.lemon.mcdevmanagermp.utils.extension.IUiState

/**
 * 资源管理列表视图作用域：活跃资源与已归档资源分离展示。
 */
enum class WorkManageScope(val label: String) {
    ACTIVE("资源列表"),
    ARCHIVED("已归档")
}

/**
 * 资源状态快捷筛选分类。
 */
enum class WorkStatusFilter(val label: String) {
    ALL("全部"),
    ONLINE("已上架"),
    IN_PROGRESS("审测/待上架"),
    DRAFT("待提审"),
    ATTENTION("异常/已下架")
}

/**
 * 资源管理页状态概览统计。
 */
data class WorkManageSummary(
    val totalCount: Int = 0,
    val activeCount: Int = 0,
    val archivedCount: Int = 0,
    val onlineCount: Int = 0,
    val inProgressCount: Int = 0,
    val draftCount: Int = 0,
    val attentionCount: Int = 0
)

/**
 * 作品卡片操作栏拆分结果：主操作 + 次操作 + 更多菜单项。
 */
data class WorkCardActionSplit(
    val primary: WorkItemActionEnum?,
    val secondary: WorkItemActionEnum?,
    val overflow: List<WorkItemActionEnum>
)

/** 判断单个作品是否命中指定状态筛选。 */
fun ResourceData.matchesStatusFilter(filter: WorkStatusFilter): Boolean {
    val itemStatus = getStatus()
    return when (filter) {
        WorkStatusFilter.ALL -> true
        WorkStatusFilter.ONLINE -> itemStatus == WorkItemStatusEnum.ONLINE
        WorkStatusFilter.IN_PROGRESS -> itemStatus in IN_PROGRESS_STATUSES
        WorkStatusFilter.DRAFT -> itemStatus == WorkItemStatusEnum.INIT
        WorkStatusFilter.ATTENTION -> itemStatus in ATTENTION_STATUSES || weakOffline
    }
}

/** 判断单个作品是否匹配搜索关键词（按作品名或作品 ID 模糊匹配）。 */
fun ResourceData.matchesSearch(query: String): Boolean {
    val keyword = query.trim()
    if (keyword.isEmpty()) return true
    return itemName.contains(keyword, ignoreCase = true) ||
        itemId.contains(keyword, ignoreCase = true)
}

/**
 * 将作品当前状态可执行的操作拆分为主操作、次操作和更多菜单，避免按钮堆积换行。
 * 「查看详情」始终直接展示在卡片操作栏中，不收纳至更多菜单；
 * 未归档时展示「状态主操作 + 查看详情」，已归档时展示「查看详情 + 移出归档」。
 */
fun ResourceData.splitCardActions(isArchived: Boolean): WorkCardActionSplit {
    val itemStatus = getStatus()
    val allActions = itemStatus.actions(isFree = price <= 0)
    if (allActions.isEmpty()) {
        return WorkCardActionSplit(primary = null, secondary = null, overflow = emptyList())
    }

    if (isArchived) {
        val primary = WorkItemActionEnum.VIEW_DETAIL.takeIf { it in allActions } ?: allActions.first()
        val overflow = allActions.filter { it != primary }
        return WorkCardActionSplit(primary = primary, secondary = null, overflow = overflow)
    }

    val preferredPrimary = when (itemStatus) {
        WorkItemStatusEnum.ONLINE -> WorkItemActionEnum.UPDATE
        WorkItemStatusEnum.INIT -> WorkItemActionEnum.SUBMIT_REVIEW
        WorkItemStatusEnum.ACCEPT -> WorkItemActionEnum.PUBLISH
        WorkItemStatusEnum.REJECTED,
        WorkItemStatusEnum.OFFLINE,
        WorkItemStatusEnum.SYSTEM_OFFLINE -> WorkItemActionEnum.UPDATE

        WorkItemStatusEnum.PREPARING,
        WorkItemStatusEnum.REVIEWING -> WorkItemActionEnum.CANCEL_REVIEW

        WorkItemStatusEnum.SELF_TEST,
        WorkItemStatusEnum.SELF_TEST_PREPARE -> WorkItemActionEnum.CANCEL_TEST

        WorkItemStatusEnum.ONLINE_PREPARING,
        WorkItemStatusEnum.UNKNOWN -> WorkItemActionEnum.VIEW_DETAIL
    }
    val primary = preferredPrimary.takeIf { it in allActions } ?: allActions.first()
    val secondary = WorkItemActionEnum.VIEW_DETAIL.takeIf { it in allActions && it != primary }
    val overflow = allActions.filter { it != primary && it != secondary }
    return WorkCardActionSplit(primary = primary, secondary = secondary, overflow = overflow)
}

private val IN_PROGRESS_STATUSES = setOf(
    WorkItemStatusEnum.PREPARING,
    WorkItemStatusEnum.REVIEWING,
    WorkItemStatusEnum.SELF_TEST,
    WorkItemStatusEnum.SELF_TEST_PREPARE,
    WorkItemStatusEnum.ACCEPT,
    WorkItemStatusEnum.ONLINE_PREPARING
)

private val ATTENTION_STATUSES = setOf(
    WorkItemStatusEnum.REJECTED,
    WorkItemStatusEnum.OFFLINE,
    WorkItemStatusEnum.SYSTEM_OFFLINE
)

data class WorkManageState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isPerforming: Boolean = false,
    val performingMessage: String = "",
    val items: List<ResourceData> = emptyList(),
    val archivedItemIds: Set<String> = emptySet(),
    val scope: WorkManageScope = WorkManageScope.ACTIVE,
    val statusFilter: WorkStatusFilter = WorkStatusFilter.ALL,
    val searchQuery: String = "",
    val feedback: ReviewFeedbackVO? = null,
    val feedbackItemName: String = ""
) : IUiState {

    /** 当前未归档的作品列表 */
    val activeItems: List<ResourceData>
        get() = items.filterNot { it.itemId in archivedItemIds }

    /** 当前已归档的作品列表 */
    val archivedItems: List<ResourceData>
        get() = items.filter { it.itemId in archivedItemIds }

    /** 当前视图作用域下的作品全集 */
    val scopedItems: List<ResourceData>
        get() = when (scope) {
            WorkManageScope.ACTIVE -> activeItems
            WorkManageScope.ARCHIVED -> archivedItems
        }

    /** 概览统计：顶部总数反映全局活跃/归档分布，四类状态计数反映当前作用域分布 */
    val summary: WorkManageSummary
        get() {
            val active = activeItems
            val archived = archivedItems
            val scoped = when (scope) {
                WorkManageScope.ACTIVE -> active
                WorkManageScope.ARCHIVED -> archived
            }
            return WorkManageSummary(
                totalCount = items.size,
                activeCount = active.size,
                archivedCount = archived.size,
                onlineCount = scoped.count { it.matchesStatusFilter(WorkStatusFilter.ONLINE) },
                inProgressCount = scoped.count { it.matchesStatusFilter(WorkStatusFilter.IN_PROGRESS) },
                draftCount = scoped.count { it.matchesStatusFilter(WorkStatusFilter.DRAFT) },
                attentionCount = scoped.count { it.matchesStatusFilter(WorkStatusFilter.ATTENTION) }
            )
        }

    /** 经视图作用域、状态筛选与关键词过滤后的最终展示列表 */
    val filteredItems: List<ResourceData>
        get() = scopedItems.filter { item ->
            item.matchesStatusFilter(statusFilter) && item.matchesSearch(searchQuery)
        }

    /** 是否处于任意筛选或搜索条件中 */
    val hasActiveFilters: Boolean
        get() = statusFilter != WorkStatusFilter.ALL || searchQuery.isNotBlank()

    /** 返回指定状态筛选项在当前作用域中的数量 */
    fun countForFilter(filter: WorkStatusFilter): Int = when (filter) {
        WorkStatusFilter.ALL -> scopedItems.size
        WorkStatusFilter.ONLINE -> summary.onlineCount
        WorkStatusFilter.IN_PROGRESS -> summary.inProgressCount
        WorkStatusFilter.DRAFT -> summary.draftCount
        WorkStatusFilter.ATTENTION -> summary.attentionCount
    }
}

sealed interface WorkManageAction : IUiAction {
    data object LoadData : WorkManageAction
    data object RefreshData : WorkManageAction

    /** 切换资源列表 / 已归档视图 */
    data class SelectScope(val scope: WorkManageScope) : WorkManageAction

    /** 切换状态筛选 */
    data class SelectStatusFilter(val filter: WorkStatusFilter) : WorkManageAction

    /** 更新搜索关键词 */
    data class UpdateSearchQuery(val query: String) : WorkManageAction

    /** 清空状态筛选与搜索词 */
    data object ClearFilters : WorkManageAction

    /** 将单个作品加入本地归档 */
    data class ArchiveItem(val item: ResourceData) : WorkManageAction

    /** 将单个作品移出本地归档 */
    data class UnarchiveItem(val item: ResourceData) : WorkManageAction

    /** 批量将选中的作品加入本地归档 */
    data class ArchiveItems(val itemIds: Set<String>) : WorkManageAction

    /** 执行仅需 itemId 的写操作 */
    data class PerformAction(val item: ResourceData, val action: WorkItemActionEnum) :
        WorkManageAction

    /** 提交自测，passCheck=true 表示免机审 */
    data class SubmitSelfTest(val item: ResourceData, val passCheck: Boolean) : WorkManageAction

    /** 调整定价 */
    data class AdjustPrice(val item: ResourceData, val newPrice: Int) : WorkManageAction

    /** 定时上架(7)：time 格式 "YYYY-MM-DD HH:mm:ss" */
    data class AppointOnline(val item: ResourceData, val time: String) : WorkManageAction

    /** 查看审核反馈(5)：拉取后写入 state.feedback 展示 */
    data class LoadFeedback(val item: ResourceData) : WorkManageAction

    /** 关闭审核反馈弹窗 */
    data object DismissFeedback : WorkManageAction
}

sealed interface WorkManageEffect : IUiEffect {
    data class ShowToast(val message: String) : WorkManageEffect
    data object NeedReLogin : WorkManageEffect
}
