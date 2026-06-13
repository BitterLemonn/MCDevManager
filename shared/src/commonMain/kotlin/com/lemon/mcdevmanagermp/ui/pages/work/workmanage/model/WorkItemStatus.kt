package com.lemon.mcdevmanagermp.ui.pages.work.workmanage.model

import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.model.WorkItemStatus.Companion.fromRealStatus


/**
 * 作品上架操作类型
 */
enum class WorkItemAction(val label: String) {
    SUBMIT_REVIEW("提交审核"),
    PUBLISH("上架"),
    UPDATE("更新"),
    TAKE_DOWN("下架")
}

/**
 * 作品上架状态。
 *
 * 注意：[fromRealStatus] 的状态码映射为基于网易 MC 平台的占位推断，
 * 接入真实写操作接口或确认状态码后可在此调整。
 *
 * @param label 状态展示文案
 * @param actions 该状态下可执行的操作列表
 */
enum class WorkItemStatus(val label: String, val actions: List<WorkItemAction>) {
    ONLINE("已上架", listOf(WorkItemAction.UPDATE, WorkItemAction.TAKE_DOWN)),
    REVIEWING("审核中", emptyList()),
    REJECTED("审核未通过", listOf(WorkItemAction.SUBMIT_REVIEW)),
    UNPUBLISHED("待上架", listOf(WorkItemAction.SUBMIT_REVIEW)),
    OFFLINE("已下架", listOf(WorkItemAction.PUBLISH)),
    SYSTEM_OFFLINE("系统下架", listOf(WorkItemAction.UPDATE)),
    UNKNOWN("未知", emptyList());

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
