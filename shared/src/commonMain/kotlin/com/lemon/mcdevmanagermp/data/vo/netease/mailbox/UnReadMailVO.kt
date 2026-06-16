package com.lemon.mcdevmanagermp.data.vo.netease.mailbox

import kotlinx.serialization.Serializable

@Serializable
data class UnReadMailVO(
    val count: Int = 0
)
