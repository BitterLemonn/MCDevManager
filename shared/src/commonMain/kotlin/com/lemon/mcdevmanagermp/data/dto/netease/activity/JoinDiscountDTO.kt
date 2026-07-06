package com.lemon.mcdevmanagermp.data.dto.netease.activity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JoinDiscountDTO(
    @SerialName("activity_id")
    val activityId: String,
    @SerialName("module_id")
    val moduleId: String,
    @SerialName("item_id_list")
    val itemIdList: List<String>,
    val discount: Int,
    @SerialName("partition_id")
    val partitionId: String,
    val intro: String
)