package com.lemon.mcdevmanagermp.data.page

import com.lemon.mcdevmanagermp.data.consts.enums.RankSubCategoryTypeEnum

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

val commonRankCategoryContent = RankCategoryContent.Multi(
    listOf(
        RankGroupData(RankSubCategoryTypeEnum.MOD.typeName, emptyList()),
        RankGroupData(RankSubCategoryTypeEnum.MAP.typeName, emptyList()),
        RankGroupData(RankSubCategoryTypeEnum.RESOURCE_PACK.typeName, emptyList()),
        RankGroupData(RankSubCategoryTypeEnum.SERVER.typeName, emptyList())
    )
)
