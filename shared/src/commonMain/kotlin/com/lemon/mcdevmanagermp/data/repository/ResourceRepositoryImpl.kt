package com.lemon.mcdevmanagermp.data.repository

import com.lemon.mcdevmanagermp.data.api.AnalyzeApi
import com.lemon.mcdevmanagermp.data.api.ResourceApi
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.dto.netease.income.OneResRealtimeIncomeVO
import com.lemon.mcdevmanagermp.data.vo.netease.analyze.NewResDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.analyze.ResDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.analyze.ResMonthDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceListVO
import com.lemon.mcdevmanagermp.domain.resource.ResourceRepository
import com.lemon.mcdevmanagermp.utils.UnifiedExceptionHandler

class ResourceRepositoryImpl : ResourceRepository {
    companion object {
        val INSTANCE by lazy { ResourceRepositoryImpl() }
        private val analyzeApi = AnalyzeApi.INSTANCE
        private val resourceApi = ResourceApi.INSTANCE
    }

    override suspend fun getAllResources(platform: String): NetworkState<ResourceListVO> {
        return UnifiedExceptionHandler.handleRequest {
            analyzeApi.getAllResource(platform = platform)
        }
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

    override suspend fun getResourceDetail(itemId: String): NetworkState<ResourceDetailVO> {
        return UnifiedExceptionHandler.handleRequest {
            resourceApi.getResourceDetail(itemId)
        }
    }
}
