package com.lemon.mcdevmanager.data.repository

import com.lemon.mcdevmanager.api.AnalyzeApi
import com.lemon.mcdevmanager.api.DeveloperFeedbackApi
import com.lemon.mcdevmanager.data.common.CookiesStore
import com.lemon.mcdevmanager.data.common.NETEASE_USER_COOKIE
import com.lemon.mcdevmanager.data.global.AppContext
import com.lemon.mcdevmanager.data.netease.developerFeedback.DeveloperFeedbackBean
import com.lemon.mcdevmanager.data.netease.developerFeedback.DeveloperFeedbackResponseBean
import com.lemon.mcdevmanager.utils.CookiesExpiredException
import com.lemon.mcdevmanager.utils.NetworkState
import com.lemon.mcdevmanager.utils.UnifiedExceptionHandler

class DeveloperFeedbackRepository {
    companion object {
        @Volatile
        private var instance: DeveloperFeedbackRepository? = null
        fun getInstance() = instance ?: synchronized(this) {
            instance ?: DeveloperFeedbackRepository().also { instance = it }
        }
    }

    suspend fun submitFeedback(
        content: String,
        contact: String,
        feedbackType: String,
        functionType: String,
        imgPathList: List<String> = emptyList()
    ): NetworkState<DeveloperFeedbackResponseBean> {
        val cookie = AppContext.cookiesStore[AppContext.nowNickname]
        cookie?.let {
            CookiesStore.addCookie(NETEASE_USER_COOKIE, cookie)
            val feedbackBean = DeveloperFeedbackBean(
                feedbackType = feedbackType,
                functionType = functionType,
                content = content,
                contact = contact,
                extraList = imgPathList
            )
            return UnifiedExceptionHandler.handleSuspend {
                DeveloperFeedbackApi.create().seedFeedback(feedbackBean)
            }
        } ?: return NetworkState.Error("无法获取用户cookie, 请重新登录", CookiesExpiredException)
    }
}