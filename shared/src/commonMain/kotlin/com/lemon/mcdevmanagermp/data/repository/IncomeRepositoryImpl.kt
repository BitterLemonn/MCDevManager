package com.lemon.mcdevmanagermp.data.repository

import com.lemon.mcdevmanagermp.data.api.IncomeApi
import com.lemon.mcdevmanagermp.data.api.InfoApi
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.common.NoNeedData
import com.lemon.mcdevmanagermp.data.dto.netease.income.ApplyIncomeDTO
import com.lemon.mcdevmanagermp.data.dto.netease.income.IncentiveListDTO
import com.lemon.mcdevmanagermp.data.vo.netease.income.ApplyIncomeDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.income.IncomeDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.user.UserInfoVO
import com.lemon.mcdevmanagermp.domain.income.IncomeRepository
import com.lemon.mcdevmanagermp.utils.UnifiedExceptionHandler

class IncomeRepositoryImpl : IncomeRepository {
    companion object {
        val INSTANCE by lazy { IncomeRepositoryImpl() }
        private val incomeApi = IncomeApi.INSTANCE
        private val infoApi = InfoApi.INSTANCE
    }

    override suspend fun getIncome(platform: String): NetworkState<IncomeDetailVO> {
        return UnifiedExceptionHandler.handleRequest { incomeApi.getIncome(platform) }
    }

    override suspend fun getApplyDetail(id: String): NetworkState<ApplyIncomeDetailVO> {
        return UnifiedExceptionHandler.handleRequest { incomeApi.getApplyDetail(id) }
    }

    override suspend fun applyIncome(request: ApplyIncomeDTO): NetworkState<NoNeedData> {
        return UnifiedExceptionHandler.handleRequest { incomeApi.applyIncome(request) }
    }

    override suspend fun getIncentiveFund(platform: String): NetworkState<IncentiveListDTO> {
        return UnifiedExceptionHandler.handleRequest { incomeApi.getIncentiveFund(platform) }
    }

    override suspend fun getUserInfo(): NetworkState<UserInfoVO> {
        return UnifiedExceptionHandler.handleRequest { infoApi.getUserInfo() }
    }
}
