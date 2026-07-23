package com.lemon.mcdevmanagermp.data.vo.netease.promotion

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CanApplyPromotionVO(
    val permit: Map<String, Boolean>,
    val reason: Map<String, String>,
    @SerialName("recent_records")
    val recentRecords: Map<String, Boolean>
)
