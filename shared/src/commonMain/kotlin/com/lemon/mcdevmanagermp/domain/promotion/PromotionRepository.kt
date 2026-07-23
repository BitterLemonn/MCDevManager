package com.lemon.mcdevmanagermp.domain.promotion

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.common.NoNeedData
import com.lemon.mcdevmanagermp.data.dto.netease.promotion.ApplyPromotionDTO
import com.lemon.mcdevmanagermp.data.vo.netease.promotion.CanApplyPromotionVO
import com.lemon.mcdevmanagermp.data.vo.netease.promotion.UserApplyVO

interface PromotionRepository {
    suspend fun getCanApplyPromotion(): NetworkState<CanApplyPromotionVO>
    suspend fun applyPromotion(content: ApplyPromotionDTO): NetworkState<NoNeedData>

    suspend fun modifyApplyPromotion(
        applicationId: String,
        content: ApplyPromotionDTO
    ): NetworkState<NoNeedData>

    suspend fun getUserApply(
        start: Int = 0,
        span: Int = 10,
        platform: String = "pe",
        type: String = "apply"
    ): NetworkState<UserApplyVO>
}
