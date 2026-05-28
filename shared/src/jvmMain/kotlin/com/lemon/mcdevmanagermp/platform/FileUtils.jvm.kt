package com.lemon.mcdevmanagermp.platform

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