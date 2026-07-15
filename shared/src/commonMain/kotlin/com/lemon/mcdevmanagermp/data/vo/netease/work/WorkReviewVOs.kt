package com.lemon.mcdevmanagermp.data.vo.netease.work

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 提交审核响应
 */
@Serializable
data class ReviewApplyResultVO(
    @SerialName("need_check_apply")
    val needCheckApply: Boolean = false,
    @SerialName("queue_length")
    val queueLength: Int = 0
)

/**
 * 审核反馈响应
 */
@Serializable
data class ReviewFeedbackVO(
    val feedback: String = "",
    @SerialName("op_time")
    val opTime: String = "",
    @SerialName("rich_feedback")
    val richFeedback: String = ""
)
