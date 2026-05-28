package com.lemon.mcdevmanagermp.data.dto.netease.login

import com.lemon.mcdevmanagermp.utils.encrpy.getRandomTid
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import com.lemon.mcdevmanagermp.data.consts.pd as PD
import com.lemon.mcdevmanagermp.data.consts.pkid as PKID
import com.lemon.mcdevmanagermp.data.consts.pkht as PKHT
import com.lemon.mcdevmanagermp.data.consts.channel as CHANNEL

@Serializable
data class EncParamsDTO(
    val encParams: String
)

@Serializable
data class TicketDTO(
    val un: String,
    val pd: String = PD,
    val pkid: String = PKID,
    val channel: Int = CHANNEL,
    val topURL: String,
    val rtid: String = getRandomTid()
)

@Serializable
data class LoginDTO(
    val un: String,
    val pw: String,
    val pd: String = PD,
    val l: Int = 0,
    val d: Int = 10,
    val t: Long = Clock.System.now().toEpochMilliseconds(),
    val tk: String,
    val pwdKeyUp: Int = 1,
    val pkid: String = PKID,
    val domains: String = "",
    val pvParam: PVResultStrDTO,
    val channel: Int = CHANNEL,
    val topURL: String,
    val rtid: String = getRandomTid()
)

@Serializable
data class GetPowerDTO(
    val pkid: String = PKID,
    val pd: String = PD,
    val un: String,
    val channel: Int = CHANNEL,
    val topURL: String,
    val rtid: String = getRandomTid()
)

@Serializable
data class GetCapIdDTO(
    val pd: String = PD,
    val pkid: String = PKID,
    val pkht: String = PKHT,
    val channel: Int = CHANNEL,
    val topURL: String,
    val rtid: String = getRandomTid()
)

@Serializable
data class PVResultStrDTO(
    val maxTime: Int,
    val puzzle: String,
    val spendTime: Int,
    val runTimes: Int,
    val sid: String,
    val args: String
)


@Serializable
data class PVResultDTO(
    val x: String,
    val t: Int,
    var sign: Int = 0
)
