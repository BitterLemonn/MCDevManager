package com.lemon.mcdevmanagermp.domain.upload

import com.lemon.mcdevmanagermp.data.common.JSONConverter
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** 解析网易 FP 上传回执中的资源地址。 */
fun parseUploadUrl(body: String): String {
    if (body.isBlank()) return ""
    val parsed = runCatching {
        val json = JSONConverter.parseToJsonElement(body).jsonObject
        json["url"]?.jsonPrimitive?.content
            ?: json["filename"]?.jsonPrimitive?.content
    }.getOrNull()
    if (!parsed.isNullOrEmpty()) return parsed

    return Regex(""""url"\s*:\s*"([^"]+)"""").find(body)?.groupValues?.get(1)
        ?: Regex(""""filename"\s*:\s*"([^"]+)"""").find(body)?.groupValues?.get(1)
        ?: ""
}
