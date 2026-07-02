package com.lemon.mcdevmanagermp.data.consts.enums

import kotlinx.serialization.Serializable

/**
 * 资源文件（res）上传 / 校验 / 审核状态流转。
 *
 * 与作品级状态 [WorkItemStatusEnum] 不同：本枚举描述单个资源文件的
 * 「上传 → 本地校验 → 网易审核 → 微软审核 → 上架」双审核链路。
 */
@Serializable
enum class ResFileStatusEnum(val value: String, val label: String) {
    INIT("init", "待提交校验"),
    UPLOADING("uploading", "上传中"),
    UPLOAD_FAIL("upload_fail", "上传失败"),
    VALIDATING("validating", "校验中"),
    VALIDATOR_PASS("validator_pass", "校验通过"),
    VALIDATOR_FAIL("validator_fail", "校验失败"),
    NETEASE_REVIEWING("netease_reviewing", "网易审核中"),
    NETEASE_REJECT("netease_reject", "网易审核拒绝"),
    MS_REVIEWING("ms_reviewing", "微软审核中"),
    MS_PASS("ms_pass", "微软审核通过"),
    MS_REJECT("ms_reject", "微软审核失败"),
    ONLINE_ING("online_ing", "上架中"),
    ONLINE("online", "已上架"),
    OFFLINE("offline", "已下架");

    companion object {
        fun fromValue(value: String): ResFileStatusEnum? = entries.find { it.value == value }
    }
}
