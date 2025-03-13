package com.lemon.mcdevmanager.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class OverviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo val nickname: String,
    @ColumnInfo val days14AverageDiamond: Int,
    @ColumnInfo val days14AverageDownload: Int,
    @ColumnInfo val days14TotalDiamond: Int,
    @ColumnInfo val days14TotalDownload: Int,
    @ColumnInfo val lastMonthDiamond: Int,
    @ColumnInfo val lastMonthDownload: Int,
    @ColumnInfo val thisMonthDiamond: Int,
    @ColumnInfo val thisMonthDownload: Int,
    @ColumnInfo val yesterdayDiamond: Int,
    @ColumnInfo val yesterdayDownload: Int,
    @ColumnInfo val lastMonthProfit: String = "0.00",
    @ColumnInfo val lastMonthTax: String = "0.00",
    @ColumnInfo val thisMonthProfit: String = "0.00",
    @ColumnInfo val thisMonthTax: String = "0.00",
    @ColumnInfo val timestamp: Long = System.currentTimeMillis()
)
