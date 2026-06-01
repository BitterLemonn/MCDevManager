package com.lemon.mcdevmanagermp.domain.rankList

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.page.RankCategoryContent
import com.lemon.mcdevmanagermp.data.page.RankCategoryData
import com.lemon.mcdevmanagermp.data.page.RankCategoryTypeEnum
import com.lemon.mcdevmanagermp.data.page.RankGroupData
import com.lemon.mcdevmanagermp.data.page.RankListItemData
import com.lemon.mcdevmanagermp.data.page.RankSubCategoryTypeEnum
import com.lemon.mcdevmanagermp.data.page.commonRankCategoryContent
import com.lemon.mcdevmanagermp.data.vo.netease.ranklist.CommonRankListData
import com.lemon.mcdevmanagermp.data.vo.netease.ranklist.HotSearchData
import com.lemon.mcdevmanagermp.data.vo.netease.ranklist.PeHotData

class RankListUseCase(
    private val rankListRepository: RankListRepository
) {

    suspend fun getRankData(
        category: RankCategoryTypeEnum,
        subCategory: RankSubCategoryTypeEnum? = null
    ): RankCategoryData {
        val firstType = subCategory?.toFirstType(category) ?: defaultFirstType(category)
        val content = when (category) {
            RankCategoryTypeEnum.PE_HOT -> {
                val result = rankListRepository.getPeHotRankList(firstType)
                if (result is NetworkState.Success) {
                    val mapped = result.data?.data?.map { it.toRankListItem() } ?: emptyList()
                    buildContent(mapped, subCategory)
                } else emptyMultiContent
            }

            RankCategoryTypeEnum.HOT_SEARCH -> {
                val result = rankListRepository.getHotSearchRankList(firstType)
                if (result is NetworkState.Success) {
                    val mapped = result.data?.data?.map { it.toRankListItem() } ?: emptyList()
                    RankCategoryContent.Single(mapped)
                } else RankCategoryContent.Single(emptyList())
            }

            RankCategoryTypeEnum.PE_DOWNLOAD -> fetchCommonRank(category, subCategory, firstType) {
                rankListRepository.getPeDownloadRankList(it)
            }

            RankCategoryTypeEnum.PE_SELL -> fetchCommonRank(category, subCategory, firstType) {
                rankListRepository.getPeSellRankList(it)
            }

            RankCategoryTypeEnum.PC_DOWNLOAD -> fetchCommonRank(category, subCategory, firstType) {
                rankListRepository.getPcDownloadRankList(it)
            }

            RankCategoryTypeEnum.PC_LIKE -> fetchCommonRank(category, subCategory, firstType) {
                rankListRepository.getPcLikeRankList(it)
            }
        }
        return RankCategoryData(category.typeName, content)
    }

    private suspend fun fetchCommonRank(
        category: RankCategoryTypeEnum,
        subCategory: RankSubCategoryTypeEnum?,
        firstType: Int,
        fetcher: suspend (Int) -> NetworkState<*>
    ): RankCategoryContent {
        val result = fetcher(firstType)
        return if (result is NetworkState.Success) {
            @Suppress("UNCHECKED_CAST")
            val data =
                (result.data as? com.lemon.mcdevmanagermp.data.vo.netease.ranklist.RankListVO<CommonRankListData>)?.data
                    ?: emptyList()
            val mapped = data.map { it.toRankListItem() }
            buildContent(mapped, subCategory)
        } else emptyMultiContent
    }

    private fun buildContent(
        items: List<RankListItemData>,
        subCategory: RankSubCategoryTypeEnum?
    ): RankCategoryContent {
        return if (subCategory != null) {
            commonRankCategoryContent.let {
                val groups = it.groups.map { group ->
                    if (group.categoryName == subCategory.typeName) group.copy(data = items)
                    else group
                }
                RankCategoryContent.Multi(groups)
            }
        } else {
            val groups = RankSubCategoryTypeEnum.entries.map { sub ->
                if (items.isNotEmpty() && sub == RankSubCategoryTypeEnum.entries.first()) {
                    RankGroupData(sub.typeName, items)
                } else {
                    RankGroupData(sub.typeName, emptyList())
                }
            }
            RankCategoryContent.Multi(groups)
        }
    }

    companion object {
        private val emptyMultiContent = commonRankCategoryContent

        private fun PeHotData.toRankListItem() = RankListItemData(
            title = resName,
            rank = scoreRank,
            rankChange = rankChange,
            isNew = isNew,
            imgUrl = iconUrl.ifBlank { null }
        )

        private fun HotSearchData.toRankListItem() = RankListItemData(
            title = content,
            rank = rank,
            rankChange = rankChange,
            isNew = isNew
        )

        private fun CommonRankListData.toRankListItem() = RankListItemData(
            title = itemName,
            rank = rank,
            rankChange = rankChange,
            isNew = isNew
        )

        private fun RankSubCategoryTypeEnum.toFirstType(category: RankCategoryTypeEnum): Int =
            when (category) {
                RankCategoryTypeEnum.PE_HOT, RankCategoryTypeEnum.HOT_SEARCH,
                RankCategoryTypeEnum.PE_DOWNLOAD, RankCategoryTypeEnum.PE_SELL -> when (this) {
                    RankSubCategoryTypeEnum.MOD -> 2
                    RankSubCategoryTypeEnum.MAP -> 1
                    RankSubCategoryTypeEnum.RESOURCE_PACK -> 3
                    RankSubCategoryTypeEnum.SERVER -> 6
                }

                RankCategoryTypeEnum.PC_DOWNLOAD, RankCategoryTypeEnum.PC_LIKE -> when (this) {
                    RankSubCategoryTypeEnum.MOD -> 3
                    RankSubCategoryTypeEnum.MAP -> 5
                    RankSubCategoryTypeEnum.RESOURCE_PACK -> 4
                    RankSubCategoryTypeEnum.SERVER -> 11
                }
            }

        private fun defaultFirstType(category: RankCategoryTypeEnum): Int = when (category) {
            RankCategoryTypeEnum.PE_HOT -> 2
            RankCategoryTypeEnum.HOT_SEARCH -> 0
            RankCategoryTypeEnum.PE_DOWNLOAD -> 2
            RankCategoryTypeEnum.PE_SELL -> 2
            RankCategoryTypeEnum.PC_DOWNLOAD -> 3
            RankCategoryTypeEnum.PC_LIKE -> 3
        }
    }
}
