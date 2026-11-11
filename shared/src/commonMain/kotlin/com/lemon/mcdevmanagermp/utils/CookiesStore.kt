package com.lemon.mcdevmanagermp.utils

object CookiesStore {
    private val cookies = mutableMapOf<String, String>()

    fun addCookies(list: List<String>) {
        list.forEach {
            val cookie = it.substringBefore(';')
            val separator = cookie.indexOf('=')
            if (separator <= 0) return@forEach

            val key = cookie.substring(0, separator).trim()
            if (key.isEmpty()) return@forEach

            // Cookie 值也可能包含等号，只在第一个等号处分隔。
            val value = cookie.substring(separator + 1).trim()
            cookies[key] = value
        }
    }

    fun removeCookie(key: String){
        cookies.remove(key)
    }

    fun addCookie(key: String, value: String) {
        cookies[key] = value
    }

    fun getCookie(key: String): String? {
        return cookies[key]
    }

    fun getAllCookiesString(): String {
        if (cookies.isEmpty()) return ""
        return cookies.map { "${it.key}=${it.value}" }.joinToString("; ")
    }

    fun getAllCookiesMap(): Map<String, String>{
        if (cookies.isEmpty()) return HashMap()
        return cookies.toMap()
    }

    fun clearCookies() {
        cookies.clear()
    }
}
