package com.lemon.mcdevmanagermp.ui.pages.main

import com.lemon.mcdevmanagermp.ui.base.BaseViewModel

class MainViewModel : BaseViewModel<MainState, MainAction, MainEffect>(MainState()) {

    override fun dispatch(action: MainAction) {
        when (action) {
            is MainAction.SelectTab -> setState { copy(selectedTab = action.tab) }
        }
    }
}
