package com.lemon.mcdevmanagermp.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable
    data object Splash : Route

    @Serializable
    data object Login : Route

    @Serializable
    data object Main : Route

    @Serializable
    data object Analyze : Route

    @Serializable
    data object RealtimeProfit : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data class IncomeDetail(val isLastMonth: Boolean = false) : Route

    @Serializable
    data object Income : Route
}
