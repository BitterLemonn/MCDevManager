package com.lemon.mcdevmanagermp.data.vo.netease.activity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscountActivityVO(
    @SerialName("activity_id")
    val activityId: String,
    @SerialName("activity_name")
    val name: String,
    @SerialName("activity_type")
    val type: Int,
    val status: String,
    @SerialName("switch")
    val switch: String,
    @SerialName("begin_at")
    val beginAt: Int,
    @SerialName("end_at")
    val endAt: Int,
    @SerialName("apply_end_at")
    val applyEndAt: Int,
    @SerialName("create_time")
    val createTime: String,
    @SerialName("update_time")
    val updateTime: String,
    @SerialName("activity_description")
    val activityDescription: String,
    @SerialName("activity_instruction")
    val activityInstruction: String,
    @SerialName("activity_modules")
    val modules: List<DiscountActivityModuleVO> = emptyList(),
    @SerialName("activity_partition")
    val partition: List<DiscountActivityPartitionVO> = emptyList()
)

@Serializable
data class DiscountActivityModuleVO(
    @SerialName("item_pri_type_list")
    val itemPriTypeList: List<Int>,
    @SerialName("max_discount")
    val maxDiscount: Int,
    @SerialName("min_diamond")
    val minDiamond: Int,
    @SerialName("min_discount")
    val minDiscount: Int,
    @SerialName("module_id")
    val moduleId: String,
    @SerialName("module_name")
    val moduleName: String,
    @SerialName("price_update_limit_day")
    val priceUpdateLimitDay: Int,
    @SerialName("rebate_discount_num")
    val rebateDiscountNum: Int,
    @SerialName("rebate_max_num")
    val rebateMaxNum: Int
)

@Serializable
data class DiscountActivityPartitionVO(
    @SerialName("item_pri_type_list")
    val itemPriTypeList: List<Int>,
    @SerialName("partition_id")
    val partitionId: String,
    @SerialName("partition_name")
    val partitionName: String,
    @SerialName("partition_rule")
    val partitionRule: String
)