package com.lemon.mcdevmanagermp

import com.lemon.mcdevmanagermp.domain.main.ProfitMonth
import com.lemon.mcdevmanagermp.domain.main.profitMonthWindow
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProfitMonthWindowTest {
    @Test
    fun lastMonthRemainsVisibleThroughDay15() {
        assertTrue(profitMonthWindow(LocalDate(2026, 8, 15)).showLastMonth)
        assertFalse(profitMonthWindow(LocalDate(2026, 8, 16)).showLastMonth)
    }

    @Test
    fun nextMonthAppearsWhenItsNineDayOpeningWindowStarts() {
        assertFalse(profitMonthWindow(LocalDate(2026, 8, 22)).showNextMonth)
        assertTrue(profitMonthWindow(LocalDate(2026, 8, 23)).showNextMonth)
        assertFalse(profitMonthWindow(LocalDate(2026, 2, 19)).showNextMonth)
        assertTrue(profitMonthWindow(LocalDate(2026, 2, 20)).showNextMonth)
    }

    @Test
    fun monthShiftHandlesYearBoundary() {
        assertEquals(ProfitMonth(2027, 1), ProfitMonth(2026, 12).shift(1))
        assertEquals(ProfitMonth(2025, 12), ProfitMonth(2026, 1).shift(-1))
    }
}
