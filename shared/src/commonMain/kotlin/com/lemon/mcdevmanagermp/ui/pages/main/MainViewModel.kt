package com.lemon.mcdevmanagermp.ui.pages.main

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.repository.UserRepositoryImpl
import com.lemon.mcdevmanagermp.domain.main.MainUseCase
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import kotlinx.coroutines.launch

class MainViewModel : BaseViewModel<MainState, MainAction, MainEffect>(MainState()) {

    private val mainUseCase = MainUseCase(
        userRepository = UserRepositoryImpl.INSTANCE
    )

    init {
        loadDashboard()
    }

    override fun dispatch(action: MainAction) {
        when (action) {
            is MainAction.SelectTab -> setState { copy(selectedTab = action.tab) }
            MainAction.LoadData -> loadDashboard()
            MainAction.RefreshData -> loadDashboard()
            MainAction.ToggleDrawer -> setState { copy(showDrawer = !showDrawer) }
            MainAction.DismissTips -> setState { copy(tipsDismissed = true) }
        }
    }

    private fun loadDashboard() {
        setState { copy(isRefreshing = true) }
        viewModelScope.launch {
            try {
                val result = mainUseCase.loadDashboard()
                setState {
                    copy(
                        userInfo = result.userInfo,
                        overview = result.overview,
                        levelInfo = result.levelInfo,
                        isRefreshing = false
                    )
                }
                if (mainUseCase.isSessionExpired(result.userInfo, result.overview, result.levelInfo)) {
                    sendEffect(MainEffect.SessionExpired)
                }
            } catch (_: Exception) {
                setState { copy(isRefreshing = false) }
                sendEffect(MainEffect.ShowToast("数据加载失败"))
            }
        }
    }
}
