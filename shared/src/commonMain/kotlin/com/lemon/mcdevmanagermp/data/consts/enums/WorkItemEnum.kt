package com.lemon.mcdevmanagermp.data.consts.enums

import kotlinx.serialization.Serializable

/**
 * 作品上架操作类型
 */
enum class WorkItemActionEnum(val label: String) {
    SUBMIT_REVIEW("提交审核"),
    SUBMIT_SELF_TEST("提交自测"),
    CANCEL_REVIEW("取消审核"),
    PUBLISH("上架"),
    UPDATE("更新"),
    ADJUST_PRICE("调整定价"),
    CANCEL_TEST("取消自测"),
    VIEW_FEEDBACK("查看反馈"),
    APPOINT_ONLINE("定时上架"),
    DELETE("删除")
}

/**
 * 作品上架状态。
 * @param label 状态展示文案
 */
@Serializable
enum class WorkItemStatusEnum(val label: String, val des: String) {
    ONLINE("已上架", "online"),
    INIT("待提交审核", "init"),
    PREPARING("系统准备中", "preparing"),
    REVIEWING("审核中", "reviewing"),
    SELF_TEST("自测中", "self_test"),
    SELF_TEST_PREPARE("自测准备中", "self_test_prepare"),
    REJECTED("审核未通过", "reject"),
    ACCEPT("待上架", "accept"),
    ONLINE_PREPARING("系统准备中", "online_preparing"),
    OFFLINE("已下架", "offline"),
    SYSTEM_OFFLINE("系统下架", "system_offline"),
    UNKNOWN("未知", "unknown");

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

        INIT -> listOf(
            WorkItemActionEnum.SUBMIT_REVIEW,
            WorkItemActionEnum.SUBMIT_SELF_TEST,
            WorkItemActionEnum.UPDATE,
            WorkItemActionEnum.DELETE
        )
        // 系统准备中, 审核中 -> [取消审核]
        PREPARING, REVIEWING -> listOf(WorkItemActionEnum.CANCEL_REVIEW)
        // 审核未通过, 系统下架, 弱下架 -> [更新, 查看反馈]
        REJECTED, SYSTEM_OFFLINE, OFFLINE -> listOf(
            WorkItemActionEnum.UPDATE,
            WorkItemActionEnum.VIEW_FEEDBACK
        )
        // 已通过审核 -> [上架, 定时上架]
        ACCEPT -> listOf(WorkItemActionEnum.PUBLISH, WorkItemActionEnum.APPOINT_ONLINE)
        // 自测中, 自测准备中 -> [取消自测]
        SELF_TEST, SELF_TEST_PREPARE -> listOf(WorkItemActionEnum.CANCEL_TEST)
        // 上架准备中
        ONLINE_PREPARING -> emptyList()
        UNKNOWN -> emptyList()
    }

    companion object {
        fun fromStatusString(status: String): WorkItemStatusEnum {
            return entries.firstOrNull { it.des == status } ?: UNKNOWN
        }
    }
}
