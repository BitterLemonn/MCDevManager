package com.lemon.mcdevmanagermp.platform

/**
 * 下载通知管理器（平台特定实现）
 * Android: NotificationManager + NotificationCompat 进度条
 * iOS: UNUserNotificationCenter
 * JVM: 空实现（通过 Dialog 显示进度）
 */
expect class DownloadNotifier() {
    fun startNotification(title: String)
    fun updateProgress(progress: Int, downloadedBytes: Long, totalBytes: Long)
    fun finishNotification(success: Boolean, filePath: String?)
    fun cancelNotification()
}
