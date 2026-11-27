package com.lemon.mcdevmanagermp

import com.lemon.mcdevmanagermp.data.db.dao.EcoFeeRecordDao
import com.lemon.mcdevmanagermp.data.db.entity.EcoFeeRecordEntity
import com.lemon.mcdevmanagermp.data.repository.EcoFeeRecordRepository
import com.lemon.mcdevmanagermp.data.vo.netease.income.IncomeVO
import com.lemon.mcdevmanagermp.domain.main.ProfitMonth
import com.lemon.mcdevmanagermp.ui.pages.incomeDetail.IncomeDetailState
import com.lemon.mcdevmanagermp.ui.pages.incomeDetail.selectPeriod
import com.lemon.mcdevmanagermp.utils.calculateProfit
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class EcoFeeMonthIsolationTest {
    private val october = ProfitMonth(2026, 10)
    private val september = ProfitMonth(2026, 9)
    private val flow = calculateProfit(mapOf("a" to 20000.0, "b" to 5000.0))
    private val octoberBill = IncomeVO(dataMonth="202610", income="151.25", playPlanIncome="20.00")
    private val septemberBill = IncomeVO(dataMonth="202609", income="80.00")

    private class MemoryDao : EcoFeeRecordDao {
        val rows = mutableMapOf<Pair<String,String>, EcoFeeRecordEntity>()
        override suspend fun get(accountKey: String, dataMonth: String) = rows[accountKey to dataMonth]
        override suspend fun getRatedByAccount(accountKey: String) = rows.values.filter { it.accountKey == accountKey && it.ecoFeeRate != null }
        override suspend fun upsert(record: EcoFeeRecordEntity) { rows[record.accountKey to record.dataMonth] = record }
    }

    @Test fun repeatedCachedMonthSwitchesAlwaysPairSameMonth() {
        var state = IncomeDetailState(september, flow, settlement=septemberBill, isSettlementLoading=false)
        repeat(50) {
            state = state.selectPeriod(october, flow, listOf(septemberBill, octoberBill))
            assertEquals(octoberBill, state.settlement)
            state = state.selectPeriod(september, flow, listOf(septemberBill, octoberBill))
            assertEquals(septemberBill, state.settlement)
        }
    }
    @Test fun loadingClearsOldBillAndDoesNotReuseAmbiguousOrOtherPlatformBill() {
        val old = IncomeDetailState(september, flow, settlement=septemberBill)
        val loading = old.selectPeriod(october, null, null)
        assertNull(loading.settlement)
        assertTrue(loading.isLoading)
        assertTrue(loading.isSettlementLoading)
        assertNull(old.selectPeriod(october, flow, listOf(octoberBill, octoberBill)).settlement)
        assertNull(old.selectPeriod(october, flow, listOf(octoberBill.copy(platform="pc"))).settlement)
    }
    @Test fun storageRejectsCrossMonthEvenIfCallerHasOldState() = runTest {
        val dao = MemoryDao()
        val repo = EcoFeeRecordRepository { dao }
        repo.saveSnapshot("test", october, flow)
        val before = dao.get("test", "2026-10")
        assertFalse(repo.saveReverseResult("test", october, flow, septemberBill))
        assertEquals(before, dao.get("test", "2026-10"))
        assertFalse(repo.saveReverseResult("test", october, flow, octoberBill.copy(platform="pc")))
        assertTrue(repo.saveReverseResult("test", october, flow, octoberBill))
        assertEquals(0.3, dao.get("test", "2026-10")!!.ecoFeeRate!!, 1e-9)
    }
    @Test fun wrongHistoricalRecordIsDetectedAndRepairedWithMatchingBill() = runTest {
        val dao = MemoryDao()
        val repo = EcoFeeRecordRepository { dao }
        repo.saveReverseResult("test", october, flow, octoberBill)
        val correct = dao.get("test", "2026-10")!!
        dao.upsert(correct.copy(ecoFeeRate=0.6960667, actualIncome=80.0))
        assertFalse(repo.matchesSettlement("test", october, octoberBill))
        assertTrue(repo.saveReverseResult("test", october, flow, octoberBill))
        assertTrue(repo.matchesSettlement("test", october, octoberBill))
        assertFalse(repo.matchesSettlement("other-account", october, octoberBill))
        assertFalse(repo.matchesSettlement("test", october, septemberBill))
    }
}
