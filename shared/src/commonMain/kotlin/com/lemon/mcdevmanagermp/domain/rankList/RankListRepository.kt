package com.lemon.mcdevmanagermp.domain.rankList

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.vo.netease.ranklist.CommonRankListData
import com.lemon.mcdevmanagermp.data.vo.netease.ranklist.HotSearchData
import com.lemon.mcdevmanagermp.data.vo.netease.ranklist.PeHotData
import com.lemon.mcdevmanagermp.data.vo.netease.ranklist.RankListVO

interface RankListRepository {
    suspend fun getPeHotRankList(firstType: Int = 2): NetworkState<RankListVO<PeHotData>>

    suspend fun getHotSearchRankList(firstType: Int = 0): NetworkState<RankListVO<HotSearchData>>

    suspend fun getPeDownloadRankList(firstType: Int = 2): NetworkState<RankListVO<CommonRankListData>>

    suspend fun getPeSellRankList(firstType: Int = 2): NetworkState<RankListVO<CommonRankListData>>

    suspend fun getPcDownloadRankList(firstType: Int = 2): NetworkState<RankListVO<CommonRankListData>>

    suspend fun getPcLikeRankList(firstType: Int = 2): NetworkState<RankListVO<CommonRankListData>>
}