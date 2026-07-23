package com.lemon.mcdevmanagermp.data.repository

import com.lemon.mcdevmanagermp.data.common.AppContext
import com.lemon.mcdevmanagermp.domain.account.CookieRepository

class CookieRepositoryImpl : CookieRepository {

    companion object {
        val INSTANCE by lazy { CookieRepositoryImpl() }
    }

    override fun getAllCookiesMap(): Map<String, String> =
        AppContext.cookiesStore.getAllCookiesMap()

    override fun addCookie(key: String, value: String) =
        AppContext.cookiesStore.addCookie(key, value)

    override fun clearCookies() = AppContext.cookiesStore.clearCookies()
}
