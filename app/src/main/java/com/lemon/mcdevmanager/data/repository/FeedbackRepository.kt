package com.lemon.mcdevmanager.data.repository

import com.lemon.mcdevmanager.api.FeedbackApi
import com.lemon.mcdevmanager.data.common.CookiesStore
import com.lemon.mcdevmanager.data.common.NETEASE_USER_COOKIE
import com.lemon.mcdevmanager.data.global.AppContext
import com.lemon.mcdevmanager.data.netease.feedback.FeedbackResponseBean
import com.lemon.mcdevmanager.data.netease.feedback.ReplyBean
import com.lemon.mcdevmanager.utils.NetworkState
import com.lemon.mcdevmanager.utils.NoNeedData
import com.lemon.mcdevmanager.utils.ResponseData
import com.lemon.mcdevmanager.utils.UnifiedExceptionHandler
import com.lemon.mcdevmanager.utils.dataJsonToString
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class FeedbackRepository {
    companion object {
        @Volatile
        private var instance: FeedbackRepository? = null
        fun getInstance() = instance ?: synchronized(this) {
            instance ?: FeedbackRepository().also { instance = it }
        }
    }

    suspend fun loadFeedback(
        page: Int,
        keyword: String = "",
        order: String = "DESC",
        types: List<Int> = emptyList(),
        replyCount: Int = -1
    ): NetworkState<FeedbackResponseBean> {
        val start = (page - 1) * 20
        val cookie = AppContext.cookiesStore[AppContext.nowNickname]
        val keywordStr = keyword.ifEmpty { null }
        val typeStr = if (types.isNotEmpty()) types.joinToString("__") else null
        val realReplyCount = if (replyCount != -1) replyCount else null
        val orderStr = if (order == "DESC") null else "ASC"
        val sortStr = if (orderStr != null) "create_time" else null
        cookie?.let {
            CookiesStore.addCookie(NETEASE_USER_COOKIE, cookie)
            return UnifiedExceptionHandler.handleSuspend {
                FeedbackApi.create().loadFeedback(
                    from = start,
                    size = 20,
                    sort = sortStr,
                    order = orderStr,
                    status = typeStr,
                    key = keywordStr,
                    replyCount = realReplyCount
                )
            }
        } ?: return NetworkState.Error("无法获取用户cookie, 请重新登录")

    }

    suspend fun sendReply(
        feedbackId: String,
        content: String
    ): NetworkState<NoNeedData> {
        val cookie = AppContext.cookiesStore[AppContext.nowNickname]
        cookie?.let {
            CookiesStore.addCookie(NETEASE_USER_COOKIE, cookie)
            val realContent = dataJsonToString(ReplyBean(content))
            val requestBody =
                realContent.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            return UnifiedExceptionHandler.handleSuspend {
                FeedbackApi.create().sendReply(feedbackId, requestBody)
            }
        } ?: return NetworkState.Error("无法获取用户cookie, 请重新登录")
    }
}