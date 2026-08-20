package com.lemon.mcdevmanagermp.data.dto.netease.income

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApplyIncomeDTO(
    @SerialName("income_id")
    val incomeIds: List<String>
)

@Serializable
data class LobbyIncomeResourceListVO(
    val count: Int = 0,
    val items: List<LobbyIncomeResourceVO> = emptyList()
)

@Serializable
data class LobbyIncomeResourceVO(
    @SerialName("item_id")
    val itemId: String = "",
    @SerialName("item_name")
    val itemName: String = ""
)

// 实时收益
@Serializable
data class OneResRealtimeIncomeVO(
    val count: Int = 0,
    @SerialName("total_diamonds")
    val totalDiamonds: Int = 0,
    @SerialName("total_points")
    val totalPoints: Int = 0,
    val orders: List<OneResRealtimeIncomeOrder> = emptyList()
)

// 一条实时收益
@Serializable
data class OneResRealtimeIncomeOrder(
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
