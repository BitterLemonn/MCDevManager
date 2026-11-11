package com.lemon.mcdevmanagermp

import com.lemon.mcdevmanagermp.data.common.JSONConverter
import com.lemon.mcdevmanagermp.data.vo.netease.income.IncomeVO
import com.lemon.mcdevmanagermp.data.vo.netease.user.UserInfoVO
import com.lemon.mcdevmanagermp.domain.main.ProfitMonth
import com.lemon.mcdevmanagermp.ui.pages.incomeDetail.buildEcoFeeRecord
import com.lemon.mcdevmanagermp.ui.pages.incomeDetail.matchesProfitMonth
import com.lemon.mcdevmanagermp.ui.pages.incomeDetail.reverseInputIncome
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

/** 实际收益反推生态费率、结算单匹配、企业开发者判定 */
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
    fun dataMonthMatchesAcrossFormats() {
        val month = ProfitMonth(2026, 10)
        assertTrue(matchesProfitMonth("2026-10", month))
        assertTrue(matchesProfitMonth("202610", month))
        assertTrue(matchesProfitMonth("2026-1", ProfitMonth(2026, 1)))
        assertTrue(matchesProfitMonth("20261", ProfitMonth(2026, 1)))
        assertFalse(matchesProfitMonth("2026-11", month))
        assertFalse(matchesProfitMonth("2025-10", month))
        assertFalse(matchesProfitMonth("", month))
        assertFalse(matchesProfitMonth("abc", month))
    }

    @Test
    fun ecoFeeRecordBuiltWhenSettledAndComplete() {
        val month = ProfitMonth(2026, 10)
        val s = settlement(dataMonth = "2026-10", income = profit.totalProfit.toString())
        val record = checkNotNull(buildEcoFeeRecord("开发者", month, profit, s))
        assertEquals("2026-10", record.dataMonth)
        assertClose(ECO_FEE_RATE, record.ecoFeeRate)
        assertEquals(profit.totalProfit, record.actualIncome, 1e-9)
        assertEquals(profit.sumProfit / 100.0 * ECO_FEE_RATE, record.ecoFeeAmount, 1e-9)
    }

    @Test
    fun ecoFeeRecordSkippedWhenIncomplete() {
        val s = settlement(income = profit.totalProfit.toString())
        assertNull(buildEcoFeeRecord("开发者", ProfitMonth(2026, 10), profit.copy(isFlowComplete = false), s))
    }

    @Test
    fun ecoFeeRecordSkippedWhenRateOutOfRange() {
        // 实际收益远超预估可解释范围，反推费率为负，不保存
        val s = settlement(income = (profit.totalProfit * 10).toString())
        assertNull(buildEcoFeeRecord("开发者", ProfitMonth(2026, 10), profit, s))
    }

    @Test
    fun ecoFeeRecordSkippedWithoutAccount() {
        val s = settlement(income = profit.totalProfit.toString())
        assertNull(buildEcoFeeRecord("", ProfitMonth(2026, 10), profit, s))
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
