package com.lemon.mcdevmanagermp

import com.lemon.mcdevmanagermp.data.consts.enums.WorkItemActionEnum
import com.lemon.mcdevmanagermp.data.consts.enums.WorkItemStatusEnum
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.WorkManageScope
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.WorkManageState
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.WorkStatusFilter
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component.buildWorkTimeEntries
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.splitCardActions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkManageUiLogicTest {

    private val onlinePaid = ResourceData(
        itemId = "460001",
        itemName = "暮色森林扩展包",
        status = WorkItemStatusEnum.ONLINE.des,
        price = 600,
        priceType = "diamond"
    )
    private val onlineWeakOffline = ResourceData(
        itemId = "460002",
        itemName = "老版本材质包",
        status = WorkItemStatusEnum.ONLINE.des,
        price = 0,
        weakOffline = true
    )
    private val reviewing = ResourceData(
        itemId = "460003",
        itemName = "星际探险玩法",
        status = WorkItemStatusEnum.REVIEWING.des,
        price = 1200
    )
    private val draft = ResourceData(
        itemId = "460004",
        itemName = "测试草稿模组",
        status = WorkItemStatusEnum.INIT.des,
        price = 0
    )
    private val rejected = ResourceData(
        itemId = "460005",
        itemName = "待整改地图",
        status = WorkItemStatusEnum.REJECTED.des,
        price = 300
    )
    private val offline = ResourceData(
        itemId = "460006",
        itemName = "历史下架组件",
        status = WorkItemStatusEnum.OFFLINE.des,
        price = 0
    )

    @Test
    fun archiveScopeAndSummaryCountsAreConsistent() {
        val state = WorkManageState(
            items = listOf(onlinePaid, onlineWeakOffline, reviewing, draft, rejected, offline),
            archivedItemIds = setOf("460006", "deleted-id-not-in-list")
        )

        // 全局活跃与已归档计数自动忽略已不存在于列表中的 ID
        assertEquals(6, state.summary.totalCount)
        assertEquals(5, state.summary.activeCount)
        assertEquals(1, state.summary.archivedCount)

        // 活跃视图统计
        assertEquals(2, state.summary.onlineCount)
        assertEquals(1, state.summary.inProgressCount)
        assertEquals(1, state.summary.draftCount)
        // rejected + onlineWeakOffline（弱下架归入需关注）
        assertEquals(2, state.summary.attentionCount)
        assertEquals(listOf("460001", "460002", "460003", "460004", "460005"), state.filteredItems.map { it.itemId })

        // 切换到已归档视图，仅展示归档项，且统计基于归档项计算
        val archivedState = state.copy(scope = WorkManageScope.ARCHIVED)
        assertEquals(listOf("460006"), archivedState.filteredItems.map { it.itemId })
        assertEquals(0, archivedState.summary.onlineCount)
        assertEquals(1, archivedState.summary.attentionCount)
        assertEquals(1, archivedState.countForFilter(WorkStatusFilter.ALL))
    }

    @Test
    fun statusFilterAndSearchQueryComposeAccurately() {
        val base = WorkManageState(
            items = listOf(onlinePaid, onlineWeakOffline, reviewing, draft, rejected, offline),
            archivedItemIds = setOf("460006")
        )

        assertFalse(base.hasActiveFilters)

        val attentionOnly = base.copy(statusFilter = WorkStatusFilter.ATTENTION)
        assertTrue(attentionOnly.hasActiveFilters)
        assertEquals(listOf("460002", "460005"), attentionOnly.filteredItems.map { it.itemId })

        val byName = base.copy(searchQuery = "星际")
        assertTrue(byName.hasActiveFilters)
        assertEquals(listOf("460003"), byName.filteredItems.map { it.itemId })

        val byId = base.copy(searchQuery = "460004")
        assertEquals(listOf("460004"), byId.filteredItems.map { it.itemId })

        val combined = base.copy(statusFilter = WorkStatusFilter.ONLINE, searchQuery = "材质")
        assertEquals(listOf("460002"), combined.filteredItems.map { it.itemId })
    }

    @Test
    fun splitCardActionsPreservesAllActionsWithoutDuplication() {
        // 已上架付费：主操作=更新，次操作=查看详情，更多=调整定价/查看反馈
        val paidSplit = onlinePaid.splitCardActions(isArchived = false)
        assertEquals(WorkItemActionEnum.UPDATE, paidSplit.primary)
        assertEquals(WorkItemActionEnum.VIEW_DETAIL, paidSplit.secondary)
        assertEquals(
            listOf(WorkItemActionEnum.ADJUST_PRICE, WorkItemActionEnum.VIEW_FEEDBACK),
            paidSplit.overflow
        )

        // 已归档状态下，主操作为查看详情，次操作位留给「移出归档」，其余动作进入 overflow
        val archivedSplit = onlinePaid.splitCardActions(isArchived = true)
        assertEquals(WorkItemActionEnum.VIEW_DETAIL, archivedSplit.primary)
        assertNull(archivedSplit.secondary)
        assertEquals(
            setOf(
                WorkItemActionEnum.UPDATE,
                WorkItemActionEnum.ADJUST_PRICE,
                WorkItemActionEnum.VIEW_FEEDBACK
            ),
            archivedSplit.overflow.toSet()
        )

        // 遍历全部状态，校验查看详情绝不进入更多菜单，且主+次+更多 集合与原 actions 完全等价无重复
        WorkItemStatusEnum.entries.forEach { status ->
            listOf(0, 500).forEach { price ->
                val sample = ResourceData(itemId = "id", status = status.des, price = price)
                val expected = status.actions(isFree = price <= 0)
                listOf(false, true).forEach { archived ->
                    val split = sample.splitCardActions(isArchived = archived)
                    assertFalse(WorkItemActionEnum.VIEW_DETAIL in split.overflow)
                    val combined = buildList {
                        split.primary?.let(::add)
                        split.secondary?.let(::add)
                        addAll(split.overflow)
                    }
                    assertEquals(expected.size, combined.size)
                    assertEquals(expected.toSet(), combined.toSet())
                }
            }
        }
    }

    @Test
    fun buildWorkTimeEntriesIncludesReviewTimeWhenValid() {
        val full = ResourceData(
            itemId = "460010",
            createTime = "2026-10-01 12:00:00",
            applyReviewTime = "2026-10-03 18:30:00",
            onlineTime = "2026-10-05 10:00:00"
        )
        assertEquals(
            listOf("创建 2026-10-01", "提审 2026-10-03", "上线 2026-10-05"),
            buildWorkTimeEntries(full)
        )

        val draftOnly = ResourceData(
            itemId = "460011",
            createTime = "2026-10-01 12:00:00",
            applyReviewTime = "",
            onlineTime = "UNKNOWN"
        )
        assertEquals(listOf("创建 2026-10-01"), buildWorkTimeEntries(draftOnly))
    }
}
