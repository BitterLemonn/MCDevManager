package com.lemon.mcdevmanagermp.domain.income

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.common.NoNeedData
import com.lemon.mcdevmanagermp.data.dto.netease.income.ApplyIncomeDTO
import com.lemon.mcdevmanagermp.data.vo.netease.income.ApplyIncomeDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.income.IncomeVO

/**
 * 收益 UseCase：封装收益数据获取、结算申请、结算详情查询逻辑
 */
class IncomeUseCase(
    private val incomeRepository: IncomeRepository
) {
    /**
     * 加载所有平台的收益数据和用户信息
     */
    suspend fun loadAllData(): IncomeLoadResult {
        val peResult = incomeRepository.getIncome("pe")
        val pcResult = incomeRepository.getIncome("pc")
        val userInfoResult = incomeRepository.getUserInfo()

        val peList = if (peResult is NetworkState.Success) {
            peResult.data?.incomes?.sortedByDescending { it.dataMonth } ?: emptyList()
        } else emptyList()

        val pcList = if (pcResult is NetworkState.Success) {
            pcResult.data?.incomes?.sortedByDescending { it.dataMonth } ?: emptyList()
        } else emptyList()

        val unExtractedIncome = if (userInfoResult is NetworkState.Success) {
            userInfoResult.data?.unExtractIncome
        } else null

        return IncomeLoadResult(
            peList = peList,
            pcList = pcList,
            unExtractedIncome = unExtractedIncome,
            peError = if (peResult is NetworkState.Error) peResult.msg else null,
            pcError = if (pcResult is NetworkState.Error) pcResult.msg else null
        )
    }

    /**
     * 获取结算详情
     */
    suspend fun getApplyDetail(id: String): NetworkState<ApplyIncomeDetailVO> {
        return incomeRepository.getApplyDetail(id)
    }

    /**
     * 申请结算
     */
    suspend fun applyIncome(incomeIds: List<String>): NetworkState<NoNeedData> {
        return incomeRepository.applyIncome(ApplyIncomeDTO(incomeIds))
    }
}

data class IncomeLoadResult(
    val peList: List<IncomeVO>,
    val pcList: List<IncomeVO>,
    val unExtractedIncome: String?,
    val peError: String?,
    val pcError: String?
)
