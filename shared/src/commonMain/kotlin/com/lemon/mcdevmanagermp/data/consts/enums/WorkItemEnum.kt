package com.lemon.mcdevmanagermp.data.consts.enums

import kotlinx.serialization.Serializable

/**
 * 作品上架操作类型
 */
enum class WorkItemActionEnum(val label: String) {
    SUBMIT_REVIEW("提交审核"),
    CANCEL_REVIEW("取消审核"),
    PUBLISH("上架"),
    UPDATE("更新"),
    ADJUST_PRICE("调整定价"),
    CANCEL_TEST("取消自测"),
    VIEW_FEEDBACK("查看反馈"),
    APPOINT_ONLINE("定时上架")
}

/**
 * 作品上架状态。
 * @param label 状态展示文案
 */
@Serializable
enum class WorkItemStatusEnum(val label: String) {
    ONLINE("已上架"),
    INIT("待提交审核"),
    PREPARE("系统准备中"),
    REVIEWING("审核中"),
    SELF_TEST("自测中"),
    SELF_TEST_PREPARE("自测准备中"),
    REJECTED("审核未通过"),
    ACCEPT("待上架"),
    OFFLINE("已下架"),
    SYSTEM_OFFLINE("系统下架"),
    UNKNOWN("未知");

    /**
     * 该状态下可执行的操作列表。
     * @param isFree 作品是否免费（price <= 0）
     */
    fun actions(isFree: Boolean): List<WorkItemActionEnum> = when (this) {
        // 已上架 -> [更新, 调整定价, 查看反馈]
        ONLINE -> buildList {
            add(WorkItemActionEnum.UPDATE)
            if (!isFree) add(WorkItemActionEnum.ADJUST_PRICE)
            add(WorkItemActionEnum.VIEW_FEEDBACK)
        }

        INIT -> listOf(WorkItemActionEnum.SUBMIT_REVIEW, WorkItemActionEnum.UPDATE)
        // 系统准备中, 审核中 -> [取消审核]
        PREPARE, REVIEWING -> listOf(WorkItemActionEnum.CANCEL_REVIEW)
        // 审核未通过, 系统下架, 弱下架 -> [更新, 查看反馈]
        REJECTED, SYSTEM_OFFLINE, OFFLINE -> listOf(
            WorkItemActionEnum.UPDATE,
            WorkItemActionEnum.VIEW_FEEDBACK
        )
        // 已通过审核 -> [上架, 定时上架]
        ACCEPT -> listOf(WorkItemActionEnum.PUBLISH, WorkItemActionEnum.APPOINT_ONLINE)
        // 自测中, 自测准备中 -> [取消自测]
        SELF_TEST, SELF_TEST_PREPARE -> listOf(WorkItemActionEnum.CANCEL_TEST)
        UNKNOWN -> emptyList()
    }

    companion object {
        fun fromStatusString(status: String): WorkItemStatusEnum = when (status) {
            "online" -> ONLINE
            "init" -> INIT
            "reviewing" -> REVIEWING
            "rejected" -> REJECTED
            "accept" -> ACCEPT
            "offline" -> OFFLINE
            "self_test" -> SELF_TEST
            "preparing" -> PREPARE
            "self_test_prepare" -> SELF_TEST_PREPARE
            "system_offline" -> SYSTEM_OFFLINE
            else -> UNKNOWN
        }
    }
}
