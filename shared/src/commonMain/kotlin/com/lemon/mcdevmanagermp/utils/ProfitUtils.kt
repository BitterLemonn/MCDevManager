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
    val moduleDiamonds: Map<String, Double> = emptyMap()
)

data class ModuleIncomeDetail(
    val moduleName: String,
    val flowIncome: Double,
    val developerShare: Double,
    val shareReturn: Double,
    val subsidyAmount: Double,
    val totalIncome: Double
)

/** 预估使用的生态费率：流水先扣除该比例，剩余部分再按分成比例结算给开发者 */
const val ECO_FEE_RATE = 0.3

private fun getSharedProfit(profit: Double): Double = when {
    profit < 1_000_000 -> 0.5
    profit < 10_000_000 -> 0.525
    else -> 0.55
}

fun ProfitData.toModuleIncomeDetails(): List<ModuleIncomeDetail> {
    if (subsidyPercent == 0.0 && moduleDiamonds.isEmpty()) return emptyList()
    return moduleDiamonds
        .filter { (_, revenue) -> revenue > 0 }
        .map { (name, revenue) ->
            val sharedProfit = getSharedProfit(revenue)
            val shareReturnDiamonds = revenue * (1 - ECO_FEE_RATE) * (1 - sharedProfit) * subsidyPercent
            val shareRmb = getDeveloperProfit(revenue, subsidyPercent) / 100.0
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

fun getDeveloperProfit(profit: Double, subsidyPercent: Double): Double {
    val sharedProfit = when {
        profit < 1_000_000 -> 0.5
        profit < 10_000_000 -> 0.525
        else -> 0.55
    }
    return profit * (1 - ECO_FEE_RATE) * (sharedProfit + (1 - sharedProfit) * subsidyPercent)
}

/**
 * 根据实际税前收益反推实际生态费率。
 *
 * 预估口径：总收益 = Σ 流水 × (1 - 生态费率) × 分成系数 / 100 + 模组激励 + 流水激励。
 * 只有开发者分成（含分成返还）随生态费率线性变化，激励为固定金额，因此可直接解出费率。
 *
 * @param actualIncome 实际税前收益（元），口径需与 [ProfitData.totalProfit] 一致
 * @return 实际生态费率；超出 0~1 说明输入与预估口径不一致。当月无流水时无法反推，返回 null
 */
fun ProfitData.reverseEcoFeeRate(actualIncome: Double): Double? {
    // developerProfit 已按 ECO_FEE_RATE 扣过生态费，先还原为"零生态费"时的分成（元）
    val shareWithoutFee = developerProfit / (1 - ECO_FEE_RATE) / 100.0
    if (shareWithoutFee <= 0.0) return null
    val fixedSubsidy = subsidyProfit.values.sum() + profitSubsidy
    return 1 - (actualIncome - fixedSubsidy) / shareWithoutFee
}
