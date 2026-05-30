package com.lemon.mcdevmanagermp.data.page

data class RankListItemData(
    val title: String,
    val rank: Int,
    val rankChange: Int = 0,
    val isNew: Boolean = false,
    val imgUrl: String? = null,
)

data class RankGroupData(
    val categoryName: String,
    val data: List<RankListItemData>
)

data class RankCategoryData(
    val categoryTitle: String,
    val content: RankCategoryContent
)

sealed interface RankCategoryContent {
    data class Single(val list: List<RankListItemData>) : RankCategoryContent
    data class Multi(val groups: List<RankGroupData>) : RankCategoryContent
}

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

val commonRankCategoryContent = RankCategoryContent.Multi(
    listOf(
        RankGroupData(RankSubCategoryTypeEnum.MOD.typeName, emptyList()),
        RankGroupData(RankSubCategoryTypeEnum.MAP.typeName, emptyList()),
        RankGroupData(RankSubCategoryTypeEnum.RESOURCE_PACK.typeName, emptyList()),
        RankGroupData(RankSubCategoryTypeEnum.SERVER.typeName, emptyList())
    )
)
