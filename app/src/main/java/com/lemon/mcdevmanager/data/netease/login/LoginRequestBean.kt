package com.lemon.mcdevmanager.data.netease.login

import com.lemon.mcdevmanager.utils.getRandomTid
import kotlinx.serialization.Serializable

@Serializable
data class TicketRequestBean(
    val un: String,
    val pd: String = "x19_developer",
    val pkid: String = "kBSLIYY"
)

@Serializable
data class LoginRequestBean(
    val un: String,
    val pw: String,
    val tk: String,
    val d: Int = 10,
    val l: Int = 0,
    val pd: String = "x19_developer",
    val pkid: String = "kBSLIYY",
    val t: Long = System.currentTimeMillis()
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