package com.lemon.mcdevmanager.data.repository

import com.lemon.mcdevmanager.api.AnalyzeApi
import com.lemon.mcdevmanager.data.netease.resource.ResDetailResponseBean
import com.lemon.mcdevmanager.data.netease.resource.ResourceResponseBean
import com.lemon.mcdevmanager.utils.NetworkState
import com.lemon.mcdevmanager.utils.UnifiedExceptionHandler

class DetailRepository {
    companion object {
        @Volatile
        private var instance: DetailRepository? = null
        fun getInstance() = instance ?: synchronized(this) {
            instance ?: DetailRepository().also { instance = it }
        }
    }

    suspend fun getAllResource(): NetworkState<ResourceResponseBean> {
        return UnifiedExceptionHandler.handleSuspend {
            AnalyzeApi.create().getAllResource()
        }
    }

    suspend fun getDailyDetail(
        platform: String,
        startDate: String,
        endDate: String,
        itemList: List<String>,
        sort: String = "dateid",
        order: String = "ASC",
        start: Int = 0,
        span: Int = Int.MAX_VALUE
    ): NetworkState<ResDetailResponseBean> {
        val itemListStr = itemList.joinToString(",")
        return UnifiedExceptionHandler.handleSuspend {
            AnalyzeApi.create().getDayDetail(
                platform = platform,
                category = platform,
                startDate = startDate,
                endDate = endDate,
                itemListStr = itemListStr,
                sort = sort,
                order = order,
                start = start,
                span = span
            )
        }
    }
}