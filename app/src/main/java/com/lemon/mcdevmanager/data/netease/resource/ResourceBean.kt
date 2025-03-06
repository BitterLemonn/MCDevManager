package com.lemon.mcdevmanager.data.netease.resource

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResourceBean(
    @SerialName("create_time")
    val createTime: String,
    @SerialName("item_id")
    val itemId: String,
    @SerialName("item_name")
    val itemName: String,
    @SerialName("online_time")
    val onlineTime: String = "UNKNOWN",
    @SerialName("pri_type")
    val priType: Int,
    val price: Int
)

@Serializable
data class ResourceResponseBean(
    val count: Int,
    val item: List<ResourceBean>
)


@Serializable
data class ResDetailBean(
    @SerialName("DAU")
    val dau: Int,
    @SerialName("cnt_buy")
    val cntBuy: Int,
    @SerialName("dateid")
    val dateId: String,
    @SerialName("diamond")
    val diamond: Int,
    @SerialName("download_num")
    val downloadNum: Int = 0,
    @SerialName("iid")
    val iid: String,
    @SerialName("platform")
    val platform: String,
    @SerialName("points")
    val points: Int,
    @SerialName("refund_rate")
    val refundRate: Double,
    @SerialName("res_name")
    val resName: String,
    @SerialName("upload_time")
    val uploadTime: String
)

@Serializable
data class ResMonthDetailBean(
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
data class NewResDetailBean(
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
    val uploadTime: String
)

@Serializable
data class NewResDetailResponseBean(
    val data: List<NewResDetailBean>
)

@Serializable
data class ResDetailResponseBean(
    val data: List<ResDetailBean>
)

@Serializable
data class ResMonthDetailResponseBean(
    val data: List<ResMonthDetailBean>
)