package com.lemon.mcdevmanager.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanager.ui.widget.SNACK_ERROR
import com.lemon.mcdevmanager.ui.widget.SNACK_SUCCESS
import com.lemon.mcdevmanager.ui.widget.SNACK_WARN
import com.orhanobut.logger.Logger
import com.zj.mvi.core.SharedFlowEvents
import com.zj.mvi.core.setEvent
import com.zj.mvi.core.setState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class LogViewModel : ViewModel() {
    private val _viewState = MutableStateFlow(LogViewState())
    val viewState = _viewState.asStateFlow()
    private val _viewEvent = SharedFlowEvents<LogViewEvent>()
    val viewEvent = _viewEvent.asSharedFlow()

    fun dispatch(action: LogViewAction) {
        when (action) {
            is LogViewAction.DownloadOverSingle -> downloadOverSingle()
            is LogViewAction.DownloadFailSingle -> downloadOverSingle(false)
            is LogViewAction.UpdateLogDirPath -> {
                _viewState.setState { copy(logDirPath = action.path) }
            }

            is LogViewAction.LoadLogList -> loadLogList()
            is LogViewAction.SelectLog -> {
                _viewState.setState {
                    copy(selectedLogList = selectedLogList.toMutableList().apply {
                        if (action.isSelect) add(action.filename)
                        else remove(action.filename)
                    })
                }
            }

            is LogViewAction.ClearSelectedLog -> {
                _viewState.setState { copy(selectedLogList = emptyList()) }
            }

            is LogViewAction.LoadLogContent -> loadLogContent(action.filename)
            is LogViewAction.LoadMoreLogContent -> loadMoreLogContent()
            is LogViewAction.ExportLog -> exportLog()
            is LogViewAction.DeleteLog -> deleteLog()
            is LogViewAction.DeleteThreeDaysAgoLog -> deleteThreeDaysAgoLog()
        }
    }

    private fun downloadOverSingle(isSuccess: Boolean = true) {
        _viewState.setState { copy(downloadOverNum = downloadOverNum + 1) }
        if (!isSuccess) {
            _viewState.setState { copy(downloadFailNum = downloadFailNum + 1) }
        }

        if (viewState.value.downloadOverNum == viewState.value.selectedLogList.size) {
            val failedNum = viewState.value.downloadFailNum

            _viewState.setState { copy(downloadOverNum = 0, downloadFailNum = 0) }
            viewModelScope.launch {
                if (failedNum == viewState.value.selectedLogList.size) {
                    _viewEvent.setEvent(LogViewEvent.ShowToast("日志文件导出失败", SNACK_ERROR))
                } else
                    if (failedNum > 0) {
                        _viewEvent.setEvent(
                            LogViewEvent.ShowToast(
                                "日志文件导出完成, 有$failedNum 个文件导出失败, 文件已保存至下载目录MCDevManager文件夹",
                                SNACK_WARN
                            )
                        )
                    } else {
                        _viewEvent.setEvent(
                            LogViewEvent.ShowToast(
                                "日志文件导出完成, 文件已保存至下载目录MCDevManager文件夹",
                                SNACK_SUCCESS
                            )
                        )
                    }
                _viewState.setState { copy(selectedLogList = emptyList(), isShowLoading = false) }
            }
        }
    }

    private fun loadLogList() {
        viewModelScope.launch(Dispatchers.IO) {
            val logDir = File(viewState.value.logDirPath)
            if (!logDir.exists() || !logDir.isDirectory) {
                _viewEvent.setEvent(LogViewEvent.ShowToast("日志目录${logDir}不存在"))
                return@launch
            } else {
                val logList =
                    logDir.listFiles()?.filter { it.isFile }?.sortedBy { it.name }?.map { it.name }
                        ?: emptyList()
                _viewState.setState { copy(logList = logList) }
            }
        }
    }

    private fun loadLogContent(filename: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val logFile = File(viewState.value.logDirPath, filename)
            if (!logFile.exists() || !logFile.isFile) {
                _viewEvent.setEvent(LogViewEvent.ShowToast("日志文件${filename}不存在"))
                return@launch
            } else {
                try {
                    _viewState.setState {
                        copy(
                            currentLogFile = logFile,
                            logFileName = filename,
                            logContent = "",
                            isLoadingMore = true
                        )
                    }

                    val reader = BufferedReader(FileReader(logFile))
                    _viewState.setState { copy(currentReader = reader) }

                    loadMoreLogContent()
                } catch (e: Exception) {
                    _viewEvent.setEvent(LogViewEvent.ShowToast("读取日志文件失败: ${e.message}"))
                }
            }
        }
    }

    private fun loadMoreLogContent() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _viewState.setState { copy(isLoadingMore = true) }
                val reader = viewState.value.currentReader ?: return@launch
                val buffer = StringBuilder(viewState.value.logContent)
                var counter = 0
                var line: String? = null

                while (counter < LOG_CHUNK_SIZE && reader.readLine().also { line = it } != null) {
                    buffer.append(line).append("\n")
                    counter++
                }

                val hasMoreContent = line != null

                _viewState.setState {
                    copy(
                        logContent = buffer.toString(),
                        hasMoreContent = hasMoreContent,
                        isLoadingMore = false
                    )
                }

                if (!hasMoreContent) {
                    reader.close()
                    _viewState.setState { copy(currentReader = null) }
                }
            } catch (e: Exception) {
                _viewEvent.setEvent(LogViewEvent.ShowToast("读取日志文件失败: ${e.message}"))
                _viewState.setState { copy(isLoadingMore = false) }
            }
        }
    }

    private fun exportLog() {
        viewModelScope.launch(Dispatchers.IO) {
            for (filename in viewState.value.selectedLogList) {
                val logFile = File(viewState.value.logDirPath, filename)
                if (!logFile.exists() || !logFile.isFile) {
                    _viewEvent.setEvent(LogViewEvent.ShowToast("日志文件${filename}不存在"))
                    return@launch
                } else {
                    _viewEvent.setEvent(LogViewEvent.ExportLog(filename, logFile.absolutePath))
                    _viewState.setState { copy(isShowLoading = true) }
                }
            }
        }
    }

    private fun deleteLog() {
        viewModelScope.launch(Dispatchers.IO) {
            for (filename in viewState.value.selectedLogList) {
                val logFile = File(viewState.value.logDirPath, filename)
                if (!logFile.exists() || !logFile.isFile) {
                    _viewEvent.setEvent(LogViewEvent.ShowToast("日志文件不存在"))
                    return@launch
                } else {
                    logFile.delete()
                }
            }
            _viewState.setState { copy(selectedLogList = emptyList()) }
            loadLogList()
        }
    }

    private fun deleteThreeDaysAgoLog() {
        viewModelScope.launch(Dispatchers.IO) {
            val logDir = File(viewState.value.logDirPath)
            if (!logDir.exists() || !logDir.isDirectory) {
                _viewEvent.setEvent(LogViewEvent.ShowToast("日志目录${logDir}不存在"))
                return@launch
            } else {
                val logList =
                    logDir.listFiles()?.filter { it.isFile }?.map { it.name } ?: emptyList()
                val sevenDaysAgo = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000
                logList.forEach {
                    val logFile = File(viewState.value.logDirPath, it)
                    if (logFile.lastModified() < sevenDaysAgo) {
                        logFile.delete()
                    }
                }
                _viewState.setState { copy(selectedLogList = emptyList()) }
                loadLogList()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewState.value.currentReader?.close()
    }

    companion object {
        private const val LOG_CHUNK_SIZE = 1000
    }
}

data class LogViewState(
    val logList: List<String> = emptyList(),
    val selectedLogList: List<String> = emptyList(),
    val logContent: String = "",
    val logFileName: String = "",
    val logDirPath: String = "",
    val downloadOverNum: Int = 0,
    val downloadFailNum: Int = 0,
    val isShowLoading: Boolean = false,

    val currentLogFile: File? = null,
    val currentReader: BufferedReader? = null,
    val hasMoreContent: Boolean = false,
    val isLoadingMore: Boolean = false
)

sealed class LogViewAction {
    data object DownloadOverSingle : LogViewAction()
    data object DownloadFailSingle : LogViewAction()
    data class UpdateLogDirPath(val path: String) : LogViewAction()
    data object LoadLogList : LogViewAction()
    data class SelectLog(val filename: String, val isSelect: Boolean = true) : LogViewAction()
    data object ClearSelectedLog : LogViewAction()
    data class LoadLogContent(val filename: String) : LogViewAction()
    data object LoadMoreLogContent : LogViewAction()
    data object ExportLog : LogViewAction()
    data object DeleteLog : LogViewAction()
    data object DeleteThreeDaysAgoLog : LogViewAction()
}

sealed class LogViewEvent {
    data class ExportLog(val filename: String, val filePath: String) : LogViewEvent()
    data class ShowToast(val message: String, val type: String = SNACK_ERROR) : LogViewEvent()
}
