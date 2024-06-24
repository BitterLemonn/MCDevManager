package com.lemon.mcdevmanager.data.netease.login

import com.lemon.mcdevmanager.utils.dataJsonToString
import com.lemon.mcdevmanager.utils.getRandomTid
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TicketRequestBean(
    val un: String,
    val pd: String = "x19_developer",
    val pkid: String = "kBSLIYY",
    val channel: Int = 0,
    val topURL: String,
    val rtid: String = getRandomTid()
)

@Serializable
data class LoginRequestBean(
    val un: String,
    val pw: String,
    val pd: String = "x19_developer",
    val l: Int = 0,
    val d: Int = 10,
    val t: Long = System.currentTimeMillis(),
    val tk: String,
    val pwdKeyUp: Int = 1,
    val pkid: String = "kBSLIYY",
    val domains: String = "",
    val pvParam: PVResultStrBean,
    val channel: Int = 0,
    val topURL: String,
    val rtid: String = getRandomTid()
)

@Serializable
data class GetPowerRequestBean(
    val pkid: String = "kBSLIYY",
    val pd: String = "x19_developer",
    val un: String,
    val channel: Int = 0,
    val topURL: String,
    val rtid: String = getRandomTid()
)

@Serializable
data class GetCapIdRequestBean(
    val pd: String = "x19_developer",
    val pkid: String = "kBSLIYY",
    val pkht: String = "mcdev.webapp.163.com",
    val channel: Int = 0,
    val topURL: String,
    val rtid: String = getRandomTid()
)

@Serializable
data class EncParams(
    val encParams: String
)

@Serializable
data class PVResultBean(
    val maxTime: Int,
    val puzzle: String,
    val spendTime: Int,
    val runTimes: Int,
    val sid: String,
    val args: PVResultArgs
)

@Serializable
data class PVResultStrBean(
    val maxTime: Int,
    val puzzle: String,
    val spendTime: Int,
    val runTimes: Int,
    val sid: String,
    val args: String
)

@Serializable
data class PVResultArgs(
    val x: String,
    val t: Int,
    var sign: Int = 0
)