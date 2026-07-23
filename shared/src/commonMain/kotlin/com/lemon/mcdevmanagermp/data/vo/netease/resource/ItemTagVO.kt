package com.lemon.mcdevmanagermp.data.vo.netease.resource

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 资源详情资源标签
 */
@Serializable
data class ItemTagVO(
    @SerialName("tag_list")
    val tagList: List<String> = emptyList()
)