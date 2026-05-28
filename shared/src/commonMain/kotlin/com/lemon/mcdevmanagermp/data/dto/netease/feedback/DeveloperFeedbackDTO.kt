package com.lemon.mcdevmanagermp.data.dto.netease.feedback

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeveloperFeedbackDTO(
    @SerialName("feedback_type")
    val feedbackType: String,
    @SerialName("function_type")
    val functionType: String,
    @SerialName("desc")
    val content: String,
    val contact: String,
    @SerialName("extra_list")
    val extraList: List<String> = emptyList()
)