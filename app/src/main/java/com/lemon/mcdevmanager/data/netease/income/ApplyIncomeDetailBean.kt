package com.lemon.mcdevmanager.data.netease.income

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApplyIncomeDetailBean(
    @SerialName("adjust_money")
    val adjustMoney: String = "0.00",
    @SerialName("available_detail")
    val availableDetail: List<AvailableDetail> = emptyList(),
    @SerialName("available_income")
    val availableIncome: String = "",
    val bank: String = "",
    @SerialName("card_no")
    val cardNo: String = "",
    val city: String = "",
    @SerialName("currency_type")
    val currencyType: String = "",
    @SerialName("data_month")
    val dataMonth: String = "",
    @SerialName("developer_urs")
    val developerUrs: String = "",
    @SerialName("extra_info")
    val extraInfo: ExtraInfo = ExtraInfo(),
    val id: String = "",
    @SerialName("incentive_income")
    val incentiveIncome: String = "0.00",
    val income: String = "",
    val platform: String = "",
    @SerialName("play_plan_income")
    val playPlanIncome: String = "0.00",
    @SerialName("provider_name")
    val providerName: String = "",
    val province: String = "",
    @SerialName("real_name")
    val realName: String = "",
    val status: String = "",
    @SerialName("sub_bank")
    val subBank: String = "",
    val tax: String = "",
    @SerialName("tax_income")
    val taxIncome: String = "0.00",
    @SerialName("tech_service_fee")
    val techServiceFee: Double = 0.0,
    @SerialName("total_diamond")
    val totalDiamond: Int = 0,
    @SerialName("total_usage_price")
    val totalUsagePrice: Double = 0.0,
    @SerialName("type")
    private val _type: String = ""
){
    val type: String
        get() = if (_type == "individual_withhold") "个人开发者代扣代缴" else if (_type == "individual_owned") "个人开发者自备税票" else if (_type == "company_owned") "公司开发者自备税票" else "---"
}

@Serializable
data class ExtraInfo(
    @SerialName("adv_income")
    val advIncome: Double = 0.0
)

@Serializable
data class AvailableDetail(
    @SerialName("data_month")
    val dataMonth: String = "",
    val income: String = ""
)