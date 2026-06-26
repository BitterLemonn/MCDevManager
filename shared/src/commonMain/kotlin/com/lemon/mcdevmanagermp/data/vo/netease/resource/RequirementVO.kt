package com.lemon.mcdevmanagermp.data.vo.netease.resource

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequirementVO(
    val count: Int = 0,
    val item: List<RequirementItemData> = emptyList()
)

@Serializable
data class RequirementItemData(
    @SerialName("developer_name")
    val developerName: String = "",
    @SerialName("is_owner")
    val isOwner: Boolean = false,
    @SerialName("item_id")
    val itemId: String = "",
    @SerialName("item_name")
    val itemName: String = "",
    @SerialName("mc_version")
    val mcVersion: List<String> = emptyList(),
    @SerialName("pri_type")
    val priType: Int = 0,
    @SerialName("sub_type")
    val subType: Int = 0
)