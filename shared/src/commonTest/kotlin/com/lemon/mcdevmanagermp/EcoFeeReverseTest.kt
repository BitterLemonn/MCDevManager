package com.lemon.mcdevmanagermp

import com.lemon.mcdevmanagermp.data.common.JSONConverter
import com.lemon.mcdevmanagermp.data.repository.isEcoFeeReverseSupported
import com.lemon.mcdevmanagermp.data.repository.monthKey
import com.lemon.mcdevmanagermp.data.repository.normalizeMonthKey
import com.lemon.mcdevmanagermp.data.repository.parseMonthKey
import com.lemon.mcdevmanagermp.data.repository.reverseInputIncome
import com.lemon.mcdevmanagermp.data.vo.netease.income.IncomeVO
import com.lemon.mcdevmanagermp.data.vo.netease.user.UserInfoVO
import com.lemon.mcdevmanagermp.domain.main.ProfitMonth
import com.lemon.mcdevmanagermp.domain.main.isMonthClosed
import com.lemon.mcdevmanagermp.utils.ECO_FEE_RATE
import com.lemon.mcdevmanagermp.utils.ProfitData
import com.lemon.mcdevmanagermp.utils.calculateProfit
import com.lemon.mcdevmanagermp.utils.computeActualModuleSummary
import com.lemon.mcdevmanagermp.utils.reverseEcoFeeRate
import kotlinx.datetime.LocalDate
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** 实际收益反推生态费率、结算单月份匹配、企业开发者判定 */
class EcoFeeReverseTest {
    // 覆盖不同分成档位与模组激励档位，并带流水激励（月流水 100w~500w 钻石区间）
    private val profit = calculateProfit(
        mapOf("A" to 2_400_000.0, "B" to 650_000.0, "C" to 80_000.0)
    )

    private fun assertClose(expected: Double, actual: Double?) {
        assertTrue(actual != null && abs(expected - actual) < 1e-9, "expected $expected but was $actual")
    }

    private fun settlement(
        dataMonth: String = "2026-10",
        income: String = "0.00",
        playPlanIncome: String = "0.00"
    ) = IncomeVO(dataMonth = dataMonth, income = income, playPlanIncome = playPlanIncome)

    @Test
    fun estimatedTotalReversesToEstimatedRate() {
        assertTrue(profit.profitSubsidy > 0 && profit.subsidyProfit.isNotEmpty())
        assertClose(ECO_FEE_RATE, profit.reverseEcoFeeRate(profit.totalProfit))
    }

    @Test
    fun incomeUnderDifferentRateReversesToThatRate() {
        val fixedSubsidy = profit.subsidyProfit.values.sum() + profit.profitSubsidy
        val shareWithoutFee = profit.developerProfit / (1 - ECO_FEE_RATE) / 100.0
        // 按 20% 生态费正向计算的实际收益，应反推回 20%
        val actualAt20 = shareWithoutFee * (1 - 0.2) + fixedSubsidy
        assertClose(0.2, profit.reverseEcoFeeRate(actualAt20))
    }

    @Test
    fun noFlowCannotReverse() {
        assertNull(ProfitData().reverseEcoFeeRate(100.0))
    }

    @Test
    fun incompleteFlowCannotReverse() {
        assertNull(profit.copy(isFlowComplete = false).reverseEcoFeeRate(profit.totalProfit))
    }

    @Test
    fun settlementIncomeReversesToEstimatedRate() {
        // 结算单收益 = 预估总收益 + 畅玩计划收益，剔除畅玩计划后应反推回预估费率
        val playPlan = 12.34
        val s = settlement(income = (profit.totalProfit + playPlan).toString(), playPlanIncome = playPlan.toString())
        assertEquals(profit.totalProfit, s.reverseInputIncome, 1e-9)
        assertClose(ECO_FEE_RATE, profit.reverseEcoFeeRate(s.reverseInputIncome))
    }

    @Test
    fun dataMonthNormalizesToMonthKey() {
        assertEquals("2026-10", normalizeMonthKey("2026-10"))
        assertEquals("2026-10", normalizeMonthKey("202610"))
        assertEquals("2026-01", normalizeMonthKey("2026-1"))
        assertEquals("2026-01", normalizeMonthKey("20261"))
        assertNull(normalizeMonthKey("2026-13"))
        assertNull(normalizeMonthKey("2026-0"))
        assertNull(normalizeMonthKey(""))
        assertNull(normalizeMonthKey("abc"))
        assertEquals("2026-10", monthKey(ProfitMonth(2026, 10)))
        assertEquals("2026-01", monthKey(ProfitMonth(2026, 1)))
    }

