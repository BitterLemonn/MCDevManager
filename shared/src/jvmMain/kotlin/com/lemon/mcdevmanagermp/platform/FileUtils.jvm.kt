package com.lemon.mcdevmanagermp.platform

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.awtTransferable
import io.github.vinceglb.filekit.PlatformFile
import java.awt.datatransfer.DataFlavor
import java.io.File

private object ClassRef

actual fun getLogDirectory(): String {
    val jarPath = ClassRef::class.java.protectionDomain.codeSource.location.toURI().path
    val appDir = File(File(jarPath).parentFile, "logs")
    if (!appDir.exists()) {
        appDir.mkdirs()
    }
    return appDir.absolutePath
}

actual fun setupUncaughtExceptionHandler() {
    val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        com.lemon.mcdevmanagermp.utils.CrashHandler.handleException(throwable)
        defaultHandler?.uncaughtException(thread, throwable)
    }
}

actual fun platformFileFromPath(path: String): PlatformFile? =
    runCatching { PlatformFile(File(path)) }.getOrNull()

@OptIn(ExperimentalComposeUiApi::class)
actual fun DragAndDropEvent.readFilePaths(): List<String> = runCatching {
    val transferable = awtTransferable
    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        @Suppress("UNCHECKED_CAST")
        (transferable.getTransferData(DataFlavor.javaFileListFlavor) as? List<File>)
            ?.map { it.absolutePath } ?: emptyList()
    } else emptyList()
}.getOrDefault(emptyList())
