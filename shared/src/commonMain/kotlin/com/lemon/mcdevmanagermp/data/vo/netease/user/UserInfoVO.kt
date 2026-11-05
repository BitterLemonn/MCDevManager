package com.lemon.mcdevmanagermp.data.vo.netease.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 用户信息
 */
@Serializable
data class UserInfoVO(
    val exp: Int,
    val level: Int,
    @SerialName("head_img")
    val headImg: String? = null,
    val nickname: String,
    val income: String,
    @SerialName("onsale_item_count")
    val onSaleItemCount: Int,
    @SerialName("cur_month_incentive_fund")
    val curMonthIncentiveFund: Double,
    @SerialName("unextract_income")
    val unExtractIncome: String,
    // 是否开通前置模组功能
    @SerialName("prerequisite_switch")
    val prerequisiteSwitch: Boolean,
    // 税费结算类型：individual_withhold(个人代扣代缴) / individual_owned(个人自备税票) / company_owned(公司自备税票)
    @SerialName("pay_type")
    val payType: String? = null
) {
    /**
     * 是否为企业开发者。
     * 与官方开发者平台判定一致：pay_type 为 company_owned 即视为企业开发者（含认证已过期的情况），
     * 企业开发者自行开票缴税，平台不代扣个人劳务报酬税。
     */
    val isEnterprise: Boolean
        get() = payType == "company_owned"
}
