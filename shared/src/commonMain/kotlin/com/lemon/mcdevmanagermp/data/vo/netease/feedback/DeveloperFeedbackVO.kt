package com.lemon.mcdevmanagermp.data.vo.netease.feedback

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeveloperFeedbackVO(
    @SerialName("feedback_id")
    val feedbackId: String
)
