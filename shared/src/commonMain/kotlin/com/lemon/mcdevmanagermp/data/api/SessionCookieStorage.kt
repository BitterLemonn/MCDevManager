package com.lemon.mcdevmanagermp.data.api

import com.lemon.mcdevmanagermp.utils.CookiesStore
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.CookieEncoding
import io.ktor.http.Url
import io.ktor.http.encodeCookieValue

/** 字符串存储统一使用可直接写入 HTTP Cookie 头的线格式。 */
internal class SessionCookieStorage : CookiesStorage {
    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        if (cookie.value.isEmpty()) {
            CookiesStore.removeCookie(cookie.name)
            return
        }
        // RAW（通常来自响应或请求头捕获）保持原样；显式编码仅转换一次。
        CookiesStore.addCookie(cookie.name, encodeCookieValue(cookie.value, cookie.encoding))
    }

    override suspend fun get(requestUrl: Url): List<Cookie> =
        CookiesStore.getAllCookiesMap().map {
            // 默认 URI_ENCODING 会再次转义已有的 %，回存后每轮继续膨胀。
            Cookie(it.key, it.value, encoding = CookieEncoding.RAW)
        }

    override fun close() = Unit
}
