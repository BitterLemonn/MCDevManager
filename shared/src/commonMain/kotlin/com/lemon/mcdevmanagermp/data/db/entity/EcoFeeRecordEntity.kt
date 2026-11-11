package com.lemon.mcdevmanagermp.data.db.entity

import androidx.room.Entity

/**
 * 每月生态费反推记录，按账号 + 月份隔离。
 * 该月结算单出账且流水完整、反推出 0~100% 费率后保存，供收益详情页查看历史生态费。
 */
@Entity(tableName = "eco_fee_record", primaryKeys = ["accountKey", "dataMonth"])
data class EcoFeeRecordEntity(
    val accountKey: String,
    /** 数据月份，统一格式 yyyy-MM */
    val dataMonth: String,
    /** 反推出的实际生态费率（0~1） */
    val ecoFeeRate: Double,
    /** 实际生态费金额（元） */
    val ecoFeeAmount: Double,
    /** 结算单实际税前收益，已剔除畅玩计划（元） */
    val actualIncome: Double,
    /** 结算单畅玩计划收益（元） */
    val playPlanIncome: Double,
    /** 预估总收益（元） */
    val estimatedTotalProfit: Double,
    /** 月总流水（钻石） */
    val sumProfit: Double,
    val updatedAt: Long
)
