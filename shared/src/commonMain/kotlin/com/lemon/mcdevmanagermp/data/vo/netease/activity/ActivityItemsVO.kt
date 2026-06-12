package com.lemon.mcdevmanagermp.data.vo.netease.activity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActivityItemsVO(
    val items: List<ActivityItemVO> = emptyList()
)

@Serializable
data class ActivityItemVO(
    @SerialName("adv_obtain_num")
    val advObtainNum: Int,
    @SerialName("item_id")
    val itemId: String,
    @SerialName("item_name")
    val itemName: String,
    @SerialName("pri_type")
    val priType: Int,
    @SerialName("price")
    val price: Int,
    @SerialName("price_type")
    val priceType: String,
    @SerialName("status")
    val status: String,
    @SerialName("sub_type")
    val subType: Int
)