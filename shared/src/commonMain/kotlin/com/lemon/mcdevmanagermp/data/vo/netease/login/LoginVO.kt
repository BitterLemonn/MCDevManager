package com.lemon.mcdevmanagermp.data.vo.netease.login

import kotlinx.serialization.Serializable

@Serializable
data class BaseLoginVO(
    val ret: Int = 0,
    val dt: String = "",
    val msg: String? = null
)

@Serializable
data class TicketVO(
    val ret: Int = 0,
    val tk: String = "",
    val dt: String = "",
    val msg: String? = null
)

@Serializable
data class PowerVO(
    val ret: Int = 0,
    val pVInfo: PVInfoVO = PVInfoVO(),
    val dt: String = "",
    val msg: String? = null
)

@Serializable
data class CapIdVO(
    val ret: Int = 0,
    val capId: String = "",
    val pv: Boolean = false,
    val capFlag: Int = 0,
    val dt: String = "",
    val msg: String? = null
)


@Serializable
data class PVInfoVO(
    val sid: String = "",
    val hashFunc: String = "",
    val needCheck: Boolean = false,
    val args: PVArgs = PVArgs(),
    val maxTime: Int = 0,
    val minTime: Int = 0
)

@Serializable
data class PVArgs(
    val mod: String = "",
    val t: Int = 0,
    val puzzle: String = "",
    val x: String = ""
)