package com.lemon.mcdevmanagermp.ui.pages.work.workmanage.model

import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.model.WorkItemStatus.Companion.fromRealStatus


/**
 * 作品上架操作类型
 */
enum class WorkItemAction(val label: String) {
    SUBMIT_REVIEW("提交审核"),
    PUBLISH("上架"),
    UPDATE("更新"),
    ADJUST_PRICE("调整定价")
}

/**
 * 作品上架状态。
 *
 * 注意：[fromRealStatus] 的状态码映射为基于网易 MC 平台的占位推断，
 * 接入真实写操作接口或确认状态码后可在此调整。
 *
 * @param label 状态展示文案
 */
enum class WorkItemStatus(val label: String) {
    ONLINE("已上架"),
    REVIEWING("审核中"),
    REJECTED("审核未通过"),
    UNPUBLISHED("待上架"),
    OFFLINE("已下架"),
    SYSTEM_OFFLINE("系统下架"),
    UNKNOWN("未知");

    /**
     * 该状态下可执行的操作列表。
     * 已上架(ONLINE)的非免费资源额外支持「调整定价」。
     *
     * @param isFree 作品是否免费（price <= 0）
     */
    fun actions(isFree: Boolean): List<WorkItemAction> = when (this) {
        ONLINE -> buildList {
            add(WorkItemAction.UPDATE)
            if (!isFree) add(WorkItemAction.ADJUST_PRICE)
        }
        REJECTED -> listOf(WorkItemAction.SUBMIT_REVIEW)
        UNPUBLISHED -> listOf(WorkItemAction.SUBMIT_REVIEW)
        OFFLINE -> listOf(WorkItemAction.PUBLISH)
        SYSTEM_OFFLINE -> listOf(WorkItemAction.UPDATE)
        REVIEWING, UNKNOWN -> emptyList()
    }

    companion object {
        /**
         * 根据接口返回的 item_real_status 码推断作品状态
         */
        fun fromRealStatus(code: Int): WorkItemStatus = when (code) {
            1 -> ONLINE
            2 -> REVIEWING
            3 -> REJECTED
            4 -> UNPUBLISHED
            5 -> OFFLINE
            8 -> SYSTEM_OFFLINE
            else -> run {
                print("未知状态码：$code")
                UNKNOWN
            }
        }
    }
}
