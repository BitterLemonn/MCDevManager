package com.lemon.mcdevmanagermp.data.api

import com.lemon.mcdevmanagermp.data.common.NoNeedData
import com.lemon.mcdevmanagermp.data.common.ResponseData
import com.lemon.mcdevmanagermp.data.consts.NETEASE_MC_DEV_LINK
import com.lemon.mcdevmanagermp.data.dto.netease.income.ApplyIncomeDTO
import com.lemon.mcdevmanagermp.data.dto.netease.income.IncentiveListDTO
import com.lemon.mcdevmanagermp.data.vo.netease.income.ApplyIncomeDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.income.IncomeDetailVO
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

interface IncomeApi {

    // 结算收益
    @PUT("/incomes/apply")
    suspend fun applyIncome(
        @Body request: ApplyIncomeDTO
    ): ResponseData<NoNeedData>

    // 获取结算信息
    @GET("/incomes")
    suspend fun getIncome(
        @Query("platform") platform: String = "pe",
        @Query("start") start: Int = 0,
        @Query("span") span: Int = Int.MAX_VALUE
    ): ResponseData<IncomeDetailVO>

    // 获取结算详情
    @GET("/incomes/{id}")
    suspend fun getApplyDetail(
        @Path("id") id: String
    ): ResponseData<ApplyIncomeDetailVO>

    // 获取激励金
    @GET("/incentive_fund/detail")
    suspend fun getIncentiveFund(
        @Query("platform") platform: String = "pe",
        @Query("start") start: Int = 0,
        @Query("span") span: Int = Int.MAX_VALUE
    ): ResponseData<IncentiveListDTO>

    companion object {
        val INSTANCE by lazy {
            ApiFactory.provideLoggerKtorfit(NETEASE_MC_DEV_LINK).createIncomeApi()
        }
    }
}