package com.lemon.mcdevmanagermp.domain.promotion

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.dto.netease.promotion.ApplyPromotionDTO
import com.lemon.mcdevmanagermp.data.vo.netease.promotion.UserApplyVO

class PromotionUseCase(
    private val promotionRepository: PromotionRepository
) {

    /**
     * 查询当前账号各日期是否可申请（permit 的 key 为 Unix 秒日期）。
     */
    suspend fun loadCanApply(): CanApplyResult {
        return when (val result = promotionRepository.getCanApplyPromotion()) {
            is NetworkState.Success -> CanApplyResult(
                permit = result.data?.permit ?: emptyMap(),
                reason = result.data?.reason ?: emptyMap(),
                recentRecords = result.data?.recentRecords ?: emptyMap(),
                error = null
            )

            is NetworkState.Error -> CanApplyResult(error = result.msg)
        }
    }

    /**
     * 提交 PE 轮播图申请。
     *
     * 固定字段（语义以后端为准）：
     * - positionId = "6"（PE 轮播图广告位）
     * - itemCategory = "pe"
     * - specialPromotion = false
     * - startTimeZone = -8
     * - isCheck = true
     *
     * @param startTimeSeconds 开始日期 Unix 秒（来自 can_apply.permit 的 key）
     * @param peChannel 宣传图上传后的 URL
     */
    suspend fun applyPromotion(
        itemId: String,
        startTimeSeconds: Long,
        peChannel: String,
        extra: String,
        activity: String,
        feature: String,
        update: String
    ): String? {
        return when (val result = promotionRepository.applyPromotion(
            buildApplyDTO(itemId, startTimeSeconds, peChannel, extra, activity, feature, update)
        )) {
            is NetworkState.Success -> null
            is NetworkState.Error -> result.msg
        }
    }

    /**
     * 修改已提交（审核中）的轮播图申请。
     * @param applicationId 申请记录 id（UserApplyItemVO.activityId）
     */
    suspend fun modifyApplyPromotion(
        applicationId: String,
        itemId: String,
        startTimeSeconds: Long,
        peChannel: String,
        extra: String,
        activity: String,
        feature: String,
        update: String
    ): String? {
        return when (
            val result = promotionRepository.modifyApplyPromotion(
                applicationId,
                buildApplyDTO(itemId, startTimeSeconds, peChannel, extra, activity, feature, update)
            )
        ) {
            is NetworkState.Success -> null
            is NetworkState.Error -> result.msg
        }
    }

    private fun buildApplyDTO(
        itemId: String,
        startTimeSeconds: Long,
        peChannel: String,
        extra: String,
        activity: String,
        feature: String,
        update: String
    ) = ApplyPromotionDTO(
        positionId = "6",
        startTime = startTimeSeconds,
        itemId = itemId,
        itemCategory = "pe",
        extra = extra,
        activity = activity,
        feature = feature,
        update = update,
        peChannel = peChannel,
        specialPromotion = false,
        startTimeZone = -8,
        isCheck = true
    )

    /**
     * 查询当前账号已申请的轮播图记录（含审核状态）。platform 固定 pe、type 固定 apply。
     */
    suspend fun loadUserApply(
        start: Int = 0,
        span: Int = 10
    ): NetworkState<UserApplyVO> {
        return promotionRepository.getUserApply(start = start, span = span)
    }
}

data class CanApplyResult(
    val permit: Map<String, Boolean> = emptyMap(),
    val reason: Map<String, String> = emptyMap(),
    val recentRecords: Map<String, Boolean> = emptyMap(),
    val error: String?
)
