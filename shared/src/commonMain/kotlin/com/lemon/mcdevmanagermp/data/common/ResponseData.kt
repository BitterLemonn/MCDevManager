package com.lemon.mcdevmanagermp.data.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * 响应体通用类
 * @param T 响应体包含的数据的类型
 * @param status 响应体返回的状态
 * @param msg 响应体返回的信息
 * @param errors 参数校验错误及对应字段
 */
@Serializable
data class ResponseData<T>(
    val status: String,
    val data: T? = null,
    val msg: String? = null,
    val errors: JsonElement? = null,
) {
    fun errorMessage(): String = errors
        ?.flattenErrorMessages()
        ?.takeIf { it.isNotEmpty() }
        ?.joinToString("\n")
        ?: msg?.takeIf { it.isNotBlank() }
        ?: status
}

private fun JsonElement.flattenErrorMessages(path: String = ""): List<String> = when (this) {
    is JsonObject -> entries.flatMap { (field, value) ->
        value.flattenErrorMessages(if (path.isEmpty()) field else "$path.$field")
    }

    is JsonArray -> flatMap { it.flattenErrorMessages(path) }
    is JsonPrimitive -> contentOrNull
        ?.takeIf { it.isNotBlank() }
        ?.let { listOf(if (path.isEmpty()) it else "$path: $it") }
        .orEmpty()
}

/**
 * 响应处理包装通用类
 * 成功时返回 NetworkState.Success 包含响应返回的数据
 * 失败时返回 NetworkState.Error 包含响应返回的错误信息
 * @param T 响应体包含的数据的类型
 */
sealed class NetworkState<out T> {
    data class Success<T>(val data: T? = null, val msg: String? = null) : NetworkState<T>()
    data class Error<T>(val msg: String, val e: Exception? = null) : NetworkState<T>()
}

@Serializable
data object NoNeedData
