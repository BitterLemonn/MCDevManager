package com.lemon.mcdevmanagermp.domain.upload

import com.lemon.mcdevmanagermp.data.common.JSONConverter
import com.lemon.mcdevmanagermp.data.dto.netease.activity.FileInfoDTO
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

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

/**
 * 生成作品保存接口的视频/封面取值：新上传的文件必须提交上传回执对象（body/file_type/sign），
 * 服务端凭回执登记文件；沿用已保存的旧值时提交地址字符串。
 */
fun uploadReceiptOrAddress(fileInfo: FileInfoDTO?, address: String): JsonElement =
    fileInfo?.let {
        buildJsonObject {
            put("body", it.body)
            put("file_type", it.fileType)
            put("sign", it.sign)
        }
    } ?: JsonPrimitive(address)
