package com.lemon.mcdevmanager.utils

import com.lemon.mcdevmanager.data.common.CookiesStore
import com.lemon.mcdevmanager.data.common.NETEASE_USER_COOKIE
import com.lemon.mcdevmanager.data.database.database.GlobalDataBase
import com.lemon.mcdevmanager.data.github.update.LatestReleaseBean
import com.lemon.mcdevmanager.data.global.AppContext
import com.lemon.mcdevmanager.data.netease.login.BaseLoginBean
import com.lemon.mcdevmanager.data.netease.login.CapIdBean
import com.lemon.mcdevmanager.data.netease.login.PowerBean
import com.lemon.mcdevmanager.data.netease.login.TicketBean
import com.orhanobut.logger.Logger
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException

object UnifiedExceptionHandler {
    private const val TAG = "UnifiedException"
    suspend fun <T> handleSuspend(function: suspend () -> ResponseData<T>): NetworkState<T> {
        return try {
            val result = function.invoke()
            parseData(result)
        } catch (e: SocketTimeoutException) {
            Logger.e("$TAG:链接超时\n$e")
            return NetworkState.Error(
                "网络好像被末影人搬走了",
                SocketTimeoutException("网络好像被末影人搬走了")
            )
        } catch (e: ConnectException) {
            Logger.e("$TAG:无法连接到服务器\n$e")
            return NetworkState.Error(
                "服务器掉进深暗之域了",
                ConnectException("服务器掉进深暗之域了")
            )
        } catch (e: HttpException) {
            if (e.code() == 401) {
                Logger.e("$TAG:Token失效\n$e")
                return NetworkState.Error("登录过期啦!", LoginException("登录过期啦!"))
            } else return NetworkState.Error("未知错误，请联系管理员", e)
        } catch (e: Exception) {
            e.message?.let { Logger.e("$TAG:$it") } ?: Logger.e(e::class.toString())
            return NetworkState.Error("未知错误，请联系管理员", e)
        }
    }

    suspend fun <T> handleSuspendWithGithubData(function: suspend () -> T): NetworkState<T> {
        return try {
            when (val result = function.invoke()) {
                is LatestReleaseBean -> {
                    val uniData = ResponseData("200", result)
                    parseData(uniData)
                }

                else -> NetworkState.Error("函数调用错误，非Github更新接口请使用handleSuspend")
            }
        } catch (e: SocketTimeoutException) {
            Logger.e("$TAG:链接超时\n$e")
            return NetworkState.Error(
                "网络好像被末影人搬走了",
                SocketTimeoutException("网络好像被末影人搬走了")
            )
        } catch (e: ConnectException) {
            Logger.e("$TAG:无法连接到服务器\n$e")
            return NetworkState.Error(
                "服务器掉进深暗之域了",
                ConnectException("服务器掉进深暗之域了")
            )
        } catch (e: HttpException) {
            if (e.code() == 403) {
                Logger.e("$TAG:GithubApi请求上限\n$e")
                return NetworkState.Error("请求过于频繁,请稍后再次尝试", e)
            } else return NetworkState.Error("未知错误，请联系管理员", e)
        } catch (e: Exception) {
            e.message?.let { Logger.e("$TAG:$it") } ?: Logger.e(e::class.toString())
            return NetworkState.Error("未知错误，请联系管理员", e)
        }
    }

    suspend fun <T> handleSuspendWithNeteaseData(function: suspend () -> T): NetworkState<String> {
        return try {
            when (val result = function.invoke()) {
                is TicketBean -> {
                    val uniData = ResponseData(result.ret.toString(), result.tk)
                    parseData(uniData)
                }

                is BaseLoginBean -> {
                    val uniData = ResponseData(result.ret.toString(), null)
                    parseData(uniData)
                }

                is PowerBean -> {
                    val uniData =
                        ResponseData(result.ret.toString(), dataJsonToString(result.pVInfo))
                    parseData(uniData)
                }

                is CapIdBean -> {
                    val uniData = ResponseData(result.ret.toString(), result.capId)
                    parseData(uniData)
                }

                else -> NetworkState.Error("函数调用错误，非网易登录接口请使用handleSuspend")
            }
        } catch (e: SocketTimeoutException) {
            Logger.e("$TAG:链接超时\n$e")
            return NetworkState.Error(
                "网络好像被末影人搬走了",
                SocketTimeoutException("网络好像被末影人搬走了")
            )
        } catch (e: ConnectException) {
            Logger.e("$TAG:无法连接到服务器\n$e")
            return NetworkState.Error(
                "服务器掉进深暗之域了",
                ConnectException("服务器掉进深暗之域了")
            )
        } catch (e: HttpException) {
            if (e.code() == 401) {
                Logger.e("$TAG:Token失效\n$e")
                return NetworkState.Error("登录过期啦!", LoginException("登录过期啦!"))
            } else return NetworkState.Error("未知错误，请联系管理员", e)
        } catch (e: Exception) {
            e.message?.let { Logger.e("$TAG:$it") } ?: Logger.e(e::class.toString())
            return NetworkState.Error("未知错误，请联系管理员", e)
        }
    }

    private fun <T> parseData(result: ResponseData<T>): NetworkState<T> {
        val returnCookies = CookiesStore.getCookie(NETEASE_USER_COOKIE)
        // 更新用户cookie
        if (returnCookies != null && returnCookies != AppContext.cookiesStore[AppContext.nowNickname]) {
            AppContext.cookiesStore[AppContext.nowNickname] = returnCookies
            GlobalDataBase.database.userDao().getUserByNickname(AppContext.nowNickname)?.let {
                val user = it.copy(cookie = returnCookies)
                GlobalDataBase.database.userDao().updateUser(user)
                Logger.d("用户${AppContext.nowNickname}的cookie已更新: $returnCookies")
            } ?: run {
                Logger.e("用户${AppContext.nowNickname}不存在")
            }
        }

        return when (result.status) {
            "200", "ok", "OK", "Ok" -> result.data?.let { NetworkState.Success(result.data) }
                ?: NetworkState.Success(msg = result.status)

            "201" -> result.data?.let { NetworkState.Success(result.data) }
                ?: NetworkState.Success(msg = result.status)

            "401", "no_login" -> NetworkState.Error("登录过期了，请重新登录", CookiesExpiredException)

            else ->
                NetworkState.Error(result.status)
        }
    }

    class LoginException(message: String? = null) : Exception(message)
}