package com.lemon.mcdevmanagermp.data.vo.netease.ranklist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PeHotData(
    @SerialName("avg_role_time")
    val avgRoleTime: Double = 0.0,
    @SerialName("b1_cnt_buy")
    val b1CntBuy: Int? = null,
    @SerialName("b1_remark_num")
    val b1RemarkNum: Int? = null,
    @SerialName("b2_cnt_buy")
    val b2CntBuy: Int? = null,
    @SerialName("b2_remark_num")
    val b2RemarkNum: Int? = null,
    @SerialName("b3_cnt_buy")
    val b3CntBuy: Int? = null,
    @SerialName("b3_remark_num")
    val b3RemarkNum: Int? = null,
    @SerialName("b4_cnt_buy")
    val b4CntBuy: Int? = null,
    @SerialName("b4_remark_num")
    val b4RemarkNum: Int? = null,
    @SerialName("b5_cnt_buy")
    val b5CntBuy: Int? = null,
    @SerialName("b5_remark_num")
    val b5RemarkNum: Int? = null,
    @SerialName("b6_cnt_buy")
    val b6CntBuy: Int? = null,
    @SerialName("b6_remark_num")
    val b6RemarkNum: Int? = null,
    @SerialName("cnt_buy")
    val cntBuy: Int = 0,
    @SerialName("create_time")
    val createTime: String = "",
    @SerialName("developer_name")
    val developerName: String = "",
    @SerialName("diamond_price")
    val diamondPrice: Double = 0.0,
    @SerialName("first_type")
    val firstType: String = "",
    @SerialName("icon_url")
    val iconUrl: String = "",
    val iid: String = "",
    val labels: String = "",
    @SerialName("last_rank")
    val lastRank: Int = -1,
    @SerialName("on_rank_days")
    val onRankDays: Int = 0,
    @SerialName("points_price")
    val pointsPrice: Double = 0.0,
    @SerialName("price_type")
    val priceType: String = "",
    @SerialName("remark_num")
    val remarkNum: String = "",
    @SerialName("res_name")
    val resName: String = "",
    @SerialName("score_rank")
    val scoreRank: Int = 0,
    @SerialName("star_adjusted")
    val starAdjusted: Double = 0.0,
    @SerialName("type_avg_role_time")
    val typeAvgRoleTime: Double = 0.0,
    @SerialName("update_dt")
    val updateDt: String = ""
) {
    val rankChange: Int
        get() = lastRank - scoreRank
    val isNew: Boolean
        get() = lastRank == -1
}
