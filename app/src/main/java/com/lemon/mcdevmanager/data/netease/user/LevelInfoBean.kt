package com.lemon.mcdevmanager.data.netease.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LevelInfoBean(
    @SerialName("current_class")
    val currentClass: Int,
    @SerialName("current_level")
    val currentLevel: Int,
    @SerialName("exp_ceiling")
    val expCeiling: Double,
    @SerialName("exp_floor")
    val expFloor: Double,
    @SerialName("total_exp")
    val totalExp: Double,
    @SerialName("upgrade_class_achieve")
    val upgradeClassAchieve: Boolean,
    @SerialName("contribution_month")
    val contributionMonth: String,
    @SerialName("contribution_netgame_class")
    val contributionNetGameClass: Int,
    @SerialName("contribution_netgame_rank")
    val contributionNetGameRank: Int,
    @SerialName("contribution_netgame_score")
    val contributionNetGameScore: String,
    @SerialName("contribution_class")
    val contributionClass: Int,
    @SerialName("contribution_rank")
    val contributionRank: Int,
    @SerialName("contribution_score")
    val contributionScore: String
)