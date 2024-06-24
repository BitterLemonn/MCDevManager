package com.lemon.mcdevmanager.data.netease.login

import com.lemon.mcdevmanager.utils.dataJsonToString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BaseLoginBean(
    val ret: Int
)

@Serializable
data class TicketBean(
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
    @SerialName("args")
    private val _args: PVArgs
) {
    val args: String
        get() = dataJsonToString(_args)
}

@Serializable
data class PVArgs(
    val mod: String,
    val t: Int,
    val puzzle: String,
    val x: String
)

@Serializable
data class CapIdBean(
    val ret: Int,
    val capId: String,
    val pv: Boolean,
    val capFlag: Int
)