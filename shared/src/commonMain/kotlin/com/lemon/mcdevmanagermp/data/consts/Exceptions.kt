package com.lemon.mcdevmanagermp.data.consts

class NetworkException(message: String, cause: Exception?) : Exception(message, cause)
class LoginException(message: String? = null) : Exception(message)
class NeteaseLoginException(
    val ret: Int,
    val dt: String = "",
    message: String
) : Exception(message)
class CookiesExpiredException : Exception("Cookies 已过期")
class CookiesNotValidException : Exception("Cookies 不合法")