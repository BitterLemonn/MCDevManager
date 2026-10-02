package com.lemon.mcdevmanagermp.data.repository

import com.lemon.mcdevmanagermp.data.common.AppContext
import com.lemon.mcdevmanagermp.domain.account.CookieRepository
import com.lemon.mcdevmanagermp.domain.resource.MCConstsCache

class CookieRepositoryImpl : CookieRepository {

    companion object {
        val INSTANCE by lazy { CookieRepositoryImpl() }
    }

    override fun getAllCookiesMap(): Map<String, String> =
        AppContext.cookiesStore.getAllCookiesMap()

    override fun addCookie(key: String, value: String) =
        AppContext.cookiesStore.addCookie(key, value)

    override fun clearCookies() {
        AppContext.cookiesStore.clearCookies()
        // cookie 是账号身份载体：清空即代表换号/退出，随请求缓存的账号级数据必须一并失效
        MCConstsCache.clear()
    }
}
