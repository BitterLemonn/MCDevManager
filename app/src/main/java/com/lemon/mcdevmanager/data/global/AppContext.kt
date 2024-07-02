package com.lemon.mcdevmanager.data.global

object AppContext {
    val cookiesStore = HashMap<String, String>()
    var nowNickname = "UNKNOWN"

    val accountList = mutableListOf<String>()
}