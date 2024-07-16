package com.lemon.mcdevmanager.data.repository

import com.lemon.mcdevmanager.api.AnalyzeApi
import com.lemon.mcdevmanager.data.common.CookiesStore
import com.lemon.mcdevmanager.data.common.NETEASE_USER_COOKIE
import com.lemon.mcdevmanager.data.global.AppContext
import com.lemon.mcdevmanager.data.netease.resource.ResDetailResponseBean
import com.lemon.mcdevmanager.data.netease.resource.ResourceResponseBean
import com.lemon.mcdevmanager.utils.CookiesExpiredException
import com.lemon.mcdevmanager.utils.NetworkState
import com.lemon.mcdevmanager.utils.UnifiedExceptionHandler
import java.util.Locale

class DetailRepository {
    companion object {
        @Volatile
        private var instance: DetailRepository? = null
        fun getInstance() = instance ?: synchronized(this) {
            instance ?: DetailRepository().also { instance = it }
        }
    }

    suspend fun getAllResource(platform: String): NetworkState<ResourceResponseBean> {
        val cookie = AppContext.cookiesStore[AppContext.nowNickname]
        cookie?.let {
            CookiesStore.addCookie(NETEASE_USER_COOKIE, cookie)
            return UnifiedExceptionHandler.handleSuspend {
                AnalyzeApi.create().getAllResource(if (platform == "pe") "pe" else "comp")
            }
        } ?: return NetworkState.Error("无法获取用户cookie, 请重新登录", CookiesExpiredException)
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
        val cookie = AppContext.cookiesStore[AppContext.nowNickname]
        cookie?.let {
            CookiesStore.addCookie(NETEASE_USER_COOKIE, cookie)
            return UnifiedExceptionHandler.handleSuspend {
                AnalyzeApi.create().getDayDetail(
                    platform = platform,
                    category = if (platform == "pe") "pe" else "comp",
                    startDate = startDate,
                    endDate = endDate,
                    itemListStr = itemListStr,
                    sort = sort,
                    order = order,
                    start = start,
                    span = span
                )
            }
        } ?: return NetworkState.Error("无法获取用户cookie, 请重新登录", CookiesExpiredException)
    }
}