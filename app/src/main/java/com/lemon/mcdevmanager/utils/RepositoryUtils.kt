package com.lemon.mcdevmanager.utils

import kotlinx.serialization.Serializable

/**
 * 响应体通用类
 * @param T 响应体包含的数据的类型
 * @param code 响应体返回的状态码
 * @param message 响应体返回的信息
 */
@Serializable
data class ResponseData<T>(val code: Int, val data: T? = null, val message: String?)

/**
 * 响应处理包装通用类
 * 成功时返回 NetworkState.Success 包含响应返回的数据
 * 失败时返回 NetworkState.Error 包含响应返回的错误信息
 * 无视返回值 NetworkState.NoNeedResponse 包含响应返回的信息
 * @param T 响应体包含的数据的类型
 */
sealed class NetworkState<out T> {
    data class Success<T>(val data: T? = null, val msg: String? = null) : NetworkState<T>()
    data class Error<T>(val msg: String, val e: Exception? = null) : NetworkState<T>()
}