package com.lemon.mcdevmanagermp

import com.lemon.mcdevmanagermp.data.common.JSONConverter
import com.lemon.mcdevmanagermp.data.repository.monthKey
import com.lemon.mcdevmanagermp.data.repository.normalizeMonthKey
import com.lemon.mcdevmanagermp.data.repository.reverseInputIncome
import com.lemon.mcdevmanagermp.data.vo.netease.income.IncomeVO
import com.lemon.mcdevmanagermp.data.vo.netease.user.UserInfoVO
import com.lemon.mcdevmanagermp.domain.main.ProfitMonth
import com.lemon.mcdevmanagermp.utils.ECO_FEE_RATE
import com.lemon.mcdevmanagermp.utils.ProfitData
import com.lemon.mcdevmanagermp.utils.calculateProfit
import com.lemon.mcdevmanagermp.utils.reverseEcoFeeRate
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
}
