package com.lemon.mcdevmanagermp.platform

actual fun restartApp() {
    // iOS 不支持应用内重启（UpdateStrategy 为 OPEN_BROWSER，不会调用此方法）
    kotlin.system.exitProcess(0)
}
