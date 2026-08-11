package com.lemon.mcdevmanagermp.data.dto.netease.work

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApplySelfTestDTO(
    @SerialName("self_test_pass_check")
    val selfTestPassCheck: Boolean = false,
)