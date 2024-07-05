package com.lemon.mcdevmanager.data.netease.resourceDetail

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResourceBean(
    @SerialName("create_time")
    val createTime: String,
    @SerialName("item_id")
    val itemId: String,
    @SerialName("item_name")
    val itemName: String,
    @SerialName("online_time")
    val onlineTime: String,
    @SerialName("pri_type")
    val priType: Int,
    val price: Int
)

@Serializable
data class ResourceResponseBean(
    val count: Int,
    val item: List<ResourceBean>
)