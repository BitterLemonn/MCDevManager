package com.lemon.mcdevmanagermp.ui.pages.main

import com.lemon.mcdevmanagermp.utils.extension.IUiAction
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import com.lemon.mcdevmanagermp.utils.extension.IUiState

data class MainState(
    val selectedTab: MainTab = MainTab.Home
) : IUiState

sealed interface MainAction : IUiAction {
    data class SelectTab(val tab: MainTab) : MainAction
}

sealed interface MainEffect : IUiEffect
