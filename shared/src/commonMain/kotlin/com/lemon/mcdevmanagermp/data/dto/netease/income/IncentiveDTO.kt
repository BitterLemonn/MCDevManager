package com.lemon.mcdevmanagermp.data.dto.netease.income

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IncentiveDTO(
    @SerialName("activity_id")
    val activityId: String,
    @SerialName("incentive_count")
    val incentiveCount: Double,
    val month: String,
    val source: String,
    val status: Int,
    @SerialName("update_time")
    val updateTime: Int,
)

@Serializable
data class IncentiveListDTO(
    @SerialName("incentive_details")
    val incentiveDetails: List<IncentiveDTO>,
    val count: Int
)
