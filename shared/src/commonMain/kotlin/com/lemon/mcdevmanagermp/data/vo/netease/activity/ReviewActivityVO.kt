package com.lemon.mcdevmanagermp.data.vo.netease.activity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Serializable
data class ReviewActivityVO(
    val data: List<ReviewActivityItemVO>,
    val count: Int
)

@Serializable
data class ReviewActivityItemVO(
    @SerialName("activity_description")
    val desc: String,
    @SerialName("activity_id")
    val id: String,
    @SerialName("activity_instruction")
    val instruction: String,
    @SerialName("activity_modules")
    val modules: List<ReviewActivityModuleVO> = emptyList(),
    @SerialName("activity_name")
    val name: String,
    @SerialName("apply_end_at")
    val applyEndAt: Int,
    val banner: String,
    @SerialName("begin_at")
    val beginAt: Int,
    @SerialName("create_time")
    val createTime: String,
    @SerialName("end_at")
    val endAt: Int,
    @SerialName("is_show")
    val isShow: Boolean,
    @SerialName("outer_link_enable")
    val outerLinkEnable: Boolean = false,
    val status: String,
    @SerialName("update_time")
    val updateTime: String
) {
    val statusTag: String = when (Clock.System.now().epochSeconds) {
        in beginAt..applyEndAt -> "进行中"
        in applyEndAt..endAt -> "审核中"
        else -> "已结束"
    }
}

@Serializable
data class ReviewActivityModuleVO(
    @SerialName("item_pri_type_list")
    val itemPriTypeList: List<Int> = emptyList(),
    @SerialName("item_status_list")
    val itemStatusList: List<String> = emptyList(),
    @SerialName("module_description")
    val moduleDescription: String,
    @SerialName("module_id")
    val moduleId: Int,
    @SerialName("module_name")
    val moduleName: String,
    @SerialName("multi_item_type_list")
    val multiItemTypeList: List<String> = emptyList()
)