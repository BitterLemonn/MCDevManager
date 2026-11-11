package com.lemon.mcdevmanagermp.data.db.entity

import androidx.room.Entity

/**
 * 每月生态费记录，按账号 + 月份隔离。
 * 两阶段写入：计算周期截止后登录时保存该月预估数据快照（profitDataJson）；
 * 结算单出账后用快照数据反推生态费，补全费率相关字段。
 */
@Entity(tableName = "eco_fee_record", primaryKeys = ["accountKey", "dataMonth"])
data class EcoFeeRecordEntity(
    val accountKey: String,
    /** 数据月份，统一格式 yyyy-MM */
    val dataMonth: String,
    /** 周期截止时保存的预估数据（ProfitData JSON），出账后以其为准反推 */
    val profitDataJson: String,
    /** 反推出的实际生态费率（0~1）；结算单未出账时为 null */
    val ecoFeeRate: Double? = null,
    /** 实际生态费金额（元）；未出账时为 null */
    val ecoFeeAmount: Double? = null,
    /** 结算单实际税前收益，已剔除畅玩计划（元）；未出账时为 null */
    val actualIncome: Double? = null,
    /** 结算单畅玩计划收益（元）；未出账时为 null */
    val playPlanIncome: Double? = null,
    val updatedAt: Long
)
