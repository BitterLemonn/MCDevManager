package com.lemon.mcdevmanagermp.data.vo.netease.mailbox

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MailContentVO(
    val detail: String = "",
    @SerialName("extra_list")
    val extraList: List<String> = emptyList(),
    val sender: String = ""
)
