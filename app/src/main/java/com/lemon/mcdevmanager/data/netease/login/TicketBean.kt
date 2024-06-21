package com.lemon.mcdevmanager.data.netease.login

import kotlinx.serialization.Serializable

@Serializable
data class BaseLoginBean(
    val ret: Int
)

@Serializable
data class TicketBean (
    val ret: Int,
    val tk: String
)

@Serializable
data class PowerBean(
    val ret: Int,
    val pVInfo: PVInfo
)

@Serializable
data class PVInfo(
    val puzzle: String,
)
