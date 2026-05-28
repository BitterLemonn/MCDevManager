package com.lemon.mcdevmanagermp.data.vo.netease.ranklist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommonRankListData(
    @SerialName("item_id")
    val itemId: String,
    @SerialName("item_name")
    val itemName: String,
    val rank: Int,
    @SerialName("rank_change")
    private val _rankChange: Int,
    @SerialName("sub_type")
    val subType: String
) {
    val rankChange: Int
        get() = _rankChange * -1
    val isNew: Boolean
        get() = rank - rankChange > 50
}
