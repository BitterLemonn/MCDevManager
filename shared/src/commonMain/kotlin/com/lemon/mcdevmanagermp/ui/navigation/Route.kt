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
    data object Feedback : Route

    @Serializable
    data object Comment : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object IncomeDetail : Route
}