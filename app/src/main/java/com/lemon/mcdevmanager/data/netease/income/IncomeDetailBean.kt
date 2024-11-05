package com.lemon.mcdevmanager.data.netease.income

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IncomeDetailBean(
    val count: Int = 0,
    val incomes: List<IncomeBean> = emptyList()
)

@Serializable
data class IncomeBean(
    @SerialName("adjust_diamond")
    val adjustDiamond: Int = 0,
    @SerialName("available_detail")
    val availableDetail: List<IncomeAvailableDetail> = emptyList(),
    @SerialName("available_income")
    val availableIncome: String = "0.00",
    @SerialName("data_month")
    val dataMonth: String = "",
    @SerialName("incentive_income")
    val incentiveIncome: String = "0.00",
    val income: String = "0.00",
    @SerialName("op_time")
    val opTime: String = "",
    val platform: String = "",
    @SerialName("play_plan_income")
    val playPlanIncome: String = "0.00",
    val status: String = "",
    val tax: String = "0.00",
    @SerialName("tech_service_fee")
    val techServiceFee: Double = 0.0,
    @SerialName("total_diamond")
    val totalDiamond: Int = 0,
    @SerialName("total_usage_price")
    val totalUsagePrice: Double = 0.0,
    val type: String = ""
)

@Serializable
data class IncomeAvailableDetail(
    @SerialName("data_month")
    val dataMonth: String = "",
    val income: String = "0.00"
)