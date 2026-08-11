package com.lemon.mcdevmanagermp.utils

import com.lemon.mcdevmanagermp.data.common.AppContext
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.common.ResponseData
import com.lemon.mcdevmanagermp.data.consts.CookiesExpiredException
import com.lemon.mcdevmanagermp.data.consts.LoginException
import com.lemon.mcdevmanagermp.data.consts.NETEASE_USER_COOKIE
import com.lemon.mcdevmanagermp.data.consts.NeteaseLoginException
import com.lemon.mcdevmanagermp.data.consts.NetworkException
import com.lemon.mcdevmanagermp.data.consts.neteaseLoginErrorMessage
import com.lemon.mcdevmanagermp.data.dto.netease.login.NeteaseLoginResult
import com.lemon.mcdevmanagermp.data.vo.github.LatestReleaseVO
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException

object UnifiedExceptionHandler {
    private const val TAG = "UnifiedException"

    suspend fun <T> handleRequest(
        block: suspend () -> ResponseData<T>
    ): NetworkState<T> {
        // 首次执行
        var result = runRequestOnce(block)
        // 若判定为登录过期，自动重试一次，避免 Cookie 短暂失效或服务端瞬时异常导致的误判
        if (result.isLoginExpiredError()) {
            Logger.d("$TAG:检测到登录过期，自动重试一次以避免误判")
            result = runRequestOnce(block)
        }
        return result
    }

    private suspend fun <T> runRequestOnce(
        block: suspend () -> ResponseData<T>
    ): NetworkState<T> {
        return try {
            parseData(block())
        } catch (e: Exception) {
            handleException(e)
        }
    }

    private fun <T> NetworkState<T>.isLoginExpiredError(): Boolean {
        if (this !is NetworkState.Error) return false
        val cause = e ?: return false
        return cause is CookiesExpiredException
    }

    suspend fun handleGithubRequest(
        block: suspend () -> LatestReleaseVO
    ): NetworkState<LatestReleaseVO> {
        return try {
            parseData(ResponseData("200", block()))
        } catch (e: Exception) {
            handleException(e)
        }
    }

    suspend fun handleNeteaseLoginRequest(
        block: suspend () -> NeteaseLoginResult
    ): NetworkState<String> {
        return try {
            val result = block()
            if (result.ret == 200 || result.ret == 201) {
                NetworkState.Success(result.extractData())
            } else {
                val message = neteaseLoginErrorMessage(result.ret, result.dt, result.msg)
                Logger.e("$TAG: 网易登录失败 ret=${result.ret}, dt=${result.dt}")
                NetworkState.Error(
                    message,
                    NeteaseLoginException(result.ret, result.dt, message)
                )
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    private fun <T> handleException(e: Exception): NetworkState<T> {
        if (e is CancellationException) throw e

        return when (e) {
            is ConnectTimeoutException, is SocketTimeoutException -> {
                Logger.e("$TAG:链接超时", e)
                NetworkState.Error("网络好像被末影人搬走了", e)
            }

            is ResponseException -> {
                when (val statusCode = e.response.status.value) {
                    401 -> {
                        Logger.e("$TAG:Token失效", e)
                        NetworkState.Error("登录过期啦!", LoginException("登录过期啦!"))
                    }

                    403 -> {
                        Logger.e("$TAG:请求受限", e)
                        NetworkState.Error("请求过于频繁,请稍后再次尝试", e)
                    }

                    else -> {
                        Logger.e("$TAG:HTTP错误 $statusCode", e)
                        NetworkState.Error("服务器开小差了 ($statusCode)", e)
                    }
                }
            }

            is IOException -> {
                Logger.e("$TAG:网络错误", e)
                NetworkState.Error("服务器掉进深暗之域了", e)
            }

            else -> {
                Logger.e("$TAG:未知错误", e)
                NetworkState.Error("未知错误，请联系管理员", e)
            }
        }
    }

    private fun <T> parseData(
        result: ResponseData<T>,
        noNeedRefreshCookies: Boolean = false
    ): NetworkState<T> {
        if (!noNeedRefreshCookies) {
            refreshCookiesIfChanged()
        }

        return when (result.status) {
            "200", "201", "ok", "OK", "Ok" -> result.data?.let { NetworkState.Success(it) }
                ?: NetworkState.Success(msg = result.msg ?: result.status)

            "401", "no_login" -> {
                Logger.e("$TAG: ${result.msg}")
                NetworkState.Error(
                    "登录过期了，请重新登录",
                    CookiesExpiredException()
                )
            }

            else -> {
                Logger.e("$TAG: ${result.msg}")
                NetworkState.Error(result.msg ?: result.status)
            }
        }
    }

    private fun refreshCookiesIfChanged() {
        val returnCookies = CookiesStore.getCookie(NETEASE_USER_COOKIE) ?: return
        // Cookie 持久化
//        Logger.d("Cookie 已更新")
        AppContext.cookiesStore.addCookie(NETEASE_USER_COOKIE, returnCookies)
    }

    suspend fun <T> unwrapNetworkState(state: NetworkState<T>): T? {
        return when (state) {
            is NetworkState.Success -> state.data
            is NetworkState.Error -> {
                Logger.e("$TAG: 请求发生错误: ${state.msg}", state.e)
                throw NetworkException(state.msg, state.e)
            }
        }
    }
}
