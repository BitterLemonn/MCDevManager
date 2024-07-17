package com.lemon.mcdevmanager.utils

import com.lemon.mcdevmanager.data.database.database.GlobalDataBase
import com.lemon.mcdevmanager.data.global.AppContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun logout(accountName: String) {
    AppContext.accountList.remove(accountName)
    AppContext.cookiesStore.remove(accountName)
    withContext(Dispatchers.IO) {
        GlobalDataBase.database.infoDao().deleteOverviewByNickname(accountName)
        GlobalDataBase.database.userDao().deleteUserByNickname(accountName)
    }
}