package com.lemon.mcdevmanagermp.domain.activity

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.dto.netease.activity.JoinActivityDTO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ReviewActivityItemVO

class ActivityUseCase(
    private val activityRepository: ActivityRepository
) {

    /**
     * 加载活动列表
     * @param start 起始位置
     * @param span 每页数量
     * @return 活动列表结果
     */
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

    /**
     * 加载活动详情 - 获取各 module 的候选模组和参与模组
     */
    suspend fun loadActivityModules(
        activityId: String,
        modules: List<com.lemon.mcdevmanagermp.data.vo.netease.activity.ReviewActivityModuleVO>
    ): ActivityModulesResult {
        val candidatesMap =
            mutableMapOf<Int, List<com.lemon.mcdevmanagermp.data.vo.netease.activity.CandidatesItemVO>>()
        val itemsMap =
            mutableMapOf<Int, List<com.lemon.mcdevmanagermp.data.vo.netease.activity.ActivityItemVO>>()
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

    /**
     * 加载指定 module 的候选模组列表
     */
    suspend fun loadModuleCandidates(
        activityId: String,
        moduleId: Int
    ): Pair<List<com.lemon.mcdevmanagermp.data.vo.netease.activity.CandidatesItemVO>, String?> {
        return when (val result =
            activityRepository.getActivityCandidates(activityId, moduleId.toString())) {
            is NetworkState.Success -> (result.data?.items ?: emptyList()) to null
            is NetworkState.Error -> emptyList<com.lemon.mcdevmanagermp.data.vo.netease.activity.CandidatesItemVO>() to result.msg
        }
    }

    /**
     * 参与活动
     */
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
}

data class ActivityLoadResult(
    val activities: List<ReviewActivityItemVO>,
    val totalCount: Int,
    val error: String?
)

data class ActivityModulesResult(
    val candidatesMap: Map<Int, List<com.lemon.mcdevmanagermp.data.vo.netease.activity.CandidatesItemVO>>,
    val itemsMap: Map<Int, List<com.lemon.mcdevmanagermp.data.vo.netease.activity.ActivityItemVO>>,
    val errors: List<String>
)
