package com.lemon.mcdevmanager.data.repository

import com.lemon.mcdevmanager.api.CommentApi
import com.lemon.mcdevmanager.data.common.CookiesStore
import com.lemon.mcdevmanager.data.common.NETEASE_USER_COOKIE
import com.lemon.mcdevmanager.data.global.AppContext
import com.lemon.mcdevmanager.data.netease.comment.CommentList
import com.lemon.mcdevmanager.utils.CookiesExpiredException
import com.lemon.mcdevmanager.utils.NetworkState
import com.lemon.mcdevmanager.utils.UnifiedExceptionHandler

class CommentRepository {
    companion object {
        @Volatile
        private var instance: CommentRepository? = null
        fun getInstance() = instance ?: synchronized(this) {
            instance ?: CommentRepository().also { instance = it }
        }
    }

    suspend fun getCommentList(
        page: Int = 0,
        span: Int = 20,
        key: String? = null,
        tag: String? = null,
        state: Int? = null,
        startDate: String? = null,
        endDate: String? = null
    ): NetworkState<CommentList> {
        val cookie = AppContext.cookiesStore[AppContext.nowNickname]
        cookie?.let {
            CookiesStore.addCookie(NETEASE_USER_COOKIE, cookie)
            return UnifiedExceptionHandler.handleSuspend {
                CommentApi.create().getCommentList(
                    start = page * span,
                    span = span,
                    key = key,
                    tag = tag,
                    state = state,
                    startDate = startDate,
                    endDate = endDate
                )
            }
        } ?: return NetworkState.Error("无法获取用户cookie, 请重新登录", CookiesExpiredException)
    }
}