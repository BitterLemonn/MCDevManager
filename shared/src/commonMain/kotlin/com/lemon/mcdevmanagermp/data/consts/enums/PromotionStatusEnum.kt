package com.lemon.mcdevmanagermp.data.consts.enums

import kotlinx.serialization.Serializable
import kotlin.time.Clock

/**
 * 轮播图申请审核状态。
 *
 * 与作品级状态 [WorkItemStatusEnum] 不同，
 * 作品上架走 accept/rejected，二者不可混用。
 */
@Serializable
enum class PromotionStatusEnum(val value: String, val label: String) {
    REVIEWING("reviewing", "审核中"),
    INIT("init", "初始状态"),
    REVIEWING_1("reviewing_1", "待官方一审"),
    REVIEWING_2("reviewing_2", "待官方二审"),
    PAYABLE("payable", "待支付"),
    AUCTION_SUCCESS("auction_success", "竞拍成功"),
    AUCTION_FAIL("auction_fail", "竞拍失败"),
    READY("ready", "待上线"),
    REJECT("reject", "审核不通过"),
    PAY_OVERTIME("pay_overtime", "支付超时"),
    AUTO_OFFLINE("auto_offline", "自动下线"),
    FORCE_OFFLINE("force_offline", "强制下线"),
    CANCEL("cancel", "对应资源位取消"),
    TO_MODIFY("to_modify", "待更正修改"),
    UNSELECTED("unselected", "排序靠后，进入替补队列"),

    // 特殊状态
    SHOWING("showing", "展示中"),
    SHOWING_COMPLETE("showing_complete", "展示完成"),
    EXPIRED("expired", "已过期"),
    UNKNOWN("unknown", "未知"),
    ;

    companion object {
        private fun fromValue(value: String): PromotionStatusEnum? =
            entries.find { it.value == value }

        fun fromValue(value: String, startTime: Int): PromotionStatusEnum {
            val status = fromValue(value)
            // 检查当前时间是否在轮播图申请开始时间之前 还是开始时间之后一周内 还是开始时间之后一周之后
            val timeStaus = when {
                Clock.System.now().toEpochMilliseconds() / 1000 < startTime -> -1
                Clock.System.now().toEpochMilliseconds() / 1000 < startTime + 7 * 24 * 60 * 60 -> 0
                else -> 1
            }
            return when (status) {
                READY -> when (timeStaus) {
                    0 -> SHOWING
                    1 -> SHOWING_COMPLETE
                    else -> READY
                }

                else -> when (timeStaus) {
                    1 -> EXPIRED
                    else -> status ?: UNKNOWN
                }
            }
        }
    }
}
