package com.lemon.mcdevmanager.data.common

object CookiesStore {
    private val cookies = mutableMapOf<String, String>()

    fun addCookies(list: List<String>) {
        list.forEach {
            val cookie = it.split(";")[0]
            val key = cookie.split("=")[0]
            val value = cookie.split("=")[1]
            cookies[key] = value
        }
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

    fun clearCookies() {
        cookies.clear()
    }
}