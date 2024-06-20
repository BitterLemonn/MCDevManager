package com.lemon.mcdevmanager.utils

import com.orhanobut.logger.Logger
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException

object UnifiedExceptionHandler {
    private const val TAG = "UnifiedException"
    suspend fun <T> handleSuspend(function: suspend () -> ResponseData<T>): NetworkState<T> {
        return try {
            val result = function.invoke()
            when (result.code) {
                200 -> result.data?.let { NetworkState.Success(result.data) }
                    ?: NetworkState.Success(msg = result.message)
                else ->
                    NetworkState.Error(result.message ?: "未知错误，请联系管理员")
            }
        } catch (e: SocketTimeoutException) {
            Logger.e("$TAG:链接超时")
            return NetworkState.Error("网络好像被末影人搬走了", SocketTimeoutException("网络好像被末影人搬走了"))
        } catch (e: ConnectException) {
            Logger.e("$TAG:无法连接到服务器")
            return NetworkState.Error("服务器掉进深暗之域了", ConnectException("服务器掉进深暗之域了"))
        }catch (e: HttpException){
            if (e.code() == 401) {
                Logger.e("$TAG:Token失效")
                return NetworkState.Error("登录过期啦!", LoginException("登录过期啦!"))
            }else return NetworkState.Error("未知错误，请联系管理员", e)
        } catch (e: Exception) {
            e.message?.let { Logger.e("$TAG:$it") }?:Logger.e(e::class.toString())
            return NetworkState.Error("未知错误，请联系管理员", e)
        }
    }

//    suspend fun <T> handleSuspendWithToken(function: suspend () -> ResponseData<T>): NetworkState<T>{
//        val token = AppContext.profile?.token
//        Logger.d(function)
//        return if (token.isNullOrBlank())
//            NetworkState.Error("用户信息丢失了哦", LoginException("用户信息丢失了哦"))
//        else handleSuspend { function.invoke() }
//    }

    class LoginException(message: String): Exception(message)
}