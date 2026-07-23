package com.lemon.mcdevmanagermp.data.consts.enums

import kotlinx.serialization.Serializable

/**
 * 活动（运营审核）模组状态。
 *
 * 与作品级状态 [WorkItemStatusEnum] 不同：活动审核走 approved/rejected，
 * 作品上架走 accept/rejected，二者不可混用。
 */
@Serializable
enum class ActivityStatusEnum(val value: String, val label: String) {
    REVIEWING("reviewing", "审核中"),
    APPROVED("approved", "已通过"),
    REJECTED("rejected", "已拒绝");

    companion object {
        fun fromValue(value: String): ActivityStatusEnum? = entries.find { it.value == value }
    }
}
