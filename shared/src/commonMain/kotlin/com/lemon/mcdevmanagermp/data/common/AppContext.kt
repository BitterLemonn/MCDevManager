package com.lemon.mcdevmanagermp.data.common

import com.lemon.mcdevmanagermp.data.db.AppDatabase
import com.lemon.mcdevmanagermp.platform.createAppDatabaseBuilder
import com.lemon.mcdevmanagermp.utils.CookiesStore

object AppContext {
    var cookiesStore = CookiesStore
    val database: AppDatabase by lazy { createAppDatabaseBuilder().build() }
}
