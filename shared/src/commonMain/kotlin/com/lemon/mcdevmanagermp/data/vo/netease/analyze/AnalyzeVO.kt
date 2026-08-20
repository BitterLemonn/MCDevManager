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
    val avgDau: Int = 0,
    @SerialName("avg_day_buy")
    val avgDayBuy: Int,
    @SerialName("download_num")
    val downloadNum: Int = 0,
    @SerialName("iid")
    val iid: String,
    @SerialName("mau")
    val mau: Int = 0,
    @SerialName("monthid")
    val monthId: String,
    @SerialName("platform")
    val platform: String = "pe",
    @SerialName("res_name")
    val resName: String,
    @SerialName("total_diamond")
    val totalDiamond: Int,
    @SerialName("total_points")
    val totalPoints: Int = 0,
    @SerialName("upload_time")
    val uploadTime: String = "UNKNOWN"
)

@Serializable
data class ResAnalyzeData(
    @SerialName("DAU")
    val dau: Int = 0,
    @SerialName("avg_first_type_buy")
    val avgFirstTypeBuy: Double = 0.0,
    @SerialName("avg_first_type_diamond")
    val avgFirstTypeDiamond: Double = 0.0,
    @SerialName("avg_first_type_focus")
    val avgFirstTypeFocus: Double = 0.0,
    @SerialName("avg_first_type_role_play")
    val avgFirstTypeRolePlay: Double = 0.0,
    @SerialName("avg_playtime")
    val avgPlaytime: Double = 0.0,
    @SerialName("avg_total_first_type_buy")
    val avgTotalFirstTypeBuy: Double = 0.0,
    @SerialName("cnt_buy")
    val cntBuy: Int,
    @SerialName("dateid")
    val dateId: String,
    val diamond: Int,
    @SerialName("download_num")
    val downloadNum: Int = 0,
    @SerialName("first_type_avg_role_time")
    val firstTypeAvgRoleTime: Double = 0.0,
    @SerialName("focus_cnt")
    val focusCnt: Int = 0,
    val iid: String,
    @SerialName("pass_avg_role_time_ratio")
    val passAvgRoleTimeRatio: Double = 0.0,
    @SerialName("pass_buy_cnt_ratio")
    val passBuyCntRatio: Double = 0.0,
    @SerialName("pass_cnt_role_play_ratio")
    val passCntRolePlayRatio: Double = 0.0,
    @SerialName("pass_focus_cnt_ratio")
    val passFocusCntRatio: Double = 0.0,
    @SerialName("pass_pay_diamond_ratio")
    val passPayDiamondRatio: Double = 0.0,
    val platform: String = "pe",
    val points: Int = 0,
    @SerialName("refund_rate")
    val refundRate: Double = 0.0,
    @SerialName("res_name")
    val resName: String,
    @SerialName("star_adjusted")
    val starAdjusted: Double = 0.0,
    @SerialName("upload_time")
    val uploadTime: String = "UNKNOWN",
    @SerialName("wishlist_adds_uv")
    val wishlistAddsUv: Int = 0,  // 愿望单新增
    @SerialName("wishlist_gifts")
    val wishlistGifts: Int = 0,   // 愿望单赠送
    @SerialName("wishlist_purchases")
    val wishlistPurchases: Int = 0, // 愿望单购买
    @SerialName("wishlist_removes_uv")
    val wishlistRemovesUv: Int = 0  // 愿望单移除
)
