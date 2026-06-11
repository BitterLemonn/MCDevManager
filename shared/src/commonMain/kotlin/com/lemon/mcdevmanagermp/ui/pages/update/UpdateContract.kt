package com.lemon.mcdevmanagermp.ui.pages.update

import com.lemon.mcdevmanagermp.domain.update.CheckUpdateResult
import com.lemon.mcdevmanagermp.utils.extension.IUiAction
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import com.lemon.mcdevmanagermp.utils.extension.IUiState

data class UpdateState(
    val isChecking: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f,
    val downloadSpeedBps: Long = 0L,
    val isPatching: Boolean = false,
    val patchComplete: Boolean = false,
    val checkResult: CheckUpdateResult? = null,
    val errorMessage: String? = null,
    val showDialog: Boolean = false
) : IUiState

sealed interface UpdateAction : IUiAction {
    data class CheckUpdate(val isManual: Boolean = false) : UpdateAction
    data object DismissDialog : UpdateAction
    data object IgnoreVersion : UpdateAction
    data object StartDownload : UpdateAction
    data object InstallUpdate : UpdateAction
    data object RestartApp : UpdateAction
    data object OpenInBrowser : UpdateAction
}

sealed interface UpdateEffect : IUiEffect {
    data class ShowToast(val message: String) : UpdateEffect
    data class OpenUrl(val url: String) : UpdateEffect
}
