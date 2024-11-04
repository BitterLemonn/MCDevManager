package com.lemon.mcdevmanager.data.repository

import com.lemon.mcdevmanager.api.IncomeApi
import com.lemon.mcdevmanager.data.common.CookiesStore
import com.lemon.mcdevmanager.data.common.NETEASE_USER_COOKIE
import com.lemon.mcdevmanager.data.global.AppContext
import com.lemon.mcdevmanager.data.netease.income.IncentiveBean
import com.lemon.mcdevmanager.data.netease.income.IncentiveListBean
import com.lemon.mcdevmanager.data.netease.income.IncomeBean
import com.lemon.mcdevmanager.utils.CookiesExpiredException
import com.lemon.mcdevmanager.utils.NetworkState
import com.lemon.mcdevmanager.utils.NoNeedData
import com.lemon.mcdevmanager.utils.UnifiedExceptionHandler
import com.lemon.mcdevmanager.utils.dataJsonToString
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class IncomeRepository {
    companion object {
        @Volatile
        private var instance: IncomeRepository? = null
        fun getInstance() = instance ?: synchronized(this) {
            instance ?: IncomeRepository().also { instance = it }
        }
    }


    suspend fun applyIncome(incomeIds: List<String>): NetworkState<NoNeedData> {
        val cookie = AppContext.cookiesStore[AppContext.nowNickname]
        cookie?.let {
            CookiesStore.addCookie(NETEASE_USER_COOKIE, cookie)

            val incomeData = dataJsonToString(IncomeBean(incomeIds))
            val incomeBody = incomeData.toRequestBody("application/json".toMediaTypeOrNull())
            return UnifiedExceptionHandler.handleSuspend {
                IncomeApi.create().applyIncome(incomeBody)
            }
        } ?: return NetworkState.Error("无法获取用户cookie, 请重新登录", CookiesExpiredException)
    }

    suspend fun getIncentiveFund(): NetworkState<IncentiveListBean> {
        val cookie = AppContext.cookiesStore[AppContext.nowNickname]
        cookie?.let {
            CookiesStore.addCookie(NETEASE_USER_COOKIE, cookie)
            return UnifiedExceptionHandler.handleSuspend {
                IncomeApi.create().getIncentiveFund()
            }
        } ?: return NetworkState.Error("无法获取用户cookie, 请重新登录", CookiesExpiredException)
    }
}