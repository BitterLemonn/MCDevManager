package com.lemon.mcdevmanagermp.domain.analyze

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.dto.netease.income.LobbyGoodVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * 联机大厅商品 UseCase：把「作品」展开到「作品下的商品」。
 *
 * 联机大厅作品（地图）本身不产生收益，销售数据挂在作品下的各个商品上；
 * 而数据分析的商品口径接口（`data_analysis/goods/` 下的日、月详情，`mc_type=1`）
 * 只接受商品 ID。作品 ID 与商品 ID 属于不同域，不可直接互换传参。
 */
class LobbyGoodsUseCase(
    private val analyzeRepository: AnalyzeRepository
) {
    /**
     * 获取每个联机大厅作品下的商品：作品名 → 商品列表。
     *
     * 作品列表获取失败时返回 Error；单个作品的商品获取失败时，该作品记为无商品，
     * 不阻断其余作品（部分失败优于整体失败）。
     */
    suspend fun getGoodsByOwner(): NetworkState<Map<String, List<LobbyGoodVO>>> {
        val owners = when (val result = analyzeRepository.getLobbyIncomeResources()) {
            is NetworkState.Success -> result.data?.items.orEmpty()
            is NetworkState.Error -> return NetworkState.Error(result.msg, result.e)
        }

        val goodsByOwner = coroutineScope {
            owners.map { owner ->
                async {
                    val goods = when (val result = analyzeRepository.getLobbyGoodsList(owner.itemId)) {
                        is NetworkState.Success -> result.data?.goods.orEmpty()
                        is NetworkState.Error -> emptyList()
                    }
                    owner.itemName to goods.filter { it.goodsId.isNotBlank() }
                }
            }.awaitAll()
        }.toMap()

        return NetworkState.Success(goodsByOwner)
    }

    /** 展开成扁平的商品列表（供以商品为查询单位的页面使用）。 */
    suspend fun getLobbyGoodsResources(): NetworkState<List<ResourceData>> =
        when (val result = getGoodsByOwner()) {
            is NetworkState.Success -> NetworkState.Success(
                result.data.orEmpty().values.flatten()
                    .distinctBy { it.goodsId }
                    .map { ResourceData(itemId = it.goodsId, itemName = it.name) }
            )

            is NetworkState.Error -> NetworkState.Error(result.msg, result.e)
        }
}
