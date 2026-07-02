package com.lemon.mcdevmanagermp.data.vo.netease.activity

import com.lemon.mcdevmanagermp.data.consts.enums.priceTypeLabel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CandidatesVO(
    val items: List<CandidatesItemVO>
)

@Serializable
data class CandidatesItemVO(
    @SerialName("adv_obtain_num")
    val advObtainNum: Int,
    @SerialName("item_id")
    val itemId: String,
    @SerialName("item_name")
    val itemName: String,
    @SerialName("pri_type")
    val priType: Int,
    val price: Int,
    @SerialName("price_type")
    val priceType: String,
    @SerialName("sub_type")
    val subType: Int
) {
    val priceTypeName: String = priceTypeLabel(priceType)
}