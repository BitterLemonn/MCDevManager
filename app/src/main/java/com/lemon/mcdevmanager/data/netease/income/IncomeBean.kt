package com.lemon.mcdevmanager.data.netease.income

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IncomeBean(
    @SerialName("income_id")
    val incomeIds: List<String>
)