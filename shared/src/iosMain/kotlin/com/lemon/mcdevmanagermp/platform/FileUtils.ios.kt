package com.lemon.mcdevmanagermp.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

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
    platform.Foundation.NSSetUncaughtExceptionHandler { exception ->
        val stackSymbols = exception.callStackSymbols.joinToString("\n") { it.toString() }
        com.lemon.mcdevmanagermp.utils.CrashHandler.handleException(
            Throwable("${exception.name}: ${exception.reason}\n$stackSymbols")
        )
    }
}