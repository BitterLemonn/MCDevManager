package com.lemon.mcdevmanagermp.data.vo.netease.analyze

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 资源详情（数据分析）
 */
@Serializable
data class ResDetailVO(
    val data: List<ResAnalyzeData>
)

/**
 * 资源月详情（数据分析）
 */
@Serializable
data class ResMonthDetailVO(
    val data: List<ResMonthAnalyzeData>
)


@Serializable
data class ResMonthAnalyzeData(
    @SerialName("avg_dau")
    val avgDau: Int,
    @SerialName("avg_day_buy")
    val avgDayBuy: Int,
    @SerialName("download_num")
    val downloadNum: Int,
    @SerialName("iid")
    val iid: String,
    @SerialName("mau")
    val mau: Int,
    @SerialName("monthid")
    val monthId: String,
    @SerialName("platform")
    val platform: String,
    @SerialName("res_name")
    val resName: String,
    @SerialName("total_diamond")
    val totalDiamond: Int,
    @SerialName("total_points")
    val totalPoints: Int,
    @SerialName("upload_time")
    val uploadTime: String = "UNKNOWN"
)

@Serializable
data class ResAnalyzeData(
    @SerialName("DAU")
    val dau: Int,
    @SerialName("avg_first_type_buy")
    val avgFirstTypeBuy: Double,
    @SerialName("avg_first_type_diamond")
    val avgFirstTypeDiamond: Double,
    @SerialName("avg_first_type_focus")
    val avgFirstTypeFocus: Double,
    @SerialName("avg_first_type_role_play")
    val avgFirstTypeRolePlay: Double,
    @SerialName("avg_playtime")
    val avgPlaytime: Double,
    @SerialName("avg_total_first_type_buy")
    val avgTotalFirstTypeBuy: Double,
    @SerialName("cnt_buy")
    val cntBuy: Int,
    @SerialName("dateid")
    val dateId: String,
    val diamond: Int,
    @SerialName("download_num")
    val downloadNum: Int,
    @SerialName("first_type_avg_role_time")
    val firstTypeAvgRoleTime: Double,
    @SerialName("focus_cnt")
    val focusCnt: Int,
    val iid: String,
    @SerialName("pass_avg_role_time_ratio")
    val passAvgRoleTimeRatio: Double,
    @SerialName("pass_buy_cnt_ratio")
    val passBuyCntRatio: Double,
    @SerialName("pass_cnt_role_play_ratio")
    val passCntRolePlayRatio: Double,
    @SerialName("pass_focus_cnt_ratio")
    val passFocusCntRatio: Double,
    @SerialName("pass_pay_diamond_ratio")
    val passPayDiamondRatio: Double,
    val platform: String,
    val points: Int,
    @SerialName("refund_rate")
    val refundRate: Double,
    @SerialName("res_name")
    val resName: String,
    @SerialName("star_adjusted")
    val starAdjusted: Double,
    @SerialName("upload_time")
    val uploadTime: String,
    @SerialName("wishlist_adds_uv")
    val wishlistAddsUv: Int,  // 愿望单新增
    @SerialName("wishlist_gifts")
    val wishlistGifts: Int,   // 愿望单赠送
    @SerialName("wishlist_purchases")
    val wishlistPurchases: Int, // 愿望单购买
    @SerialName("wishlist_removes_uv")
    val wishlistRemovesUv: Int  // 愿望单移除
)
