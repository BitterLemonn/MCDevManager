package com.lemon.mcdevmanager.data.netease.developerFeedback

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeveloperFeedbackResponseBean(
    @SerialName("feedback_id")
    val feedbackId: String
)