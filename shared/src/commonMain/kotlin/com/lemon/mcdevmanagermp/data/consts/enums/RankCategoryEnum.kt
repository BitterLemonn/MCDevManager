package com.lemon.mcdevmanagermp.data.consts.enums

enum class RankCategoryTypeEnum(val typeName: String) {
    PE_HOT("手游热门飙升"),
    HOT_SEARCH("热搜榜"),
    PE_DOWNLOAD("手游免费榜"),
    PE_SELL("手游畅销榜"),
    PC_DOWNLOAD("端游下载榜"),
    PC_LIKE("端游点赞榜")
}

enum class RankSubCategoryTypeEnum(val typeName: String) {
    MOD("模组"),
    MAP("地图"),
    RESOURCE_PACK("材质光影"),
    SERVER("联机大厅")
}