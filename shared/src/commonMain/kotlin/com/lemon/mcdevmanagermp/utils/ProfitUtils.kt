package com.lemon.mcdevmanagermp.utils

import kotlinx.serialization.Serializable

@Serializable
data class ProfitData(
    val sumProfit: Double = 0.0,
    val totalProfit: Double = 0.0,
    val developerProfit: Double = 0.0,
    val profitSubsidy: Double = 0.0,
    val subsidyProfit: Map<String, Double> = emptyMap(),
    val subsidyPercent: Double = 0.0,
    val moduleDiamonds: Map<String, Double> = emptyMap(),
    /**
     * 绿宝石计价模组的商品销售流水（面额口径）。平台允许旧绿宝石模组改为钻石模组，
     * 但绿宝石计价期间的销售在数据分析接口里按钻石面额折算返回，与实际结算收益不符，
     * 计入会把收益预估与生态费反推整体抬高数倍，因此与 [moduleDiamonds] 分离存放、不参与计算；
     * 已改为钻石定价的模组流水为真钻石口径，计入 [moduleDiamonds] 正常参与计算。
     */
    val emeraldModDiamonds: Map<String, Double> = emptyMap(),
    /**
     * 流水是否完整：拉取作品列表或单个作品流水时有请求失败，失败部分会按 0 计入，此时为 false。
     * 随收益数据一起缓存和传递；需要精确流水的计算（如反推生态费）必须先检查它。
     * 覆盖普通作品流水、商品化模组列表、钻石定价模组的商品列表与商品流水。
     * 绿宝石定价模组的商品列表或商品流水请求失败不影响本标记。
     */
    val isFlowComplete: Boolean = true
)

data class ModuleIncomeDetail(
    val moduleName: String,
    val flowIncome: Double,
    val developerShare: Double,
    val shareReturn: Double,
    val subsidyAmount: Double,
    val totalIncome: Double
)

data class ActualModuleIncomeItem(
    val moduleName: String,
    val flowIncome: Double,
    val actualDeveloperShare: Double,
    val actualShareReturn: Double,
    val subsidyAmount: Double,
    val actualTotalIncome: Double,
    val estimatedTotalIncome: Double,
    val incomeShare: Double
) {
    val deltaFromEstimate: Double get() = actualTotalIncome - estimatedTotalIncome
}

data class ActualModuleIncomeSummary(
    val actualEcoFeeRate: Double,
    val settlementIncome: Double,
    val playPlanIncome: Double,
    val profitSubsidy: Double,
    val modulesTotalIncome: Double,
    val settlementNetIncome: Double,
    val items: List<ActualModuleIncomeItem>
)

/** 预估使用的生态费率：流水先扣除该比例，剩余部分再按分成比例结算给开发者 */
const val ECO_FEE_RATE = 0.3

private fun getSharedProfit(profit: Double): Double = when {
    profit < 1_000_000 -> 0.5
    profit < 10_000_000 -> 0.525
    else -> 0.55
}

fun ProfitData.toModuleIncomeDetails(ecoFeeRate: Double = ECO_FEE_RATE): List<ModuleIncomeDetail> {
    if (subsidyPercent == 0.0 && moduleDiamonds.isEmpty()) return emptyList()
    return moduleDiamonds
        .filter { (_, revenue) -> revenue > 0 }
        .map { (name, revenue) ->
            val sharedProfit = getSharedProfit(revenue)
            val shareReturnDiamonds = revenue * (1 - ecoFeeRate) * (1 - sharedProfit) * subsidyPercent
            val shareRmb = getDeveloperProfit(revenue, subsidyPercent, ecoFeeRate) / 100.0
            val subsidy = subsidyProfit[name] ?: 0.0
            ModuleIncomeDetail(
                moduleName = name,
                flowIncome = revenue / 100.0,
                developerShare = shareRmb,
                shareReturn = shareReturnDiamonds / 100.0,
                subsidyAmount = subsidy,
                totalIncome = shareRmb + subsidy
            )
        }.sortedByDescending { it.flowIncome }
}

fun calculateProfit(itemProfitMap: Map<String, Double>): ProfitData {
    val sumProfit = itemProfitMap.values.sum()
    val subsidyPercent = when {
        sumProfit < 100_000 -> 0.5
        sumProfit < 300_000 -> 0.3
        sumProfit < 500_000 -> 0.2
        sumProfit < 1_000_000 -> 0.1
        else -> 0.0
    }

    var totalSharedProfit = 0.0
    for ((_, profit) in itemProfitMap) {
        val sharedProfit = getDeveloperProfit(profit, subsidyPercent)
        totalSharedProfit += sharedProfit
    }

    if (sumProfit >= 50_000_000) return ProfitData(
        sumProfit = sumProfit,
        totalProfit = totalSharedProfit / 100,
        developerProfit = totalSharedProfit,
        subsidyProfit = emptyMap(),
        subsidyPercent = 0.0,
        moduleDiamonds = itemProfitMap
    )

    val subsidyValues = mutableMapOf<String, Double>()
    val levelCounts = mutableMapOf<Int, Int>()
    val sortedItems = itemProfitMap.entries.sortedByDescending { it.value }

    for ((itemName, profit) in sortedItems) {
        var subsidyLevel = when {
            profit < 100_000 -> 0
            profit < 500_000 -> 1
            profit < 1_000_000 -> 2
            profit < 3_000_000 -> 3
            profit < 5_000_000 -> 4
            profit < 50_000_000 -> 5
            else -> 0
        }
        while (subsidyLevel > 0) {
            val count = levelCounts.getOrElse(subsidyLevel) { 0 }
            if (count < 3) {
                levelCounts[subsidyLevel] = count + 1
                break
            }
            subsidyLevel--
        }
        val subsidyAmount = getSubsidyAmount(subsidyLevel)
        if (subsidyAmount > 0.0) {
            subsidyValues[itemName] = subsidyAmount
        }
    }

    val totalSubsidy = subsidyValues.values.sum()
    val profitSubsidy = when {
        sumProfit < 1_000_000 -> 0.0
        sumProfit < 5_000_000 -> 200.0
        sumProfit < 10_000_000 -> 1000.0
        else -> 0.0
    }

    return ProfitData(
        sumProfit = sumProfit,
        totalProfit = totalSharedProfit / 100.0 + totalSubsidy + profitSubsidy,
        developerProfit = totalSharedProfit,
        profitSubsidy = profitSubsidy,
        subsidyProfit = subsidyValues,
        subsidyPercent = subsidyPercent,
        moduleDiamonds = itemProfitMap
    )
}

