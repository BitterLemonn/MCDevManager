package com.lemon.mcdevmanagermp.data.consts.enums

enum class PromotionPositionEnum(val value: String, val label: String) {
    WONDERFUL_WORLD("wonderfulworld", "精彩世界"),
    GAME_CENTER("gamecenter", "游戏中心"),
    RESOURCE_CENTER("resourcecenter", "资源中心"),
    POPUP_WINDOW("popupwindow", "首屏弹窗"),
    PE_GAME_BANNER("pegamebanner", "网络游戏"),
    COMPONENT_CENTER("componentcenter", "模组中心"),
    HOT_ITEM_NEW_GAME("hotitem_new_game", "近期推荐-新服"),
    HOT_ITEM_ACTIVITY("hotitem_activity", "近期推荐-近期活动/更新"),
    FANS_PLATEFORM("fansplateform", "发烧平台"),
    UNKNOWN("unknown", "未知");

    companion object {
        fun fromValue(value: String): PromotionPositionEnum =
            entries.find { it.value == value } ?: UNKNOWN

    }
}