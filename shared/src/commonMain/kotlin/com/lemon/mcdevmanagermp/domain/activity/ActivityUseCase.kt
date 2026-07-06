package com.lemon.mcdevmanagermp.domain.activity

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.dto.netease.activity.CancelJoinDiscountDTO
import com.lemon.mcdevmanagermp.data.dto.netease.activity.JoinActivityDTO
import com.lemon.mcdevmanagermp.data.dto.netease.activity.JoinDiscountDTO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ActivityCandidatesItemVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ActivityItemVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ActivityReviewItemVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ActivityReviewModuleVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.DiscountActivityVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.DiscountCandidatesItemVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.DiscountItemVO

class ActivityUseCase(
    private val activityRepository: ActivityRepository
) {

    suspend fun loadActivities(start: Int, span: Int = 10): ActivityLoadResult {
        return when (val result = activityRepository.getReviewActivity(start, span)) {
            is NetworkState.Success -> {
                val data = result.data
                ActivityLoadResult(
                    activities = data?.data ?: emptyList(),
                    totalCount = data?.count ?: 0,
                    error = null
                )
            }

            is NetworkState.Error -> ActivityLoadResult(
                activities = emptyList(),
                totalCount = 0,
                error = result.msg
            )
        }
    }

    suspend fun loadActivityModules(
        activityId: String,
        modules: List<ActivityReviewModuleVO>
    ): ActivityModulesResult {
        val candidatesMap =
            mutableMapOf<Int, List<ActivityCandidatesItemVO>>()
        val itemsMap =
            mutableMapOf<Int, List<ActivityItemVO>>()
        val errors = mutableListOf<String>()

        for (module in modules) {
            val moduleIdStr = module.moduleId.toString()

            when (val candidatesResult =
                activityRepository.getActivityCandidates(activityId, moduleIdStr)) {
                is NetworkState.Success -> {
                    candidatesMap[module.moduleId] = candidatesResult.data?.items ?: emptyList()
                }

                is NetworkState.Error -> errors.add("候选模组加载失败: ${candidatesResult.msg}")
            }

            when (val itemsResult = activityRepository.getActivityItems(activityId, moduleIdStr)) {
                is NetworkState.Success -> {
                    itemsMap[module.moduleId] = itemsResult.data?.items ?: emptyList()
                }

                is NetworkState.Error -> errors.add("参与模组加载失败: ${itemsResult.msg}")
            }
        }

        return ActivityModulesResult(
            candidatesMap = candidatesMap,
            itemsMap = itemsMap,
            errors = errors
        )
    }

    suspend fun loadModuleCandidates(
        activityId: String,
        moduleId: Int
    ): Pair<List<ActivityCandidatesItemVO>, String?> {
        return when (val result =
            activityRepository.getActivityCandidates(activityId, moduleId.toString())) {
            is NetworkState.Success -> (result.data?.items ?: emptyList()) to null
            is NetworkState.Error -> emptyList<ActivityCandidatesItemVO>() to result.msg
        }
    }

    suspend fun joinActivity(
        activityId: String,
        moduleId: Int,
        content: JoinActivityDTO
    ): String? {
        return when (val result =
            activityRepository.joinActivity(activityId, moduleId.toString(), content)) {
            is NetworkState.Success -> null
            is NetworkState.Error -> result.msg
        }
    }

    suspend fun loadDiscountActivity(): DiscountActivityResult {
        return when (val result = activityRepository.getDiscountActivity()) {
            is NetworkState.Success -> DiscountActivityResult(
                activity = result.data,
                error = null
            )

            is NetworkState.Error -> DiscountActivityResult(activity = null, error = result.msg)
        }
    }

    suspend fun loadDiscountModuleData(
        activityId: String,
        moduleId: String
    ): DiscountModuleDataResult {
        val candidatesResult = activityRepository.getDiscountCandidates(activityId, moduleId)
        val joinedResult = activityRepository.getDiscountJoinedItems(moduleId)

        val candidates = when (candidatesResult) {
            is NetworkState.Success -> candidatesResult.data?.items ?: emptyList()
            is NetworkState.Error -> emptyList()
        }
        val joinedItems = when (joinedResult) {
            is NetworkState.Success -> joinedResult.data?.items ?: emptyList()

            is NetworkState.Error -> emptyList()
        }
        val error = when {
            candidatesResult is NetworkState.Error -> candidatesResult.msg
            joinedResult is NetworkState.Error -> joinedResult.msg
            else -> null
        }

        return DiscountModuleDataResult(
            candidates = candidates,
            joinedItems = joinedItems,
            error = error
        )
    }

    suspend fun joinDiscount(
        activityId: String,
        moduleId: String,
        itemIdList: List<String>,
        discount: Int,
        partitionId: String,
        intro: String
    ): String? {
        return when (val result = activityRepository.joinDiscount(
            JoinDiscountDTO(
                activityId = activityId,
                moduleId = moduleId,
                itemIdList = itemIdList,
                discount = discount,
                partitionId = partitionId,
                intro = intro
            )
        )) {
            is NetworkState.Success -> null
            is NetworkState.Error -> result.msg
        }
    }

    suspend fun cancelDiscountJoin(activityId: String, itemId: String): String? {
        return when (val result = activityRepository.cancelDiscountJoin(
            CancelJoinDiscountDTO(activityId = activityId, itemId = itemId)
        )) {
            is NetworkState.Success -> null
            is NetworkState.Error -> result.msg
        }
    }
}

data class ActivityLoadResult(
    val activities: List<ActivityReviewItemVO>,
    val totalCount: Int,
    val error: String?
)

data class ActivityModulesResult(
    val candidatesMap: Map<Int, List<ActivityCandidatesItemVO>>,
    val itemsMap: Map<Int, List<ActivityItemVO>>,
    val errors: List<String>
)

data class DiscountActivityResult(
    val activity: DiscountActivityVO?,
    val error: String?
)

data class DiscountModuleDataResult(
    val candidates: List<DiscountCandidatesItemVO>,
    val joinedItems: List<DiscountItemVO>,
    val error: String?
)
