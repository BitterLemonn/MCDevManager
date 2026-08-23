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
    data object Settings : Route

    @Serializable
    data class IncomeDetail(val monthOffset: Int = 0) : Route

    @Serializable
    data object Income : Route

    @Serializable
    data object Mailbox : Route
}
