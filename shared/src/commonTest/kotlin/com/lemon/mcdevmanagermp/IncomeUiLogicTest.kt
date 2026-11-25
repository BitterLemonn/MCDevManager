package com.lemon.mcdevmanagermp

import com.lemon.mcdevmanagermp.data.vo.netease.income.IncomeVO
import com.lemon.mcdevmanagermp.ui.components.formatCurrency
import com.lemon.mcdevmanagermp.ui.components.formatCurrencyString
import com.lemon.mcdevmanagermp.ui.components.formatDisplayMonth
import com.lemon.mcdevmanagermp.ui.components.formatGrouped
import com.lemon.mcdevmanagermp.ui.pages.income.IncomeState
import com.lemon.mcdevmanagermp.ui.pages.income.IncomeStatusFilter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IncomeUiLogicTest {

    @Test
    fun formatCurrencyHandlesGroupingNegativesAndPlusSign() {
        assertEquals("¥0.00", 0.0.formatCurrency())
        assertEquals("¥123.40", 123.4.formatCurrency())
        assertEquals("¥1,234.56", 1234.56.formatCurrency())
        assertEquals("¥12,345,678.90", 12345678.9.formatCurrency())
        assertEquals("-¥89.50", (-89.5).formatCurrency())
        assertEquals("+¥500.00", 500.0.formatCurrency(showPlusSign = true))
        assertEquals("¥0.00", 0.0.formatCurrency(showPlusSign = true))
    }

    @Test
    fun formatCurrencyStringParsesAndFallsBackSafely() {
        assertEquals("¥9,876.54", "9876.54".formatCurrencyString())
        assertEquals("¥0.00", " 0 ".formatCurrencyString())
        assertEquals("---", "---".formatCurrencyString())
    }

    @Test
    fun formatGroupedHandlesIntegers() {
        assertEquals("0", 0.formatGrouped())
        assertEquals("999", 999.formatGrouped())
        assertEquals("1,000", 1000.formatGrouped())
        assertEquals("1,234,567", 1234567.formatGrouped())
        assertEquals("-12,345", (-12345).formatGrouped())
    }

    @Test
    fun formatDisplayMonthNormalizesVariousFormats() {
        assertEquals("2026年5月", formatDisplayMonth("2026-05"))
        assertEquals("2026年5月", formatDisplayMonth("2026-5"))
        assertEquals("2026年11月", formatDisplayMonth("202611"))
        assertEquals("invalid", formatDisplayMonth("invalid"))
    }

    @Test
    fun incomeStateFiltersAndSelectionBehaveConsistently() {
        val unsettled = IncomeVO(id = "1", dataMonth = "2026-10", income = "1200.00", availableIncome = "1200.00", _status = "init")
        val processing = IncomeVO(id = "2", dataMonth = "2026-09", income = "800.00", availableIncome = "0.00", _status = "applying")
        val settled = IncomeVO(id = "3", dataMonth = "2026-08", income = "1500.00", availableIncome = "0.00", _status = "pay_success")
        val failed = IncomeVO(id = "4", dataMonth = "2026-07", income = "600.00", availableIncome = "0.00", _status = "pay_fail")

        val state = IncomeState(
            peList = listOf(unsettled, processing, settled, failed),
            selectedPlatform = "pe"
        )

        assertTrue(state.hasSettleableIncome)
        assertEquals("1", state.settleableIncomeId)
        assertTrue(state.hasAttentionItems)
        assertEquals(4, state.filteredList.size)
        assertEquals("1", state.effectiveSelectedIncome?.id)

        val settledOnly = state.copy(statusFilter = IncomeStatusFilter.SETTLED, selectedIncomeId = "1")
        assertEquals(listOf(settled), settledOnly.filteredList)
        // 当前选中项被筛选过滤后自动回退到筛选结果首项
        assertEquals("3", settledOnly.effectiveSelectedIncome?.id)

        val attentionOnly = state.copy(statusFilter = IncomeStatusFilter.ATTENTION)
        assertEquals(listOf(failed), attentionOnly.filteredList)

        val emptyPc = state.copy(selectedPlatform = "pc")
        assertFalse(emptyPc.hasSettleableIncome)
        assertFalse(emptyPc.hasAttentionItems)
    }
}
