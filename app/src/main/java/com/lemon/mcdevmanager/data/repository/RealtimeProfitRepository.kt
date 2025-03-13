package com.lemon.mcdevmanager.data.repository

import com.lemon.mcdevmanager.api.AnalyzeApi
import com.lemon.mcdevmanager.data.common.CookiesStore
import com.lemon.mcdevmanager.data.common.NETEASE_USER_COOKIE
import com.lemon.mcdevmanager.data.global.AppContext
import com.lemon.mcdevmanager.data.netease.income.OneResRealtimeIncomeBean
import com.lemon.mcdevmanager.utils.CookiesExpiredException
import com.lemon.mcdevmanager.utils.NetworkState
import com.lemon.mcdevmanager.utils.UnifiedExceptionHandler
import com.orhanobut.logger.Logger
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RealtimeProfitRepository {
    companion object {
        @Volatile
        private var instance: RealtimeProfitRepository? = null
        fun getInstance() = instance ?: synchronized(this) {
            instance ?: RealtimeProfitRepository().also { instance = it }
        }
    }

    suspend fun getOneDayDetail(
        platform: String,
        iid: String,
        date: String
    ): NetworkState<OneResRealtimeIncomeBean> {
        val cookie = AppContext.cookiesStore[AppContext.nowNickname]
        cookie?.let {
            CookiesStore.addCookie(NETEASE_USER_COOKIE, cookie)

            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
            val dateDate = formatter.format(Date(formatter.parse(date).time - 86400000))
            return UnifiedExceptionHandler.handleSuspend {
                AnalyzeApi.create().getOneResRealtimeIncome(
                    platform = if (platform == "pe") "pe" else "comp",
                    iid = iid,
                    beginTime = dateDate + "T16:00:00.000Z",
                    endTime = date + "T15:59:59.999Z"
                )
            }
        } ?: return NetworkState.Error("无法获取用户cookie, 请重新登录", CookiesExpiredException)
    }

    suspend fun getOneMonthDetail(
        platform: String,
        iid: String,
        year: Int,
        month: Int
    ): NetworkState<OneResRealtimeIncomeBean> {
        val cookie = AppContext.cookiesStore[AppContext.nowNickname]
        cookie?.let {
            CookiesStore.addCookie(NETEASE_USER_COOKIE, cookie)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            // 上个月底 - 9 天
            val calLast = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 2)
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                add(Calendar.DAY_OF_MONTH, -9)
            }
            val lastMonthResult = dateFormat.format(calLast.time)
            // 这个月底 - 9 天
            val calCurrent = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH)) // 当月最后一天
                add(Calendar.DAY_OF_MONTH, -9)
            }
            val currentMonthResult = dateFormat.format(calCurrent.time)

            return UnifiedExceptionHandler.handleSuspend {
                AnalyzeApi.create().getOneResRealtimeIncome(
                    platform = if (platform == "pe") "pe" else "comp",
                    iid = iid,
                    beginTime = lastMonthResult + "T16:00:00.000Z",
                    endTime = currentMonthResult + "T15:59:59.999Z"
                )
            }
        } ?: return NetworkState.Error("无法获取用户cookie, 请重新登录", CookiesExpiredException)
    }
}