package com.lemon.mcdevmanagermp.data.dto.netease.work

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 提交审核请求体（开平第3节 PUT /items/categories/pe/{item_id}/apply_review）。 */
@Serializable
data class ApplyReviewDTO(
    @SerialName("apply_review_text") val applyReviewText: String = "",
    @SerialName("conflict_notify") val conflictNotify: Int = 0,
    @SerialName("conflict_notify_type") val conflictNotifyType: List<Int>? = null,
    @SerialName("is_check_apply") val isCheckApply: Boolean = false
)

/** 上架请求体（开平第6节 PUT /items/categories/pe/{item_id}/online）。 */
@Serializable
data class OnlineItemDTO(@SerialName("op_platform") val opPlatform: String = "all")

/** 定时上架请求体（开平第7节；appointOnlineTime=null 表示取消）。 */
@Serializable
data class AppointOnlineDTO(
    @SerialName("appoint_online_time") val appointOnlineTime: String? = null,
    @SerialName("op_platform") val opPlatform: String = "all"
)
