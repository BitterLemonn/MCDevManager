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
    // 宽松解析：允许数字字面量解析为字符串（mc_consts 中 tag/label_type 等的 id 为数字，需宽松解析为 String 字段）
    isLenient = true
    // 忽略实体类空值
    explicitNulls = false
}