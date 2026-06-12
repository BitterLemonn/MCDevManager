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
    val videoInfoList: List<FileInfoDTO> = emptyList(),
    @SerialName("image_list")
    val imageList: List<FileInfoDTO> = emptyList(),
)

@Serializable
data class FileInfoDTO(
    val body: String,
    @SerialName("file_type")
    val fileType: String,
    val sign: String
)