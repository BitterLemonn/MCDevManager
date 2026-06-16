package com.lemon.mcdevmanagermp.data.consts.enums

enum class ItemRealStatusEnum(val code: Int, val label: String) {
    ONLINE(1, "已上架"),
    SYSTEM_OFFLINE(8, "系统下架"),
    SELF_TEST(100, "自测中")
}