fun getSubsidyAmount(subsidyLevel: Int): Double = when (subsidyLevel) {
    1 -> 100.0; 2 -> 500.0; 3 -> 1_000.0; 4 -> 3_000.0; 5 -> 5_000.0; else -> 0.0
}

fun getTaxMoney(totalProfit: Double): Double {
    return when {
        totalProfit < 800 -> 0.0
        totalProfit < 4000 -> (totalProfit - 800) * 0.2
        else -> (totalProfit * 0.8) * 0.2
    }
}

fun getDeveloperProfit(
    profit: Double,
    subsidyPercent: Double,
    ecoFeeRate: Double = ECO_FEE_RATE
): Double {
    val sharedProfit = getSharedProfit(profit)
    return profit * (1 - ecoFeeRate) * (sharedProfit + (1 - sharedProfit) * subsidyPercent)
}

/**
 * 根据实际税前收益反推实际生态费率。
 *
 * 预估口径：总收益 = Σ 流水 × (1 - 生态费率) × 分成系数 / 100 + 模组激励 + 流水激励。
 * 只有开发者分成（含分成返还）随生态费率线性变化，激励为固定金额，因此可直接解出费率。
 *
 * 流水缺失会让分母偏小、算出的费率系统性偏低，因此 [ProfitData.isFlowComplete] 为 false 时拒绝计算；
 * 界面应先检查该标记，给出"数据不完整"的提示而不是"无流水"。
 *
 * @param actualIncome 实际税前收益（元），口径需与 [ProfitData.totalProfit] 一致
 * @return 实际生态费率；超出 0~1 说明输入与预估口径不一致。流水不完整或当月无流水时返回 null
 */
fun ProfitData.reverseEcoFeeRate(actualIncome: Double): Double? {
    if (!isFlowComplete) return null
    // developerProfit 已按 ECO_FEE_RATE 扣过生态费，先还原为"零生态费"时的分成（元）
    val shareWithoutFee = developerProfit / (1 - ECO_FEE_RATE) / 100.0
    if (shareWithoutFee <= 0.0) return null
    val fixedSubsidy = subsidyProfit.values.sum() + profitSubsidy
    return 1 - (actualIncome - fixedSubsidy) / shareWithoutFee
}

/**
 * 针对已实际出账的月份，基于反推出的实际生态费率按模组拆分实际税前收益。
 * 所有模组实际小计之和 + 流水激励 + 畅玩计划收益等于结算单实际税前收益 [settlementIncome]。
 *
 * 流水不完整、无流水或反推费率超出 0~1 有效范围时返回 null。
 */
fun ProfitData.computeActualModuleSummary(
    settlementIncome: Double,
    playPlanIncome: Double,
    settlementNetIncome: Double
): ActualModuleIncomeSummary? {
    val reverseInput = settlementIncome - playPlanIncome
    val actualRate = reverseEcoFeeRate(reverseInput) ?: return null
    if (actualRate !in 0.0..1.0) return null

    val estimatedByName = toModuleIncomeDetails(ECO_FEE_RATE).associateBy { it.moduleName }
    val actualModules = toModuleIncomeDetails(actualRate)
    if (actualModules.isEmpty()) return null

    val modulesTotal = actualModules.sumOf { it.totalIncome }
    val items = actualModules.map { actual ->
        val estimatedTotal = estimatedByName[actual.moduleName]?.totalIncome ?: actual.totalIncome
        val share = if (modulesTotal > 0.0) (actual.totalIncome / modulesTotal).coerceIn(0.0, 1.0) else 0.0
        ActualModuleIncomeItem(
            moduleName = actual.moduleName,
            flowIncome = actual.flowIncome,
            actualDeveloperShare = actual.developerShare,
            actualShareReturn = actual.shareReturn,
            subsidyAmount = actual.subsidyAmount,
            actualTotalIncome = actual.totalIncome,
            estimatedTotalIncome = estimatedTotal,
            incomeShare = share
        )
    }.sortedByDescending { it.actualTotalIncome }

    return ActualModuleIncomeSummary(
        actualEcoFeeRate = actualRate,
        settlementIncome = settlementIncome,
        playPlanIncome = playPlanIncome,
        profitSubsidy = profitSubsidy,
        modulesTotalIncome = modulesTotal,
        settlementNetIncome = settlementNetIncome,
        items = items
    )
}
