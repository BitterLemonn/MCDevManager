package com.lemon.mcdevmanager.data.netease.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OverviewBean(
    @SerialName("day_diamond_diff")
    val dayDiamondDiff: Int,
    @SerialName("day_download_diff")
    val dayDownloadDiff: Int,
    @SerialName("days_14_average_diamond")
    val days14AverageDiamond: Int,
    @SerialName("days_14_average_download")
    val days14AverageDownload: Int,
    @SerialName("days_14_total_diamond")
    val days14TotalDiamond: Int,
    @SerialName("days_14_total_download")
    val days14TotalDownload: Int,
    @SerialName("last_month_diamond")
    val lastMonthDiamond: Int,
    @SerialName("last_month_download")
    val lastMonthDownload: Int,
    @SerialName("month_diamond_diff")
    val monthDiamondDiff: Int,
    @SerialName("month_download_diff")
    val monthDownloadDiff: Int,
    @SerialName("this_month_diamond")
    val thisMonthDiamond: Int,
    @SerialName("this_month_download")
    val thisMonthDownload: Int,
    @SerialName("yesterday_diamond")
    val yesterdayDiamond: Int,
    @SerialName("yesterday_download")
    val yesterdayDownload: Int
)