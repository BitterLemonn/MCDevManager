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
    val prerequisiteSwitch: Boolean
)
