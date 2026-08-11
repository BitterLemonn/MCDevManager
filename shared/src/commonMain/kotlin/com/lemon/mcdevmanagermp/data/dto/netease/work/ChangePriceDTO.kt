package com.lemon.mcdevmanagermp.data.dto.netease.work

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChangePriceDTO(
    val price: Int,
    @SerialName("price_rank")
    val priceRank: Int,
    @SerialName("price_type")
    val priceType: String
)
