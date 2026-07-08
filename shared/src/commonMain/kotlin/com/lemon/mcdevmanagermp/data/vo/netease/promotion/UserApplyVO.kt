package com.lemon.mcdevmanagermp.data.vo.netease.promotion

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserApplyVO(
    val applications: List<UserApplyItemVO> = emptyList(),
    val count: Int = 0
)

@Serializable
data class UserApplyItemVO(
    val activity: String = "",
    @SerialName("application_id") val applicationId: String = "",
    @SerialName("apply_time") val applyTime: Int = 0,
    @SerialName("apply_to_auction") val applyToAuction: Boolean = false,
    val extra: String = "",
    val feature: String = "",
    @SerialName("have_used_data") val haveUsedData: Boolean = false,
    @SerialName("item_category") val itemCategory: String = "",
    @SerialName("item_id") val itemId: String = "",
    @SerialName("item_name") val itemName: String = "",
    @SerialName("pe_channel") val peChannel: String = "",
    @SerialName("position_id") val positionId: String = "",
    @SerialName("position_name") val positionName: String = "",
    @SerialName("push_content") val pushContent: String = "",
    @SerialName("push_title") val pushTitle: String = "",
    @SerialName("start_time") val startTime: Int = 0,
    val status: String = "",
    val update: String = ""
)
