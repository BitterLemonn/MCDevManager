package com.lemon.mcdevmanagermp.data.vo.github

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LatestReleaseVO(
    @SerialName("tag_name")
    val tagName: String = "",
    val draft: Boolean = false,
    @SerialName("prerelease")
    val preRelease: Boolean = false,
    val assets: List<AssetBean> = emptyList(),
    val body: String = ""
)

@Serializable
data class AssetBean(
    val name: String,
    val size: Long,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("browser_download_url")
    val url: String
)