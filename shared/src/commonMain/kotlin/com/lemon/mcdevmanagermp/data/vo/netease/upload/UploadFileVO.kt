package com.lemon.mcdevmanagermp.data.vo.netease.upload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadFileVO(
    val url: String,
    val mime: String,
    @SerialName("fsize")
    val fileSize: Long,
    val md5: String,
    val picSize: List<Int>? = null
)
