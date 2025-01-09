package com.lemon.mcdevmanager.api

import com.lemon.mcdevmanager.data.AddCookiesInterceptor
import com.lemon.mcdevmanager.data.CommonInterceptor
import com.lemon.mcdevmanager.data.common.JSONConverter
import com.lemon.mcdevmanager.data.common.NETEASE_MC_DEV_LINK
import com.lemon.mcdevmanager.data.netease.income.OneResRealtimeIncomeBean
import com.lemon.mcdevmanager.data.netease.resource.ResDetailResponseBean
import com.lemon.mcdevmanager.data.netease.resource.ResMonthDetailResponseBean
import com.lemon.mcdevmanager.data.netease.resource.ResourceResponseBean
import com.lemon.mcdevmanager.utils.ResponseData
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface AnalyzeApi {
    @GET("/items/categories/{platform}/")
    suspend fun getAllResource(
        @Path("platform") platform: String = "pe",
        @Query("start") start: Int = 0,
        @Query("span") span: Int = Int.MAX_VALUE
    ): ResponseData<ResourceResponseBean>

    @GET("/data_analysis/day_detail/")
    suspend fun getDayDetail(
        @Query("platform") platform: String,
        @Query("category") category: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("item_list_str") itemListStr: String,
        @Query("sort") sort: String = "dateid",
        @Query("order") order: String = "DESC",
        @Query("start") start: Int = 0,
        @Query("span") span: Int = Int.MAX_VALUE
    ): ResponseData<ResDetailResponseBean>

    @GET("/data_analysis/month_detail/")
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
    ): ResponseData<ResMonthDetailResponseBean>

    @GET("/items/categories/{platform}/{iid}/incomes/")
    suspend fun getOneResRealtimeIncome(
        @Path("platform") platform: String,
        @Path("iid") iid: String,
        @Query("begin_time") beginTime: String,
        @Query("end_time") endTime: String
    ): ResponseData<OneResRealtimeIncomeBean>

    companion object {
        /**
         * 获取接口实例用于调用对接方法
         * @return ServerApi
         */
        fun create(): AnalyzeApi {
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(AddCookiesInterceptor())
                .addInterceptor(CommonInterceptor())
                .build()
            return Retrofit.Builder()
                .baseUrl(NETEASE_MC_DEV_LINK)
                .addConverterFactory(
                    JSONConverter.asConverterFactory(
                        "application/json; charset=UTF8".toMediaTypeOrNull()!!
                    )
                )
                .client(client)
                .build()
                .create(AnalyzeApi::class.java)
        }
    }
}