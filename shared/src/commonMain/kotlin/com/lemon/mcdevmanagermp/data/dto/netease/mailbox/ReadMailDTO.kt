package com.lemon.mcdevmanagermp.data.dto.netease.mailbox

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReadMailDTO(
    @SerialName("mail_id_list")
    val mailIdList: List<String> = emptyList(),
    @SerialName("read_all")
    val readAll: Boolean = true
)
