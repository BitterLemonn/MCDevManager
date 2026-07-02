package com.lemon.mcdevmanagermp.platform

import androidx.compose.ui.draganddrop.DragAndDropEvent
import io.github.vinceglb.filekit.PlatformFile

expect fun getLogDirectory(): String

expect fun setupUncaughtExceptionHandler()

/**
 * 从本地路径构造 [PlatformFile]，仅用于 Desktop 拖放上传；移动端无拖放场景，返回 null。
 */
expect fun platformFileFromPath(path: String): PlatformFile?

/**
 * 从拖放事件读取文件路径列表；仅 Desktop 实际读取（java.awt），移动端返回空。
 */
expect fun DragAndDropEvent.readFilePaths(): List<String>
