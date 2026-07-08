package com.lemon.mcdevmanagermp.data.vo.netease.other

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RedSpotsVO(
    @SerialName("CREDIT_POINT_CHANGE")
    val creditPointChange: Int = 0,
    @SerialName("CREDIT_POINT_CONFIG_VERSION")
    val creditPointConfigVersion: Int = 0,
    @SerialName("CREDIT_POINT_PAGE_VIEW")
    val creditPointPageView: Int = 0,
    @SerialName("unread_coupon_activity_count")
    val unreadCouponActivityCount: Int = 0,
    @SerialName("unread_discount_activity_count")
    val unreadDiscountActivityCount: Int = 0,
    @SerialName("unread_game_gift_activity_count")
    val unreadGameGiftActivityCount: Int = 0,
    @SerialName("unread_new_job_claim")
    val unreadNewJobClaim: Int = 0,
    @SerialName("unread_pe_review_activity_count")
    val unreadPeReviewActivityCount: Int = 0,
)