package com.lemon.mcdevmanagermp.platform

/**
 * Desktop JVM 空实现 — 下载进度通过 UI Dialog 显示，无需系统通知
 */
actual class DownloadNotifier actual constructor() {
    actual fun startNotification(title: String) {}
    actual fun updateProgress(progress: Int, downloadedBytes: Long, totalBytes: Long) {}
    actual fun finishNotification(success: Boolean, filePath: String?) {}
    actual fun cancelNotification() {}
}
