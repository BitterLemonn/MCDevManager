package com.lemon.mcdevmanager.data.database.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DetailEntity(
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
    val refundRate: Int,
    @SerialName("res_name")
    val resName: String,
    @SerialName("upload_time")
    val uploadTime: String
)