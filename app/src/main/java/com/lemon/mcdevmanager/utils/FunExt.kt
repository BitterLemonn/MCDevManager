package com.lemon.mcdevmanager.utils

fun String.dumpAndGetCookiesValue(key: String): String? {
    val cookies = this.split(";")
    for (cookie in cookies) {
        val pair = cookie.split("=")
        if (pair.size == 2 && pair[0].trim() == key) {
            return pair[1].trim()
        }
    }
    return null
}

fun String.isValidCookiesStr(): Boolean {
    return this.isNotEmpty() && this.contains("=") && this.contains(";")
}