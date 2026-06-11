package com.lemon.mcdevmanagermp.data.dto.netease.activity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JoinActivityDTO(
    @SerialName("item_id")
    val itemId: String,
    @SerialName("apply_intro")
    val applyIntro: String,
    @SerialName("video_info_list")
    val videoInfoList: List<String> = emptyList(),
    @SerialName("image_list")
    val imageList: List<String> = emptyList(),
)
