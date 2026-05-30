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
            val shareReturnDiamonds = revenue * 0.7 * (1 - sharedProfit) * subsidyPercent
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

fun getDeveloperProfit(profit: Double, subsidyPercent: Double): Double {
    val sharedProfit = when {
        profit < 1_000_000 -> 0.5
        profit < 10_000_000 -> 0.525
        else -> 0.55
    }
    return profit * (1 - 0.3) * (sharedProfit + (1 - sharedProfit) * subsidyPercent)
}
