package com.lemon.mcdevmanagermp.platform

import com.lemon.mcdevmanagermp.utils.CrashHandler
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSException

@OptIn(ExperimentalForeignApi::class)
actual fun getLogDirectory(): String {
    val paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
    val documentsDirectory = paths.first() as String
    val logsDirectory = "$documentsDirectory/logs"
    val fileManager = NSFileManager.defaultManager
    if (!fileManager.fileExistsAtPath(logsDirectory)) {
        fileManager.createDirectoryAtPath(logsDirectory, true, null, null)
    }
    return logsDirectory
}

@OptIn(ExperimentalForeignApi::class)
actual fun setupUncaughtExceptionHandler() {
    platform.Foundation.NSSetUncaughtExceptionHandler(staticCFunction { exception ->
        if (exception != null) {
            val stackSymbols = exception.callStackSymbols.joinToString("\n") { it.toString() }
            CrashHandler.handleException(
                Throwable("${exception.name}: ${exception.reason}\n$stackSymbols")
            )
        }
    })
}
