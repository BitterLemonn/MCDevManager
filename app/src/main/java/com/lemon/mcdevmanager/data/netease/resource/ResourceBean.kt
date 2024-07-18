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
    val downloadNum: Int,
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
data class ResDetailResponseBean(
    val data: List<ResDetailBean>
)