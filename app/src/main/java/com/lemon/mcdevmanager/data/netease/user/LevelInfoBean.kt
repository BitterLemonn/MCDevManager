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
    val upgradeClassAchieve: Boolean
)