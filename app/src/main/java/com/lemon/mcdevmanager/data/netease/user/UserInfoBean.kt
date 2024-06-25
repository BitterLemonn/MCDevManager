package com.lemon.mcdevmanager.data.netease.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserInfoBean(
    val exp: Int,
    val level: Int,
    @SerialName("head_img")
    val headImg: String,
    val nickname: String,
    val income: String,
    @SerialName("onsale_item_count")
    val onSaleItemCount: Int,
)