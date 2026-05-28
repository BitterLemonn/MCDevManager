package com.lemon.mcdevmanagermp.data.common

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
val JSONConverter = Json {
    // 忽略实体类中不存在的字段
    ignoreUnknownKeys = true
    // 编码实体类默认值
    encodeDefaults = true
    // 忽略json空值
    coerceInputValues = true
    // 忽略实体类空值
    explicitNulls = false
}