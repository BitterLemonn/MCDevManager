package com.lemon.mcdevmanagermp.data.vo.netease.activity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscountItemsVO(
    val items: List<DiscountItemVO>
)

@Serializable
data class DiscountItemVO(
    @SerialName("item_id")
    val itemId: String,
    @SerialName("item_name")
    val itemName: String,
    @SerialName("pri_type")
    val priType: Int,
    @SerialName("sub_type")
    val subType: Int,
    @SerialName("price")
    val price: Int,
    @SerialName("price_type")
    val priceType: String,
    @SerialName("discount_activity_discount")
    val discountActivityDiscount: Int,
    @SerialName("discount_activity_partition_id")
    val discountActivityPartitionId: String,
    @SerialName("discount_activity_partition_status")
    val discountActivityPartitionStatus: String,
    @SerialName("discount_activity_status")
    val discountActivityStatus: String,
)