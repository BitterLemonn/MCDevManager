package com.lemon.mcdevmanagermp.data.vo.netease.ranklist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HotSearchData(
    val content: String,
    @SerialName("dateid")
    val dateId: String,
    @SerialName("day_buy_role_1")
    val dayBuyRole1: Int,
    @SerialName("day_buy_role_2")
    val dayBuyRole2: Int,
    @SerialName("day_buy_role_3")
    val dayBuyRole3: Int,
    @SerialName("day_buy_role_4")
    val dayBuyRole4: Int,
    @SerialName("day_buy_role_5")
    val dayBuyRole5: Int,
    @SerialName("day_buy_role_6")
    val dayBuyRole6: Int,
    @SerialName("day_buy_role_7")
    val dayBuyRole7: Int,
    @SerialName("hot_search_value")
    val hotSearchValue: Double,
    @SerialName("hot_value")
    val hotValue: Double,
    @SerialName("last_rank")
    val lastRank: Int,
    val rank: Int
) {
    val rankChange: Int
        get() = if (lastRank == -1) 0 else lastRank - rank
    val isNew: Boolean
        get() = lastRank == -1
}
