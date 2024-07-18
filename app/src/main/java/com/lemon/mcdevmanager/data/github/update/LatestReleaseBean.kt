package com.lemon.mcdevmanager.data.github.update

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LatestReleaseBean(
    @SerialName("tag_name")
    val tagName: String,
    val draft: Boolean,
    @SerialName("prerelease")
    val preRelease: Boolean,
    val assets: List<AssetBean>,
    val body: String
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