package com.lemon.mcdevmanager.data.global

import com.lemon.mcdevmanager.data.netease.user.UserInfoBean

object AppContext {
    var curUserInfo: UserInfoBean? = null

    val cookiesStore = HashMap<String, String>()
    var nowNickname = "UNKNOWN"

    val accountList = mutableListOf<String>()
    var logDirPath = ""
}