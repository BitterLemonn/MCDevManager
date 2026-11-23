package com.lemon.mcdevmanagermp.domain.analyze

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.dto.netease.income.LobbyGoodVO
import com.lemon.mcdevmanagermp.data.dto.netease.income.LobbyIncomeResourceVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * @property owner 商品所属模组
 * @property goods 模组下的商品，null 表示商品列表请求失败
 */
data class LobbyGoodsByOwner(
    val owner: LobbyIncomeResourceVO,
    val goods: List<LobbyGoodVO>?
)

/**
 * 商品化模组商品 UseCase：把「模组」展开到「模组下的商品」。
 *
 * 商品化模组（玩法组件）本身是资源作品，但销售数据挂在作品下的各个商品上；
 * 而数据分析的商品口径接口（`data_analysis/goods/` 下的日、月详情，`mc_type=1`）
 * 只接受商品 ID。作品 ID 与商品 ID 属于不同域，不可直接互换传参。
 */
class LobbyGoodsUseCase(
    private val analyzeRepository: AnalyzeRepository
) {
    /**
     * 获取每个商品化模组下的商品。模组列表获取失败时返回 Error，单个模组的商品列表
     * 获取失败时保留该模组及其失败状态，其余模组仍返回各自的结果。
     */
    suspend fun getGoodsByOwner(): NetworkState<List<LobbyGoodsByOwner>> {
        val owners = when (val result = analyzeRepository.getLobbyIncomeResources()) {
            is NetworkState.Success -> result.data?.items.orEmpty()
            is NetworkState.Error -> return NetworkState.Error(result.msg, result.e)
        }

        val results = coroutineScope {
            owners.map { owner ->
                async {
                    val goods = when (val result = analyzeRepository.getLobbyGoodsList(owner.itemId)) {
                        is NetworkState.Success -> result.data?.goods.orEmpty()
                            .filter { it.goodsId.isNotBlank() }

                        is NetworkState.Error -> null
                    }
                    LobbyGoodsByOwner(owner, goods)
                }
            }.awaitAll()
        }

        return NetworkState.Success(results)
    }

    /** 展开成扁平的商品列表（供以商品为查询单位的页面使用）。 */
    suspend fun getLobbyGoodsResources(): NetworkState<List<ResourceData>> =
        when (val result = getGoodsByOwner()) {
            is NetworkState.Success -> NetworkState.Success(
                result.data.orEmpty().flatMap { it.goods.orEmpty() }
                    .distinctBy { it.goodsId }
                    .map { ResourceData(itemId = it.goodsId, itemName = it.name) }
            )

            is NetworkState.Error -> NetworkState.Error(result.msg, result.e)
        }
}
