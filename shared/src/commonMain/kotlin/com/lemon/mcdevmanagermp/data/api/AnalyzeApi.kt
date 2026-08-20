package com.lemon.mcdevmanagermp.data.api

import com.lemon.mcdevmanagermp.data.common.ResponseData
import com.lemon.mcdevmanagermp.data.consts.NETEASE_MC_DEV_LINK
import com.lemon.mcdevmanagermp.data.dto.netease.income.LobbyIncomeResourceListVO
import com.lemon.mcdevmanagermp.data.dto.netease.income.OneResRealtimeIncomeVO
import com.lemon.mcdevmanagermp.data.vo.netease.analyze.ResDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.analyze.ResMonthDetailVO
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

interface AnalyzeApi {
    @GET("data_analysis/day_detail/")
    suspend fun getDayDetail(
        @Query("platform") platform: String,
        @Query("category") category: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("item_list_str") itemListStr: String,
        @Query("sort") sort: String = "dateid",
        @Query("order") order: String = "ASC",
        @Query("start") start: Int = 0,
        @Query("span") span: Int = Int.MAX_VALUE,
        @Query("is_need_us_rank_data") isNeedUsRankData: Boolean = true
    ): ResponseData<ResDetailVO>

    @GET("data_analysis/goods/day_detail/")
    suspend fun getLobbyDayDetail(
        @Query("platform") platform: String = "pe",
        @Query("category") category: String = "pe",
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("item_list_str") itemListStr: String,
        @Query("sort") sort: String = "dateid",
        @Query("order") order: String = "ASC",
        @Query("start") start: Int = 0,
        @Query("span") span: Int = Int.MAX_VALUE,
        @Query("is_need_us_rank_data") isNeedUsRankData: Boolean = false,
        @Query("mc_type") mcType: String = "1"
    ): ResponseData<ResDetailVO>

    @GET("data_analysis/month_detail/")
    suspend fun getMonthDetail(
        @Query("platform") platform: String,
        @Query("category") category: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("sort") sort: String = "monthid",
        @Query("order") order: String = "DESC",
        @Query("start") start: Int = 0,
        @Query("span") span: Int = Int.MAX_VALUE,
        @Query("day_sort") daySort: String = "cnt_buy",
        @Query("day_span") daySpan: Int = Int.MAX_VALUE,
        @Query("day_dateid") dayDateId: String
    ): ResponseData<ResMonthDetailVO>

    @GET("data_analysis/goods/month_detail/")
    suspend fun getLobbyMonthDetail(
        @Query("platform") platform: String = "pe",
        @Query("category") category: String = "pe",
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("sort") sort: String = "monthid",
        @Query("order") order: String = "DESC",
        @Query("start") start: Int = 0,
        @Query("span") span: Int = Int.MAX_VALUE,
        @Query("day_sort") daySort: String = "cnt_buy",
        @Query("day_span") daySpan: Int = Int.MAX_VALUE,
        @Query("day_dateid") dayDateId: String,
        @Query("mc_type") mcType: String = "1"
    ): ResponseData<ResMonthDetailVO>

    @GET("items/categories/{platform}/{iid}/incomes/")
    suspend fun getOneResRealtimeIncome(
        @Path("platform") platform: String,
        @Path("iid") iid: String,
        @Query("begin_time") beginTime: String,
        @Query("end_time") endTime: String
    ): ResponseData<OneResRealtimeIncomeVO>

    @GET("goods/pe/summary")
    suspend fun getLobbyIncomeResources(
        @Query("start") start: Int = 0,
        @Query("span") span: Int = Int.MAX_VALUE
    ): ResponseData<LobbyIncomeResourceListVO>

    @GET("items/categories/pe/{iid}/lobby_incomes/")
    suspend fun getLobbyRealtimeIncome(
        @Path("iid") iid: String,
        @Query("begin_time") beginTime: String,
        @Query("end_time") endTime: String
    ): ResponseData<OneResRealtimeIncomeVO>

    companion object {
        val INSTANCE: AnalyzeApi by lazy {
            ApiFactory.provideKtorfit(NETEASE_MC_DEV_LINK).createAnalyzeApi()
        }
    }
}
