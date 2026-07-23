package com.lemon.mcdevmanagermp.data.repository

import com.lemon.mcdevmanagermp.data.api.AnalyzeApi
import com.lemon.mcdevmanagermp.data.common.AppContext
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.db.entity.DayDetailConfigEntity
import com.lemon.mcdevmanagermp.data.dto.netease.income.OneResRealtimeIncomeVO
import com.lemon.mcdevmanagermp.data.vo.netease.analyze.NewResDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.analyze.ResDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.analyze.ResMonthDetailVO
import com.lemon.mcdevmanagermp.domain.analyze.AnalyzeRepository
import com.lemon.mcdevmanagermp.domain.analyze.DayDetailConfig
import com.lemon.mcdevmanagermp.utils.UnifiedExceptionHandler
import kotlin.time.Clock

class AnalyzeRepositoryImpl : AnalyzeRepository {
    companion object {
        val INSTANCE by lazy { AnalyzeRepositoryImpl() }
        private val analyzeApi = AnalyzeApi.INSTANCE
    }

    override suspend fun getDayDetail(
        platform: String,
        category: String,
        startDate: String,
        endDate: String,
        itemListStr: String
    ): NetworkState<ResDetailVO> {
        return UnifiedExceptionHandler.handleRequest {
            analyzeApi.getDayDetail(
                platform = platform,
                category = category,
                startDate = startDate,
                endDate = endDate,
                itemListStr = itemListStr
            )
        }
    }

    override suspend fun getNewDayDetail(
        platform: String,
        category: String,
        startDate: String,
        endDate: String,
        itemListStr: String
    ): NetworkState<NewResDetailVO> {
        return UnifiedExceptionHandler.handleRequest {
            analyzeApi.getNewDayDetail(
                platform = platform,
                category = category,
                startDate = startDate,
                endDate = endDate,
                itemListStr = itemListStr
            )
        }
    }

    override suspend fun getMonthDetail(
        platform: String,
        category: String,
        startDate: String,
        endDate: String,
        dayDateId: String
    ): NetworkState<ResMonthDetailVO> {
        return UnifiedExceptionHandler.handleRequest {
            analyzeApi.getMonthDetail(
                platform = platform,
                category = category,
                startDate = startDate,
                endDate = endDate,
                dayDateId = dayDateId
            )
        }
    }

    override suspend fun getOneResRealtimeIncome(
        platform: String,
        iid: String,
        beginTime: String,
        endTime: String
    ): NetworkState<OneResRealtimeIncomeVO> {
        return UnifiedExceptionHandler.handleRequest {
            analyzeApi.getOneResRealtimeIncome(
                platform = platform,
                iid = iid,
                beginTime = beginTime,
                endTime = endTime
            )
        }
    }

    override suspend fun getDayDetailConfig(
        accountKey: String,
        platform: String
    ): DayDetailConfig? {
        val e = AppContext.database.dayDetailConfigDao().getByKey(accountKey, platform)
            ?: return null
        return DayDetailConfig(
            accountKey = e.accountKey,
            platform = e.platform,
            dateSpanDays = e.dateSpanDays,
            selectedIIDs = if (e.selectedIIDs.isBlank()) emptyList()
            else e.selectedIIDs.split(",")
        )
    }

    override suspend fun saveDayDetailConfig(config: DayDetailConfig) {
        AppContext.database.dayDetailConfigDao().upsert(
            DayDetailConfigEntity(
                accountKey = config.accountKey,
                platform = config.platform,
                dateSpanDays = config.dateSpanDays,
                selectedIIDs = config.selectedIIDs.joinToString(","),
                updatedAt = Clock.System.now().toEpochMilliseconds()
            )
        )
    }
}
