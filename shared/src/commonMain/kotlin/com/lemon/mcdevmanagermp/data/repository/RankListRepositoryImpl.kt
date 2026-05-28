package com.lemon.mcdevmanagermp.data.repository

import com.lemon.mcdevmanagermp.data.api.InfoApi
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.vo.netease.ranklist.CommonRankListData
import com.lemon.mcdevmanagermp.data.vo.netease.ranklist.HotSearchData
import com.lemon.mcdevmanagermp.data.vo.netease.ranklist.PeHotData
import com.lemon.mcdevmanagermp.data.vo.netease.ranklist.RankListVO
import com.lemon.mcdevmanagermp.domain.rankList.RankListRepository
import com.lemon.mcdevmanagermp.utils.UnifiedExceptionHandler

class RankListRepositoryImpl : RankListRepository {
    companion object {
        val INSTANCE by lazy { RankListRepositoryImpl() }
        private val infoApi = InfoApi.INSTANCE
    }

    override suspend fun getPeHotRankList(firstType: Int): NetworkState<RankListVO<PeHotData>> {
        return UnifiedExceptionHandler.handleRequest {
            infoApi.getPeHotRankList(firstType = firstType)
        }
    }

    override suspend fun getHotSearchRankList(firstType: Int): NetworkState<RankListVO<HotSearchData>> {
        return UnifiedExceptionHandler.handleRequest {
            infoApi.getHotSearchRankList(firstType = firstType)
        }
    }

    override suspend fun getPeDownloadRankList(firstType: Int): NetworkState<RankListVO<CommonRankListData>> {
        return UnifiedExceptionHandler.handleRequest {
            infoApi.getPeDownloadRankList(firstType = firstType)
        }
    }

    override suspend fun getPeSellRankList(firstType: Int): NetworkState<RankListVO<CommonRankListData>> {
        return UnifiedExceptionHandler.handleRequest {
            infoApi.getPeSellRankList(firstType = firstType)
        }
    }

    override suspend fun getPcDownloadRankList(firstType: Int): NetworkState<RankListVO<CommonRankListData>> {
        return UnifiedExceptionHandler.handleRequest {
            infoApi.getPcDownloadRankList(firstType = firstType)
        }
    }

    override suspend fun getPcLikeRankList(firstType: Int): NetworkState<RankListVO<CommonRankListData>> {
        return UnifiedExceptionHandler.handleRequest {
            infoApi.getPcLikeRankList(firstType = firstType)
        }
    }
}