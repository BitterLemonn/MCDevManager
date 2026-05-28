package com.lemon.mcdevmanagermp.data.vo.netease.login

import kotlinx.serialization.Serializable

@Serializable
data class BaseLoginVO(
    val ret: Int
)

@Serializable
data class TicketVO(
    val ret: Int,
    val tk: String
)

@Serializable
data class PowerVO(
    val ret: Int,
    val pVInfo: PVInfoVO
)

@Serializable
data class CapIdVO(
    val ret: Int,
    val capId: String,
    val pv: Boolean,
    val capFlag: Int
)


@Serializable
data class PVInfoVO(
    val sid: String,
    val hashFunc: String,
    val needCheck: Boolean,
    val args: PVArgs,
    val maxTime: Int,
    val minTime: Int
)

@Serializable
data class PVArgs(
    val mod: String,
    val t: Int,
    val puzzle: String,
    val x: String
)