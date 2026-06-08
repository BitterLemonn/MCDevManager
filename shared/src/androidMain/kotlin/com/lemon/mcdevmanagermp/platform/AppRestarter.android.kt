package com.lemon.mcdevmanagermp.platform

actual fun restartApp() {
    // Android 上通常通过 Intent 重启，这里直接退出让用户手动重启
    kotlin.system.exitProcess(0)
}
