package com.lemon.mcdevmanager.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanager.BuildConfig
import com.lemon.mcdevmanager.data.github.update.LatestReleaseBean
import com.lemon.mcdevmanager.data.repository.UpdateRepository
import com.lemon.mcdevmanager.ui.widget.SNACK_ERROR
import com.lemon.mcdevmanager.ui.widget.SNACK_INFO
import com.lemon.mcdevmanager.utils.NetworkState
import com.zj.mvi.core.SharedFlowEvents
import com.zj.mvi.core.setEvent
import com.zj.mvi.core.setState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class AboutViewModel : ViewModel() {
    private val repository = UpdateRepository.getInstance()
    private val _viewStates = MutableStateFlow(AboutViewStates())
    val viewStates = _viewStates.asStateFlow()
    private val _viewEvents = SharedFlowEvents<AboutViewEvents>()
    val viewEvents = _viewEvents.asSharedFlow()

    fun dispatch(action: AboutViewActions) {
        when (action) {
            is AboutViewActions.DownloadAsset -> downloadAsset()
            is AboutViewActions.CheckUpdate -> checkUpdate()
        }
    }

    private fun checkUpdate() {
        viewModelScope.launch {
            flow<Unit> {
                when (val result = repository.getLatestRelease()) {
                    is NetworkState.Success -> {
                        val hasNewVersion = result.data?.let {
                            BuildConfig.VERSION_NAME != it.tagName.replace("v", "")
                        } ?: false
                        _viewStates.setState { copy(latestBean = result.data) }
                        if (hasNewVersion) _viewEvents.setEvent(AboutViewEvents.ShowNewVersionDialog)
                        else _viewEvents.setEvent(
                            AboutViewEvents.ShowToast("当前已是最新版本", SNACK_INFO)
                        )
                    }

                    is NetworkState.Error -> {
                        _viewEvents.setEvent(
                            AboutViewEvents.ShowToast(
                                "检查更新失败: ${result.msg}",
                                SNACK_ERROR
                            )
                        )
                    }
                }
            }.onStart {
                _viewStates.setState { copy(isLoading = true) }
            }.onCompletion {
                _viewStates.setState { copy(isLoading = false) }
            }.catch { e ->
                _viewStates.setState { copy(isLoading = false) }
                _viewEvents.setEvent(
                    AboutViewEvents.ShowToast(
                        "检查更新失败: ${e.message ?: "未知错误, 请联系管理员"}",
                        SNACK_ERROR
                    )
                )
            }.collect()
        }
    }

    private fun downloadAsset() {
        viewModelScope.launch {
            flow<Unit> {
                val latestBean = _viewStates.value.latestBean
                if (latestBean == null) {
                    _viewEvents.setEvent(AboutViewEvents.ShowToast("无法获取更新地址", SNACK_ERROR))
                    return@flow
                }
                val downloadUrl = latestBean.assets.firstOrNull()?.url
                if (downloadUrl == null) {
                    _viewEvents.setEvent(AboutViewEvents.ShowToast("无法获取更新地址", SNACK_ERROR))
                    return@flow
                }
                _viewEvents.setEvent(AboutViewEvents.DownloadStart(downloadUrl))
            }.catch { e ->
                _viewEvents.setEvent(
                    AboutViewEvents.DownloadFailed(e.message ?: "未知错误, 请联系管理员")
                )
            }.collect()
        }
    }
}

data class AboutViewStates(
    val downloadProgress: Int = 0,
    val latestBean: LatestReleaseBean? = null,
    val isLoading: Boolean = false
)

sealed class AboutViewActions {
    data object DownloadAsset : AboutViewActions()
    data object CheckUpdate : AboutViewActions()
}

sealed class AboutViewEvents {
    data class DownloadStart(val downloadUrl: String) : AboutViewEvents()
    data class DownloadFailed(val msg: String) : AboutViewEvents()
    data class ShowToast(val msg: String, val type: String) : AboutViewEvents()
    data object ShowNewVersionDialog : AboutViewEvents()
}