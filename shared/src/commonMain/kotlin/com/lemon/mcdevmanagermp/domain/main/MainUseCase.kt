package com.lemon.mcdevmanagermp.domain.main

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.consts.CookiesExpiredException
import com.lemon.mcdevmanagermp.data.consts.LoginException
import com.lemon.mcdevmanagermp.data.vo.netease.user.LevelInfoVO
import com.lemon.mcdevmanagermp.data.vo.netease.user.OverviewVO
import com.lemon.mcdevmanagermp.data.vo.netease.user.UserInfoVO
import com.lemon.mcdevmanagermp.domain.resource.ResourceRepository
import com.lemon.mcdevmanagermp.domain.user.UserRepository
import com.lemon.mcdevmanagermp.utils.ProfitData
import com.lemon.mcdevmanagermp.utils.calculateProfit
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus

data class MainDashboardData(
    val userInfo: NetworkState<UserInfoVO>,
    val overview: NetworkState<OverviewVO>,
    val levelInfo: NetworkState<LevelInfoVO>
)

data class ProfitResult(
    val thisMonth: ProfitData,
    val lastMonth: ProfitData
)

class MainUseCase(
    private val userRepository: UserRepository,
    private val resourceRepository: ResourceRepository
) {
    suspend fun loadDashboard(): MainDashboardData = coroutineScope {
        val userInfoDeferred = async { userRepository.getUserInfo() }
        val overviewDeferred = async { userRepository.getOverview() }
        val levelDeferred = async { userRepository.getLevelInfo() }

        MainDashboardData(
            userInfo = userInfoDeferred.await(),
            overview = overviewDeferred.await(),
            levelInfo = levelDeferred.await()
        )
    }

    suspend fun computeProfit(year: Int, month: Int): ProfitResult = coroutineScope {
        val thisMonthDiamonds = getOneMonthComponentDiamonds(year, month)
        val lastMonthDate = LocalDate(year, month, 1).minus(1, DateTimeUnit.MONTH)
        val lastMonthDiamonds = getOneMonthComponentDiamonds(
            lastMonthDate.year,
            lastMonthDate.month.number
        )

        ProfitResult(
            thisMonth = calculateProfit(thisMonthDiamonds),
            lastMonth = calculateProfit(lastMonthDiamonds)
        )
    }

    private suspend fun getOneMonthComponentDiamonds(year: Int, month: Int): Map<String, Double> =
        coroutineScope {
            val resources = resourceRepository.getAllResources()
            val resList = if (resources is NetworkState.Success) resources.data?.item
                ?: emptyList() else emptyList()

            val dateRange = monthDateRange(year, month)

            resList.map { res ->
                async {
                    val result = resourceRepository.getDayDetail(
                        platform = "pe",
                        category = "pe",
                        startDate = dateRange.first,
                        endDate = dateRange.second,
                        itemListStr = res.itemId
                    )
                    if (result is NetworkState.Success) {
                        res.itemName to (result.data?.data?.sumOf { it.diamond * (1 - it.refundRate) }
                            ?: 0.0)
                    } else {
                        res.itemName to 0.0
                    }
                }
            }.associate { it.await() }
        }

    private fun monthDateRange(year: Int, month: Int): Pair<String, String> {
        val firstDay = LocalDate(year, month, 1)
        val endDate = firstDay.plus(1, DateTimeUnit.MONTH).minus(10, DateTimeUnit.DAY)
        val startDate = firstDay.minus(9, DateTimeUnit.DAY)
        return startDate.toString().replace("-", "") to endDate.toString().replace("-", "")
    }

    fun isSessionExpired(vararg states: NetworkState<*>): Boolean {
        return states.any { state ->
            state is NetworkState.Error &&
                    (state.e is CookiesExpiredException || state.e is LoginException)
        }
    }
}
