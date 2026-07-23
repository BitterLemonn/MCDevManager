package com.lemon.mcdevmanagermp.data.consts.enums

import kotlinx.serialization.Serializable

/** 作品大类（资源包 / 作品模板 / 其他）。 */
@Serializable
enum class WorkCategoryEnum(val value: String, val label: String) {
    RESOURCE("resource", "资源包"),
    TEMPLATE("template", "作品模板"),
    OTHER("other", "其他");

    companion object {
        fun fromValue(value: String): WorkCategoryEnum? = entries.find { it.value == value }
    }
}
