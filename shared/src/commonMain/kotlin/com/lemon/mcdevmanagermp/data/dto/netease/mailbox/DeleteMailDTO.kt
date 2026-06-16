package com.lemon.mcdevmanagermp.data.dto.netease.mailbox

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeleteMailDTO(
    @SerialName("mail_id_list")
    val mailIdList: List<String>
)