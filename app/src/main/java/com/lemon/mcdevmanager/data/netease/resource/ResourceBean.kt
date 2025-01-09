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
data class ResDetailResponseBean(
    val data: List<ResDetailBean>
)

@Serializable
data class ResMonthDetailResponseBean(
    val data: List<ResMonthDetailBean>
)