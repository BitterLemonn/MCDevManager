package com.lemon.mcdevmanagermp.ui.pages.update

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.repository.UpdateRepositoryImpl
import com.lemon.mcdevmanagermp.domain.update.CheckUpdateResult
import com.lemon.mcdevmanagermp.domain.update.CheckUpdateUseCase
import com.lemon.mcdevmanagermp.platform.AppUpdateManager
import com.lemon.mcdevmanagermp.platform.DownloadNotifier
import com.lemon.mcdevmanagermp.platform.UpdatePreferences
import com.lemon.mcdevmanagermp.platform.UpdateStrategy
import com.lemon.mcdevmanagermp.platform.restartApp
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class UpdateViewModel : BaseViewModel<UpdateState, UpdateAction, UpdateEffect>(UpdateState()) {

    companion object {
        /** 本次会话中用户是否已跳过更新，防止导航返回后重复弹出 */
        var sessionDismissed = false
            private set
    }

    private val checkUpdateUseCase = CheckUpdateUseCase(UpdateRepositoryImpl.INSTANCE)
    private val updateManager = AppUpdateManager()
    private val notifier = DownloadNotifier()
    private val updatePreferences = UpdatePreferences()
    private var downloadJob: Job? = null
    private var downloadedFilePath: String? = null
    private var lastProgressMark: TimeMark? = null
    private var lastProgressBytes = 0L

    override fun dispatch(action: UpdateAction) {
        when (action) {
            is UpdateAction.CheckUpdate -> checkUpdate(action.isManual)
            UpdateAction.DismissDialog -> dismissDialog()
            UpdateAction.IgnoreVersion -> ignoreVersion()
            UpdateAction.StartDownload -> startDownload()
            UpdateAction.InstallUpdate -> installUpdate()
            UpdateAction.RestartApp -> restartApp()
            UpdateAction.OpenInBrowser -> openInBrowser()
        }
    }

    private fun checkUpdate(isManual: Boolean = false) {
        // 自动检查时，若本次会话已跳过更新，不再弹出
        if (!isManual && sessionDismissed) return

        // 手动检查时立即显示 dialog，自动检查时静默进行
        setState { copy(isChecking = true, showDialog = isManual, errorMessage = null) }
        viewModelScope.launch {
            when (val result = checkUpdateUseCase()) {
                is CheckUpdateResult.UpdateAvailable -> {
                    // 检查是否已被忽略
                    val ignored = updatePreferences.getIgnoredVersion()
                    if (ignored != null && ignored == result.latestVersion) {
                        setState { copy(isChecking = false, showDialog = false) }
                    } else {
                        setState {
                            copy(
                                isChecking = false,
                                checkResult = result,
                                showDialog = true
                            )
                        }
                    }
                }

                is CheckUpdateResult.UpToDate -> {
                    setState { copy(isChecking = false, showDialog = false) }
                    // 仅手动检查时提示已是最新版本，自动检查保持静默
                    if (isManual) {
                        sendEffect(UpdateEffect.ShowToast("已是最新版本 (v${result.version})"))
                    }
                }

                is CheckUpdateResult.Error -> {
                    setState {
                        copy(isChecking = false, showDialog = false, errorMessage = result.message)
                    }
                    // 仅手动检查时提示错误，自动检查保持静默
                    if (isManual) {
                        sendEffect(UpdateEffect.ShowToast(result.message))
                    }
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

        setState { copy(isDownloading = true, downloadProgress = 0f, downloadSpeedBps = 0L) }
        lastProgressMark = null
        lastProgressBytes = 0L
        notifier.startNotification("MCDevManagerMPR")

        downloadJob = viewModelScope.launch {
            updateManager.downloadFile(
                downloadUrl = result.downloadUrl,
                fileName = result.fileName,
                onProgress = { progress ->
                    val fileSize = result.fileSize
                    val currentBytes = (fileSize * progress).toLong()
                    val mark = TimeSource.Monotonic.markNow()
                    val speed = lastProgressMark?.let { prevMark ->
                        val elapsedMs = prevMark.elapsedNow().inWholeMilliseconds
                        if (elapsedMs > 0) (currentBytes - lastProgressBytes) * 1000L / elapsedMs else 0L
                    } ?: 0L
                    lastProgressMark = mark
                    lastProgressBytes = currentBytes
                    setState { copy(downloadProgress = progress, downloadSpeedBps = speed) }
                    notifier.updateProgress(
                        (progress * 100).toInt(),
                        currentBytes,
                        fileSize
                    )
                }
            ).onSuccess { filePath ->
                downloadedFilePath = filePath
                setState {
                    copy(
                        isDownloading = false,
                        downloadProgress = 1f,
                        downloadSpeedBps = 0L
                    )
                }
                notifier.finishNotification(true, filePath)
                dispatch(UpdateAction.InstallUpdate)
            }.onFailure { e ->
                setState { copy(isDownloading = false, downloadSpeedBps = 0L) }
                notifier.finishNotification(false, null)
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
        notifier.cancelNotification()
        sessionDismissed = true
        setState { copy(showDialog = false) }
    }

    private fun ignoreVersion() {
        val result = state.value.checkResult as? CheckUpdateResult.UpdateAvailable ?: return
        updatePreferences.setIgnoredVersion(result.latestVersion)
        dismissDialog()
    }
}
