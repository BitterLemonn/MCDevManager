package com.lemon.mcdevmanagermp.platform

/**
 * 重启应用（平台特定实现）
 * iOS 实际不会调用此方法（UpdateStrategy 为 OPEN_BROWSER）
 */
expect fun restartApp()
