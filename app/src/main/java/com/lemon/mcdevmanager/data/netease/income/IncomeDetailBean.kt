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
    val platform: String = "pe",
    @SerialName("play_plan_income")
    val playPlanIncome: String = "0.00",
    @SerialName("status")
    private val _status: String = "",
    val tax: String = "0.00",
    @SerialName("tech_service_fee")
    val techServiceFee: Double = 0.0,
    @SerialName("total_diamond")
    val totalDiamond: Int = 0,
    @SerialName("total_usage_price")
    val totalUsagePrice: Double = 0.0,
    val type: String = ""
) {
    val status: String
        get() = if (_status == "init") "未结算" else if (_status == "fail") "结算失败" else if (_status == "applying") "结算中" else if (_status == "pay_success") "已打款" else if (_status == "pay_fail") "打款失败" else if (_status == "need_modify") "结算信息待更正" else "---"
}

@Serializable
data class IncomeAvailableDetail(
    @SerialName("data_month")
    val dataMonth: String = "",
    val income: String = "0.00"
)