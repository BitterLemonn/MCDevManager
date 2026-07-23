package com.lemon.mcdevmanagermp.data.repository

import com.lemon.mcdevmanagermp.data.api.PromotionApi
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.common.NoNeedData
import com.lemon.mcdevmanagermp.data.dto.netease.promotion.ApplyPromotionDTO
import com.lemon.mcdevmanagermp.data.vo.netease.promotion.CanApplyPromotionVO
import com.lemon.mcdevmanagermp.data.vo.netease.promotion.UserApplyVO
import com.lemon.mcdevmanagermp.domain.promotion.PromotionRepository
import com.lemon.mcdevmanagermp.utils.UnifiedExceptionHandler

class PromotionRepositoryImpl : PromotionRepository {
    companion object {
        val INSTANCE by lazy { PromotionRepositoryImpl() }
        private val api = PromotionApi.INSTANCE
    }

    override suspend fun getCanApplyPromotion(): NetworkState<CanApplyPromotionVO> {
        return UnifiedExceptionHandler.handleRequest { api.getCanApplyPromotion() }
    }

    override suspend fun applyPromotion(content: ApplyPromotionDTO): NetworkState<NoNeedData> {
        return UnifiedExceptionHandler.handleRequest { api.applyPromotion(content) }
    }

    override suspend fun modifyApplyPromotion(
        applicationId: String,
        content: ApplyPromotionDTO
    ): NetworkState<NoNeedData> {
        return UnifiedExceptionHandler.handleRequest {
            api.modifyApplyPromotion(applicationId, content)
        }
    }

    override suspend fun getUserApply(
        start: Int,
        span: Int,
        platform: String,
        type: String
    ): NetworkState<UserApplyVO> {
        return UnifiedExceptionHandler.handleRequest {
            api.getUserApply(start, span, platform, type)
        }
    }
}
