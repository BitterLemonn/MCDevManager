package com.lemon.mcdevmanagermp.domain.account

/**
 * 账号领域模型
 */
data class Account(
    val id: Long = 0,
    val nickname: String,
    val cookiesJson: String,
    val lastLoginTime: Long,
    val headImg: String? = null,
)
