package com.lemon.mcdevmanagermp.ui.pages.update

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.domain.update.CheckUpdateResult
import com.lemon.mcdevmanagermp.domain.update.CheckUpdateUseCase
import com.lemon.mcdevmanagermp.platform.AppUpdateManager
import com.lemon.mcdevmanagermp.platform.UpdateStrategy
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class UpdateViewModel : BaseViewModel<UpdateState, UpdateAction, UpdateEffect>(UpdateState()) {

    private val checkUpdateUseCase = CheckUpdateUseCase()
    private val updateManager = AppUpdateManager()
    private var downloadJob: Job? = null
    private var downloadedFilePath: String? = null

    override fun dispatch(action: UpdateAction) {
        when (action) {
            UpdateAction.CheckUpdate -> checkUpdate()
            UpdateAction.DismissDialog -> dismissDialog()
            UpdateAction.StartDownload -> startDownload()
            UpdateAction.InstallUpdate -> installUpdate()
            UpdateAction.RestartApp -> restartApp()
            UpdateAction.OpenInBrowser -> openInBrowser()
        }
    }

    private fun checkUpdate() {
        setState { copy(isChecking = true, showDialog = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = checkUpdateUseCase()) {
                is CheckUpdateResult.UpdateAvailable -> {
                    setState {
                        copy(
                            isChecking = false,
                            checkResult = result,
                            showDialog = true
                        )
                    }
                }

                is CheckUpdateResult.UpToDate -> {
                    setState { copy(isChecking = false, showDialog = false) }
                    sendEffect(UpdateEffect.ShowToast("已是最新版本 (v${result.version})"))
                }

                is CheckUpdateResult.Error -> {
                    setState {
                        copy(isChecking = false, showDialog = false, errorMessage = result.message)
                    }
                    sendEffect(UpdateEffect.ShowToast(result.message))
                }
            }
        }
    }

    private fun startDownload() {
        val result = state.value.checkResult as? CheckUpdateResult.UpdateAvailable ?: return
        if (result.downloadUrl.isEmpty()) {
            sendEffect(UpdateEffect.ShowToast("未找到可下载的更新包"))
            return
        }

        setState { copy(isDownloading = true, downloadProgress = 0f) }

        downloadJob = viewModelScope.launch {
            updateManager.downloadFile(
                downloadUrl = result.downloadUrl,
                fileName = result.fileName,
                onProgress = { progress ->
                    setState { copy(downloadProgress = progress) }
                }
            ).onSuccess { filePath ->
                downloadedFilePath = filePath
                setState { copy(isDownloading = false, downloadProgress = 1f) }
                dispatch(UpdateAction.InstallUpdate)
            }.onFailure { e ->
                setState { copy(isDownloading = false) }
                sendEffect(UpdateEffect.ShowToast("下载失败: ${e.message}"))
            }
        }
    }

    private fun installUpdate() {
        val filePath = downloadedFilePath ?: return
        val strategy = updateManager.getPlatformUpdateStrategy()

        when (strategy) {
            UpdateStrategy.DOWNLOAD_AND_INSTALL -> {
                viewModelScope.launch {
                    updateManager.installUpdate(filePath)
                        .onFailure { e -> sendEffect(UpdateEffect.ShowToast("安装失败: ${e.message}")) }
                    setState { copy(showDialog = false) }
                }
            }

            UpdateStrategy.DOWNLOAD_AND_PATCH -> {
                setState { copy(isPatching = true) }
                viewModelScope.launch {
                    updateManager.installUpdate(filePath).onSuccess {
                        setState { copy(isPatching = false, patchComplete = true) }
                    }.onFailure { e ->
                        setState { copy(isPatching = false) }
                        sendEffect(UpdateEffect.ShowToast("更新失败: ${e.message}"))
                    }
                }
            }

            UpdateStrategy.OPEN_BROWSER -> {
                sendEffect(UpdateEffect.ShowToast("请前往 GitHub 下载更新"))
            }
        }
    }

    private fun restartApp() {
        try {
            val jarPath =
                this::class.java.protectionDomain?.codeSource?.location?.toURI()?.path ?: return
            Runtime.getRuntime().exec(arrayOf("java", "-jar", jarPath))
        } catch (_: Exception) {
            // 如果无法启动新进程，直接退出让用户手动重启
        }
        kotlin.system.exitProcess(0)
    }

    private fun openInBrowser() {
        val result = state.value.checkResult as? CheckUpdateResult.UpdateAvailable ?: return
        if (result.downloadUrl.isNotEmpty()) {
            sendEffect(UpdateEffect.OpenUrl(result.downloadUrl))
        } else {
            sendEffect(UpdateEffect.OpenUrl("https://github.com/BitterLemonn/MCDevManager/releases/latest"))
        }
    }

    private fun dismissDialog() {
        downloadJob?.cancel()
        setState { copy(showDialog = false) }
    }
}