    @Test
    fun dataMonthParsesToProfitMonth() {
        assertEquals(ProfitMonth(2026, 10), parseMonthKey("2026-10"))
        assertEquals(ProfitMonth(2026, 10), parseMonthKey("202610"))
        assertEquals(ProfitMonth(2026, 1), parseMonthKey("2026-1"))
        assertNull(parseMonthKey("2026-13"))
        assertNull(parseMonthKey(""))
    }

    /** 登录后自动反推依赖月份截止判定：上月已截止，本月与下月尚未截止 */
    @Test
    fun monthClosedOnlyForPastMonths() {
        val today = LocalDate(2026, 11, 12)
        assertTrue(isMonthClosed(ProfitMonth(2026, 10), today))
        assertTrue(isMonthClosed(ProfitMonth(2025, 12), today))
        assertFalse(isMonthClosed(ProfitMonth(2026, 11), today))
        assertFalse(isMonthClosed(ProfitMonth(2026, 12), today))
    }

    @Test
    fun enterpriseDetectedFromPayType() {
        val base = """"exp":0,"level":1,"nickname":"n","income":"0","onsale_item_count":0,""" +
            """"cur_month_incentive_fund":0,"unextract_income":"0","prerequisite_switch":false"""
        val company = JSONConverter.decodeFromString<UserInfoVO>("""{$base,"pay_type":"company_owned"}""")
        val individual = JSONConverter.decodeFromString<UserInfoVO>("""{$base,"pay_type":"individual_withhold"}""")
        val missing = JSONConverter.decodeFromString<UserInfoVO>("{$base}")
        assertTrue(company.isEnterprise)
        assertFalse(individual.isEnterprise)
        assertFalse(missing.isEnterprise)
        assertEquals("company_owned", company.payType)
    }

    @Test
    fun actualModuleSummaryReconcilesExactlyWithSettlementIncome() {
        val fixedSubsidy = profit.subsidyProfit.values.sum() + profit.profitSubsidy
        val shareWithoutFee = profit.developerProfit / (1 - ECO_FEE_RATE) / 100.0
        val playPlan = 320.50
        // 模拟真实生态费率为 22% 时的结算单税前收益（含畅玩计划）
        val settlementIncome = shareWithoutFee * (1 - 0.22) + fixedSubsidy + playPlan
        val settlementNet = settlementIncome - 1200.0

        val summary = profit.computeActualModuleSummary(
            settlementIncome = settlementIncome,
            playPlanIncome = playPlan,
            settlementNetIncome = settlementNet
        )

        assertTrue(summary != null)
        assertClose(0.22, summary.actualEcoFeeRate)
        assertEquals(3, summary.items.size)
        // 所有模组实际小计之和 + 流水激励 + 畅玩计划 严格等于结算单实际税前收益
        assertClose(settlementIncome, summary.modulesTotalIncome + summary.profitSubsidy + summary.playPlanIncome)
        // 各模组占比之和为 1
        assertClose(1.0, summary.items.sumOf { it.incomeShare })
        // 生态费率低于 30% 时，每个有流水模组的实际小计都应高于预估小计
        assertTrue(summary.items.all { it.deltaFromEstimate > 0.0 })
        // 列表按实际小计降序排列
        assertEquals(summary.items.sortedByDescending { it.actualTotalIncome }, summary.items)
    }

    @Test
    fun actualModuleSummaryReturnsNullWhenRateInvalidOrIncomplete() {
        assertNull(
            profit.copy(isFlowComplete = false).computeActualModuleSummary(
                settlementIncome = profit.totalProfit,
                playPlanIncome = 0.0,
                settlementNetIncome = profit.totalProfit
            )
        )
        // 超出合理区间（实际收益高于零生态费上限）
        assertNull(
            profit.computeActualModuleSummary(
                settlementIncome = profit.totalProfit * 5.0,
                playPlanIncome = 0.0,
                settlementNetIncome = profit.totalProfit * 5.0
            )
        )
    }

    @Test
    fun ecoFeeReverseIsOnlySupportedFrom2026JanOnward() {
        assertFalse(isEcoFeeReverseSupported(ProfitMonth(2025, 12)))
        assertFalse(isEcoFeeReverseSupported(ProfitMonth(2024, 6)))
        assertTrue(isEcoFeeReverseSupported(ProfitMonth(2026, 1)))
        assertTrue(isEcoFeeReverseSupported(ProfitMonth(2026, 10)))
    }
}
