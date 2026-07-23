package com.lemon.mcdevmanagermp

import com.lemon.mcdevmanagermp.domain.analyze.formatYmd
import com.lemon.mcdevmanagermp.domain.analyze.parseYmd
import com.lemon.mcdevmanagermp.domain.analyze.spanDays
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class DayDetailDateTest {

    @Test
    fun parseYmd_parses_eight_digit_date() {
        assertEquals(LocalDate(2026, 7, 10), parseYmd("20260710"))
    }

    @Test
    fun formatYmd_strips_dashes() {
        assertEquals("20260710", formatYmd(LocalDate(2026, 7, 10)))
    }

    @Test
    fun spanDays_is_inclusive_of_both_ends() {
        assertEquals(14, spanDays("20260701", "20260714"))
        assertEquals(1, spanDays("20260710", "20260710"))
        // 跨月
        assertEquals(30, spanDays("20260601", "20260630"))
        // 跨月跨年
        assertEquals(32, spanDays("20251231", "20260131"))
    }

    @Test
    fun format_parse_roundtrip() {
        val d = LocalDate(2026, 2, 9)
        assertEquals(d, parseYmd(formatYmd(d)))
    }
}
