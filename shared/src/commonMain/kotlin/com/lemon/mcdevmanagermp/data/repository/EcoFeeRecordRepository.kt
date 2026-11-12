package com.lemon.mcdevmanagermp.data.repository

import com.lemon.mcdevmanagermp.data.common.AppContext
import com.lemon.mcdevmanagermp.data.common.JSONConverter
import com.lemon.mcdevmanagermp.data.db.entity.EcoFeeRecordEntity
import com.lemon.mcdevmanagermp.data.vo.netease.income.IncomeVO
import com.lemon.mcdevmanagermp.domain.main.ProfitMonth
import com.lemon.mcdevmanagermp.utils.ProfitData
import com.lemon.mcdevmanagermp.utils.reverseEcoFeeRate
import kotlin.time.Clock

/**
 * 结算单 dataMonth 规范化为 yyyy-MM；无法解析时返回 null。
 * 兼容 "2026-10"、"2026-1"、"202610" 等格式。
 */
internal fun normalizeMonthKey(dataMonth: String): String? {
    val digits = dataMonth.filter { it.isDigit() }
    if (digits.length < 5) return null
    val year = digits.substring(0, 4).toIntOrNull() ?: return null
    val month = digits.substring(4).take(2).toIntOrNull() ?: return null
    if (month !in 1..12) return null
    return monthKey(ProfitMonth(year, month))
}

internal fun monthKey(month: ProfitMonth): String =
    "${month.year.toString().padStart(4, '0')}-${month.month.toString().padStart(2, '0')}"

/** 结算单 dataMonth 解析为 [ProfitMonth]；无法解析时返回 null。 */
internal fun parseMonthKey(dataMonth: String): ProfitMonth? {
    val key = normalizeMonthKey(dataMonth) ?: return null
    val year = key.substring(0, 4).toIntOrNull() ?: return null
    val month = key.substring(5).toIntOrNull() ?: return null
    return ProfitMonth(year, month)
}

/**
 * 结算单税前收益换算为反推生态费的输入口径：结算单 income 已包含畅玩计划收益，
 * 而预估公式（分成 + 模组激励 + 流水激励）不包含，需剔除后再反推。
 */
internal val IncomeVO.reverseInputIncome: Double
    get() = (income.toDoubleOrNull() ?: 0.0) - (playPlanIncome.toDoubleOrNull() ?: 0.0)

/**
 * 每月生态费记录存取：计算周期截止后保存该月预估数据快照，
 * 结算单出账后用快照反推生态费，历史记录供收益结算界面展示费率。
 */
internal class EcoFeeRecordRepository {
    companion object {
        val INSTANCE by lazy { EcoFeeRecordRepository() }
    }

    /**
     * 计算周期截止后保存该月预估数据快照（要求流水完整，无流水亦无意义）。
     * 已反推出费率的月份不覆盖费率结果，仅刷新快照与时间。
     */
    suspend fun saveSnapshot(accountKey: String, month: ProfitMonth, profitData: ProfitData) {
        if (accountKey.isBlank() || !profitData.isFlowComplete) return
        if (profitData.developerProfit <= 0.0) return
        val dataMonth = monthKey(month)
        val existing = AppContext.database.ecoFeeRecordDao().get(accountKey, dataMonth)
        AppContext.database.ecoFeeRecordDao().upsert(
            EcoFeeRecordEntity(
                accountKey = accountKey,
                dataMonth = dataMonth,
                profitDataJson = JSONConverter.encodeToString(ProfitData.serializer(), profitData),
                ecoFeeRate = existing?.ecoFeeRate,
                ecoFeeAmount = existing?.ecoFeeAmount,
                actualIncome = existing?.actualIncome,
                playPlanIncome = existing?.playPlanIncome,
                updatedAt = Clock.System.now().toEpochMilliseconds()
            )
        )
    }

    /**
     * 结算单出账后反推生态费并更新记录。
     * 优先使用周期截止时保存的快照计算（避免出账时平台流水数据已调整）；无快照时用传入的预估计据。
     * 无流水、流水不完整或费率超出 0~100% 时不写入。
     *
     * @return 是否写入了反推结果
     */
    suspend fun saveReverseResult(
        accountKey: String,
        month: ProfitMonth,
        profitData: ProfitData,
        settlement: IncomeVO
    ): Boolean {
        if (accountKey.isBlank() || !profitData.isFlowComplete) return false
        val dataMonth = monthKey(month)
        val existing = AppContext.database.ecoFeeRecordDao().get(accountKey, dataMonth)
        val snapshot = existing?.profitDataJson?.let { decodeProfitData(it) } ?: profitData
        val ecoFeeRate = snapshot.reverseEcoFeeRate(settlement.reverseInputIncome) ?: return false
        if (ecoFeeRate !in 0.0..1.0) return false
        AppContext.database.ecoFeeRecordDao().upsert(
            EcoFeeRecordEntity(
                accountKey = accountKey,
                dataMonth = dataMonth,
                profitDataJson = JSONConverter.encodeToString(ProfitData.serializer(), snapshot),
                ecoFeeRate = ecoFeeRate,
                ecoFeeAmount = snapshot.sumProfit / 100.0 * ecoFeeRate,
                actualIncome = settlement.reverseInputIncome,
                playPlanIncome = settlement.playPlanIncome.toDoubleOrNull() ?: 0.0,
                updatedAt = Clock.System.now().toEpochMilliseconds()
            )
        )
        return true
    }

    /** 读取某月已保存的预估快照；未保存时返回 null */
    suspend fun getSnapshot(accountKey: String, month: ProfitMonth): ProfitData? {
        val record = AppContext.database.ecoFeeRecordDao().get(accountKey, monthKey(month)) ?: return null
        return decodeProfitData(record.profitDataJson)
    }

    /** 某月是否已反推出费率 */
    suspend fun isRated(accountKey: String, month: ProfitMonth): Boolean =
        AppContext.database.ecoFeeRecordDao().get(accountKey, monthKey(month))?.ecoFeeRate != null

    /** 已反推出费率的历史记录，按月份倒序 */
    suspend fun getRated(accountKey: String): List<EcoFeeRecordEntity> =
        AppContext.database.ecoFeeRecordDao().getRatedByAccount(accountKey)

    private fun decodeProfitData(json: String): ProfitData? = try {
        JSONConverter.decodeFromString(ProfitData.serializer(), json)
    } catch (e: Exception) {
        null
    }
}
