package com.lemon.mcdevmanagermp.platform

actual fun restartApp() {
    try {
        val jarPath =
            object {}::class.java.protectionDomain?.codeSource?.location?.toURI()?.path ?: return
        Runtime.getRuntime().exec(arrayOf("java", "-jar", jarPath))
    } catch (_: Exception) {
        // 如果无法启动新进程，直接退出让用户手动重启
    }
    kotlin.system.exitProcess(0)
}
