package com.lemon.mcdevmanagermp.domain.income

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.common.NoNeedData
import com.lemon.mcdevmanagermp.data.dto.netease.income.ApplyIncomeDTO
import com.lemon.mcdevmanagermp.data.dto.netease.income.IncentiveListDTO
import com.lemon.mcdevmanagermp.data.vo.netease.income.ApplyIncomeDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.income.IncomeDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.user.UserInfoVO

interface IncomeRepository {
    suspend fun getIncome(platform: String = "pe"): NetworkState<IncomeDetailVO>
    suspend fun getApplyDetail(id: String): NetworkState<ApplyIncomeDetailVO>
    suspend fun applyIncome(request: ApplyIncomeDTO): NetworkState<NoNeedData>
    suspend fun getIncentiveFund(platform: String = "pe"): NetworkState<IncentiveListDTO>
    suspend fun getUserInfo(): NetworkState<UserInfoVO>
}
