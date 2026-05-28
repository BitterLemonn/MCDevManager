package com.lemon.mcdevmanagermp.data.api

import com.lemon.mcdevmanagermp.data.consts.NETEASE_MC_DEV_LINK
import com.lemon.mcdevmanagermp.data.common.ResponseData
import com.lemon.mcdevmanagermp.data.vo.netease.ranklist.CommonRankListData
import com.lemon.mcdevmanagermp.data.vo.netease.ranklist.HotSearchData
import com.lemon.mcdevmanagermp.data.vo.netease.ranklist.PeHotData
import com.lemon.mcdevmanagermp.data.vo.netease.ranklist.RankListVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceVO
import com.lemon.mcdevmanagermp.data.vo.netease.user.LevelInfoVO
import com.lemon.mcdevmanagermp.data.vo.netease.user.OverviewVO
import com.lemon.mcdevmanagermp.data.vo.netease.user.UserInfoVO
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

interface InfoApi {

    @GET("users/me")
    suspend fun getUserInfo(): ResponseData<UserInfoVO>

    @GET("data_analysis/overview")
    suspend fun getOverview(): ResponseData<OverviewVO>

    @GET("new_level")
    suspend fun getLevelInfo(): ResponseData<LevelInfoVO>

    @GET("items/categories/{platform}")
    suspend fun getResInfoList(
        @Path("platform") platform: String = "pe",
        @Query("start") start: Int = 0,
        @Query("span") span: Int = Int.MAX_VALUE
    ): ResponseData<ResourceVO>

    @GET("square/us_rank_list/?type=pe_hot")
    suspend fun getPeHotRankList(
        @Query("start") start: Int = 0,
        @Query("span") span: Int = 50,
        @Query("first_type") firstType: Int
    ): ResponseData<RankListVO<PeHotData>>

    @GET("square/us_rank_list/?type=hot_search")
    suspend fun getHotSearchRankList(
        @Query("start") start: Int = 0,
        @Query("span") span: Int = 50,
        @Query("first_type") firstType: Int
    ): ResponseData<RankListVO<HotSearchData>>

    @GET("square/rank_list/?type=pe_download")
    suspend fun getPeDownloadRankList(
        @Query("start") start: Int = 0,
        @Query("span") span: Int = 50,
        @Query("first_type") firstType: Int
    ): ResponseData<RankListVO<CommonRankListData>>

    @GET("square/rank_list/?type=pe_sell")
    suspend fun getPeSellRankList(
        @Query("start") start: Int = 0,
        @Query("span") span: Int = 50,
        @Query("first_type") firstType: Int
    ): ResponseData<RankListVO<CommonRankListData>>

    @GET("square/rank_list/?type=pc_download")
    suspend fun getPcDownloadRankList(
        @Query("start") start: Int = 0,
        @Query("span") span: Int = 50,
        @Query("first_type") firstType: Int
    ): ResponseData<RankListVO<CommonRankListData>>

    @GET("square/rank_list/?type=pc_like")
    suspend fun getPcLikeRankList(
        @Query("start") start: Int = 0,
        @Query("span") span: Int = 50,
        @Query("first_type") firstType: Int
    ): ResponseData<RankListVO<CommonRankListData>>

    companion object {
        val INSTANCE by lazy {
            ApiFactory.provideKtorfit(NETEASE_MC_DEV_LINK).createInfoApi()
        }
    }
}