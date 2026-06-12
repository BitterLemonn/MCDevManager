package com.lemon.mcdevmanagermp.data.vo.netease.upload

import kotlinx.serialization.Serializable

@Serializable
data class UploadFileResponseVO(
    val body: String,
    val sign: String?
)