package com.lemon.mcdevmanager.data.netease.income

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IncomeBean(
    @SerialName("income_id")
    val incomeIds: List<String>
)

@Serializable
data class OneResRealtimeIncomeBean(
    val count: Int,
    @SerialName("total_diamond")
    val totalDiamond: Int,
    @SerialName("total_points")
    val totalPoints: Int,
    val orders: List<OneResRealtimeIncomeOrderBean>
)

@Serializable
data class OneResRealtimeIncomeOrderBean(
    @SerialName("app_orderid")
    val appOrderId: String,
    @SerialName("app_uid")
    val appUid: String,
    val discount: String,
    val point: Int,
    @SerialName("point_type")
    val pointType: String,
    val price: Int,
    @SerialName("price_type")
    val priceType: String,
    @SerialName("product_name")
    val productName: String,
    @SerialName("purchase_limit")
    val purchaseLimit: Int,
    @SerialName("refund_status")
    val refundStatus: String,
    @SerialName("ship_time")
    val shipTime: String
)