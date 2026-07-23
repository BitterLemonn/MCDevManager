package com.lemon.mcdevmanagermp.domain.account

interface CookieRepository {
    fun getAllCookiesMap(): Map<String, String>
    fun addCookie(key: String, value: String)
    fun clearCookies()
}
