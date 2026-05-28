package com.lemon.mcdevmanagermp.data.vo.netease.ranklist

import kotlinx.serialization.Serializable

@Serializable
data class RankListVO<T>(
    val count: Int,
    val data: List<T>
)
