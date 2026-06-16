package com.lemon.mcdevmanagermp.data.consts.enums

/**
 * 作品上架操作类型
 */
enum class WorkItemActionEnum(val label: String) {
    SUBMIT_REVIEW("提交审核"),
    PUBLISH("上架"),
    UPDATE("更新"),
    ADJUST_PRICE("调整定价"),
    CANCEL_TEST("取消自测")
}

/**
 * 作品上架状态。
 * @param label 状态展示文案
 */
enum class WorkItemStatusEnum(val label: String) {
    ONLINE("已上架"),
    REVIEWING("审核中"),
    REJECTED("审核未通过"),
    UNPUBLISHED("待上架"),
    OFFLINE("已下架"),
    SELF_TEST("自测中"),
    SYSTEM_OFFLINE("系统下架"),
    UNKNOWN("未知");

    /**
     * 该状态下可执行的操作列表。
     * @param isFree 作品是否免费（price <= 0）
     */
    fun actions(isFree: Boolean): List<WorkItemActionEnum> = when (this) {
        ONLINE -> buildList {
            add(WorkItemActionEnum.UPDATE)
            if (!isFree) add(WorkItemActionEnum.ADJUST_PRICE)
        }

        REJECTED -> listOf(WorkItemActionEnum.SUBMIT_REVIEW)
        UNPUBLISHED -> listOf(WorkItemActionEnum.SUBMIT_REVIEW)
        OFFLINE -> listOf(WorkItemActionEnum.PUBLISH)
        SYSTEM_OFFLINE -> listOf(WorkItemActionEnum.UPDATE)
        SELF_TEST -> listOf(WorkItemActionEnum.CANCEL_TEST)
        REVIEWING, UNKNOWN -> emptyList()
    }

    fun fromStatusString(status: String): WorkItemStatusEnum = when (status) {
        "online" -> ONLINE
        "reviewing" -> REVIEWING
        "rejected" -> REJECTED
        "unpublished" -> UNPUBLISHED
        "offline" -> OFFLINE
        "self_test" -> SELF_TEST
        "system_offline" -> SYSTEM_OFFLINE
        else -> UNKNOWN
    }
}
